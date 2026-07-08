---
name: add-improvement
description: >
  Use when the user brings a new idea, improvement, complaint, or observation about the unideas app — UI, UX, flow, architecture, or anything else. This skill guides the full refinement-to-issue flow: listen, refine conversationally, validate the summary with the user, document it in the "unideas — Improvements" artifact, then create the GitHub issue and move it to Todo on the project board. Use this skill whenever the user says "tenho uma ideia", "quero melhorar", "está feio", "percebi um problema", "quero mudar", or describes anything that would become a new improvement item — even if they don't say "add-improvement" explicitly.
---

# Add Improvement — unideas Workflow

Fluxo completo para transformar uma ideia bruta em um item documentado e rastreado no GitHub.

Diferente do GymLog (que usa `docs/IMPROVEMENTS.md` versionado no repo), o unideas guarda esse guia em um **Artifact online** — decisão do usuário: evita o problema de o arquivo "se perder" ao trocar de branch, já que edições acontecem fora do fluxo normal de commits de feature.

**Artifact "unideas — Improvements":** https://claude.ai/code/artifact/ee42af85-d23b-4c39-aa3a-ded2829a2667

## Etapas

### 1. Ouvir e entender

Deixe o usuário falar livremente. Não interrompa com perguntas desnecessárias. Se a ideia for vaga, faça **uma pergunta por vez** para esclarecer o ponto mais importante antes de avançar.

### 2. Consultar evidências quando necessário

Antes de opinar ou resumir, busque evidências concretas no código (ViewModels, composables, arquivos afetados) quando o problema for técnico — ex: de onde vem um dado, por que um comportamento acontece. Não deduza — verifique. O unideas ainda não tem uma pasta de screenshots documentados; se precisar de referência visual, rode o app (skill `run` / `android` CLI) e capture a tela na hora.

### 3. Consolidar e listar

Após entender a ideia, liste tudo que o usuário disse — em tópicos curtos e claros — e pergunte: "Está correto e completo?"

Aguarde confirmação explícita antes de avançar. Se o usuário corrigir ou acrescentar algo, atualize a lista e confirme novamente.

### 4. Ler e atualizar o Artifact

O Artifact é a fonte da verdade — sempre leia o estado atual antes de editar, nunca assuma o conteúdo de memória (outra sessão pode ter atualizado):

```
WebFetch da URL do artifact acima, pedindo o conteúdo markdown completo
```

Adicione a nova entrada na seção **"Novas ideias (sem issue)"**, no formato:

```markdown
### [ideia em poucas palavras — sem número ainda]

**Contexto:** [o que motivou a ideia e qual o problema observado]

**Mudanças previstas:**
- [ponto 1]
- [ponto 2]

**Áreas afetadas:** [telas/componentes/módulos impactados — opcional]
```

Escreva o markdown completo (com a nova entrada) em um arquivo local no scratchpad e republique no mesmo `file_path` usado da última vez, passando `url` igual à URL acima para atualizar o artifact existente em vez de criar um novo.

Mostre o resultado ao usuário. Se pedir ajuste, corrija antes de avançar.

### 5. Criar a issue no GitHub

Use `/new-issue` para criar a issue com:
- Título em inglês, formato Conventional Commits (`ui:`, `feat:`, `refactor:`, etc.)
- Body em PT-BR com: Contexto, DoR, descrição das mudanças, checklist, áreas afetadas, DoD
- Label adequada (`ui`, `feature`, `quality`, etc.)
- Assignee: `@me`

`new-issue` já cuida de vincular ao GitHub Project (#4) — não repita esse passo aqui.

### 6. Mover a issue para "Todo"

O item entra no board via `new-issue` sem status definido (fica em branco até ser movido). Mova explicitamente para **Todo**:

```bash
gh api graphql -f query="
mutation {
  updateProjectV2ItemFieldValue(input: {
    projectId: \"PVT_kwHOAVNuW84Bcrp8\"
    itemId: \"<ITEM_ID>\"
    fieldId: \"PVTSSF_lAHOAVNuW84Bcrp8zhXSou4\"
    value: { singleSelectOptionId: \"f75ad846\" }
  }) { projectV2Item { id } }
}"
```

(Status field ID e opção "Todo" do projeto #4 — mesmos IDs usados em `new-issue`/`start-feature`/`finish-issue`.)

### 7. Mover a entrada no Artifact para "Issues criadas (Todo)"

Repita o passo 4 (ler o estado atual via WebFetch, editar, republicar): mova a entrada da seção **"Novas ideias (sem issue)"** para **"Issues criadas (Todo)"**, adicionando o número da issue ao título:

```markdown
### #<N> — [mesmo título de antes]
```

O conteúdo da entrada não muda — só o título ganha o número e a entrada muda de seção.

**Estágios seguintes** (mover a entrada para "Em andamento" e depois "Finalizadas") não são responsabilidade desta skill — eles acompanham naturalmente `/start-feature` (issue vai pra In Progress) e `/finish-issue` / merge (issue fecha, card vai pra Done no próximo `/start-feature`). Se quiser manter o Artifact 100% sincronizado com o board, mova a entrada manualmente nesses momentos ou peça para eu fazer isso.

---

## Referências do projeto

- Artifact de melhorias: https://claude.ai/code/artifact/ee42af85-d23b-4c39-aa3a-ded2829a2667
- Projeto GitHub: `PVT_kwHOAVNuW84Bcrp8` (https://github.com/users/SeuCaiOo/projects/4), field Status: `PVTSSF_lAHOAVNuW84Bcrp8zhXSou4`
- Opções de Status: Todo `f75ad846` · In Progress `47fc9ee4` · Done `98236657`
- Assignee padrão: `@me`

## Common mistakes

| Mistake | Fix |
|---|---|
| Editar o Artifact sem ler o estado atual antes | Sempre `WebFetch` a URL primeiro — outra sessão pode ter mudado o conteúdo |
| Criar uma issue sem passar pelo Artifact primeiro | Sempre documentar a ideia antes de criar a issue — é o registro histórico da decisão |
| Deixar a issue sem mover para "Todo" | `new-issue` não define status sozinho — sempre rodar o passo 6 |
| Assumir que screenshots existem | O unideas não tem `docs/screenshots/` ainda — rode o app ao vivo se precisar de referência visual |