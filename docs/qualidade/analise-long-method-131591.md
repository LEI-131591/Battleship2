# Análise de qualidade — Long Method (131591)

## 1. Objetivo

Esta análise documenta a configuração e a execução da inspeção **Long Method** no projeto Battleship2, através do Qodana e do GitHub Actions.

Um método excessivamente longo tende a concentrar demasiadas responsabilidades e detalhes de implementação. Isso pode dificultar a leitura, a compreensão, os testes e a manutenção. A inspeção utilizada procura métodos com um número elevado de instruções de código, excluindo comentários.

## 2. Configuração do Qodana

A configuração utilizada encontra-se em `qodana-131591.yaml`:

- **Linter:** `jetbrains/qodana-jvm-community:2026.2`
- **JDK do projeto:** 21
- **Inspeção:** `NonCommentSourceStatements`
- **Limite:** `m_limit: 30`
- **Quality gate:** `failThreshold: 0`

O perfil inline ativa apenas a inspeção indicada. O limite de 30 significa que métodos com mais de 30 instruções não comentadas são assinalados. O `failThreshold: 0` faz com que a execução falhe quando é detetado pelo menos um problema.

Existe também o ficheiro `.qodana/profiles/profile-131591.yaml`, que contém a mesma inspeção. Na configuração efetivamente utilizada pelo workflow, porém, o perfil está definido inline em `qodana-131591.yaml`.

## 3. Integração contínua

O workflow `.github/workflows/qodana-131591.yml` executa a análise com a ação oficial da JetBrains:

- em `push` para as branches `master` e `131591`;
- em eventos `pull_request`;
- manualmente, através de `workflow_dispatch`.

O workflow utiliza `qodana-131591.yaml`, publica o resultado como artefacto e ativa as anotações e o comentário no pull request.

## 4. Resultados da execução

Foi consultado o relatório SARIF do artefacto `qodana-report`, produzido pela execução GitHub Actions **42368652518**, no commit `726a563b943779d591b055f483e08ad3e226538a` (7 de novembro de 2026).

A inspeção reportou seis ocorrências, todas com nível `warning`:

| Ficheiro | Método | Instruções não comentadas | Linha inicial |
|---|---|---:|---:|
| `src/main/java/battleship/BoardGUI.java` | `createWindow()` | 31 | 40 |
| `src/main/java/battleship/Move.java` | `processEnemyFire()` | 77 | 66 |
| `src/main/java/battleship/SqliteMoveRepository.java` | `saveMove()` | 36 | 112 |
| `src/main/java/battleship/Tasks.java` | `menu()` | 116 | 50 |
| `src/main/java/restserver/AiShotStrategy.java` | `computeShots()` | 31 | 31 |
| `src/main/java/restserver/CallbackClient.java` | `sendShots()` | 32 | 47 |

A ocorrência mais extensa é `Tasks.menu()`, com 116 instruções, seguida de `Move.processEnemyFire()`, com 77. Os métodos `createWindow()` e `computeShots()` ultrapassam o limite por apenas uma instrução, mas também são reportados.

## 5. Interpretação do quality gate

A execução detetou **6 problemas** e terminou com falha porque o limite configurado é zero. Esta falha é coerente com a configuração: o Qodana executou a inspeção, encontrou ocorrências acima do limite e aplicou a condição de falha.

Assim, um check vermelho neste workflow não significa que a ferramenta não tenha funcionado. Significa que o código analisado não satisfaz o critério definido pelo quality gate. Aumentar o `failThreshold` para seis apenas para obter um check verde esconderia os problemas e enfraqueceria o critério.

## 6. Recomendações de refatoração

A inspeção identifica candidatos a revisão; não prova, por si só, que cada método deva ser dividido de uma forma específica. A refatoração deve preservar o comportamento e ser acompanhada por testes.

Uma ordem razoável para a revisão é:

1. **`Tasks.menu()` (116):** separar o tratamento dos comandos em métodos próprios, mantendo no método principal apenas o ciclo de leitura e encaminhamento.
2. **`Move.processEnemyFire()` (77):** separar a contabilização dos resultados, a construção da mensagem apresentada ao utilizador e a preparação/serialização da resposta JSON.
3. **`SqliteMoveRepository.saveMove()` (36):** isolar a inserção da jogada, a obtenção do identificador e a gravação dos tiros, preservando a transação e o rollback.
4. **`CallbackClient.sendShots()` (32):** separar a construção do pedido, a chamada HTTP, a leitura da resposta e o registo da jogada.
5. **`BoardGUI.createWindow()` (31):** extrair a criação dos cabeçalhos, grelha de células e elementos informativos.
6. **`AiShotStrategy.computeShots()` (31):** separar a seleção de alvos adjacentes, o preenchimento pelo padrão de caça e o fallback.

Estas são propostas de refatoração, não alterações já realizadas. Antes de as aplicar, deve confirmar-se que existem testes adequados para os comportamentos envolvidos.

## 7. Conclusão

A integração do Qodana está operacional: a análise foi executada pelo GitHub Actions, o relatório foi produzido e a inspeção Long Method identificou seis métodos acima do limite de 30 instruções. O quality gate falhou como consequência de `failThreshold: 0`.

O resultado demonstra a deteção automatizada e a aplicação do critério configurado. Não demonstra ainda que os métodos tenham sido refatorados nem que o projeto esteja livre de métodos longos. Caso o objetivo da avaliação seja obter zero ocorrências, será necessário realizar as refatorações propostas, executar os testes e voltar a correr o Qodana.

## 8. Evidências e rastreabilidade

- Pull request da configuração: [#22 — Configure Qodana Long Method quality gate](https://github.com/LEI-131591/Battleship2/pull/22)
- Workflow: [qodana-131591.yml](https://github.com/LEI-131591/Battleship2/blob/master/.github/workflows/qodana-131591.yml)
- Configuração: [qodana-131591.yaml](https://github.com/LEI-131591/Battleship2/blob/master/qodana-131591.yaml)
- Perfil: [profile-131591.yaml](https://github.com/LEI-131591/Battleship2/blob/master/.qodana/profiles/profile-131591.yaml)
- Execução GitHub Actions: [42368652518](https://github.com/LEI-131591/Battleship2/actions/runs/42368652518)
- Artefacto do relatório: `qodana-report` (artefacto associado à execução acima).

O PR #22 foi integrado na branch `master` em 7 de novembro de 2026.
