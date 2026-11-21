# Oportunidades de refabricação

| Local | Cheiro no código | Refabricação | Número de aluno |
| --- | --- | --- | --- |
| `Move::processEnemyFire` | Método longo com formatação de resultados por navio | Extract Method | 129823 |
| `Move::processEnemyFire`, contagem dos tiros | Condicionais aninhadas para casos que terminam a iteração | Replace Nested Conditional with Guard Clauses | 129823 |
| `Move::processEnemyFire`, texto dos tiros repetidos | Expressão de plural repetida quatro vezes | Introduce Variable | 129823 |
| `MoveCsvExporter::export` | Construção repetida de um formato CSV fixo | Introduce Constant | 129823 |
| `MoveCsvExporter::outcome` | Nome pouco específico para a transformação em texto CSV | Rename Method | 129823 |
| `CallbackClient::sendShots`, criação da rajada | Método longo com conversão de posições misturada com o envio HTTP | Extract Method | 131056 |
| `CallbackClient::sendShots`, timeout do pedido | Valor fixo sem nome na configuração do pedido | Introduce Constant | 131056 |
| `CallbackClient::sendShots`, validação HTTP | Consulta repetida do mesmo estado HTTP | Introduce Variable | 131056 |
| `CallbackClient::sendShots`, resposta desserializada | Nome de variável pouco descritivo (`m2b`) | Rename | 131056 |
| `CallbackClient::recordAiMove` | Acoplamento ao DTO de resposta inteiro quando o método usa apenas os resultados | Change Signature | 131056 |

A atribuição refere-se às alterações de LEI-129823. A equipa pode acrescentar as oportunidades dos restantes membros à tabela.

A validação partiu do ramo de testes individuais [PR #31](https://github.com/LEI-131591/Battleship2/pull/31). Para cada alteração, executei `mvn -B -Djunit.jupiter.version=6.0.3 -Djunit.jupiterplatform.version=6.0.3 verify` antes e depois, com Java 21. As dez execuções terminaram com **153 testes, zero falhas, zero erros e zero ignorados**.

Depois da integração do PR #31, repeti a validação com a base `master` em `452021c`. O comando `mvn -B -Djunit.jupiter.version=6.0.3 -Djunit.jupiterplatform.version=6.0.3 clean verify` em Java 21 terminou com **153 testes, zero falhas, zero erros e zero ignorados**.
