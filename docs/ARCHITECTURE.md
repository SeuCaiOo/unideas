# Arquitetura

Documento técnico — complementa a planta de produto (artifact privado do usuário, "unideas — Planta do Produto"), que cobre o quê/por quê. Aqui é o como: estrutura de módulos, convenções, schema.

## Padrão

MVI (Model-View-Intent): cada tela expõe um `ViewModel` que recebe `Intent`/`Action` explícitas da UI e emite um `UiState` imutável único, via `StateFlow`. Eventos pontuais (navegação, snackbars) via `Channel`/`Flow` separado (`UiAction`/`Event`), não pelo `UiState`.

Sem KMP — Android nativo puro (`com.android.application`/`com.android.library`, sem `commonMain`/`androidMain`).

## Módulos

```
:app                    — entry point, DI wiring (Koin), NavHost
:domain                 — modelos + use cases, puro Kotlin/Android, sem Compose
:data                    — Room, DataStore, implementações de repositório
:core:common            — utilitários compartilhados (sem Compose)
:core:ui                — tema, componentes Compose compartilhados
:core:backup            — backup/restore via Google Drive (Google Sign-In escopado + Drive API), auto-contido
:feature:home            — Home (Painel de Prioridades, abas Tarefas/Anotações, Todas as Prioridades)
:feature:items           — Criar/Editar Item, Detalhe do Item
:feature:sections        — Gerenciar Seções
:feature:tags            — Gerenciar Tags
:feature:settings        — Configurações (usa :core:backup)
```

### Direção de dependência

```
:feature:*  ──depends on──>  :domain, :core:ui  (settings também depende de :core:backup)
:data       ──depends on──>  :domain, :core:common
:core:backup ──depends on──> :domain, :core:common, :core:ui
:app        ──depends on──>  tudo (wiring)
```

`:feature:*` nunca depende de `:data` diretamente — só de `:domain` (interfaces/use cases). A implementação concreta é injetada via Koin no `:app`.

## Convenções

- Namespace: `com.seucaio.unideas.<módulo>` (ex.: `com.seucaio.unideas.feature.home`)
- `compileSdk = 37`, `minSdk = 24`, Java 11, desugaring habilitado em módulos com dependência de coleções/APIs modernas
- Detekt: `config/detekt/detekt.yml` sempre; `+ detekt-compose.yml` nos módulos com Compose
- Testes: JUnit + MockK + coroutines-test; `:domain` e `:data` têm `testFixtures` habilitado pra fakes compartilhados

## Persistência (Room) — schema

Ainda não implementado — entra na primeira issue que tocar `:domain`/`:data`. Rascunho conceitual (baseado na seção 4 da planta de produto):

- `Item` (id, tipo [Tarefa|Anotação], título, descrição, seçãoId?, dataVencimento?, recorrência?, concluídoEm?, criadoEm)
- `Section` (id, nome)
- `Tag` (id, nome)
- `ItemTag` (tabela de junção — Item ↔ Tag, N:N)

Exclusão de `Section`/`Tag` com itens vinculados é bloqueada na camada de `domain` (use case verifica antes de delegar ao repositório), não uma constraint de FK que falha silenciosamente.

## Backup (Google Drive)

Segue exatamente o padrão do projeto de referência GymLog: `GoogleSignIn` (Play Services, escopo Drive) → `GoogleSignInAccount` → constrói cliente `com.google.api.services.drive.Drive`. Não é o mesmo login usado por Firebase Auth (que este projeto nem usa hoje). Estrutura de classes em `:core:backup`: `BackupRepository` + use cases (`BuildDriveServiceUseCase`, `UploadBackupUseCase`, `ListBackupsUseCase`, `RestoreBackupUseCase`) + `BackupViewModel`/`UiState`/`UiAction`/`Event`.
