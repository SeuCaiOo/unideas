# unideas — Convenções de Código

Regras e boas práticas para o código nascer padronizado. Complementa [`ARCHITECTURE.md`](ARCHITECTURE.md) (estrutura) e [`FLOW.md`](FLOW.md) (navegação).

Princípios que regem tudo abaixo: **SOLID, KISS, YAGNI, DRY, Clean Code**. Na dúvida entre uma solução esperta e uma simples, escolha a simples — o objetivo do MVP é ser enxuto (correção de rota deliberada contra scope creep).

---

## Nomenclatura e idioma

- **Código, nomes de classe/função/variável, comentários: inglês.** Strings de UI: recursos (`strings.xml`) com suporte PT/EN.
- **Nunca strings PT-BR hardcoded no código** (nem em ViewModel, nem em Composable). Textos de UI vêm de `@StringRes`. Vale também pra padrões de formatação (`DateTimeFormatter.ofPattern`, etc.) — um literal tipo `'at'` embutido no padrão é hardcoded do mesmo jeito, mesmo não sendo uma chamada direta a `Text(...)` (bug real, corrigido em #17).
- Cada módulo com `strings.xml` (#17) mantém seu par `values/` (EN, default) + `values-pt/` (PT-BR) com as mesmas chaves — qualifier `values-pt` (português genérico), não `values-pt-rBR`. `app_name` fica "Unideas" sem tradução nos dois.
- Pacotes: `com.seucaio.unideas.<módulo>` (ex: `com.seucaio.unideas.feature.home`).
- Componentes compartilhados de `:uds` portados do antigo `:core:ui` (em `uds/components/legacy/`) mantêm o prefixo `Unideas` (`UnideasTopBar`, `UnideasEmptyContent`, ...); componentes nativos do `:uds` usam `Uds`/nomes genéricos próprios.
- Commits: [Conventional Commits](https://www.conventionalcommits.org/) em inglês (`feat: ...`, `fix: ...`). Issues: título em inglês, corpo em PT-BR.

---

## Camadas (Clean Architecture)

```
domain  →  contratos e regras puras (sem Android/Room/Compose/Koin)
data    →  implementa os contratos do domain (Room, mappers, repos)
feature →  consome SÓ o domain (use cases/interfaces), nunca o data
app     →  faz o wiring (DI liga data→domain, registra ViewModels, navegação)
```

- Um `:feature:*` **jamais** importa `:data`. Depende de `:domain` + `:uds`.
- Modelos de domínio nunca vazam entities do Room pra UI. Mapper (`Entity ↔ Domain`) mora em `:data/mapper/` como extension function.

---

## Domain — use cases

- Um use case = uma responsabilidade, com `operator fun invoke(...)`.
- **Use cases de delete recebem `id: Long`**, não o objeto de domínio inteiro. A PK basta; passar o objeto cria acoplamento.

```kotlin
// ✅ correto
class DeleteItemUseCase(private val repo: ItemRepository) {
    suspend operator fun invoke(id: Long) = repo.deleteItem(id)
}
```

- **Use cases que retornam `Result<T>` usam `runCatching { ... }` como corpo inteiro** — captura tanto validações (`require`) quanto exceções do repositório (ex: `SQLiteConstraintException`). Nunca `Result.success(repo.call())`.

```kotlin
// ✅ correto — runCatching cobre require + exceção do repo
suspend operator fun invoke(item: Item): Result<Long> = runCatching {
    require(item.title.isNotBlank()) { "Title is required" }
    repo.insertItem(item)
}
```

- **Operações complexas retornam "Outcome" ricos** em `domain/model/outcome/`, não flags soltas. Ex: excluir seção retorna `DeletionStatus.Deleted` ou `DeletionStatus.BlockedByLinkedItems(count)` — o ViewModel só reage ao outcome, não recalcula a regra.
- **Setup use case**: para telas de formulário, um `Get<X>FormSetupUseCase` resolve "é novo? existe? erro?" e devolve o estado inicial pronto — o ViewModel não decide "como carregar" no `init`.
- **Facade de use case** (`SectionUseCase`, `TagUseCase`, `ItemDetailUseCase`, `ItemFormUseCase`, `HomeUseCase`, `GoogleAuthUseCase`, `BackupUseCase`): quando um ViewModel precisa de vários use cases da mesma entidade, uma facade pode compor os use cases de operação única já existentes (mantidos intactos, ainda usáveis sozinhos) — um método por operação, cada um só delegando, **nunca acessando repositório diretamente**. Não é "um use case a mais fazendo a mesma coisa" — reduz quantos parâmetros o construtor do ViewModel recebe, sem duplicar lógica nem esconder regra de negócio. Quando uma facade cresce demais (`LongParameterList`), prefira **dividir por contexto** em vez de suprimir o lint — `BackupUseCase` (#16) foi dividido em `GoogleAuthUseCase` (sign-in/sessão: intent + conta atual) e um `BackupUseCase` enxuto (operações que recebem uma conta e constroem o `Drive` service internamente), espelhando a fronteira já existente entre `GoogleAuthRepository`/`BackupRepository`.

    ```kotlin
    // ✅ correto — só delega, sem lógica própria
    class SectionUseCase(
        private val getSections: GetSectionsUseCase,
        private val addSection: AddSectionUseCase,
        private val renameSection: RenameSectionUseCase,
        private val deleteSection: DeleteSectionUseCase,
    ) {
        fun getAll() = getSections()
        suspend fun add(name: String) = addSection(name)
        suspend fun rename(section: Section) = renameSection(section)
        suspend fun delete(id: Long) = deleteSection(id)
    }
    ```

    Nomeie pela **entidade** quando uma única tela usa o CRUD inteiro (Section/Tag — telas de "gerenciar entidade", 4 operações cada). Nomeie pela **tela** quando a entidade se espalha por várias telas com subconjuntos diferentes de operações (Item: `ItemDetailUseCase` cobre getDetail/delete/complete, `ItemFormUseCase` cobre get/create/edit) — um facade genérico `ItemUseCase` cobrindo tudo já foi tentado e descartado: virou um "grab-bag" sem dono claro, onde procurar `get()` na tela de detalhe não achava nada (só existia no form) e vice-versa.

---

## ViewModel — contrato MVI

Contrato de cada tela (arquivos em `feature/<tela>/viewmodel/`):

```
<Tela>UiState.kt   — sealed interface Loading | Success(dados prontos) | Error(@StringRes messageRes)
<Tela>UiAction.kt  — sealed interface: eventos one-shot (NavigateBack, ShowSnackbar(@StringRes))
<Tela>Event.kt     — sealed interface: interações do usuário (OnSaveClicked, OnTitleChanged(v), ...)
<Tela>ViewModel.kt — combine(InternalState, domainFlows) → uiState; onEvent(event) → handlers
```

### Regras inegociáveis

- **`uiState` derivado por `combine(...)`**, unindo `InternalState` (campos de UI) + flows de domínio. Evite `init { collect { ... } }` manual. **Exceção 1:** tela sem nenhum estado só-de-UI (sem filtro, sem seleção — ex: lista de gerenciamento simples) pode derivar `uiState` direto do flow de domínio (`.map`/`.catch`/`.stateIn`), sem `combine`/`InternalState` vazio só pra seguir a forma. Confirmado em `SectionsViewModel`/`TagsViewModel` (#41/#43) e `AllPrioritiesViewModel` (#28). **Exceção 2:** `UiState` não precisa ser `sealed interface Loading|Success|Error` quando a tela **sempre** consegue renderizar — um formulário cujos campos existem desde o primeiro frame (nada de "tela carregando"), onde só uma ação de fundo (buscar dados de referência, carregar o item pra editar) pode falhar. Nesse caso, `UiState` vira uma `data class` só (campos sempre presentes); a falha de carregar dados de referência fica silenciosa se degrada bem pra lista vazia (mesmo tratamento de "não tem nada ainda", não é erro de tela), e uma falha real (ex: item não encontrado em modo editar) vira uma `UiAction` one-shot (`ShowSnackbar` + `NavigateBack`) em vez de um estado bloqueante — não faz sentido ter uma tela de erro com "tentar de novo" pra algo que não existe mais. Confirmado em `ItemFormViewModel`. **Exceção 3:** quando os pedaços de estado de uma tela têm fontes de escrita genuinamente diferentes — estado UI-only mutado direto por evento do usuário, dado derivado de query sem Loading/Error próprio, e prontidão de tela (Loading/Success/Error) — a tela pode expor `StateFlow`s independentes em vez de um `UiState` único via `combine`, cada um coletado separadamente pela Screen. Não é válvula de escape geral pra "isso ficou complexo": só quando os pedaços são movidos por produtores realmente diferentes (evento vs. query vs. prontidão), não só agrupados por área da feature. Confirmado em `HomeViewModel` (#102) — `filterState` (evento), `itemsState` (query, sem Loading/Error), `uiState` (só prontidão, restrito a `hasAnyItem`).
- **`UiAction` via `Channel(Channel.BUFFERED)`** (não o default RENDEZVOUS) — evita bloquear o `send` se o coletor (Screen) não estiver pronto exatamente na hora do envio.
- **`combine` é transformação pura — sem efeitos colaterais.** Nunca `_action.send(...)` nem suspending dentro do bloco. Efeitos (navegação, snackbar) vão em handlers do `onEvent` ou em `LaunchedEffect` na Screen.
- **Proibido join manual em memória.** Dados relacionados chegam prontos do use case (Room `@Relation`/SQL join na camada data). ViewModel é "burro": mapeia domínio → UI.
- **Proibido `.value = ...`** — sempre `.update { it.copy(...) }` (atomicidade).
- **Proibido cast `(uiState.value as? Success)`.** Precisa de um dado interno? Consulte o flow de domínio privado ou o `InternalState`.
- **Nunca `Context`/`AndroidViewModel` no ViewModel.** Se precisa de `Application` (Google APIs/Drive), encapsule num `Repository` e exponha via `UseCase`. Padrão: `ViewModel → UseCase → Repository(Application)`.
- **Strings**: snackbar via `@StringRes`; erros de exceção via `e.message.orEmpty()`. Nunca PT-BR hardcoded.
- **Smart State, Dumb ViewModel**: textos derivados, flags de aviso, validações de campo ficam como propriedades calculadas dentro do `Success` — o ViewModel atualiza só os campos brutos.
- **`combine` infere até 5 params.** Precisa de mais? Agrupe em `private data class InternalState(...)` — nunca `args[0] as Type`.
- **Performance reativa**: flows de domínio como `private val ... .stateIn(scope, WhileSubscribed(5_000), initialValue = null)`. `initialValue = null` evita "flicker" de vazio (mantém `Loading` até o banco emitir). Em `flatMapLatest`, filtrar params com `.map { it.param }.distinctUntilChanged()` pra não reiniciar a query por flags de UI.
- Use cases injetados individualmente — só os que a tela usa.
- Funções pequenas (SRP); handlers privados `handleSuccess`/`handleError`; helper `suspend fun showSnackbar(@StringRes m: Int) = _action.send(...)`.

```kotlin
val uiState: StateFlow<UiState> = combine(_internalState, itemsFlow) { internal, items ->
    if (items == null) return@combine UiState.Loading
    UiState.Success(/* mapeia items + internal */)
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)
```

---

## Screen (Compose)

- `koinViewModel()` pra injetar; `collectAsStateWithLifecycle()` pra consumir o `StateFlow`.
- Consumir `UiAction` num `LaunchedEffect` que faz `collect`; resolver `@StringRes` com `LocalResources.current` (não `LocalContext` — não invalida em mudança de config). Callbacks de navegação e o effect capturado em `LaunchedEffect(Unit)` devem usar `rememberUpdatedState` pra evitar stale closure.
- `when (uiState)` cobrindo `Loading`/`Error`/`Success`; usar `UnideasLoadingContent`/`UnideasErrorContent`/`UnideasEmptyContent` do `:uds` (`uds/components/legacy/`) — **nunca reimplementar inline**. `Error` em feature: `stringResource(uiState.messageRes)`.
- **Design System primeiro**: antes de criar UI inline, checar `:uds`. Componentes compartilhados (topbar, list item, dialogs, chips, indicador de urgência) moram lá.
- **Duas telas que já compartilham composables (ex.: `HomeScreen`/`BrowseScreen` compartilhando `Filters`/`ItemsListContent`/`TasksNotesTabRow`) continuam compartilhando quando ganham UI nova.** Se um elemento novo (linha, botão, bloco) é adicionado nos dois call sites, ele nasce como composable próprio em `screen/components/` do módulo (nunca `:uds`, se depender de modelo de domínio ou de `R.*` do módulo) e é chamado uma vez de cada tela — nunca colado inline e duplicado nas duas. Vale mesmo quando o elemento é pequeno (ex.: uma `Row` de poucos parâmetros) — duplicação nasce pequena. Confirmado o motivo na prática (2026-07-21): duplicação de um `Row(Filters + ViewModeToggleButton)` entre `HomeScreen`/`BrowseScreen`, extraída depois pro próprio módulo por já violar DRY/KISS na criação.
- **UI element state** (scroll, animação, `LazyListState`) fica em Plain State Holder na camada de UI (`@Stable class ...State` + `remember...State()`), **nunca** no ViewModel. **Exceção:** qual dialog de entidade (criar/renomear/excluir) está aberto, e sobre qual item, mora na ViewModel — não em `remember` local — permitindo que o `PreviewProvider` simule os cenários com dialog aberto, não só as três variantes de `UiState`. Implementado como um **`StateFlow` independente** (`dialogState`, separado de `uiState`, sem `combine` entre os dois) em vez de um campo dentro de `Success` — mais simples que combinar os dois num `UiState` só, e evita um tipo `Result` intermediário redundante. A Screen coleta os dois `StateFlow`s separadamente. Confirmado em `SectionsViewModel`/`TagsViewModel` (#42/#44).
- **PreviewProvider** cobrindo todos os estados do `UiState` (`Loading`/`Success`/`Error`) **e** os estados de dialog de entidade quando aplicável (ver exceção acima).

### Regras visuais (planta: Material 3, light + dark, acento teal)

- Toque mínimo 48dp.
- Sem dividers entre itens de lista — usar espaçamento vertical (8/12dp).
- Sem borders pra separar blocos — usar hierarquia de superfície (shifts de background).
- Texto de corpo em `onSurfaceVariant`, nunca `#FFFFFF` puro.
- **Vermelho/âmbar reservados EXCLUSIVAMENTE pra indicação de prazo** (`:uds`'s native `DueBadge`, ou `badgeColor` nos `ListItemRow`/`PriorityPanel`) — nunca em outros elementos.
- Sem gamificação (confetti, level-up, progress com estrelas).

### Teclado / IME (edge-to-edge)

`enableEdgeToEdge()` na `MainActivity` faz o teclado ser inset de sistema. Toda tela com `bottomBar` contendo campos de texto precisa de `Modifier.imePadding()` no `Scaffold`. Campos multilinha (descrição/notas): `ImeAction.Done` + `KeyboardActions(onDone = { focusManager.clearFocus() })`.

---

## Navegação

- `sealed interface <Feature>Route` com `@Serializable` (type-safe Navigation Compose). Args via `data class ...(val id: Long)`.
- `NavHost` central no `:app`; cada feature expõe `*NavGraph` (composable) + `*Route`. Extração de args: `SavedStateHandle.toRoute<Route.X>()`.

---

## DI (Koin)

Ver [`ARCHITECTURE.md`](ARCHITECTURE.md#di--estrutura-koin). Resumo:
- Módulos por camada; `AppModule` = entry point com `includes(...)`.
- `single` para DB/DAO/Repository; `factory` para use case; `viewModel` para ViewModel.
- Repository: `singleOf(::Impl).bind<Interface>()`. Use case: `factoryOf(::X)`. ViewModel: `viewModelOf(::X)`.

---

## Datas e timezones

- Banco: `Long` epoch millis em timezone local via `LocalDate.toEpochMilli()`.
- Mapper (banco → domínio): `Long.toLocalDate()` (system default) — correto pra dados persistidos.
- **Material3 DatePicker retorna UTC midnight** → converter com `Long.toLocalDateUtc()` (em `:core:common`), diferente do mapper de banco.

---

## Testes

Cobertura mínima via `koverVerify` (70%, ver `app/build.gradle.kts`). **Desde a #41, ViewModels entram no *gate* de cobertura** — o filtro `*ViewModel*` foi removido das exclusões e cada módulo `:feature:*` com um ViewModel testado precisa aplicar o plugin `kover` e ser adicionado à agregação em `app/build.gradle.kts` (`kover(project(":feature:..."))`) , como feito para `:feature:sections` (100% de cobertura no módulo). Screens/Composables continuam excluídas (`annotatedBy(Composable/Preview/PreviewLightDark)`, `*PreviewProvider`) — só a lógica (ViewModel) é cobrada. O que **precisa** de teste:

| Alvo | Como | Mínimo |
|---|---|---|
| Use case | MockK (`test` unitário) | happy path + falha de validação (quando há `require`) |
| Repository | MockK — mocka o DAO, verifica delegação + `toDomain`/`toEntity` | happy path por método público |
| Mapper | `test` puro | round-trip `toDomain()`/`toEntity()`, campo a campo |
| DAO | `Room.inMemoryDatabaseBuilder` (`androidTest`) | inserir/ler/deletar + queries com Flow |
| ViewModel | MockK (use cases, `@MockK` + `MockKAnnotations.init(this)`) + **Turbine** (`Flow`/`StateFlow` de `uiState`/`action`) | `Loading`→`Success`, `Error` (flow lança), cada `Event` (happy path + falha) |

- Stubs compartilhados: `domain/src/testFixtures/` (domain models) via `testFixtures(project(":domain"))`; entity stubs em `data/src/test/`.
- **Turbine** (`app.cash.turbine:turbine`, `libs.turbine`) é o padrão pra testar `Flow`/`StateFlow` de ViewModel — `flow.test { awaitItem() ... }` em vez de `.first()`/coleta manual. Adicionar como `testImplementation` em cada `:feature:*` que ganhar um ViewModel testado.
- `Dispatchers.setMain(UnconfinedTestDispatcher())` em `@Before` / `Dispatchers.resetMain()` em `@After` — evita que `viewModelScope.launch` rode num dispatcher diferente do `runTest`.
- **Testes de ViewModel usam `@MockK` + `MockKAnnotations.init(this)`** (em vez de `= mockk()` inline) — mais legível com vários mocks (um por use case injetado). `:domain`/`:data` continuam com `= mockk()` inline; não é retrofit, só o padrão novo pra ViewModel a partir da #43.
- **Nome de teste de ViewModel: `` `when <condição/evento> should <comportamento esperado>` ``** — variante enxuta de Given-When-Then (sem "given", condição embutida no "when"), sem vírgula. Ex.: `` `when OnDeleteClicked completes should not emit an action` ``. `:domain`/`:data` continuam com a frase descritiva direta (`` `invoke fails when the repository throws` ``) — não é retrofit, só o padrão novo pra ViewModel a partir da #43.
- **Rodar `./gradlew koverVerify` antes de abrir PR** — a CI falha se cair abaixo do mínimo. O `pre-push` não valida cobertura; é responsabilidade do dev.

---

## Logging

- **Timber** (não `android.util.Log` direto) para qualquer log que precise sobreviver além de uma sessão de debug pontual. Em debug, `Timber.DebugTree()` (`UnideasApplication`, guardado por `BuildConfig.DEBUG`). Em release, `CrashlyticsTree` (`:core:common`) — forwarda `WARN`+ como log custom do Crashlytics e chama `recordException` (sintetizando uma exceção pra `ERROR` sem `Throwable`); `VERBOSE`/`DEBUG`/`INFO` são descartados.
- Módulos que precisam logar adicionam `implementation(libs.timber)` no próprio `build.gradle.kts` (`:app` já tem).
- Log de debug temporário e pontual (adicionado só pra rastrear um bug específico e removido depois) pode seguir usando `Log.d` mesmo — não precisa Timber pra algo descartável em minutos.

---

## Antes de abrir o PR (checklist)

**Rodar `./gradlew clean` sempre antes de `koverVerify`/`detekt`** — cache stale nesse setup multi-módulo já mascarou uma cobertura real (o número reportado não batia com o `report.xml` real). Projeto é pequeno, o clean custa segundos; não pular.

**Nunca rodar `./gradlew build` numa branch de feature — usar `assembleDebug`.** `build` roda a árvore de tasks inteira (debug + release, lint/testes das duas variantes, R8, dex — tudo) e leva minutos; `assembleDebug` gera só o necessário pra instalar e testar manualmente num device/emulador, muito mais rápido, e só piora conforme o projeto cresce. `build` é reservado pra `main` (branch de release) — só ali a árvore de release completa precisa rodar de fato.

```bash
./gradlew clean          # sempre primeiro, antes de detekt/koverVerify
./gradlew test           # unit tests passando
./gradlew koverVerify    # cobertura ok
./gradlew detekt         # sem warnings novos (ignoreFailures=true — ler o report)
./gradlew lint           # ler o report (abortOnError=false)
./gradlew assembleDebug  # gera o APK debug pra testar manualmente — NUNCA `build` numa feature branch
```

Fluxo: `/new-issue` → `/start-feature` → implementação → `/finish-issue` → `/open-pr` (target `dev`, nunca `main`).
