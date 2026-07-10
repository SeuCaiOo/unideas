# unideas — Blueprint de Implementação

O **o quê construir** e **em que ordem**. Ponte entre a planta de produto (o quê/por quê), [`ARCHITECTURE.md`](ARCHITECTURE.md) (estrutura), [`FLOW.md`](FLOW.md) (navegação) e o backlog de issues no GitHub.

> Documento vivo. Cada linha do backlog vira (ou já é) uma issue. Marque o `#N` quando a issue for criada.

---

## 1. Inventário de telas (7)

| # | Tela | Módulo | Composables/ViewModel principais |
|---|---|---|---|
| 1 | Home | `:feature:home` | `HomeScreen` + `HomeViewModel`; `PriorityPanel`, `ItemTabs`, `SectionFilter`, `TagFilterChips`, `ItemRow` |
| 2 | Todas as Prioridades | `:feature:home` | `AllPrioritiesScreen` + `AllPrioritiesViewModel` |
| 3 | Criar/Editar Item | `:feature:items` | `ItemFormScreen` + `ItemFormViewModel` |
| 4 | Detalhe do Item | `:feature:items` | `ItemDetailScreen` + `ItemDetailViewModel` |
| 5 | Gerenciar Seções | `:feature:sections` | `SectionsListScreen` + `SectionsListViewModel` |
| 6 | Gerenciar Tags | `:feature:tags` | `TagsListScreen` + `TagsListViewModel` |
| 7 | Configurações/Backup | `:feature:settings` | `SettingsScreen` + `SettingsViewModel` + `BackupViewModel` (de `:core:backup`) |

---

## 2. Inventário de classes por módulo (aprox.)

Cada tela MVI = 6 arquivos (`Screen`, `PreviewProvider`, `UiState`, `UiAction`, `Event`, `ViewModel`) + `NavGraph`/`Route` por módulo.

### `:domain` (~25)
- **Models (3):** `Item`, `Section`, `Tag`
- **Enums (2):** `ItemType` (TASK|NOTE), `UrgencyLevel` (OVERDUE|DUE_SOON|NORMAL, derivado)
- **`Recurrence`** (sealed interface, não enum): `None`/`Daily`/`Weekly`/`Monthly` (data object) + `EveryNDays(days: Int)` (data class, intervalo customizado)
- **Outcomes (3):** `DeletionStatus` (Deleted | BlockedByLinkedItems(count)), `SaveResult`, `CompletionResult` (Completed | CompletedAndRenewed(newItemId))
- **Repository interfaces (3):** `ItemRepository`, `SectionRepository`, `TagRepository`
- **Use cases (15):**
  - Item (7): `CreateItemUseCase`, `EditItemUseCase`, `DeleteItemUseCase`, `CompleteItemUseCase` (renasce se recorrente), `GetItemsUseCase` (aba + filtro seção/tags), `GetItemDetailUseCase`, `GetPriorityItemsUseCase` (vencidos + vencendo em ≤N, com limite)
  - Section (4): `GetSectionsUseCase`, `AddSectionUseCase`, `RenameSectionUseCase`, `DeleteSectionUseCase` (bloqueia se vinculada)
  - Tag (4): `GetTagsUseCase`, `AddTagUseCase`, `RenameTagUseCase`, `DeleteTagUseCase` (bloqueia se vinculada)

### `:data` (~16)
- **Entities (4):** `ItemEntity`, `SectionEntity`, `TagEntity`, `ItemTagCrossRef`
- **Relations (1–2):** `ItemWithTags` (`@Relation`, join no Room — nunca em memória)
- **DAOs (3):** `ItemDao`, `SectionDao`, `TagDao` (retornam `Flow`)
- **Database (1):** `UnideasDatabase` (singleton `@Volatile`)
- **Converters (1):** enums
- **Mappers (3):** `ItemMapper`, `SectionMapper`, `TagMapper`
- **Repo impls (3):** `ItemRepositoryImpl`, `SectionRepositoryImpl`, `TagRepositoryImpl`

### `:core:common` (~6)
- Extensions: `BooleanExtensions`, `StringExtensions`, `LongExtensions`, `DateExtensions` (`toLocalDate`/`toEpochMilli`/`toLocalDateUtc`/`toFormattedDateString`)
- `Constants` (defaults, limiar de urgência N=3, limite do painel)

### `:core:ui` (~11)
- Theme (3): `Theme` (`UnideasTheme`), `Color`, `Type` — Material 3 dark, acento teal
- Components (8): `UnideasTopBar`, `UnideasLoadingContent`, `UnideasErrorContent`, `UnideasEmptyContent`, `UnideasListItem`, `DeleteConfirmationDialog`, `UrgencyIndicator`, `TagChip`/`SectionDropdown`

### `:core:backup` (~12)
- Repos (2): `GoogleAuthRepository`, `BackupRepository` (+ impl)
- Use cases (6): `GetSignInIntentUseCase`, `BuildDriveServiceUseCase`, `UploadBackupUseCase`, `ListBackupsUseCase`, `RestoreBackupUseCase`, `GetLastBackupInfoUseCase`
- `BackupViewModel` + `BackupUiState`/`BackupUiAction`/`BackupEvent` + `BackupBottomSheet`

### `:feature:*` (~52 total)
- `items` ~14 (2 telas), `home` ~14 (2 telas), `sections` ~8, `tags` ~8, `settings` ~8

### `:app` (~7)
- `NavGraph` (NavHost central), `AppModule`/`DataModule`/`DomainModule`/`BackupModule`/`PresentationModule`, `MainActivity`

**Total aproximado: ~130 classes.** Não é meta — é o mapa. Cada uma nasce dentro da issue da sua feature, com teste quando a camada exige (ver [`CONVENTIONS.md`](CONVENTIONS.md#testes)).

---

## 3. Backlog priorizado (ordem de implementação)

Prioridades: **P0** = fundação, bloqueia tudo · **P1** = MVP core · **P2** = MVP complementar · **P3** = polimento.

O grafo de dependências manda: dados antes de use cases, use cases + design system antes das telas, telas antes do wiring final.

```
A. Fundação de dados (P0)
   A1 :domain models + interfaces ──┐
   A2 :core:common utils            ├─→ A3 :data (Room + repos)
                                     │
B. Use cases (P1) ── dependem de A3
   B1 Item use cases
   B2 Section use cases
   B3 Tag use cases

C. Design system (P1) ── independente de B, depende só de :core:common
   C1 :core:ui (theme + componentes)

D. Features (P1/P2) ── cada uma depende de (B correspondente) + C1
   D1 :feature:items    (Form + Detail)        [P1]  dep: B1, C1
   D2 :feature:home     (Home + AllPriorities)  [P1]  dep: B1, C1, D1 (navega p/ Detail/Form)
   D3 :feature:sections                         [P2]  dep: B2, C1
   D4 :feature:tags                             [P2]  dep: B3, C1
   D5 :feature:settings (shell + Organizar)     [P2]  dep: C1, D3, D4

E. Backup (P2/P3)
   E1 :core:backup (auth + drive + VM)          [P2]  dep: A1
   E2 integrar backup na Settings               [P2]  dep: E1, D5

F. Wiring & acabamento
   F1 :app NavHost + DI + MainActivity          [P1]  incremental, fecha em D2
   F2 i18n PT/EN (strings.xml)                  [P2]  transversal
   F3 estado vazio + textos orientadores (Home) [P2]  dep: D2
```

### Tabela de issues (proposta)

| Épico | Título (en) | Prioridade | Depende de | Sub-issues? |
|---|---|---|---|---|
| A1 | `feat: domain models, enums and repository interfaces` | P0 | — | — |
| A2 | `feat: core:common utilities (date/primitive extensions, constants)` | P0 | — | — |
| A3 | `feat: Room persistence layer (entities, DAOs, mappers, repositories)` | P0 | A1, A2 | pode dividir por entidade se ficar grande |
| B1 | `feat: item use cases (CRUD, complete+recurrence, priorities, filters)` | P1 | A3 | sim: recorrência e painel de prioridades podem ser sub-issues |
| B2 | `feat: section use cases (CRUD, delete blocked by linked items)` | P1 | A3 | — |
| B3 | `feat: tag use cases (CRUD, delete blocked by linked items)` | P1 | A3 | — |
| C1 | `feat: core:ui theme and shared components (Material 3 dark teal)` | P1 | A2 | — |
| D1 | `feat: item form and detail screens` | P1 | B1, C1 | sim: Form e Detail como sub-issues |
| D2 | `feat: home screen (priority panel, tabs, filters) + all priorities` | P1 | B1, C1, D1 | sim: painel, abas/filtros, AllPriorities |
| D3 | `feat: manage sections screen` | P2 | B2, C1 | — |
| D4 | `feat: manage tags screen` | P2 | B3, C1 | — |
| D5 | `feat: settings screen shell (organize → sections/tags)` | P2 | C1, D3, D4 | — |
| E1 | `feat: core:backup (Google Drive sign-in, upload/restore use cases, VM)` | P2 | A1 | sim: auth, drive client, use cases, VM/UI |
| E2 | `feat: wire Google Drive backup into settings screen` | P2 | E1, D5 | — |
| F2 | `feat: PT/EN string resources (i18n)` | P2 | transversal | — |
| F3 | `feat: home empty state with onboarding text` | P2 | D2 | — |

Wiring de DI/NavHost (F1) não é issue separada — acompanha cada feature (registrar ViewModel no `PresentationModule`, rota no `NavGraph`), fechando naturalmente ao concluir D2.

### Caminho crítico do MVP

```
A1 → A3 → B1 → (C1 em paralelo) → D1 → D2  ← primeiro fluxo usável (criar/ver/priorizar item)
                                    ↓
                        D3, D4 → D5 → E1 → E2  ← organização + backup
                                    ↓
                              F2, F3  ← i18n + estado vazio
```

**Primeira issue a criar: A1** (`:domain` — models + interfaces). Tudo depende dela.
