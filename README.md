# unideas

<!-- TODO: uma frase descrevendo a ideia do app -->

## Status

🚧 **Alpha (`0.0.x`)** — MVP funcional (milestone [`v0.1.0 — MVP`](https://github.com/SeuCaiOo/unideas/milestone/1) com as 28 issues fechadas), builds de teste distribuídos via Firebase App Distribution. Versão atual: **v0.0.3**.

Telas implementadas: Home (painel de prioridades + abas + filtros), Todas as Prioridades, Criar/Editar Item, Detalhe do Item, Gerenciar Seções, Gerenciar Tags, Configurações (com backup no Google Drive).

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin 2.2.10 |
| UI | Jetpack Compose (100%, sem XML/Fragments) |
| Persistência | Room |
| DI | Koin |
| Backup | Google Drive API (sign-in escopado, sem Firebase Auth) |
| Build | Gradle KTS, AGP 9.2.1 |

**Min SDK:** 24 · **Target/Compile SDK:** 37 · **JVM:** 11

## Arquitetura

Multi-módulo (MVI, sem KMP): `:app` + `:domain`, `:data`, `:core:common`, `:core:ui`, `:core:backup`, `:feature:{home,items,sections,tags,settings}`. Detalhes em [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md); navegação em [`docs/FLOW.md`](docs/FLOW.md).

## Getting Started

```bash
# Clone o repositório
git clone git@github.com:SeuCaiOo/unideas.git

# Build e instala o APK debug
./gradlew installDebug
```

## Contributing

Veja [AGENTS.md](AGENTS.md) / [CLAUDE.md](CLAUDE.md) para diretrizes de desenvolvimento.