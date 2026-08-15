# QA manual — runbook para testes de dispositivo

Guia para quem for validar mudanças de UI/fluxo no dispositivo físico/emulador — Claude Code, Maestro (quando configurado) ou uma pessoa. Não substitui os testes automatizados (`./gradlew test`) nem `koverVerify`; cobre o que só se vê rodando o app de verdade.

## Ferramentas e armadilhas

Preferir a CLI `android` a `adb`/`aapt` cru (ver `RUNNING.md`). Pontos que renderam retrabalho nesta sessão:

- **Coordenadas de tap**: `android layout` e `adb shell input tap` usam pixels reais do device. O PNG de `android screen capture`, quando exibido/anotado por uma ferramenta que reescala a imagem (ex.: visualização em ~1000x2000 para um device 1080x2160), **não** está em pixels reais — nunca "olhar" a posição num screenshot redimensionado e taplar direto nesse valor. Ou usa as coordenadas cruas de `android layout`, ou usa o fluxo abaixo com `screen resolve`.
- **Diálogos (`AlertDialog`/`Dialog`) não aparecem em `android layout`** — são outra janela, o dump não os alcança. Para taplar num botão de diálogo com precisão:
  ```
  android screen capture --annotate -o annotated.png
  android screen resolve --screenshot annotated.png --string "tap #N"   # N = label visto no PNG anotado
  adb shell input tap <x> <y>                                          # coordenadas devolvidas acima
  ```
- **`android run` com a flag `--debug` trava em "Waiting For Debugger"** se nenhum debugger anexar. Para só instalar+abrir, omitir `--debug`.
- **Banco de dados é a fonte da verdade, não a tela.** Uma tela pode mostrar estado desatualizado (ou vice-versa) sem que isso apareça no `android layout`. Inspecionar direto:
  ```
  adb shell "run-as com.seucaio.unideas.debug sqlite3 databases/unideas.db 'SELECT ...;'"
  ```
  Schema relevante: tabela `items` (`id`, `dueDate`, `completedAt`, `lastCompletedScheduledDate`, `recurrence`, ...), tabela `item_completion_history` (`itemId`, `scheduledDate`, `completedAt`, `note`).
- **`adb shell am force-stop com.seucaio.unideas.debug` mata o processo sem passar pelo `onEvent(OnBackRequested)`/`BackHandler` da tela.** Útil para isolar "o dado persistiu no banco?" de "a navegação de volta corrompeu o dado?" — matar o processo preserva o estado exatamente como ele estava gravado; navegar de volta pode disparar auto-save que reescreve a linha.

## Checklist — Item Detail (form básico)

Roda inteiro sempre que `ItemDetailViewModel`/`ItemFormBody`/`ItemFormFooter`/`ItemDetailScreen` mudarem, mesmo que a mudança pareça isolada — valida que nada no form básico quebrou.

1. **Criar tarefa nova**: preencher título, descrição, seção, tag, ativar lembrete/data, voltar → confirmar que aparece na lista com os campos certos.
2. **Criar anotação nova**: mesma coisa, sem os campos de data/recorrência.
3. **Editar item existente**: abrir, mudar título e descrição, sair, reabrir → confirmar que persistiu.
4. **Rascunho vazio**: criar item novo sem digitar nada, voltar → sai direto, sem diálogo (nada a perder).
5. **Título inválido com conteúdo**: criar item novo, preencher só a descrição (título vazio), voltar → primeira tentativa apenas marca erro inline ("O título é obrigatório"), **não** sai da tela; segunda tentativa de voltar abre o diálogo "Sair sem título?" (Cancelar/OK).
6. **Excluir item**: abrir item existente, ícone de lixeira na top bar → diálogo "Excluir item" → OK → some da lista.
7. **Compartilhar item**: ícone de compartilhar na top bar → abre a share sheet nativa do Android com título/descrição/data do item.

## Checklist — Conclusão/reabertura de ocorrência (`ItemOccurrenceViewModel`)

Roda sempre que `ItemOccurrenceViewModel`, `CompleteItemUseCase`, `ItemOccurrenceUseCase` ou a divisão entre os dois ViewModels da tela de detalhe mudarem. Esta área tem uma condição de corrida conhecida (ver seção abaixo) — **todo teste de conclusão precisa terminar conferindo o banco, não só a tela.**

1. **Tarefa não recorrente — concluir**: abrir tarefa (tipo TASK, sem recorrência), "Marcar como concluído" → botão vira "Reabrir tarefa", aparece "Concluída em <data>", snackbar "Tarefa concluída".
2. **Reabrir — cancelar**: tocar "Reabrir tarefa" → diálogo "Reabrir tarefa?" → Cancelar → item continua concluído, sem mudança.
3. **Reabrir — confirmar**: tocar "Reabrir tarefa" → OK → volta a "Marcar como concluído", "Concluída em" some.
4. **Tarefa recorrente — concluir**: mesma coisa numa tarefa com `recurrence != None`. Diferença esperada: **nunca** aparece "Concluída em <data>" (é comportamento correto — recorrente rastreia conclusão via `lastCompletedScheduledDate`, não `completedAt`; ver `Item.kt`). Confirmar que `dueDate` **não muda** ao concluir (#148).
5. **Histórico só em recorrente**: o ícone de histórico na top bar só aparece quando `isEditing && recurrence != Recurrence.None`. Abrir o histórico numa recorrente → lista as ocorrências com data e status ("No prazo"/atrasado). Numa não-recorrente, o ícone nem deve estar lá.
6. **Persistência pós-navegação (o teste que importa de verdade)**: concluir uma tarefa recorrente → voltar para a lista → reabrir o item → conferir se ainda mostra "Reabrir tarefa" **e** rodar a query SQL abaixo para confirmar `lastCompletedScheduledDate == dueDate` no banco. Repetir a sequência completa (concluir → voltar → reabrir) pelo menos 3x seguidas antes de dar como aprovado — é uma corrida, um "passou" isolado não prova nada.

## Bug conhecido — race condition entre `ItemDetailViewModel.persist()` e a conclusão de ocorrência

**Sintoma**: conclui uma tarefa recorrente, volta para a lista, reabre o item — às vezes o botão volta a mostrar "Marcar como concluído" mesmo a conclusão tendo sido gravada com sucesso (`item_completion_history` tem a linha, snackbar "Tarefa concluída" apareceu). É intermitente: em ~2 repetições da mesma sequência, uma reverteu e outra persistiu certo.

**Causa raiz**: `ItemDetailScreen` instancia `ItemDetailViewModel` e `ItemOccurrenceViewModel` lado a lado, cada um carregando sua própria cópia do `Item` no `init` (`itemFormUseCase.get(id).first()`), sem se sincronizarem depois. `ItemDetailViewModel.handleBackRequested()` sempre chama `persist()` ao sair da tela quando o título é válido (`ItemDetailUiState.isPristine` só é `true` para um rascunho totalmente vazio — título preenchido já basta pra `persist()` rodar, mesmo sem nenhum campo ter sido editado). `persist()` reconstrói o item a partir do `originalItem` **da própria `ItemDetailViewModel`**, que nunca aprende sobre a conclusão feita pela `ItemOccurrenceViewModel` — então a escrita de saída da tela e a escrita da conclusão correm em paralelo, gravando a mesma linha; quem escreve por último vence. Quando a escrita "burra" do `ItemDetailViewModel` vence, ela leva de volta `lastCompletedScheduledDate = null`.

**Como confirmar que foi corrigido**: repetir o item 6 do checklist acima pelo menos 5x seguidas sem nenhuma reversão, com a query abaixo confirmando o estado a cada rodada.

```sql
SELECT id, title, dueDate, completedAt, lastCompletedScheduledDate, recurrence
FROM items WHERE id = <id>;

SELECT * FROM item_completion_history WHERE itemId = <id>;
```

`lastCompletedScheduledDate` deve bater com `dueDate` quando a tarefa está concluída, e a linha correspondente deve existir em `item_completion_history` — as duas fontes precisam concordar.

## Checklist — Motor de reavaliação de ocorrências (#151)

Roda sempre que `ReminderCheckWorker`, `ProcessMissedOccurrencesUseCase` ou o pull-to-refresh da Home mudarem. Os 3 cenários dependem de estado que não dá pra alcançar só usando o app normalmente (item "já concluído hoje", "atrasado com extensão pendente" etc.) — não precisa mudar a data do sistema pra isso, o `DatabaseSeeder` (`SeedScope.FULL`, seção "Reavaliação: ...") já semeia os 3 estados prontos.

**Setup**: Settings → debug → "Popular banco" → escopo Completo (`FULL`). Os 3 itens de teste aparecem com título já descrevendo o resultado esperado (`Reavaliação: ...`).

1. **Já concluída hoje — não deve notificar**: item com `dueDate` = hoje e `lastCompletedScheduledDate` = hoje. Puxar pra atualizar na Home (pull-to-refresh) → confirmar que **nenhuma** notificação aparece pra esse item (antes do fix, `ReminderTier` não olhava `isCompleted` e recalculava URGENT mesmo já concluído).
2. **Concluída, aguardando avanço — sem MISSED duplicado**: item com `dueDate` = ontem e `lastCompletedScheduledDate` = ontem (simula "o motor ainda não rodou desde a conclusão"). Puxar pra atualizar → inspecionar o banco:
   ```
   adb shell "run-as com.seucaio.unideas.debug sqlite3 databases/unideas.db \
     'SELECT dueDate, lastCompletedScheduledDate FROM items WHERE title LIKE \"Reavaliação: concluída%\";'"
   adb shell "run-as com.seucaio.unideas.debug sqlite3 databases/unideas.db \
     'SELECT scheduledDate, completedAt FROM item_completion_history WHERE itemId = <id>;'"
   ```
   Esperado: `dueDate` avançou pro próximo ciclo, `lastCompletedScheduledDate` voltou a `NULL`, e só existe **uma** linha de histórico pra aquele `scheduledDate` (a `COMPLETED` que já existia — sem `MISSED` duplicado em cima dela).
3. **Extensão pendente nunca resolvida — carregada pro MISSED**: item com `dueDate` bem atrasado e `pendingExtensionOriginalDueDate`/`pendingExtensionCount` setados (simula "foi adiado uma vez, nunca resolvido"). Puxar pra atualizar → conferir no banco que a linha `MISSED` gerada em `item_completion_history` tem `originalScheduledDate`/`extensionCount` preenchidos, e que `pendingExtensionOriginalDueDate`/`pendingExtensionCount` do item voltaram a `NULL`/`0`.
4. **Pull-to-refresh em si**: puxar a lista pra baixo na Home → indicador de refresh aparece e some sem travar, sem crash, mesmo com a lista vazia ou com poucos itens.

**Sem usar o seeder** (cenário ad-hoc, item específico já existente): editar direto via `sqlite3` como no bloco "Ferramentas e armadilhas" acima — `UPDATE items SET dueDate = ..., lastCompletedScheduledDate = ... WHERE id = <id>;` — e repetir o passo de pull-to-refresh + inspeção.

## Risco observado mas não confirmado — flicker no botão de conclusão

Preocupação: ao abrir uma tarefa recorrente já concluída, o botão pode piscar "Concluir" por um frame antes de virar "Reabrir", porque `ItemOccurrenceUiState()` nasce com `isCompleted = false` por padrão até a coroutine do `init` da `ItemOccurrenceViewModel` carregar o item de verdade. Screenshots via `adb`/`android screen capture` têm ~300-500ms de latência por captura — não servem para flagrar um flicker de um frame só. Se isso importar, testar com gravação de tela (`adb shell screenrecord`) em vez de screenshots sequenciais, ou instrumentar com um teste Compose de UI que capture o primeiro frame.
