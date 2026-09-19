---
name: add-improvement
description: >
  Use when the user brings a new idea, improvement, complaint, or observation about the unideas app — UI, UX, flow, architecture, or anything else. This skill guides the full refinement-to-issue flow: listen, refine conversationally, validate the summary with the user, then create the GitHub issue directly and move it to Backlog on the project board. Use this skill whenever the user says "tenho uma ideia", "quero melhorar", "está feio", "percebi um problema", "quero mudar", or describes anything that would become a new improvement item — even if they don't say "add-improvement" explicitly.
---

# Add Improvement — unideas Workflow

Fluxo completo para transformar uma ideia bruta em um item documentado e rastreado no GitHub.

O GitHub (issues + board) é a única fonte da verdade para ideias/melhorias — não existe mais um artifact intermediário. Uma ideia só passa a existir de forma rastreável no momento em que vira issue (etapa 4 abaixo); antes disso ela vive só na conversa.

## Etapas

### 1. Ouvir e entender

Deixe o usuário falar livremente. Não interrompa com perguntas desnecessárias. Se a ideia for vaga, faça **uma pergunta por vez** para esclarecer o ponto mais importante antes de avançar.

### 2. Consultar evidências quando necessário

Antes de opinar ou resumir, busque evidências concretas no código (ViewModels, composables, arquivos afetados) quando o problema for técnico — ex: de onde vem um dado, por que um comportamento acontece. Não deduza — verifique. O unideas ainda não tem uma pasta de screenshots documentados; se precisar de referência visual, rode o app (skill `run` / `android` CLI) e capture a tela na hora.

### 3. Consolidar e listar

Após entender a ideia, liste tudo que o usuário disse — em tópicos curtos e claros — e pergunte: "Está correto e completo?"

Aguarde confirmação explícita antes de avançar. Se o usuário corrigir ou acrescentar algo, atualize a lista e confirme novamente.

### 4. Criar a issue no GitHub

Use `/new-issue` para criar a issue com:
- Título em inglês, formato Conventional Commits (`ui:`, `feat:`, `refactor:`, etc.)
- Body em PT-BR com: Contexto, DoR, descrição das mudanças, checklist, áreas afetadas, DoD
- Label adequada (`ui`, `feature`, `quality`, etc.)
- Assignee: `@me`

`new-issue` já cuida de vincular ao GitHub Project (#4) — não repita esse passo aqui.

### 5. Mover a issue para "Backlog"

O board tem dois níveis de espera: **Backlog** (tudo que é capturado e especificado) e **Todo** (o subconjunto priorizado, o que vem a seguir — promovido manualmente quando vira prioridade). Um item novo entra via `new-issue` sem status (fica em branco) e deve ir para **Backlog**:

```bash
gh api graphql -f query="
mutation {
  updateProjectV2ItemFieldValue(input: {
    projectId: \"PVT_kwHOAVNuW84Bcrp8\"
    itemId: \"<ITEM_ID>\"
    fieldId: \"PVTSSF_lAHOAVNuW84Bcrp8zhXSou4\"
    value: { singleSelectOptionId: \"19386e88\" }
  }) { projectV2Item { id } }
}"
```

(Status field ID e opção "Backlog" do projeto #4. Promover Backlog→Todo é manual, quando o item vira prioridade — não é responsabilidade desta skill.)

**Estágios seguintes** (In Progress, Done, Released) são responsabilidade do `/start-feature` (ao iniciar a issue) e do `/finish-issue`/`/open-pr` (ao mergear) — nada precisa ser feito aqui além de deixar a issue em Backlog.

---

## Referências do projeto

- Projeto GitHub: `PVT_kwHOAVNuW84Bcrp8` (https://github.com/users/SeuCaiOo/projects/4), field Status: `PVTSSF_lAHOAVNuW84Bcrp8zhXSou4`
- Opções de Status: Backlog `19386e88` · Todo `f75ad846` · In Progress `47fc9ee4` · Done `98236657`
- Assignee padrão: `@me`

## Common mistakes

| Mistake | Fix |
|---|---|
| Criar uma issue sem validar o resumo com o usuário primeiro | Sempre confirmar a lista consolidada (passo 3) antes de abrir a issue |
| Deixar a issue sem mover para "Backlog" | `new-issue` não define status sozinho — sempre rodar o passo 5 |
| Assumir que screenshots existem | O unideas não tem `docs/screenshots/` ainda — rode o app ao vivo se precisar de referência visual |