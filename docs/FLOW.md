# unideas — Fluxos de Navegação

> Documento vivo. Atualizar conforme telas forem adicionadas/alteradas.
> Formato: indentação representa profundidade de navegação. Complementa [`ARCHITECTURE.md`](ARCHITECTURE.md) e a planta de produto.

---

## Telas do MVP (7)

| # | Tela | Módulo | Rota (type-safe) |
|---|---|---|---|
| 1 | **Home** (Painel de Prioridades + abas Tarefas/Anotações) | `:feature:home` | `HomeRoute.Panel` |
| 2 | **Todas as Prioridades** | `:feature:home` | `HomeRoute.AllPriorities` |
| 3 | **Criar/Editar Item** | `:feature:items` | `ItemsRoute.Form(itemId: Long?)` |
| 4 | **Detalhe do Item** | `:feature:items` | `ItemsRoute.Detail(itemId: Long)` |
| 5 | **Gerenciar Seções** | `:feature:sections` | `SectionsRoute.List` |
| 6 | **Gerenciar Tags** | `:feature:tags` | `TagsRoute.List` |
| 7 | **Configurações / Backup** | `:feature:settings` | `SettingsRoute.Settings` |

Ponto de entrada do app: `HomeRoute.Panel` (`startDestination` real do `NavHost`, desde D2.1/#27 — ver `MainActivity`). **Não há bottom navigation bar** — a Home é o centro; Configurações/Seções/Tags são acessadas a partir dela. Rotas são `@Serializable` (Navigation Compose type-safe); `NavHost` central vive no `:app`, cada feature expõe seu `*NavGraph` + `*Route`.

> **Ponto de entrada de debug remanescente:** Settings mantém uma seção **"Debug"**, só de desenvolvimento, com um item "Items" que abre `ItemsRoute.List` (#62) — uma listagem simples de todos os Items (sem abas/filtro/painel de prioridade, isso é escopo da Home), que por sua vez navega pra `ItemsRoute.Detail`/`ItemsRoute.Form`. Não foi removida quando a Home passou a existir (D2/#11) — segue como atalho útil pra QA manual; decisão de descartá-la fica em aberto.

---

## Home

**Acesso:** tela inicial do app.

```
HomeScreen
  ├── Painel de Prioridades (topo, FIXO — persiste ao trocar de aba)
  │     → itens vencidos + vencendo em breve, limitados a N
  │     → "Ver todas" (aparece só quando excede o limite)
  │           → AllPrioritiesScreen  (HomeRoute.AllPriorities)
  │
  ├── Abas [ Tarefas | Anotações ]  (trocam o conteúdo abaixo, painel continua fixo)
  │     → filtro por Seção (dropdown) + Tags (chips, múltipla seleção)
  │     → item da lista: título, cor de urgência, ícone de recorrência (se houver),
  │        checkbox de conclusão (SÓ na aba Tarefas)
  │           → toca no item → ItemDetailScreen  (ItemsRoute.Detail(id))
  │           → checkbox (Tarefas) → conclui direto; se recorrente, renasce
  │     → [estado vazio] texto orientando como começar (sem tela de onboarding)
  │
  ├── FAB "+"
  │     → escolher tipo (Tarefa / Anotação)
  │           → ItemFormScreen (criar)  (ItemsRoute.Form(itemId = null))
  │
  └── ícone Configurações (topo)
        → SettingsScreen  (SettingsRoute.Settings)
```

**Regras:**
- O Painel de Prioridades é o elemento **mais importante visualmente** — superfície teal, não é componente secundário.
- Ele **não muda** ao alternar Tarefas/Anotações.
- Cor de urgência (vermelho = vencido, âmbar = vencendo em ≤N dias) é o **único** uso dessas cores na UI.

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

## Criar / Editar Item

**Acesso:** Home FAB "+" (criar) · Detalhe do Item → "Editar" (editar). Tela única reutilizada pros dois tipos e pros dois modos.

```
ItemFormScreen  (ItemsRoute.Form(itemId))
  → seletor de tipo no topo (Tarefa / Anotação) — troca opcional
  → Título (curto, obrigatório)
  → Descrição (multilinha, opcional)
  → Seção (dropdown, opcional)
  → Tags (chip-input, múltiplas, opcional)
  → Data de vencimento (date picker, opcional) — disponível pros dois tipos
       → se há data → Recorrência (Nenhuma / Diária / Semanal / Mensal)
  → Salvar → volta pra tela anterior (Home ou Detalhe)
```

**Regras:**
- `itemId == null` → modo criar; `itemId != null` → modo editar (carrega o item).
- Só **Título** é obrigatório. Recorrência só habilita se houver data de vencimento.
- Sem data → recorrência indisponível/oculta.
- Com data → também habilita **Horário de vencimento** (opcional) e **Aviso** (nenhum / N dias antes) — usados pelo `:core:notifications` pra decidir quando notificar (#95/#114).

---

## Detalhe do Item

**Acesso:** Home (lista ou painel) · Todas as Prioridades → toca num item.

```
ItemDetailScreen  (ItemsRoute.Detail(id))
  → texto selecionável/copiável (título + descrição)
  → metadados: seção, tags, vencimento, criado em, concluído em (se concluída)
  → ações:
       [Compartilhar]  → share sheet do sistema
       [Editar]        → ItemFormScreen (editar)  (ItemsRoute.Form(id))
       [Excluir]       → DeleteConfirmationDialog → confirma → volta pra Home
       [Concluir]      → só em Tarefas; conclui; se recorrente, renasce ao concluir
  → "←" → volta
```

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
