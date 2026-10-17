# Parte D — ensaio real de estratégia contra frota oculta

Data: 17 de outubro de 2026. LLM: o assistente desta conversa.
Programa: Battleship2, commit `17aa27b0f8fccdb87183de1c4f96997460328beb`.

## Método

O assistente escolheu cada rajada depois de receber o resultado anterior. Um adaptador Java utilizou Fleet.createRandom(), Game.fireShots(...) e Move.processEnemyFire(...) da versão real do projeto. O adaptador suprimiu a impressão do mapa e devolveu apenas o JSON e a contagem de navios restantes. A contagem também pode ser deduzida dos afundamentos acumulados.
O assistente usou ferramentas para manter o diário e enumerar colocações geométricas compatíveis com os dados já recebidos. As ferramentas não escolheram automaticamente as rajadas nem forneceram a frota oculta.
A frota foi mantida na memória do processo. Um compromisso SHA-256 foi publicado antes da primeira jogada. A frota e o sal só foram revelados após o programa confirmar zero navios restantes. O compromisso foi conferido e todas as respostas foram reavaliadas com a frota revelada.

## Resultado

| Métrica | Valor |
|---|---:|
| Rajadas | 18 |
| Tiros | 54 |
| Acertos, incluindo os que afundaram navios | 25 |
| Água | 29 |
| Repetidos | 0 |
| Fora do tabuleiro | 0 |
| Navios afundados | 11 |
| Navios restantes | 0 |

É um ensaio de uma partida, com três acertos logo na primeira rajada. Não foi realizada uma comparação com jogadores humanos ou uma amostra de várias frotas. O resultado não demonstra superioridade sobre humanos nem garante 18 rajadas em partidas futuras.

## Ajustes do prompt e exemplos

A versão inicial usava exploração em regiões afastadas e investigação de vizinhos ortogonais. Após a primeira resposta, o prompt foi refinado para explicitar a ambiguidade dos resultados agregados. Os exemplos usados no refinamento estão documentados nas trocas abaixo.

1. Rajada 1: três acertos, mas os tipos individuais continuam ambíguos. A versão refinada mantém essa incerteza e investiga uma região de cada vez.
2. Rajada 7: uma Barca afundada entre B9/F9/I3. Não foram excluídos os três halos. Mais tarde, os halos da Fragata e do Galeão permitiram concluir que a Barca estava em B9.
3. Rajadas 10–11: a Fragata atingida em E8/E9/E10 foi terminada em E7 usando o limite do tabuleiro.
4. Rajadas 12–13: os dois acertos no Galeão em regiões afastadas foram resolvidos pela geometria. As posições H4 e H6 pertenciam ao Galeão; H5/G4/I4 concluíram a forma orientada a Oeste.
5. Final: só se excluíram halos confirmados ou comuns a todas as hipóteses. Depois de terminar a Nau, restavam quatro casas possíveis para duas Barcas.

O prompt final consolidado está em [D-prompt-final.md](D-prompt-final.md). As regras de incerteza foram usadas a partir da rajada 2. O texto final incorpora também as deduções posteriores; não foi ainda ensaiado integralmente numa segunda partida.

## Diário das decisões

| Rajada | Coordenadas | Acertos em navios | Água | Afundamentos | Restantes |
|---:|---|---|---:|---|---:|
| 1 | B2, E5, H8 | 2 × Caravela, 1 × Nau | 0 | — | 11 |
| 2 | B1, B3, A2 | — | 3 | — | 11 |
| 3 | C2, D2, D5 | — | 1 | 1 × Nau | 10 |
| 4 | E4, E6, F5 | — | 2 | 1 × Caravela | 9 |
| 5 | G8, H7, H9 | — | 3 | — | 9 |
| 6 | I8, B6, H3 | — | 2 | 1 × Caravela | 8 |
| 7 | B9, F9, I3 | — | 2 | 1 × Barca | 7 |
| 8 | C5, C6, C7 | — | 1 | 1 × Caravela | 6 |
| 9 | G1, G2, G3 | — | 3 | — | 6 |
| 10 | E8, E9, E10 | 3 × Fragata | 0 | — | 6 |
| 11 | E7, I5, J5 | — | 2 | 1 × Fragata | 5 |
| 12 | A8, H4, H6 | 2 × Galeao | 1 | — | 5 |
| 13 | H5, G4, I4 | — | 0 | 1 × Galeao | 4 |
| 14 | A5, H10, H1 | 1 × Nau | 2 | — | 4 |
| 15 | A4, A6, A7 | — | 2 | 1 × Barca | 3 |
| 16 | G10, I10, J10 | — | 3 | — | 3 |
| 17 | I1, J1, C4 | — | 1 | 1 × Nau | 2 |
| 18 | F1, F2, J6 | — | 1 | 2 × Barca | 0 |

A coluna de acertos segue hitsOnBoats; os tipos afundados aparecem na coluna seguinte.

### Rajada 1

**Decisão antes da resposta:** Exploração inicial de três regiões afastadas, sem acesso ao mapa.

```json
[
  {
    "row": "B",
    "column": 2
  },
  {
    "row": "E",
    "column": 5
  },
  {
    "row": "H",
    "column": 8
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [
    {
      "hits": 2,
      "type": "Caravela"
    },
    {
      "hits": 1,
      "type": "Nau"
    }
  ],
  "missedShots": 0
}
```

### Rajada 2

**Decisão antes da resposta:** Os três alvos anteriores são acertos certos, mas o tipo por coordenada é ambíguo. Investigar vizinhos de B2 sem atribuir o tipo à posição.

```json
[
  {
    "row": "B",
    "column": 1
  },
  {
    "row": "B",
    "column": 3
  },
  {
    "row": "A",
    "column": 2
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 3
}
```

### Rajada 3

**Decisão antes da resposta:** A2, B1 e B3 são água; B2 exige continuação por C2. D2 testa a hipótese de Nau vertical. D5 investiga o acerto E5.

```json
[
  {
    "row": "C",
    "column": 2
  },
  {
    "row": "D",
    "column": 2
  },
  {
    "row": "D",
    "column": 5
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 1,
      "type": "Nau"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 1
}
```

### Rajada 4

**Decisão antes da resposta:** Nau B2-C2-D2 confirmada e afundada; D5 é água. E5 é uma Caravela: testar os três vizinhos restantes para a terminar.

```json
[
  {
    "row": "E",
    "column": 4
  },
  {
    "row": "E",
    "column": 6
  },
  {
    "row": "F",
    "column": 5
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 1,
      "type": "Caravela"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 2
}
```

### Rajada 5

**Decisão antes da resposta:** Caravela de E5 afundada, cauda ainda ambígua. Investigar três vizinhos ortogonais da outra Caravela, em H8.

```json
[
  {
    "row": "G",
    "column": 8
  },
  {
    "row": "H",
    "column": 7
  },
  {
    "row": "H",
    "column": 9
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 3
}
```

### Rajada 6

**Decisão antes da resposta:** G8, H7 e H9 são água: a Caravela H8 deve continuar em I8. Usar os outros tiros para explorar B6 e H3.

```json
[
  {
    "row": "I",
    "column": 8
  },
  {
    "row": "B",
    "column": 6
  },
  {
    "row": "H",
    "column": 3
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 1,
      "type": "Caravela"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 2
}
```

### Rajada 7

**Decisão antes da resposta:** Caravela H8-I8 concluída. B6 e H3 são água. Retomar exploração fora dos halos confirmados, em B9, F9 e I3.

```json
[
  {
    "row": "B",
    "column": 9
  },
  {
    "row": "F",
    "column": 9
  },
  {
    "row": "I",
    "column": 3
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 1,
      "type": "Barca"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 2
}
```

### Rajada 8

**Decisão antes da resposta:** Uma Barca foi afundada em B9/F9/I3, sem identificação individual: não excluir halos de qualquer uma dessas hipóteses. Explorar um grupo contíguo C5-C7 para reduzir nova ambiguidade.

```json
[
  {
    "row": "C",
    "column": 5
  },
  {
    "row": "C",
    "column": 6
  },
  {
    "row": "C",
    "column": 7
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 1,
      "type": "Caravela"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 1
}
```

### Rajada 9

**Decisão antes da resposta:** Terceira Caravela afundada em C5-C6 ou C6-C7; apenas o halo comum às duas hipóteses é seguro. Explorar G1-G3, fora das exclusões confirmadas.

```json
[
  {
    "row": "G",
    "column": 1
  },
  {
    "row": "G",
    "column": 2
  },
  {
    "row": "G",
    "column": 3
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 3
}
```

### Rajada 10

**Decisão antes da resposta:** G1-G3 são água. Explorar grupo contíguo E8-E10 na zona direita, preservando as hipóteses ainda ambíguas da Barca.

```json
[
  {
    "row": "E",
    "column": 8
  },
  {
    "row": "E",
    "column": 9
  },
  {
    "row": "E",
    "column": 10
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [
    {
      "hits": 3,
      "type": "Fragata"
    }
  ],
  "missedShots": 0
}
```

### Rajada 11

**Decisão antes da resposta:** Três acertos em Fragata em E8-E10 e limite direito do tabuleiro implicam E7 como quarta posição. Explorar ainda I5-J5.

```json
[
  {
    "row": "E",
    "column": 7
  },
  {
    "row": "I",
    "column": 5
  },
  {
    "row": "J",
    "column": 5
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 1,
      "type": "Fragata"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 2
}
```

### Rajada 12

**Decisão antes da resposta:** Usar o diário e as posições ainda compatíveis dos navios grandes. A8, H4 e H6 cobrem o maior número dessas hipóteses; nenhum é tiro repetido ou halo confirmado.

```json
[
  {
    "row": "A",
    "column": 8
  },
  {
    "row": "H",
    "column": 4
  },
  {
    "row": "H",
    "column": 6
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [
    {
      "hits": 2,
      "type": "Galeao"
    }
  ],
  "missedShots": 1
}
```

### Rajada 13

**Decisão antes da resposta:** Os dois acertos no Galeão têm de ser H4 e H6, pois A8 é demasiado distante. F5 e J5 já excluem as formas Norte/Sul; H5 é comum às hipóteses Este/Oeste. Testar H5 e as duas extremidades G4-I4 da hipótese Oeste.

```json
[
  {
    "row": "H",
    "column": 5
  },
  {
    "row": "G",
    "column": 4
  },
  {
    "row": "I",
    "column": 4
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 1,
      "type": "Galeao"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 0
}
```

### Rajada 14

**Decisão antes da resposta:** Galeão G4/H4/H5/H6/I4 confirmado. O seu halo exclui I3 e a Fragata excluiu F9, logo a Barca da rajada 7 era B9. Explorar A5, H10 e H1, cobrindo várias das sete posições ainda possíveis para a Nau restante.

```json
[
  {
    "row": "A",
    "column": 5
  },
  {
    "row": "H",
    "column": 10
  },
  {
    "row": "H",
    "column": 1
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [
    {
      "hits": 1,
      "type": "Nau"
    }
  ],
  "missedShots": 2
}
```

### Rajada 15

**Decisão antes da resposta:** Há uma Nau em uma de três regiões afastadas. Concentrar a rajada em A4/A6/A7 resolve a hipótese de A5 em vez de repetir três sondagens ambíguas.

```json
[
  {
    "row": "A",
    "column": 4
  },
  {
    "row": "A",
    "column": 6
  },
  {
    "row": "A",
    "column": 7
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 1,
      "type": "Barca"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 2
}
```

### Rajada 16

**Decisão antes da resposta:** A região de A5 não contém a Nau; houve apenas uma Barca em A4/A6/A7, ainda sem posição individual certa. A Nau restante está em H10 ou H1. Testar as continuações G10/I10/J10 da hipótese direita.

```json
[
  {
    "row": "G",
    "column": 10
  },
  {
    "row": "I",
    "column": 10
  },
  {
    "row": "J",
    "column": 10
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 3
}
```

### Rajada 17

**Decisão antes da resposta:** As três continuações direitas são água, pelo que a Nau é H1-I1-J1. Terminar com I1/J1 e usar o terceiro tiro para procurar Barca em C4.

```json
[
  {
    "row": "I",
    "column": 1
  },
  {
    "row": "J",
    "column": 1
  },
  {
    "row": "C",
    "column": 4
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 1,
      "type": "Nau"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 1
}
```

### Rajada 18

**Decisão antes da resposta:** A Caravela de E5 só pode ser E4-E5: E6 toca na Fragata e F5 tocaria no Galeão. Com a Nau H1-I1-J1 confirmada, restam apenas B4/F1/F2/J6 para duas Barcas. Testar F1/F2/J6; F1 e F2 não podem conter duas Barcas adjacentes.

```json
[
  {
    "row": "F",
    "column": 1
  },
  {
    "row": "F",
    "column": 2
  },
  {
    "row": "J",
    "column": 6
  }
]
```

**Resposta real do programa:**

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 2,
      "type": "Barca"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 1
}
```

## Auditoria posterior ao jogo

Compromisso inicial: `cca304836e3197aeac4b16d47c920edcc3787488e44072f8c756905624a22744`.
Sal revelado no fim: `ccac5dd8-e9d4-4f78-8e70-1e456065b330`.
Verificação: SHA-256(sal + quebra de linha + JSON compacto da frota) corresponde ao compromisso inicial.
O replay posterior confirmou as 54 coordenadas e todos os contadores, acertos e afundamentos. A auditoria por tiro presente em D-trocas-json.json foi calculada APENAS depois de terminar a partida; essas informações individuais não estiveram disponíveis para escolher as rajadas.

## Ficheiros

- D-prompt-final.md: texto consolidado para publicar no README.
- D-trocas-json.json: diário estruturado, revelação final e auditoria posterior.
- D-sessao-consola.json: saída original do terminal. As quebras e duplicações de caracteres do ConPTY foram normalizadas para extrair os JSON; os valores foram depois confirmados pelo replay.
- HiddenProtocolSession.java: adaptador utilizado para impedir a exposição do mapa durante o ensaio.
- D-video-roteiro.md: proposta de demonstração adicional, até um minuto.
