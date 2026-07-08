---
name: new-issue
description: Use when creating a GitHub issue in the unideas project — covers title format, label selection, assignee, body template, and linking to the GitHub Project board.
---

# New Issue — unideas Workflow

## Rules

- Title: **English**, `type: short description` (Conventional Commits prefix)
- Body: **PT-BR**
- Assignee: whoever is creating the issue (`--assignee "@me"`) — don't hardcode a username, another dev may join the project in the future
- Always link to the GitHub Project after creating

## Step-by-step

### 1. Create the issue

```bash
gh issue create \
  --title "<type>: <short description>" \
  --body "$(cat <<'EOF'
## Contexto

<o que é e por que está sendo feito>

**Pré-requisito:** <issue dependente ou "nenhum">

---

## Definition of Ready (DoR)

> Issue só pode ser iniciada quando todos os itens abaixo estiverem ok.

- [ ] Contexto e objetivo estão claros
- [ ] Pré-requisitos concluídos
- [ ] Checklist de implementação definido
- [ ] Issue vinculada ao projeto e assignee definido

---

## Checklist

- [ ] <passo 1>
- [ ] <passo 2>

---

## Definition of Done (DoD)

> Issue só é considerada concluída quando todos os itens abaixo estiverem ok.

- [ ] Todos os itens do checklist concluídos
- [ ] Testes unitários escritos e passando (`./gradlew test`)
- [ ] Cobertura de testes ok (`./gradlew koverVerify`)
- [ ] Sem warnings novos de Detekt (`./gradlew detekt`)
- [ ] PR aberto, revisado e mergeado em `dev`
EOF
)" \
  --label "<label>" \
  --assignee "@me"
```

### 2. Apply label mapping

| Commit type | Label |
|---|---|
| `feat` | `feature` |
| `fix` | `fix` or `bug` |
| `chore`, `build` | `chore` or `configuration` |
| `ci` | `tooling` |
| `docs` | `documentation` |
| `test` | `testing` |
| `style`, `refactor`, `perf` | `quality` |
| `ui` | `ui` |

### 3. Link to GitHub Project

```bash
ISSUE_ID=$(gh api repos/SeuCaiOo/unideas/issues/<number> --jq '.node_id')
gh api graphql -f query="
mutation {
  addProjectV2ItemById(input: {
    projectId: \"PVT_kwHOAVNuW84Bcrp8\"
    contentId: \"$ISSUE_ID\"
  }) { item { id } }
}"
```

Project URL: https://github.com/users/SeuCaiOo/projects/4

## Common mistakes

| Mistake | Fix |
|---|---|
| Title in PT-BR | Must be in English |
| Assignee hardcoded to a specific username | Always `--assignee "@me"` |
| Not linked to project | Run GraphQL mutation after creating |
| Label not applied | Pass `--label` on creation or `gh issue edit <n> --add-label` |