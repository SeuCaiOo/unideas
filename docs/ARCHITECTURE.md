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
:core:common         — utilitários Kotlin puros (extensions, constantes). Sem Compose
:core:ui             — UnideasTheme + componentes Compose compartilhados. Depende de :core:common via `api`
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
:domain       ←  :data
:domain       ←  :feature:*  (só interfaces/use cases, nunca :data)
:domain, :core:common, :core:ui  ←  :core:backup
:feature:settings  →  :core:backup
tudo  ←  :app  (faz o wiring de DI e navegação)
```

Regra dura: **`:feature:*` nunca depende de `:data` diretamente** — só de `:domain` (interfaces + use cases). A implementação concreta do repositório é injetada via Koin no `:app`. Isso mantém as features testáveis e desacopladas da persistência.

## Estrutura de pacotes por módulo

Namespace base: `com.seucaio.unideas`. Cada módulo tem seu sufixo.

### `:domain` — `com.seucaio.unideas.domain`

```
domain/
├── model/            — modelos de domínio (datas como LocalDate/LocalDateTime)
│   ├── Item.kt
│   ├── ItemType.kt          — enum TASK | NOTE
│   ├── Recurrence.kt        — sealed interface: None/Daily/Weekly/Monthly (data object) + EveryNDays(days: Int) (data class, intervalo customizado)
│   ├── UrgencyLevel.kt      — enum OVERDUE | DUE_SOON | NORMAL (derivado de dueDate)
│   ├── Section.kt
│   ├── Tag.kt
│   └── outcome/             — resultados ricos de operações (ver CONVENTIONS.md)
│       ├── DeletionStatus.kt   — Deleted | BlockedByLinkedItems(count)
│       ├── SaveResult.kt
│       └── CompletionResult.kt — Completed | CompletedAndRenewed(newItemId)
├── repository/       — interfaces (contratos), sem implementação
│   ├── ItemRepository.kt
│   ├── SectionRepository.kt
│   └── TagRepository.kt
└── usecase/
    ├── item/         — Create/Edit/Delete/Complete/GetItems/GetItemDetail/GetPriorityItems
    ├── section/      — Get/Add/Rename/Delete (delete verifica vínculo antes)
    └── tag/          — Get/Add/Rename/Delete (delete verifica vínculo antes)
```

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
│   ├── converter/    — TypeConverters (enums; datas ficam como Long, sem converter)
│   └── relation/     — POJOs @Relation/@Embedded (ex: ItemWithTags) — joins no Room, nunca em memória
├── mapper/           — extension functions Entity ↔ Domain
├── repository/       — ItemRepositoryImpl, SectionRepositoryImpl, TagRepositoryImpl
└── di/               — DataModule.kt (Koin, local ao módulo — ver seção DI abaixo)
```

### `:core:common` — `com.seucaio.unideas.core.common`

```
core/common/
├── extensions/       — Kotlin extensions puros (Boolean.orFalse, String.EMPTY, Long.toLocalDate, etc.)
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

Flat na raiz do módulo — `Screen` + `PreviewProvider` de cada tela direto em `feature/<nome>/`, sem subpasta por tela (nome do arquivo já distingue). `navigation/`, `viewmodel/` e `di/` são as únicas subpastas, compartilhadas por todas as telas do módulo.

```
feature/items/
├── navigation/
│   ├── ItemsNavGraph.kt
│   └── ItemsRoute.kt              — @Serializable, type-safe: Form(itemId: Long?) | Detail(itemId: Long) | List
├── di/
│   └── FeatureModule.kt           — val itemsModule
├── viewmodel/                     — uma tela = 4 arquivos (UiState/UiAction/Event/ViewModel), todas juntas aqui
│   ├── ItemFormUiState.kt / ItemFormUiAction.kt / ItemFormEvent.kt / ItemFormViewModel.kt
│   ├── ItemDetailUiState.kt / ItemDetailUiAction.kt / ItemDetailEvent.kt / ItemDetailViewModel.kt / ItemDetailDialogState.kt
│   └── ItemsListUiState.kt / ItemsListUiAction.kt / ItemsListEvent.kt / ItemsListViewModel.kt
├── ItemFormScreen.kt + ItemFormPreviewProvider.kt      — criar/editar Item (tela única)
├── ItemDetailScreen.kt + ItemDetailPreviewProvider.kt  — detalhe do Item
└── ItemsListScreen.kt + ItemsListPreviewProvider.kt    — listagem dev-only (#62), sem abas/filtro; descartável quando Home (D2.1/#27) existir
```

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
domain/di/DomainModule.kt     — Use Cases (factoryOf); todos os de Section, Tag e Item já registrados (GetItems entrou em #62,
                                 pré-requisito de ItemsListViewModel — existia desde #23 mas nunca tinha sido wireado)
core/backup/di/BackupModule.kt — repos + use cases de :core:backup — ainda não existe, chega com E1
feature/*/di/FeatureModule.kt — ViewModels de cada :feature:* (viewModelOf/viewModel{}); um módulo por :feature:*
                                 (items/sections/tags/settings já existem; home chega com D2)

:app/di/AppModule.kt — includes(dataModule, domainModule, sectionsModule, tagsModule, settingsModule, itemsModule);
                        backupModule entra quando E1 existir; startKoin roda em UnideasApplication (#42, primeiro bootstrap do projeto)
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
- Use cases: `GetSignInIntentUseCase`, `BuildDriveServiceUseCase`, `UploadBackupUseCase`, `ListBackupsUseCase`, `RestoreBackupUseCase`, `GetLastBackupInfoUseCase`
- `BackupViewModel` + `BackupUiState`/`BackupUiAction`/`BackupEvent`, exibido via `ModalBottomSheet`/seção na tela de Configurações.

Sem sync automático, sem bidirecional — só "fazer backup agora" / "restaurar backup" sob demanda. `ViewModel → UseCase → Repository(Application)`: o `Context`/`Application` que as Google APIs exigem fica encapsulado no repositório, **nunca** no ViewModel.

## Convenção de datas

| Camada | Tipo | Motivo |
|---|---|---|
| Entity (Room) | `Long` (epoch millis) | nativo, sem converter, ordenável |
| Domain model | `LocalDate` / `LocalDateTime` | type-safe, legível na lógica |
| Mapper | extensions em `:core:common` | `Long.toLocalDate()` / `LocalDate.toEpochMilli()` |
| UI (picker) | `Long.toLocalDateUtc()` | Material3 DatePicker retorna **UTC midnight** — converter diferente do banco |

`coreLibraryDesugaring` habilitado nos módulos que usam `java.time`.
