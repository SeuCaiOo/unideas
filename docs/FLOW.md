# unideas — Fluxos de Navegação

> Documento vivo. Atualizar conforme telas forem adicionadas/alteradas.
> Formato: indentação representa profundidade de navegação. Complementa [`ARCHITECTURE.md`](ARCHITECTURE.md) e a planta de produto.

---

## Telas do MVP (7 + 1 dev-only)

| # | Tela | Módulo | Rota (type-safe) |
|---|---|---|---|
| 1 | **Home** (lista Tarefas/Anotações + painel de prioridades como Bottom Sheet) | `:feature:home` | `HomeRoute.Home` |
| 2 | **Todas as Prioridades** | `:feature:home` | `HomeRoute.AllPriorities` |
| 3 | **Criar/Editar/Detalhar Item** (tela única — ver "Detalhe do Item" abaixo) | `:feature:items` | `ItemsRoute.Detail(itemId: Long? = null, initialType: ItemType = TASK)` |
| 4 | **Configurações do Item** (seção/tags/recorrência/aviso + troca de tipo guardada — #160) | `:feature:items` | `ItemsRoute.Config(itemId: Long)` |
| 5 | **Gerenciar Seções** | `:feature:sections` | `SectionsRoute.List` |
| 6 | **Gerenciar Tags** | `:feature:tags` | `TagsRoute.List` |
| 7 | **Configurações / Backup** | `:feature:settings` | `SettingsRoute.Settings` |
| — | *(dev-only)* Todos os Itens (sem abas/seleção) | `:feature:items` | `ItemsRoute.List` |

Ponto de entrada do app: `HomeRoute.Home` (`startDestination` do `AppNavHost`, em `:app/navigation/`). **Não há bottom navigation bar** (existiu brevemente durante o #138, removida na mesma issue) — a Home é o centro; Configurações/Seções/Tags são acessadas a partir dela. Rotas são `@Serializable` (Navigation Compose type-safe); `AppNavHost` central vive no `:app` (`app/src/main/java/com/seucaio/unideas/navigation/`), cada feature expõe seu `*NavGraph` + `*Route`.

**`ItemsRoute.Form` não existe mais (#134).** Criar e editar item deixaram de ser telas separadas — `ItemDetailScreen`/`ItemDetailViewModel` fazem os dois papéis: `itemId == null` entra em modo criação (com `initialType` definindo Tarefa/Anotação inicial), `itemId != null` carrega o item pra edição/visualização.

> **Ponto de entrada de debug remanescente:** Settings mantém uma seção **"Debug"**, só de desenvolvimento, com um item "Items" que abre `ItemsRoute.List` (#62) — uma listagem simples de todos os Items (sem abas/filtro/painel de prioridade, isso é escopo da Home), que por sua vez navega pra `ItemsRoute.Detail`. Não foi removida quando a Home passou a existir (D2/#11) — segue como atalho útil pra QA manual; decisão de descartá-la fica em aberto.

---

## Home

**Acesso:** tela inicial do app.

```
HomeScreen
  ├── HomeTopBar
  │     → botão "Prioridades" → abre PriorityBottomSheet (não é navegação — state local da HomeScreen)
  │     → ícone Configurações → SettingsScreen  (SettingsRoute.Settings)
  │
  ├── Abas [ Tarefas | Anotações ]
  │     → filtro por Seção (dropdown) + Tags (chips, múltipla seleção)
  │     → item da lista: título, cor de urgência, ícone de recorrência (se houver),
  │        checkbox de conclusão (SÓ na aba Tarefas)
  │           → toca no item → ItemDetailScreen  (ItemsRoute.Detail(itemId = id))
  │           → checkbox (Tarefas) → marca a ocorrência atual como concluída (não gera item novo —
  │              ver ARCHITECTURE.md); se atrasada/recorrente com histórico, mais nuance fica em ItemDetailScreen
  │           → toque longo → entra em modo Seleção (HomeMode.Selection)
  │     → [modo Seleção] selecionar itens (inclusive "selecionar todos" por seção) → excluir em lote,
  │        com dialog de confirmação (#140)
  │     → [estado vazio] texto orientando como começar (sem tela de onboarding)
  │
  ├── AddItemFab
  │     → escolher tipo (Tarefa / Anotação)
  │           → ItemDetailScreen (modo criação)  (ItemsRoute.Detail(itemId = null, initialType))
  │
  └── PriorityBottomSheet (aberto a partir do botão "Prioridades" da HomeTopBar)
        → itens vencidos + vencendo em breve, limitados a N
        → "Ver todas" (aparece só quando excede o limite)
              → AllPrioritiesScreen  (HomeRoute.AllPriorities)
        → toca num item → ItemDetailScreen
```

**Regras:**
- O painel de prioridades **não é mais um painel fixo no topo** (mudou no #138) — é um Bottom Sheet, acionado sob demanda, pra dar mais espaço vertical à lista principal.
- Seleção múltipla e exclusão em lote vivem na Home (long-press num item da lista), não numa tela separada.
- Cor de urgência (vermelho = vencido, âmbar = vencendo em ≤N dias) é o **único** uso dessas cores na UI.
- **Pull-to-refresh** (#101/D): puxar a lista pra baixo dispara o motor de reavaliação de ocorrências fora do ciclo periódico do `ReminderCheckWorker` — mesmo gatilho manual que existia só via Settings ("Rodar verificação de lembretes agora"), agora também acessível direto na tela principal.
- Grupos de seção na lista abrem **expandidos por padrão** (#147) — antes só abriam expandidos se fixados; sem nada fixado, a Home inteira abria recolhida.
- O botão "Prioridades" na `HomeTopBar` e o auto-abrir do `PriorityBottomSheet` no cold start só aparecem/disparam quando existe **pelo menos um item de prioridade** (#147) — antes disparava sempre, mesmo vazio.

---

## Todas as Prioridades

**Acesso:** Home → "Ver todas" (só aparece quando o painel excede o limite).

```
AllPrioritiesScreen
  → lista completa dos itens que apareceriam no painel se não houvesse limite
     (vencidos + vencendo em breve, ordenados por urgência/vencimento)
  → toca num item → ItemDetailScreen
  → "←" → volta pra Home
```

---

## Criar / Editar / Detalhar Item

**Acesso:** Home `AddItemFab` (criar) · Home/Todas as Prioridades → toca num item (ver/editar/concluir). Tela única (`ItemDetailScreen`) reutilizada pros dois tipos e pros três modos — não existe mais uma tela de formulário separada (#134).

```
ItemDetailScreen  (ItemsRoute.Detail(itemId, initialType))
  → itemId == null → modo criação: tipo fixado por initialType (escolhido antes de entrar na tela,
                      ex. botões de Home), mostrado como badge — sem seletor de tipo inline (#162)
  → itemId != null → carrega o item; campos editáveis inline (sem alternar "modo edição" explícito)
  → salvamento automático a cada alteração (sem botão "Salvar" — auto-save por campo; texto tem
     debounce de ~500ms, campos estruturados salvam na hora — #133)
  → primeiro auto-save bem-sucedido preenche itemId (uiState) — é o que libera os NavCards de
     Configurações/Histórico e o footer de conclusão (escondidos até lá, não só desabilitados — #162)
  → tela silenciosamente recarrega o item ao retomar foco (`OnScreenResumed`/`LifecycleResumeEffect`),
     pra refletir mudanças feitas na Config Screen ao voltar pra cá (#160)
  → Título (curto, obrigatório) · Descrição (multilinha, opcional, com toolbar de Markdown — #93;
     preview do Markdown mostra "Sem descrição" quando vazia, em vez de nada — #162)
  → (todo o resto — Seção, Tags, Data de vencimento, Recorrência, Horário, Aviso — vive só na
     Config Screen desde o #162; não aparece mais inline aqui, ver seção abaixo)
  → ações (Tarefa, ocorrência dentro do prazo):
       [Concluir]      → nota opcional; ItemOccurrenceViewModel (#101/B), separado do form
  → ações (Tarefa, ocorrência vencida — OverdueOccurrenceActions):
       [Concluir atrasado] → NoteConfirmDialog, nota **obrigatória** explicando o atraso (#101/A)
       [Ignorar]            → NoteConfirmDialog, nota **obrigatória**; avança dueDate um ciclo na hora,
                               sem esperar o worker (#101/A)
       [Aumentar prazo]     → ExtendDeadlineDatePickerDialog; empurra dueDate sem fechar a ocorrência
                               (não conta como concluída nem perdida) (#101/A)
  → ações (comuns, só quando state.isEditing — ver acima):
       [Compartilhar]     → share sheet do sistema (sempre visível, mesmo em criação)
       [Configurações]    → NavCard "Configurações" → ItemConfigScreen (ItemsRoute.Config(itemId,
                             isNewItem)) — substituiu o ícone de engrenagem no toolbar do #160 (#162)
       [Excluir]          → DeleteConfirmationDialog → confirma → volta pra Home (sempre visível)
       [Ver histórico]    → NavCard "Histórico" → ItemHistoryScreen (ItemsRoute.History(itemId)) — só
                             pra item recorrente; resumo (% no prazo, sequência atual), filtros, cartão
                             por ocorrência com hora, dias de atraso, nota e trilha de extensão (#101/C)
  → "←" → volta
```

**Regras:**
- Só **Título** é obrigatório.
- Concluir uma ocorrência recorrente não avança `dueDate` na hora — isso é feito de forma preguiçosa pelo `ReminderCheckWorker` (`ProcessMissedOccurrencesUseCase`) na próxima varredura periódica, pull-to-refresh na Home, ou ao reabrir o app, não pela ação de concluir em si (ver `ARCHITECTURE.md`, motor de reavaliação #101/D). "Ignorar" é a exceção — avança `dueDate` na hora.
- `ItemDetailScreen` hoisteia dois ViewModels lado a lado (`ItemDetailViewModel` pro form, `ItemOccurrenceViewModel` pro ciclo de vida da ocorrência) — sincronizados via `OnItemUpdatedExternally` pra uma escrita de um lado não sobrescrever a do outro (#101/B).
- Item criado e depois excluído antes do primeiro save "de verdade" (ex. usuário desiste e sai) ainda é removido corretamente — `OnDeleteConfirmClicked` usa o `itemId` interno do ViewModel (que já pode ter sido auto-salvo), não o `itemId` de rota (#162).

---

## Configurações do Item

**Acesso:** `ItemDetailScreen` (modo edição, ou seja `state.isEditing == true`) → `NavCard` "Configurações" no `ItemFormBody` (#162 — substituiu o ícone de engrenagem no toolbar do #160). O card só existe/aparece depois que o item tem `itemId` real (primeiro auto-save concluído) — pra item novo ainda não salvo, o card fica escondido em vez de clicável-mas-sem-efeito.

```
ItemConfigScreen  (ItemsRoute.Config(itemId, isNewItem = false))
  → Seção (dropdown, opcional) · Tags (chip-input, múltiplas, opcional)
  → Lembrete (switch) → Recorrência · Data de vencimento (se recorrência = Nenhuma) · Horário · Aviso
     — mesmos campos/componentes do form principal, disponíveis pros dois tipos (#160)
  → Zona de risco: tipo atual do item + botão [Alterar] — só aparece quando isNewItem == false
       (item recém-criado, sem histórico/ocorrência acumulada, não tem risco a avisar — #165 batch)
       → DeleteConfirmationDialog (confirmação genérica, título/mensagem por tipo alvo)
       → confirma → reset total: dueDate/dueTime/recurrence/reminderWarning voltam a
         nulo/None, mesmo que o novo tipo também suportasse esses campos — nunca seletivo.
         Título/descrição/seção/tags NUNCA são tocados. Histórico (`ItemCompletionHistory`)
         nunca é apagado, só para de receber novas entradas se o item deixar de ser Tarefa.
  → "←" → volta (ItemDetailScreen recarrega os campos ao retomar foco, ver acima)
```

**Regras:**
- Só alcançável a partir de um item já existente (`itemId` obrigatório) — não existe mais seletor de tipo inline no form (`TypeSelectorField` foi removido no #162); o tipo é fixado só na criação (`initialType`, escolhido antes de entrar na tela) e mostrado como badge, nunca editável ali.
- `isNewItem` é derivado do `itemId` de rota (imutável) do `ItemDetailScreen`, não do `itemId` mutável do `uiState` — só assim o sinal "isso era criação" sobrevive ao primeiro auto-save (ver #165 batch).
- `ItemConfigViewModel` reaproveita só `ItemFormUseCase` — seção/tags saíram pro `SectionsTagsViewModel` dedicado (#170), hoisteado ao lado na Config Screen, com bottom sheet de criação rápida (create-only).
- Layout desenhado via Claude Design (V1 escolhida): https://claude.ai/code/artifact/3534d226-b174-40da-85dd-5358c32dd180

---

## Gerenciar Seções

**Acesso:** Configurações → "Organizar" → Seções.

```
SectionsListScreen  (SectionsRoute.List)
  → lista de seções
  → FAB "+" / campo → criar nova
  → renomear (swipe ou menu de contexto)
  → excluir (swipe ou menu de contexto)
       [se há itens vinculados]
       → BLOQUEADO: dialog informando quantos itens estão vinculados (sem exclusão)
       [se não há]
       → DeleteConfirmationDialog → confirma → remove
```

---

## Gerenciar Tags

**Acesso:** Configurações → "Organizar" → Tags. Mesma estrutura de Seções.

```
TagsListScreen  (TagsRoute.List)
  → lista de tags
  → FAB "+" / campo → criar nova
  → excluir → mesma regra de bloqueio por vínculo das Seções
  (renomear NÃO é obrigatório no MVP)
```

---

## Configurações / Backup

**Acesso:** Home → ícone Configurações.

```
SettingsScreen  (SettingsRoute.Settings)
  ├── Backup (Google Drive)
  │     → status de conexão (conectado / desconectado) + botão Conectar
  │           → GoogleSignIn (escopo Drive) → volta com conta conectada
  │     → "Fazer backup agora" → UploadBackupUseCase → atualiza data/hora do último backup
  │     → "Restaurar backup" → ListBackups → escolhe → RestoreBackupUseCase
  │     → texto de status: data/hora do último backup
  │
  └── Organizar
        → Seções → SectionsListScreen
        → Tags   → TagsListScreen
```

---

## Notificações de lembrete (#95)

**Entrada:** fora do `NavHost` normal — chega via notificação do sistema, gerada pelo `PeriodicWorkRequest` 4x/dia (00h/06h/12h/18h) do `:core:notifications`.

```
Notificação de item individual (título = item, descrição resumida)
  → toca → deep link `unideas://item/{id}` (ACTION_VIEW, setPackage) → ItemsRoute.Detail(id)
       → app fechado: MainActivity.onCreate + handleDeepLink alimenta o NavController
       → app em background/aberto: MainActivity.onNewIntent

Notificação de resumo (agrupa os itens de uma tier, setGroupSummary)
  → toca → abre o app (launcher intent), sem navegar pra um item específico
```

**Tiers** (calculados pelo `ReminderTier`, puro em `:domain`): radar (já coberto pelo Painel de Prioridades da Home, sem notificação de sistema) → normal (canal dispensável, dispara dentro da janela de aviso configurada no item) → urgente (canal não-dispensável, `ongoing`, dispara pra todo item vencendo no dia). Concluir um item cancela só a notificação daquele item; o resumo do grupo permanece enquanto sobrar pelo menos 1 item na tier.

**Debug (Settings → Depuração):** "Rodar verificação de lembretes agora" (força uma varredura fora do ciclo periódico) e "Testar notificação" (posta uma notificação avulsa, normal ou urgente, sem depender de itens reais).

---

## Ordem de configuração (livre)

O app é local-first e não força ordem: o usuário pode criar itens sem seções/tags, ou criar seções/tags antes. Seção/Tag são opcionais na criação de um Item.
