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
:core:ui             — UnideasTheme + componentes Compose compartilhados. Depende de :core:common via `api`. Em substituição gradual por :uds (#84/#82) — não adicionar componentes novos aqui
:uds                 — novo design system (pacote com.seucaio.unideas.ds), portado de outro projeto (#87), domain-agnostic (não depende de :domain nem de :core:common). Vai substituir :core:ui aos poucos conforme as telas forem migradas (#84); expõe Compose via `api` — quem depende de :uds não precisa redeclarar BOM/artifacts de Compose
:core:backup         — backup/restore via Google Drive (Google Sign-In escopado + Drive API), auto-contido
:feature:home        — Home (Painel de Prioridades, abas Tarefas/Anotações) + Todas as Prioridades
:feature:items       — Criar/Editar Item + Detalhe do Item
:feature:sections    — Gerenciar Seções
:feature:tags        — Gerenciar Tags
:feature:settings    — Configurações (usa :core:backup)
```

### Direção de dependência

```
:core:common  ←  :data
:core:common  ←  :core:ui (api)  ←  :feature:*
:uds  ←  :app  (ainda não usado por nenhum :feature:*; passa a ser consumido conforme #84 migra as telas)
:domain       ←  :data
:domain       ←  :feature:*  (só interfaces/use cases, nunca :data)
:domain, :core:common, :core:ui, :data  ←  :core:backup
:feature:settings  →  :core:backup
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
│   ├── SectionsAndTags.kt   — snapshot único de sections+tags pra telas que selecionam dos dois (ex: ItemForm)
│   ├── ItemType.kt          — enum TASK | NOTE
│   ├── Recurrence.kt        — sealed interface: None/Daily/Weekly/Monthly (data object) + EveryNDays(days: Int) (data class, intervalo customizado)
│   ├── UrgencyLevel.kt      — enum OVERDUE | DUE_SOON | NORMAL (derivado de dueDate)
│   ├── Section.kt
│   ├── Tag.kt
│   ├── SeedScope.kt         — enum EMPTY | BASIC | FULL — cenário de dado de exemplo, debug-only (#19)
│   └── outcome/             — resultados ricos de operações (ver CONVENTIONS.md)
│       ├── DeletionStatus.kt   — Deleted | BlockedByLinkedItems(count)
│       ├── SaveResult.kt
│       └── CompletionResult.kt — Completed | CompletedAndRenewed(newItemId)
├── repository/       — interfaces (contratos), sem implementação
│   ├── ItemRepository.kt
│   ├── SectionRepository.kt
│   ├── TagRepository.kt
│   └── DatabaseRepository.kt     — clearAll()/seed(scope) — debug-only tooling (#19), implementado em :data
└── usecase/
    ├── GetSectionsAndTagsUseCase.kt  — snapshot único (suspend, não Flow) pra ItemForm; sem combine, dados não mudam com a tela aberta
    ├── item/         — Create/Edit/Delete/Complete/GetItem/GetItemDetail/GetItems/GetPriorityItems
    │   ├── ItemDetailUseCase.kt   — facade delegando pros use cases que ItemDetailViewModel usa (getDetail/delete/complete)
    │   ├── ItemFormUseCase.kt     — facade delegando pros use cases que ItemFormViewModel usa (get/create/edit)
    │   └── HomeUseCase.kt         — facade delegando pros use cases que HomeViewModel/AllPrioritiesViewModel usam (getPriorityItems/getItems/complete)
    ├── section/      — Get/Add/Rename/Delete (delete verifica vínculo antes)
    │   └── SectionUseCase.kt      — facade delegando pros 4 acima (CRUD completo, um método por operação)
    ├── tag/          — Get/Add/Rename/Delete (delete verifica vínculo antes)
    │   └── TagUseCase.kt          — facade delegando pros 4 acima, mesmo formato de SectionUseCase
    └── settings/     — SeedDatabaseUseCase/ClearDatabaseUseCase — debug-only (#19), gatilho só em Settings quando BuildConfig.DEBUG
```

**Facades de use case** (`SectionUseCase`, `TagUseCase`, `ItemDetailUseCase`, `ItemFormUseCase`, `HomeUseCase`): compõem os use cases de operação única já existentes (mantidos intactos, ainda usáveis sozinhos) — um método por operação, cada um só delegando (`fun add(name) = addSection(name)`), **sem acesso a repositório**. Existem só pra reduzir a quantidade de use cases que um ViewModel precisa receber no construtor; não são um "God object" — nomeados pela tela que servem quando a entidade se espalha por telas com subconjuntos diferentes de operações (caso do Item: `ItemDetailUseCase` ≠ `ItemFormUseCase` ≠ `HomeUseCase`), ou pela entidade quando uma única tela usa o CRUD inteiro (caso de Section/Tag). `HomeUseCase` é compartilhada por `HomeViewModel` e `AllPrioritiesViewModel` (mesma tela-conceito, dois pontos de entrada). Ver `CONVENTIONS.md` para o critério completo.

### `:data` — `com.seucaio.unideas.data`

```
data/
├── local/
│   ├── entity/       — @Entity Room (datas como Long epoch millis)
│   │   ├── ItemEntity.kt
│   │   ├── SectionEntity.kt
│   │   ├── TagEntity.kt
│   │   └── ItemTagCrossRef.kt      — junção N:N Item ↔ Tag
│   ├── dao/          — ItemDao, SectionDao, TagDao (retornam Flow)
│   ├── database/     — UnideasDatabase (singleton @Volatile + Room builder)
│   │                    DatabaseSeeder.kt — debug-only (#19): semeia via DAO direto (não pelos use cases), pacote excluído do koverVerify
│   ├── converter/    — TypeConverters (enums; datas ficam como Long, sem converter)
│   └── relation/     — POJOs @Relation/@Embedded (ItemWithTags; ItemWithTagsAndSection também resolve a seção) — joins no Room, nunca em memória
├── mapper/           — extension functions Entity ↔ Domain
├── repository/       — ItemRepositoryImpl, SectionRepositoryImpl, TagRepositoryImpl, DatabaseRepositoryImpl
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

### `:core:ui` — `com.seucaio.unideas.core.ui`

```
core/ui/
├── theme/            — UnideasTheme, Color, Type (Material 3, light + dark, acento teal)
└── components/       — composables compartilhados entre features:
    ├── UnideasTopBar.kt
    ├── UnideasLoadingContent.kt
    ├── UnideasErrorContent.kt
    ├── UnideasEmptyContent.kt          — estado vazio com texto orientador
    ├── UnideasListItem.kt
    ├── DeleteConfirmationDialog.kt
    ├── UrgencyIndicator.kt             — cor de prazo (vermelho/âmbar) — uso EXCLUSIVO de prazo
    ├── TagChip.kt / SectionDropdown.kt
    └── AppVersionFooter.kt             — recebe versionName como parâmetro (não lê BuildConfig do :app)
```

### `:feature:*` — `com.seucaio.unideas.feature.<nome>`

Dois formatos, conforme o módulo tem uma tela só ou várias:

- **Módulo com uma tela** (Sections, Tags, Settings): flat na raiz — `Screen` + `PreviewProvider` direto em `feature/<nome>/`, sem subpasta por tela. `navigation/`, `viewmodel/` e `di/` são as únicas subpastas.
- **Módulo com várias telas** (Items — Form/Detail/List): cada tela ganha seu próprio `features/<tela>/{screen,viewmodel}/`, já que um único pacote `viewmodel/` compartilhado misturava os 4 arquivos MVI de cada tela sem nenhuma separação visual. `navigation/` e `di/` continuam fora de `features/`, compartilhados pelas telas do módulo.

```
feature/items/
├── navigation/
│   ├── ItemsNavGraph.kt
│   └── ItemsRoute.kt              — @Serializable, type-safe: Form(itemId: Long?) | Detail(itemId: Long) | List
├── di/
│   └── FeatureModule.kt           — val itemsModule
└── features/
    ├── form/
    │   ├── screen/    — ItemFormScreen.kt + ItemFormPreviewProvider.kt
    │   └── viewmodel/ — ItemFormUiState.kt / ItemFormUiAction.kt / ItemFormEvent.kt / ItemFormViewModel.kt
    ├── detail/
    │   ├── screen/    — ItemDetailScreen.kt + ItemDetailPreviewProvider.kt
    │   └── viewmodel/ — ItemDetailUiState.kt / ItemDetailUiAction.kt / ItemDetailEvent.kt / ItemDetailViewModel.kt / ItemDetailDialogState.kt
    └── list/
        ├── screen/    — ItemsListScreen.kt + ItemsListPreviewProvider.kt   — listagem dev-only (#62), sem abas/filtro; acessível via seção "Debug" do Settings, mantida mesmo com a Home (D2/#11) já existindo
        └── viewmodel/ — ItemsListUiState.kt / ItemsListUiAction.kt / ItemsListEvent.kt / ItemsListViewModel.kt
```

```
feature/home/
├── navigation/
│   ├── HomeNavGraph.kt
│   └── HomeRoute.kt              — @Serializable, type-safe: Panel | AllPriorities
├── di/
│   └── FeatureModule.kt          — val homeModule
└── features/
    ├── panel/
    │   ├── screen/    — HomeScreen.kt + HomePreviewProvider.kt + components/ (PriorityPanel.kt, Filters.kt, HomeItemRow.kt)
    │   └── viewmodel/ — HomeUiState.kt / HomeUiAction.kt / HomeEvent.kt / HomeViewModel.kt
    └── allpriorities/
        ├── screen/    — AllPrioritiesScreen.kt + AllPrioritiesPreviewProvider.kt   — reaproveita HomeItemRow do pacote panel/
        └── viewmodel/ — AllPrioritiesUiState.kt / AllPrioritiesUiAction.kt / AllPrioritiesEvent.kt / AllPrioritiesViewModel.kt
```

`feature/sections/` e `feature/tags/` continuam flat (uma tela só cada) — o padrão `features/<tela>/` só se aplica quando o módulo tem mais de uma tela.

O inventário completo de telas/ViewModels/use cases/entidades está em [`BLUEPRINT.md`](BLUEPRINT.md) (congelado como planejamento original — status vivo de cada issue fica no artifact "unideas — Improvements" e no board do GitHub Project).

## Persistência (Room) — schema

Datas armazenadas como **`Long` (epoch millis)** na entity; convertidas pra `LocalDate`/`LocalDateTime` no domínio via mappers (`coreLibraryDesugaring` habilita `java.time` no minSdk 24).

### `ItemEntity` → tabela `items`
```
id: Long                 PK autoincrement
type: String             TASK | NOTE (enum via TypeConverter)
title: String            obrigatório (não vazio)
description: String?      opcional, multilinha
sectionId: Long?          FK → sections.id (SET NULL on delete — mas exclusão é bloqueada antes, ver regra)
dueDate: Long?            epoch millis, opcional
recurrence: String        NONE | DAILY | WEEKLY | MONTHLY (default NONE; só válido se dueDate != null)
completedAt: Long?        epoch millis; != null = concluída (só faz sentido pra TASK)
createdAt: Long           epoch millis, preenchido na criação
```

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
- **Recorrência "renasce ao concluir"**: `CompleteItemUseCase`, ao concluir um item com `recurrence` diferente de `Recurrence.None`, marca o atual como concluído E gera uma nova instância com a próxima `dueDate` (calculada a partir da data de vencimento **original**, via `recurrence.nextDueDate(...)`). Não é regra computada em tela — é geração de novo registro.
- **Urgência** (`UrgencyLevel`) é **derivada** de `dueDate` vs. hoje, não persistida: `< hoje` = `OVERDUE` (vermelho); `<= hoje + N dias` = `DUE_SOON` (âmbar); senão `NORMAL`. `N` (limiar "vencendo em breve") fica em `Constants` — 3 dias por padrão (a decidir se configurável).

## DI — estrutura Koin

Cada módulo registra seu próprio Koin module — DI é **local ao módulo**, não centralizada em `:app` (diferente do GymLog, onde tudo ficava em `:app/di/`; decisão deliberada aqui: cada camada é dona da sua fiação). `AppModule.kt` (`:app`) é o único ponto de entrada no `startKoin`, só agregando os demais via `includes(...)`.

```
data/di/DataModule.kt         — UnideasDatabase (single), DAOs (single), Repositories (singleOf().bind()) — confirmado em #21/#22
domain/di/DomainModule.kt     — Use Cases (factoryOf); todos os de Section, Tag e Item já registrados, incl. HomeUseCase (#66)
core/backup/di/BackupDataModule.kt — backupDataModule: GoogleAuthRepository + BackupRepository (singleOf().bind()),
                                      use cases (factoryOf) e BackupViewModel (viewModelOf) — completo em #30 (E1.2)
feature/*/di/FeatureModule.kt — ViewModels de cada :feature:* (viewModelOf/viewModel{}); um módulo por :feature:*
                                 (items/sections/tags/settings/home já existem)

:app/di/AppModule.kt — includes(dataModule, domainModule, backupDataModule, sectionsModule, tagsModule, settingsModule,
                        itemsModule, homeModule); backupDataModule entrou em #30, ainda sem tela consumindo (E2/#16);
                        startKoin roda em UnideasApplication (#42, primeiro bootstrap do projeto)
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
| Domain model | `LocalDate` / `LocalDateTime` | type-safe, legível na lógica |
| Mapper | extensions em `:core:common` | `Long.toLocalDate()` / `LocalDate.toEpochMilli()` |
| UI (picker) | `Long.toLocalDateUtc()` | Material3 DatePicker retorna **UTC midnight** — converter diferente do banco |

`coreLibraryDesugaring` habilitado nos módulos que usam `java.time`.
