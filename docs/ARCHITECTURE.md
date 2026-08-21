# Arquitetura

Documento técnico — complementa a planta de produto (artifact privado do usuário, "unideas — Planta do Produto"), que cobre o **quê**/**por quê**. Aqui é o **como**: estrutura de módulos, pacotes, schema, DI e convenções de datas.

Docs relacionados:
- [`FLOW.md`](FLOW.md) — fluxos de navegação entre as telas
- [`CONVENTIONS.md`](CONVENTIONS.md) — convenções de código, contrato MVI, testes, boas práticas
- [`BLUEPRINT.md`](BLUEPRINT.md) — inventário completo de classes/telas a construir + ordem de implementação

## Padrão

**MVI (Model-View-Intent)** + **Clean Architecture**. Cada tela expõe um `ViewModel` que:
- recebe `Event` explícitas da UI (`onEvent(event)`),
- emite um único `UiState` imutável via `StateFlow` (derivado por `combine`, nunca `collect` manual no `init`),
- dispara ações one-shot (navegação, snackbar) via `UiAction` num `Channel(Channel.BUFFERED)` exposto como `receiveAsFlow()` — **nunca** pelo `UiState`.

MVVM fica registrado como variação possível pra telas triviais no futuro, mas o padrão do MVP é MVI.

Sem KMP — Android nativo puro (`com.android.application`/`com.android.library`, sem `commonMain`/`androidMain`).

Princípios: **SOLID, KISS, YAGNI, DRY, Clean Code.**

## Módulos

```
:app                 — entry point, DI wiring (Koin startKoin), NavHost central, MainActivity
:domain              — models, enums, repository interfaces, use cases. Kotlin puro, sem Android/Room/Compose/Koin
:data                — Room (entities, DAOs, database, migrations, converters), mappers, impl de repositório, DataModule
:core:common         — utilitários (extensions, constantes); maioria Kotlin puro, uma exceção Android-dependente. Sem Compose
:uds                 — design system (pacote com.seucaio.unideas.ds), portado de outro projeto (#87), domain-agnostic (não depende de :domain nem de :core:common). Substituiu :core:ui por completo (#82) — todo trabalho novo de UI compartilhada vai aqui; expõe Compose via `api` — quem depende de :uds não precisa redeclarar BOM/artifacts de Compose. `uds/components/legacy/` guarda componentes portados ao pé da letra do antigo :core:ui (alguns com exceção documentada à regra "sem R.*" do módulo, por serem transitórios)
:core:backup         — backup/restore via Google Drive (Google Sign-In escopado + Drive API), auto-contido
:core:notifications  — notificações de lembrete (#95): PeriodicWorkRequest 4x/dia, ReminderNotifier
                        (2 canais: normal dispensável / urgente ongoing), notificação por item + resumo
                        de grupo por tier, deep link pro item ao tocar
:feature:home        — Home (lista de itens, abas Tarefas/Anotações, seleção múltipla + exclusão em lote) +
                        painel de prioridades (Bottom Sheet, acionado pelo FAB — não mais painel fixo) +
                        Todas as Prioridades
:feature:items       — Criar/Editar Item (tela única, unificada — ver seção de pacotes abaixo) + histórico de recorrência
:feature:sections    — Gerenciar Seções
:feature:tags        — Gerenciar Tags
:feature:settings    — Configurações (usa :core:backup)
```

### Direção de dependência

```
:core:common  ←  :data
:uds  ←  :app, :feature:*, :core:backup  (design system compartilhado; expõe Compose via `api`)
:domain       ←  :data
:domain       ←  :feature:*  (só interfaces/use cases, nunca :data)
:domain, :core:common, :uds, :data  ←  :core:backup
:feature:settings  →  :core:backup
:domain, :core:common  ←  :core:notifications
:feature:settings  →  :core:notifications  (botões de debug "Testar notificação"/"Rodar verificação agora")
tudo  ←  :app  (faz o wiring de DI e navegação)
```

Regra dura: **`:feature:*` nunca depende de `:data` diretamente** — só de `:domain` (interfaces + use cases). A implementação concreta do repositório é injetada via Koin no `:app`. Isso mantém as features testáveis e desacopladas da persistência.

**Exceção confirmada em #30 (E1.2):** `:core:backup` **é** `implementation(project(":data"))` — diferente da regra acima. Backup manipula o arquivo físico do Room (`database.close()`/`checkpoint()`/`getDatabasePath()`) pra copiar/restaurar o `.db` bruto direto no Drive, não só via interface de repositório — não dá pra fazer isso só com `:domain`. Mesma exceção existe no GymLog (projeto-fonte de convenções do bootstrap).

## Estrutura de pacotes por módulo

Namespace base: `com.seucaio.unideas`. Cada módulo tem seu sufixo.

### `:domain` — `com.seucaio.unideas.domain`

```
domain/
├── model/            — modelos de domínio (datas como LocalDate/LocalDateTime)
│   ├── Item.kt
│   ├── ItemDetail.kt        — Item + sectionName resolvido (join feito em :data, nunca no ViewModel — ver ItemWithTagsAndSection)
│   ├── SectionsAndTags.kt   — par sections+tags, emitido como Flow ao vivo por SectionsAndTagsUseCase pras telas
│   │                          que selecionam dos dois (Home, ItemDetail, Config) — não é mais um snapshot único
│   │                          desde #170 (criação inline de seção/tag exige refletir sem sair da tela)
│   ├── ItemType.kt          — enum TASK | NOTE
│   ├── Recurrence.kt        — sealed interface: None/Daily/Weekly/Monthly (data object) + EveryNDays(days: Int) (data class, intervalo customizado)
│   ├── UrgencyLevel.kt      — enum OVERDUE | DUE_SOON | NORMAL (derivado de dueDate)
│   ├── ReminderTier.kt      — cálculo puro do nível de urgência de notificação (radar/normal/urgente), #95/#96
│   ├── ReminderWarning.kt   — sealed: None | DaysBefore(days: Int), config de aviso do item, #114
│   ├── ItemCompletionHistory.kt — id, itemId, scheduledDate, completedAt (nulo = não feito), note; status computado
│   │                              ON_TIME/LATE/MISSED derivado de completedAt vs scheduledDate (#126); originalScheduledDate/
│   │                              extensionCount (#101/D) — carregados do Item quando a ocorrência resolve/é perdida já tendo
│   │                              sido adiada, reconstroem "quantas vezes essa ocorrência foi adiada antes de fechar"
│   ├── Section.kt
│   ├── Tag.kt
│   ├── SeedScope.kt         — enum EMPTY | BASIC | FULL — cenário de dado de exemplo, debug-only (#19)
│   └── outcome/             — resultados ricos de operações (ver CONVENTIONS.md)
│       ├── DeletionStatus.kt   — Deleted | BlockedByLinkedItems(count)
│       ├── SaveResult.kt
│       └── CompletionResult.kt — Completed | Uncompleted (toggle simples; não gera item novo — ver seção de persistência)
├── repository/       — interfaces (contratos), sem implementação
│   ├── ItemRepository.kt
│   ├── ItemCompletionHistoryRepository.kt — CRUD do histórico de ocorrência, implementado em :data (#126;
│   │                                          update/deleteById(id) adicionados no #169 — delete por id, distinto de
│   │                                          deleteOccurrence(itemId, scheduledDate), que já existia pro toggle de desmarcar)
│   ├── SectionRepository.kt
│   ├── TagRepository.kt
│   ├── DatabaseRepository.kt     — clearAll()/seed(scope) — debug-only tooling (#19), implementado em :data
│   └── ReminderRefreshTrigger.kt — reposta as notificações após uma conclusão de item, implementado em :core:notifications
└── usecase/
    ├── SectionsAndTagsUseCase.kt  — facade get+create sobre SectionUseCase/TagUseCase: getAll(): Flow<SectionsAndTags>
    │                                 (live, combine) + addSection/addTag; usado por HomeViewModel, ItemDetailViewModel
    │                                 e SectionsTagsViewModel (Config Screen) — 1 parâmetro por ViewModel em vez de
    │                                 injetar SectionUseCase+TagUseCase separados (#170)
    ├── item/         — Create/Edit/Delete/Complete/GetItem/GetItemDetail/GetItems/GetPriorityItems
    │   ├── ItemDetailUseCase.kt   — facade delegando pros use cases que ItemDetailViewModel usa (getDetail/delete)
    │   ├── ItemFormUseCase.kt     — facade delegando pros use cases que ItemFormViewModel usa (get/create/edit)
    │   ├── ItemOccurrenceUseCase.kt — facade sobre complete/ignore/extend, usada por ItemOccurrenceViewModel (#101/B — ver
    │   │                              nota abaixo sobre a divisão ItemDetailViewModel × ItemOccurrenceViewModel)
    │   ├── HomeUseCase.kt         — facade delegando pros use cases que HomeViewModel/AllPrioritiesViewModel usam
    │   │                            (getPriorityItems/getItems/complete/refreshReminders — #101/D, pull-to-refresh na Home)
    │   ├── CompleteItemUseCase.kt — concluir; nota obrigatória se a conclusão for atrasada (completedAt > scheduledDate) (#101/A)
    │   ├── IgnoreOccurrenceUseCase.kt — ação manual "ignorar ocorrência vencida" (nota obrigatória, avança dueDate um ciclo),
    │   │                                distinta da detecção automática do ProcessMissedOccurrencesUseCase (#101/A)
    │   ├── ExtendItemDueDateUseCase.kt — "aumentar prazo" de ocorrência vencida: empurra dueDate sem fechar a ocorrência,
    │   │                                  seta Item.pendingExtensionOriginalDueDate/pendingExtensionCount (#101/A)
    │   ├── SetItemPinnedUseCase.kt          — fixa/desafixa item no painel de prioridades (#127, mesmo padrão do pin de Section)
    │   ├── ItemCompletionHistoryUseCase.kt — getHistory (Flow, lista o histórico de ocorrência de um item recorrente, #126)
    │   │                                      + save/delete (#169, CRUD completo sobre uma entrada — cria via `id == 0L`,
    │   │                                      edita senão; unicidade (itemId, scheduledDate) validada explicitamente em vez
    │   │                                      de delegar ao `OnConflictStrategy` do Room; scheduledDate não pode ser futura;
    │   │                                      nota obrigatória se a conclusão registrada for atrasada). Renomeado de
    │   │                                      `GetItemCompletionHistoryUseCase` — não passa por `CompleteItemUseCase`/
    │   │                                      `toggleOccurrence()`, que também mexeriam no estado de agendamento ao vivo do `Item`
    │   └── ProcessMissedOccurrencesUseCase.kt — avança dueDate de item recorrente vencido, gravando histórico "não feito"
    │                                             (pulando o ciclo já coberto por um COMPLETED, dedup — #101/D), carrega
    │                                             extensão pendente pro registro MISSED gerado; único ponto de chamada é o
    │                                             ReminderCheckWorker (#96, ver seção de persistência)
    ├── section/      — Get/Add/Rename/Delete (delete verifica vínculo antes)
    │   └── SectionUseCase.kt      — facade delegando pros 4 acima (CRUD completo, um método por operação)
    ├── tag/          — Get/Add/Rename/Delete (delete verifica vínculo antes)
    │   └── TagUseCase.kt          — facade delegando pros 4 acima, mesmo formato de SectionUseCase
    └── settings/     — SeedDatabaseUseCase/ClearDatabaseUseCase — debug-only (#19), gatilho só em Settings quando BuildConfig.DEBUG
```

**Facades de use case** (`SectionUseCase`, `TagUseCase`, `ItemDetailUseCase`, `ItemFormUseCase`, `ItemOccurrenceUseCase`, `HomeUseCase`): compõem os use cases de operação única já existentes (mantidos intactos, ainda usáveis sozinhos) — um método por operação, cada um só delegando (`fun add(name) = addSection(name)`), **sem acesso a repositório**. Existem só pra reduzir a quantidade de use cases que um ViewModel precisa receber no construtor; não são um "God object" — nomeados pela tela que servem quando a entidade se espalha por telas com subconjuntos diferentes de operações (caso do Item: `ItemDetailUseCase` ≠ `ItemFormUseCase` ≠ `ItemOccurrenceUseCase` ≠ `HomeUseCase`), ou pela entidade quando uma única tela usa o CRUD inteiro (caso de Section/Tag). `HomeUseCase` é compartilhada por `HomeViewModel` e `AllPrioritiesViewModel` (mesma tela-conceito, dois pontos de entrada). Ver `CONVENTIONS.md` para o critério completo.

### `:data` — `com.seucaio.unideas.data`

```
data/
├── local/
│   ├── entity/       — @Entity Room (datas como Long epoch millis)
│   │   ├── ItemEntity.kt
│   │   ├── ItemCompletionHistoryEntity.kt — tabela item_completion_history, FK CASCADE → items, índice único (itemId, scheduledDate) (#126/#133)
│   │   ├── SectionEntity.kt
│   │   ├── TagEntity.kt
│   │   └── ItemTagCrossRef.kt      — junção N:N Item ↔ Tag
│   ├── dao/          — ItemDao, ItemCompletionHistoryDao, SectionDao, TagDao (retornam Flow)
│   ├── database/     — UnideasDatabase (singleton @Volatile + Room builder), version 9
│   │                    migration/ — MIGRATION_2_3 até MIGRATION_8_9 (ver seção de persistência)
│   │                    DatabaseSeeder.kt — debug-only (#19): semeia via DAO direto (não pelos use cases), pacote excluído do koverVerify
│   ├── converter/    — TypeConverters (enums; datas ficam como Long, sem converter)
│   └── relation/     — POJOs @Relation/@Embedded (ItemWithTags; ItemWithTagsAndSection também resolve a seção) — joins no Room, nunca em memória
├── mapper/           — extension functions Entity ↔ Domain (inclui ItemCompletionHistoryMapper)
├── repository/       — ItemRepositoryImpl, ItemCompletionHistoryRepositoryImpl, SectionRepositoryImpl, TagRepositoryImpl, DatabaseRepositoryImpl
└── di/               — DataModule.kt (Koin, local ao módulo — ver seção DI abaixo)
```

### `:core:common` — `com.seucaio.unideas.core.common`

```
core/common/
├── extensions/       — Kotlin extensions (Boolean.orFalse, String.EMPTY, Long.toLocalDate, etc.);
│                       maioria pura, mas Context.restartApplication() (#76) é Android-dependente —
│                       comportamento genérico de app (não específico de nenhum módulo), por isso
│                       mora aqui e não em :core:backup, que é quem hoje o consome
└── util/             — Constants (defaults, chaves), sem Android
```

### `:uds` — `com.seucaio.unideas.ds`

```
uds/
├── theme/                 — UdsTheme, Color, Type, Dimens (Material 3, light + dark, acento teal)
│                            — PinnedTint.kt / LeftAccentBorder.kt (#165) — tinta/borda de acento reaproveitadas
│                              por ListItemRow e PriorityPanel (urgência/fixado)
├── components/            — organizado por papel (buttons/, chips/, inputs/, lists/, navigation/,
│                            panels/, feedback/), catálogo completo no README do módulo
│                            — inputs/ ganhou SelectionBottomSheet, GridSelectionBottomSheet e SwitchSection (#130)
│                            — lists/ reorganizado em lists/item/ (ListItemCard, ListItemCheckbox, ListItemRow,
│                              ListItemTrailingIndicator) + lists/model/ListItemUi.kt (#127/#140)
│                            — lists/NavCard.kt (#162) — card de navegação com chevron, reaproveitável (ex.:
│                              entrada pra Config Screen/Histórico na tela de Detalhe)
└── components/legacy/     — componentes portados ao pé da letra do antigo :core:ui, mesmos nomes
    ├── UnideasTopBar.kt
    ├── UnideasLoadingContent.kt
    ├── UnideasErrorContent.kt
    ├── UnideasEmptyContent.kt          — estado vazio: ícone (TaskAlt) + texto; `titleRes` opcional (#165)
    │                                      adiciona um título (usado só no onboarding real da Home)
    ├── UnideasListItem.kt / EntityListItemWithMenu.kt
    ├── ConfirmationDialog.kt
    ├── ConditionalFab.kt
    └── AppVersionFooter.kt             — recebe versionName como parâmetro (não lê BuildConfig do :app)
```

Ver `uds/README.md` para as regras de portabilidade do módulo e a exceção documentada de `legacy/` (pasta transitória, com componentes que ainda usam `@StringRes`/`R.*` — algo que o resto do `:uds` proíbe).

### `:feature:*` — `com.seucaio.unideas.feature.<nome>`

Dois formatos, conforme o módulo tem uma tela só ou várias:

- **Módulo com uma tela** (Sections, Tags, Settings): flat na raiz — `Screen` + `PreviewProvider` direto em `feature/<nome>/`, sem subpasta por tela. `navigation/`, `viewmodel/` e `di/` são as únicas subpastas.
- **Módulo com várias telas** (Items — Form/Detail/List): cada tela ganha seu próprio `features/<tela>/{screen,viewmodel}/`, já que um único pacote `viewmodel/` compartilhado misturava os 4 arquivos MVI de cada tela sem nenhuma separação visual. `navigation/` e `di/` continuam fora de `features/`, compartilhados pelas telas do módulo.

**`additem/` foi aposentado (#134)** — criar e editar item deixaram de ser telas separadas. `ItemDetailScreen`/`ItemDetailViewModel` fazem os dois papéis: `itemId == null` entra em modo criação (type inicial vindo de `initialType`), `itemId != null` carrega o item existente. Não existe mais `ItemFormScreen`.

**Tipo do item trava após a criação (#160/#162).** Não existe mais seletor de tipo inline no formulário (`TypeSelectorField` foi removido) — Tarefa/Anotação é definida só na criação (`initialType`) e exibida como badge (`TextBadge`) em ambos os modos; trocar Tarefa↔Anotação de um item já existente exige a Config Screen (guardrail com confirmação + reset total, ver `config/` abaixo). `dueDate`/`dueTime`/`recurrence`/`reminderWarning` deixaram de ser exclusivos de Tarefa — `completedAt`/conclusão continua sendo a única diferença estrutural real entre os dois tipos, reforçada em runtime por `CompleteItemUseCase`.

**Tela de Detalhe reestruturada (#162).** `ItemFormBody` mostra o badge de tipo + `TitleDescriptionFields` rolável, e — só quando `state.isEditing` (item já persistido) — dois `NavCard` (`:uds`, com chevron) pra "Configurações" e "Histórico" (cada um com resumo próprio) mais o `ItemFormFooter` (conclusão), fixados fora da área de scroll. Pra item novo ainda não salvo (auto-save ainda não rodou), esse bloco inteiro fica oculto — não só desabilitado — já que `Configurações`/`Histórico`/conclusão não fazem sentido sem um `itemId` real. A antiga seção inline "Mais opções" (`ItemFormOptionsSection`/`ItemFormCommonOptions`/`ItemFormTaskOptions`) foi removida — os campos que ela continha (data/hora, recorrência, aviso, seção, tags) migraram pra Config Screen no #160.

```
feature/items/
├── navigation/
│   ├── ItemsNavGraph.kt
│   └── ItemsRoute.kt              — @Serializable: Detail(itemId: Long? = null, initialType: ItemType = TASK) |
│                                     History(itemId: Long) (#101/C) | Config(itemId: Long) (#160) | List
├── di/
│   └── FeatureModule.kt           — val itemsModule
└── ui/
    ├── components/
    │   ├── ItemActions.kt, DueDateRow.kt
    │   ├── fields/       — CompletionField, DueDateField, DueTimeField, ReminderWarningField, SectionField,
    │   │                    TagsField, TitleDescriptionFields (+preview) — DueDate/DueTime/ReminderWarning/Section/Tags
    │   │                    são consumidos pela Config Screen (#160), não mais pelo form principal
    │   │   ├── markdown/     — MarkdownFormat/PreviewToggle/SelectionContextMenu/SyntaxHighlight/SyntaxInserter/Toolbar (#93)
    │   │   ├── model/        — ItemFormFields.kt
    │   │   └── recurrence/   — RecurrenceBottomSheet (picker principal, sobre SelectionBottomSheet do :uds),
    │   │                        EveryNDaysBottomSheet, WeekdayBottomSheet, DayOfMonthBottomSheet (sobre GridSelectionBottomSheet) (#130)
    │   └── form/         — ItemFormBody (badge de tipo + TitleDescriptionFields + NavCards Config/Histórico + footer,
    │                        só quando state.isEditing — #162), ItemFormFooter (conclusão),
    │                        OverdueOccurrenceActions (#101/B — botões "Ignorar"/"Aumentar prazo" lado a lado, só p/ vencida)
    └── screens/
        ├── detail/
        │   ├── itemdetail/     — ItemDetailScreen.kt + ItemDetailPreviewProvider.kt (formulário: título, descrição,
        │   │                      seção, tags, data/recorrência) + viewmodel/ (ItemDetailUiState/UiAction/Event/
        │   │                      ViewModel/DialogState)
        │   └── itemoccurrence/ — ciclo de vida da ocorrência (concluir/concluir atrasado/ignorar/aumentar prazo),
        │                          separado do form desde #101/B: NoteConfirmDialog (nota obrigatória em atraso/ignorar),
        │                          ExtendDeadlineDatePickerDialog + viewmodel/ (ItemOccurrenceUiState/UiAction/Event/
        │                          ViewModel/DialogState). `ItemDetailScreen` hoisteia os dois ViewModels lado a lado,
        │                          com uma ponte de sincronização (`OnItemUpdatedExternally`) — sem ela, uma escrita
        │                          de um lado podia sobrescrever silenciosamente uma mudança recém-feita do outro
        │                          (race condition real, achada e corrigida em #101/B — ver docs/QA_MANUAL_TESTING.md)
        ├── history/    — ItemHistoryScreen.kt (tela própria, substituiu o ItemHistoryBottomSheet — #101/C): resumo
        │                 (% no prazo, contagem por status, sequência atual), filtros (Todas/No prazo/Atrasadas/Com
        │                 nota), ItemHistoryCard por ocorrência (hora, dias de atraso, nota, trilha de extensão) —
        │                 CRUD completo desde o #169: FAB adiciona entrada retroativa, menu por card (`MoreVert`/
        │                 `DropdownMenu`, mirando `EntityListItemWithMenu`) edita/exclui uma entrada existente
        │                 + ItemHistoryPreviewProvider.kt + viewmodel/ (ItemHistoryUiState/Event/UiAction/ViewModel/
        │                 DialogState — `dialogState` fora do `combine`, exceção 3 do MVI) + ui/components/
        │                 AddEditHistoryEntryBottomSheet.kt (outer + `*Content`, reused create/edit, date picker com
        │                 `SelectableDates` bloqueando data futura e datas já usadas, toggle concluído/perdido, nota)
        ├── config/     — ItemConfigScreen.kt (tela própria, #160): edição de seção/tags/lembrete/recorrência/
        │                 data/horário/aviso de um item já existente, mais o fluxo guardado de troca de tipo
        │                 (dialog de confirmação + reset total dos campos "pesados" — `switchedType()`/
        │                 `ItemConfigDialogState.TypeSwitchConfirm`, escondido quando `isNewItem == true` — #165
        │                 batch) + viewmodel/ (ItemConfigUiState/Event/UiAction/ViewModel/DialogState). Reaproveita
        │                 só `ItemFormUseCase`. Acessada via `NavCard` "Configurações" no `ItemFormBody`
        │                 (`state.isEditing`) — substituiu o ícone de engrenagem no toolbar do #160; a seção
        │                 "Mais opções" inline foi removida no #162
        │                 viewmodel/sectionstags/ — SectionsTagsViewModel (#170), ViewModel dedicado à listagem de
        │                 sections/tags da Config Screen (hoisteado ao lado do ItemConfigViewModel) + criação rápida
        │                 (create-only; CRUD completo continua em Settings) via `QuickCreateBottomSheet`
        │                 (`ui/components/fields/sectionstags/`); injeta só `SectionsAndTagsUseCase`
        └── list/      — ItemsListScreen.kt + ItemsListPreviewProvider.kt   — listagem dev-only (#62), sem abas/filtro/seleção;
                          acessível via seção "Debug" do Settings, mantida mesmo com a Home (D2/#11) já existindo
                          viewmodel/ — ItemsListUiState.kt / ItemsListUiAction.kt / ItemsListEvent.kt / ItemsListViewModel.kt
```

```
feature/home/
├── navigation/
│   ├── HomeNavGraph.kt
│   └── HomeRoute.kt              — @Serializable: Home | AllPriorities (painel de prioridades não é rota — é
│                                    Bottom Sheet acionado a partir da Home, não navegação)
├── di/
│   └── FeatureModule.kt          — val homeModule
└── features/
    ├── home/
    │   ├── screen/    — HomeScreen.kt + HomePreviewProvider.kt
    │   │                components/chrome/    — AddItemFab, HomeDialogs, HomeFab, HomeTopBar
    │   │                components/filters/    — Filters, ItemsFiltersBar, TasksNotesTabRow
    │   │                components/items/      — DueBadgeMapping, ItemRowMappers, ItemsContent, ItemsGridContent,
    │   │                                          ItemsListContent, RecurrenceSummaryMapping
    │   └── viewmodel/ — HomeUiState.kt / HomeUiAction.kt / HomeEvent.kt / HomeViewModel.kt / ItemSectionGroupMapper.kt
    │                    — HomeUiState.HomeMode: Normal | Selection(selectedItemIds) — seleção múltipla + exclusão
    │                      em lote vive aqui (long-press no item), não em feature/items (#140)
    │                    — `isRefreshing: StateFlow<Boolean>` próprio (evento-driven, fora do `combine` de `uiState` —
    │                      exceção 3 do padrão MVI) alimenta o `PullToRefreshBox` da Home; dispara
    │                      `HomeUseCase.refreshReminders()` → `ReminderRefreshTrigger`, gatilho manual do motor de
    │                      reavaliação de ocorrências (#101/D, ver seção de persistência)
    ├── priority/
    │   ├── screen/    — PriorityBottomSheet.kt + PriorityPreviewProvider.kt   — painel de prioridades, hoje um
    │   │                Bottom Sheet mostrado a partir da HomeScreen (state local, não rota própria), não mais
    │   │                painel fixo no topo (#138)
    │   └── viewmodel/ — PriorityEvent.kt / PriorityUiAction.kt / PriorityUiState.kt / PriorityViewModel.kt
    └── allpriorities/
        ├── screen/    — AllPrioritiesScreen.kt + AllPrioritiesPreviewProvider.kt
        └── viewmodel/ — AllPrioritiesUiState.kt / AllPrioritiesUiAction.kt / AllPrioritiesEvent.kt / AllPrioritiesViewModel.kt
```

`feature/sections/` e `feature/tags/` continuam flat (uma tela só cada) — o padrão `features/<tela>/` só se aplica quando o módulo tem mais de uma tela.

O inventário completo de telas/ViewModels/use cases/entidades está em [`BLUEPRINT.md`](BLUEPRINT.md) (congelado como planejamento original — status vivo de cada issue fica no artifact "unideas — Improvements" e no board do GitHub Project).

## Persistência (Room) — schema

Datas armazenadas como **`Long` (epoch millis)** na entity; convertidas pra `LocalDate`/`LocalDateTime` no domínio via mappers (`coreLibraryDesugaring` habilita `java.time` no minSdk 24).

### `ItemEntity` → tabela `items`
```
id: Long                          PK autoincrement
type: String                      TASK | NOTE (enum via TypeConverter)
title: String                     obrigatório (não vazio)
description: String?               opcional, multilinha
sectionId: Long?                   FK → sections.id (SET NULL on delete — mas exclusão é bloqueada antes, ver regra)
dueDate: Long?                     epoch millis, opcional
dueTime: Int?                      segundos do dia, opcional (só válido se dueDate != null; conversão no mapper, não em Converters)
recurrence: String                 NONE | DAILY | WEEKLY | MONTHLY | EVERY_N_DAYS:N (default NONE; só válido se dueDate != null)
reminderWarning: String            NONE | DAYS_BEFORE:N (default NONE; só válido se dueDate != null)
completedAt: Long?                 epoch millis; != null = concluída (item não-recorrente; só faz sentido pra TASK)
lastCompletedScheduledDate: Long?  epoch millis; ocorrência recorrente mais recente marcada como concluída (#133) —
                                    isCompleted de um item recorrente compara isso contra dueDate, em vez de completedAt
isPinned: Boolean                  fixado manualmente no painel de prioridades, independente do cálculo de urgência (#127)
pendingExtensionOriginalDueDate: Long?  epoch millis, opcional — dueDate anterior à 1ª extensão desde a última resolução
                                    da ocorrência (#101/A); null = nunca adiada desde então
pendingExtensionCount: Int         quantas vezes "aumentar prazo" empurrou dueDate desde a última resolução (#101/A);
                                    ambos limpos (null/0) quando a ocorrência resolve (concluída/ignorada) ou é
                                    processada como perdida — carregados pro item_completion_history antes de zerar
createdAt: Long                    epoch millis, preenchido na criação
```

### `ItemCompletionHistoryEntity` → tabela `item_completion_history`
```
id: Long           PK autoincrement
itemId: Long        FK → items.id (CASCADE on delete)
scheduledDate: Long  epoch millis — a ocorrência (dueDate) a que este registro se refere
completedAt: Long?   epoch millis; nulo = ocorrência não feita ("missed")
note: String?        opcional — justificativa livre (ex: "sem internet"); obrigatória ao concluir atrasado ou ignorar (#101/A)
originalScheduledDate: Long?  epoch millis, opcional — dueDate antes de ser adiada, se a ocorrência foi estendida
                                antes de resolver (#101/D); null = nunca foi adiada
extensionCount: Int  quantas vezes foi adiada antes de resolver (#101/D); 0 = nunca
```
Índice único em `(itemId, scheduledDate)` — uma ocorrência só pode ter um registro de histórico (#133).

### `SectionEntity` → tabela `sections`
```
id: Long                 PK autoincrement
name: String             obrigatório, único
```

### `TagEntity` → tabela `tags`
```
id: Long                 PK autoincrement
name: String             obrigatório, único
```

### `ItemTagCrossRef` → tabela `item_tag` (junção N:N)
```
itemId: Long             FK → items.id (CASCADE on delete)
tagId: Long              FK → tags.id  (CASCADE on delete)
PK composta (itemId, tagId)
```

### Regras de integridade na camada de domínio (não no FK)
- **Excluir `Section`/`Tag` com itens vinculados é BLOQUEADO** — o use case (`DeleteSectionUseCase`/`DeleteTagUseCase`) conta os vínculos e retorna `DeletionStatus.BlockedByLinkedItems(count)` **antes** de delegar ao repositório. Não é uma constraint de FK que falha silenciosamente; o usuário vê quantos itens estão vinculados.
- **Recorrência: uma linha só por série, `dueDate` avança, não "renasce" (rearquitetado em #126).** Um item recorrente **nunca** gera uma nova linha em `items`. `CompleteItemUseCase` só grava um registro em `item_completion_history` pra ocorrência atual (`scheduledDate = dueDate`) e marca `lastCompletedScheduledDate = dueDate` — `dueDate` em si não muda. Quem avança `dueDate` de fato é `ProcessMissedOccurrencesUseCase`, chamado pelo `ReminderCheckWorker` (`:core:notifications`) a cada varredura periódica: pra todo item recorrente cujo `dueDate` já passou, ele anda `dueDate` pra frente via `recurrence.nextDueDate(...)`, gravando um registro `completedAt = null` (não feito) em `item_completion_history` pra cada ciclo pulado, até `dueDate >= hoje`. Ou seja, o avanço de ciclo é **lazy** (só acontece quando alguém abre o app ou o worker roda), não disparado pela ação de concluir. `IgnoreOccurrenceUseCase` é a exceção — "ignorar" avança `dueDate` imediatamente (não espera o worker), já que é uma decisão explícita do usuário sobre a ocorrência atual.
- **Motor de reavaliação de ocorrências (#101/D) — não re-notificar/duplicar o que já foi resolvido.** Três gaps que o `ReminderCheckWorker`/`ProcessMissedOccurrencesUseCase` fecham, todos girando em torno do mesmo problema: a conclusão de uma ocorrência recorrente é rastreada fora de banda (`lastCompletedScheduledDate`), sem mover `dueDate` — então o resto do pipeline precisa saber ignorar esse estado.
  1. `ReminderCheckWorker` filtra `item.isCompleted` **depois** de rodar `processMissedOccurrences`, antes de calcular `ReminderTier` — não dá pra fazer isso via query SQL no `ItemDao` (`WHERE completedAt IS NULL` não pega item recorrente, que nunca seta `completedAt`), porque isso escondereia o item do `ProcessMissedOccurrencesUseCase` também, e ele precisa continuar vendo o item pra avançar `dueDate` quando ele ficar atrasado de verdade.
  2. `ProcessMissedOccurrencesUseCase` não grava um `MISSED` pra um `scheduledDate` que já é o `lastCompletedScheduledDate` do item — evita duplicar um registro `COMPLETED` que já existe pra aquele ciclo.
  3. `ProcessMissedOccurrencesUseCase` carrega `pendingExtensionOriginalDueDate`/`pendingExtensionCount` pro registro `MISSED` do primeiro ciclo pulado (mesmo padrão de `CompleteItemUseCase`/`IgnoreOccurrenceUseCase`) e limpa os dois campos do `Item` ao avançar — uma extensão pendente nunca resolvida não fica órfã.

  Gatilhos: `ReminderCheckWorker` (`PeriodicWorkRequest`, `:core:notifications`) e pull-to-refresh manual na Home (`HomeUseCase.refreshReminders()` → `ReminderRefreshTrigger.refreshNow()`, mesmo mecanismo que já disparava o worker fora do ciclo após concluir um item).
- **Urgência** (`UrgencyLevel`) é **derivada** de `dueDate` vs. hoje, não persistida: `< hoje` = `OVERDUE` (vermelho); `<= hoje + N dias` = `DUE_SOON` (âmbar); senão `NORMAL`. `N` (limiar "vencendo em breve") fica em `Constants` — 3 dias por padrão (a decidir se configurável). `isPinned` (item fixado manualmente) entra na priorização independente desse cálculo.

### Migrations (histórico, `data/local/database/migration/`)
```
MIGRATION_4_5   cria item_completion_history + índice; apaga itens recorrentes existentes (modelo antigo não
                tinha vínculo entre instâncias de uma série — não dava pra migrar de forma determinística) (#126)
MIGRATION_5_6   dedup de (itemId, scheduledDate) duplicados (mantém o id maior); troca o índice simples por
                índice ÚNICO (itemId, scheduledDate) — completar 2x vira no-op no nível do banco (#133)
MIGRATION_6_7   adiciona items.lastCompletedScheduledDate (#133)
MIGRATION_7_8   adiciona items.isPinned, default 0 (#127)
MIGRATION_8_9   adiciona items.pendingExtensionOriginalDueDate/pendingExtensionCount e
                item_completion_history.originalScheduledDate/extensionCount (#101/C)
```
Sem `fallbackToDestructiveMigration` — migration faltando falha alto, nunca perde dados silenciosamente (app pré-MVP, mas a regra vale mesmo assim).

## DI — estrutura Koin

Cada módulo registra seu próprio Koin module — DI é **local ao módulo**, não centralizada em `:app` (diferente do GymLog, onde tudo ficava em `:app/di/`; decisão deliberada aqui: cada camada é dona da sua fiação). `AppModule.kt` (`:app`) é o único ponto de entrada no `startKoin`, só agregando os demais via `includes(...)`.

```
data/di/DataModule.kt         — UnideasDatabase (single), DAOs (single, incl. ItemCompletionHistoryDao),
                                 Repositories (singleOf().bind(), incl. ItemCompletionHistoryRepositoryImpl) — confirmado em #21/#22/#126
domain/di/DomainModule.kt     — Use Cases (factoryOf); todos os de Section, Tag e Item já registrados, incl. HomeUseCase (#66),
                                 SetItemPinnedUseCase (#127), ItemCompletionHistoryUseCase (#126, CRUD completo desde #169) e
                                 ProcessMissedOccurrencesUseCase (#126), IgnoreOccurrenceUseCase, ExtendItemDueDateUseCase e
                                 ItemOccurrenceUseCase (#101/A/B)
core/backup/di/BackupDataModule.kt — backupDataModule: GoogleAuthRepository + BackupRepository (singleOf().bind()),
                                      use cases (factoryOf) e BackupViewModel (viewModelOf) — completo em #30 (E1.2)
core/notifications/di/NotificationsModule.kt — notificationsModule: ReminderNotifier (single), ReminderRefreshTriggerImpl
                                      (single, bind ReminderRefreshTrigger), ReminderCheckWorker (workerOf, Koin-WorkManager;
                                      depende de ProcessMissedOccurrencesUseCase desde #96 — cada varredura periódica também
                                      avança dueDate de itens recorrentes vencidos, não só notifica) — #95/#115/#96
feature/*/di/FeatureModule.kt — ViewModels de cada :feature:* (viewModelOf/viewModel{}); um módulo por :feature:*
                                 (items/sections/tags/settings/home já existem)

:app/di/AppModule.kt — includes(dataModule, domainModule, backupDataModule, notificationsModule, sectionsModule,
                        tagsModule, settingsModule, itemsModule, homeModule); backupDataModule entrou em #30, ainda sem
                        tela consumindo (E2/#16); startKoin roda em UnideasApplication (#42, primeiro bootstrap do projeto)
```

| Tipo | Escopo | DSL |
|---|---|---|
| `UnideasDatabase` | `single` | `single { UnideasDatabase.getInstance(androidApplication()) }` |
| DAO | `single` | `single { get<UnideasDatabase>().itemDao() }` |
| Repository | `single` | `singleOf(::ItemRepositoryImpl).bind<ItemRepository>()` |
| Use Case | `factory` | `factoryOf(::GetPriorityItemsUseCase)` |
| ViewModel | por VM | `viewModelOf(::HomeViewModel)` |

`UnideasDatabase` mantém singleton manual (`@Volatile` + `synchronized`) via `getInstance(context)` além do registro Koin, garantindo instância única mesmo fora do grafo de DI (ex: testes instrumentados).

## Backup (Google Drive)

Fluxo próprio e separado, específico pro Drive (**não** reaproveita Google Sign-In do Firebase Auth — este app não tem login geral):

`GoogleSignIn` (Play Services, escopo Drive) → `GoogleSignInAccount` → constrói cliente `com.google.api.services.drive.Drive` operando na pasta `appDataFolder`.

Estrutura em `:core:backup`:
- `GoogleAuthRepository` / `BackupRepository` (interfaces + impl auto-contidas no módulo)
- Use cases de sessão (sem `Drive` como parâmetro de entrada): `GetSignInIntentUseCase`, `GetSignedInAccountUseCase`, `BuildDriveServiceUseCase`
- Use cases de dados (recebem uma conta/`Drive`): `UploadBackupUseCase`, `ListBackupsUseCase`, `RestoreBackupUseCase`, `GetLastBackupInfoUseCase`
- `GoogleAuthUseCase` — facade sobre os 3 use cases de sessão (`getSignInIntent`/`getSignedInAccount`/`buildDriveService`)
- `BackupUseCase` — facade sobre os 4 use cases de dados; recebe `GoogleSignInAccount` direto e constrói o `Drive` internamente (compõe `BuildDriveServiceUseCase`), então o `BackupViewModel` nunca lida com o tipo `Drive` (#16)
- `BackupViewModel` — checa conexão (`GoogleAuthUseCase.getSignedInAccount()`) no `init` e pré-carrega o último backup se já conectado; `isConnected` explícito em `BackupUiState.Ready`, evento `OnConnectClick` dedicado (não dispara sign-in implícito no primeiro clique de backup/restore)
- `BackupViewModel` + `BackupUiState`/`BackupUiAction`/`BackupEvent`, exibido via `ModalBottomSheet` a partir de um item de lista na tela de Configurações (`SettingsScreen` hoisteia o mesmo `BackupViewModel` via `koinViewModel()` — Koin resolve a mesma instância pro item da lista e pro sheet, sem precisar repassar o ViewModel explicitamente entre composables). `BackupBottomSheet` é o **único** coletor de `BackupUiAction` (recebe `snackbarHostState` direto do `SettingsScreen`) — `Channel` não faz broadcast, então dois coletores do mesmo canal perdiam ações um pro outro de forma não-determinística (bug real, corrigido junto de #76).
- `checkpoint()` (`UnideasDatabase`) força o WAL a descarregar no `.db` principal antes do upload: `SupportSQLiteDatabase.query()` é lazy no Android — o `PRAGMA` só roda de fato quando o cursor é lido (`.use { it.moveToFirst() }`), não bastava abrir e fechar. Sem isso todo backup subia um arquivo vazio (4096 bytes, só cabeçalho) — bug real encontrado e corrigido em #76.
- Restore troca o arquivo físico do Room no disco; qualquer singleton Room/Koin já resolvido no processo (DAOs, repositórios) continua com o file handle antigo. Em vez de rastrear cada referência, `BackupUiAction.RestoreCompleted` reage reiniciando o processo inteiro via `Context.restartApplication()` (`:core:common`, ver seção abaixo) — só matar o processo garante que tudo seja reconstruído contra os dados restaurados; `finishAffinity()` sozinho não é suficiente (confirmado em device: processo sobrevive com o mesmo pid).

Sem sync automático, sem bidirecional — só "fazer backup agora" / "restaurar backup" sob demanda. `ViewModel → UseCase → Repository(Application)`: o `Context`/`Application` que as Google APIs exigem fica encapsulado no repositório, **nunca** no ViewModel.

### Setup externo (Google Cloud Console / Firebase)

O projeto Firebase `unideas-app` (já existente para Crashlytics/App Distribution) também hospeda o client OAuth do Drive — não precisa criar um projeto GCP separado, um projeto Firebase **é** um projeto Google Cloud por baixo (mesmo project ID). Pré-requisitos, feitos uma vez em [console.cloud.google.com](https://console.cloud.google.com) (não há API/CLI pra isso, é ação manual no navegador):

1. **APIs & Services → Library** → habilitar **"Google Drive API"**.
2. **APIs & Services → OAuth consent screen** → configurar (tipo "External" serve pra uso próprio/dev).
3. **APIs & Services → Credentials → Create Credentials → OAuth client ID → Android**, um client por variante (package name + SHA-1, obtidos via `./gradlew signingReport` — ver `docs/RELEASE.md`):
   - `com.seucaio.unideas.debug` (debug)
   - `com.seucaio.unideas` (release)

Não precisa client "Web" nem nada hardcoded no app — o código usa só `.requestEmail().requestScopes(Scope(DriveScopes.DRIVE_APPDATA))` (sem `requestIdToken`/`serverClientId`), então o Play Services resolve o client automaticamente batendo `package_name` + assinatura contra o que está registrado no Console.

## Convenção de datas

| Camada | Tipo | Motivo |
|---|---|---|
| Entity (Room) | `Long` (epoch millis) | nativo, sem converter, ordenável |
| Domain model | `LocalDate` / `LocalDateTime` / `LocalTime` | type-safe, legível na lógica |
| Mapper | extensions em `:core:common` | `Long.toLocalDate()` / `LocalDate.toEpochMilli()` |
| Entity (Room), hora-do-dia | `Int` (segundos do dia) | `Item.dueTime` (#114) — sem timezone envolvida, epoch millis seria overkill |
| Mapper, hora-do-dia | extensions em `:core:common` | `Int.toLocalTime()` / `LocalTime.toSecondOfDayInt()` |
| UI (picker) | `Long.toLocalDateUtc()` | Material3 DatePicker retorna **UTC midnight** — converter diferente do banco |

`coreLibraryDesugaring` habilitado nos módulos que usam `java.time`.
