# unideas — Fluxos de Navegação

> Documento vivo. Atualizar conforme telas forem adicionadas/alteradas.
> Formato: indentação representa profundidade de navegação. Complementa [`ARCHITECTURE.md`](ARCHITECTURE.md) e a planta de produto.

---

## Telas do MVP (7)

| # | Tela | Módulo | Rota (type-safe) |
|---|---|---|---|
| 1 | **Home** (Painel de Prioridades + abas Tarefas/Anotações) | `:feature:home` | `HomeRoute.Home` |
| 2 | **Todas as Prioridades** | `:feature:home` | `HomeRoute.AllPriorities` |
| 3 | **Criar/Editar Item** | `:feature:items` | `ItemsRoute.Form(itemId: Long?)` |
| 4 | **Detalhe do Item** | `:feature:items` | `ItemsRoute.Detail(itemId: Long)` |
| 5 | **Gerenciar Seções** | `:feature:sections` | `SectionsRoute.List` |
| 6 | **Gerenciar Tags** | `:feature:tags` | `TagsRoute.List` |
| 7 | **Configurações / Backup** | `:feature:settings` | `SettingsRoute.Settings` |

Ponto de entrada do app: `HomeRoute.Home`. **Não há bottom navigation bar** — a Home é o centro; Configurações/Seções/Tags são acessadas a partir dela. Rotas são `@Serializable` (Navigation Compose type-safe); `NavHost` central vive no `:app`, cada feature expõe seu `*NavGraph` + `*Route`.

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

## Ordem de configuração (livre)

O app é local-first e não força ordem: o usuário pode criar itens sem seções/tags, ou criar seções/tags antes. Seção/Tag são opcionais na criação de um Item.
