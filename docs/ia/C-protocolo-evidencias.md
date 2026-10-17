# Parte C — ensaio real do protocolo de comunicação

Data: 17 de outubro de 2026. LLM escolhido: o assistente desta conversa.
Versão do programa: commit `17aa27b0f8fccdb87183de1c4f96997460328beb` do repositório LEI-131591/Battleship2.
ID do jogo: `b034b9f2-1666-4314-90e5-d76ee8b88a30`.

## Método e âmbito

O assistente escolheu e introduziu as rajadas no programa Java real através das ferramentas de terminal. As respostas abaixo foram extraídas da saída de `Move.processEnemyFire(...)` e conferidas com os registos SQLite. Não se trata de respostas simuladas pelo LLM nem de uma conversa com outro modelo.
O programa foi executado numa pasta de ensaio. O mapa estava visível e os tiros foram escolhidos para cobrir casos do protocolo. Os resultados deste ensaio não medem a estratégia do oponente contra uma frota oculta (parte D).

## Regras compreendidas

- Tabuleiro: A–J e 1–10.
- Frota: 4 Barcas, 3 Caravelas, 2 Naus, 1 Fragata e 1 Galeão; 11 navios e 25 posições.
- Navios diferentes não se sobrepõem nem se tocam, incluindo diagonais.
- Galeão com cinco posições em T, nas orientações Norte, Sul, Este e Oeste.
- Cada rajada tem exatamente três tiros; termina quando os onze navios forem afundados.
Os quatro tabuleiros validados estão em [C-tabuleiros.md](C-tabuleiros.md). São tabuleiros de exemplo, distintos da frota aleatória desta sessão.

## Protocolo compreendido

A rajada é um array JSON simples com três objetos contendo `row` e `column`. A consola recebe a tradução textual `rajada A5 C10 F5`. O array não deve ser envolvido por chavetas sem uma chave.
O resultado é o objeto JSON do programa com os campos `validShots`, `repeatedShots`, `outsideShots`, `missedShots`, `hitsOnBoats` e `sunkBoats`. O array de coordenadas que a consola imprime a seguir não é o resultado.
`validShots` exclui repetições e exteriores. Em cada rajada, `validShots + repeatedShots + outsideShots = 3`. `sunkBoats.count` conta navios afundados nessa rajada; não é uma contagem de tiros. A ordem das propriedades do objeto e das entradas agrupadas por tipo não altera o seu significado.
A designação do Galeão no protocolo do programa é `Galeao`, sem acento.

## Trocas reais

Arranque: `java -cp target/BattleshipGamePlayer-2.0.jar battleship.Main`. Foi executado `gerafrota` uma única vez antes da primeira rajada. A mesma partida foi mantida durante as onze trocas.

### Rajada 1

Mensagem escolhida pelo LLM:

```json
[
  {
    "row": "A",
    "column": 5
  },
  {
    "row": "C",
    "column": 10
  },
  {
    "row": "F",
    "column": 5
  }
]
```

Comando introduzido no programa:

```text
rajada A5 C10 F5
```

Resposta real do programa:

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

**Interpretação do LLM:** Os três tiros são válidos e atingiram água. Nenhum acerto ou afundamento.

### Rajada 2

Mensagem escolhida pelo LLM:

```json
[
  {
    "row": "A",
    "column": 5
  },
  {
    "row": "A",
    "column": 5
  },
  {
    "row": "K",
    "column": 11
  }
]
```

Comando introduzido no programa:

```text
rajada A5 A5 K11
```

Resposta real do programa:

```json
{
  "validShots": 0,
  "sunkBoats": [],
  "repeatedShots": 2,
  "outsideShots": 1,
  "hitsOnBoats": [],
  "missedShots": 0
}
```

**Interpretação do LLM:** A5 já tinha sido usado e foi indicado duas vezes: dois tiros repetidos. K11 está fora do tabuleiro. Não há tiros válidos.

### Rajada 3

Mensagem escolhida pelo LLM:

```json
[
  {
    "row": "A",
    "column": 1
  },
  {
    "row": "A",
    "column": 1
  },
  {
    "row": "F",
    "column": 3
  }
]
```

Comando introduzido no programa:

```text
rajada A1 A1 F3
```

Resposta real do programa:

```json
{
  "validShots": 2,
  "sunkBoats": [
    {
      "count": 1,
      "type": "Barca"
    }
  ],
  "repeatedShots": 1,
  "outsideShots": 0,
  "hitsOnBoats": [
    {
      "hits": 1,
      "type": "Caravela"
    }
  ],
  "missedShots": 0
}
```

**Interpretação do LLM:** A primeira tentativa em A1 afundou uma Barca; a segunda é repetida na própria rajada. F3 atingiu uma Caravela, que ainda flutua.

### Rajada 4

Mensagem escolhida pelo LLM:

```json
[
  {
    "row": "G",
    "column": 3
  },
  {
    "row": "C",
    "column": 5
  },
  {
    "row": "C",
    "column": 6
  }
]
```

Comando introduzido no programa:

```text
rajada G3 C5 C6
```

Resposta real do programa:

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
  "hitsOnBoats": [
    {
      "hits": 2,
      "type": "Nau"
    }
  ],
  "missedShots": 0
}
```

**Interpretação do LLM:** Uma Caravela foi afundada e houve dois acertos numa Nau. Todos os tiros são válidos.

### Rajada 5

Mensagem escolhida pelo LLM:

```json
[
  {
    "row": "C",
    "column": 7
  },
  {
    "row": "H",
    "column": 1
  },
  {
    "row": "I",
    "column": 1
  }
]
```

Comando introduzido no programa:

```text
rajada C7 H1 I1
```

Resposta real do programa:

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 1,
      "type": "Caravela"
    },
    {
      "count": 1,
      "type": "Nau"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 0
}
```

**Interpretação do LLM:** Uma Nau e uma Caravela foram afundadas. sunkBoats conta navios, não posições ocupadas.

### Rajada 6

Mensagem escolhida pelo LLM:

```json
[
  {
    "row": "D",
    "column": 1
  },
  {
    "row": "D",
    "column": 2
  },
  {
    "row": "D",
    "column": 3
  }
]
```

Comando introduzido no programa:

```text
rajada D1 D2 D3
```

Resposta real do programa:

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
  "missedShots": 0
}
```

**Interpretação do LLM:** Uma Nau foi afundada pelos três tiros desta rajada. Três tiros válidos correspondem a um navio afundado.

### Rajada 7

Mensagem escolhida pelo LLM:

```json
[
  {
    "row": "G",
    "column": 5
  },
  {
    "row": "H",
    "column": 5
  },
  {
    "row": "I",
    "column": 5
  }
]
```

Comando introduzido no programa:

```text
rajada G5 H5 I5
```

Resposta real do programa:

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

**Interpretação do LLM:** Três acertos numa Fragata, que ainda não foi afundada. hitsOnBoats conta acertos agrupados pelo tipo.

### Rajada 8

Mensagem escolhida pelo LLM:

```json
[
  {
    "row": "J",
    "column": 5
  },
  {
    "row": "A",
    "column": 6
  },
  {
    "row": "A",
    "column": 10
  }
]
```

Comando introduzido no programa:

```text
rajada J5 A6 A10
```

Resposta real do programa:

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 2,
      "type": "Barca"
    },
    {
      "count": 1,
      "type": "Fragata"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 0
}
```

**Interpretação do LLM:** A Fragata foi afundada e duas Barcas foram afundadas. A entrada Barca usa count igual a 2.

### Rajada 9

Mensagem escolhida pelo LLM:

```json
[
  {
    "row": "H",
    "column": 7
  },
  {
    "row": "H",
    "column": 10
  },
  {
    "row": "I",
    "column": 10
  }
]
```

Comando introduzido no programa:

```text
rajada H7 H10 I10
```

Resposta real do programa:

```json
{
  "validShots": 3,
  "sunkBoats": [
    {
      "count": 1,
      "type": "Caravela"
    },
    {
      "count": 1,
      "type": "Barca"
    }
  ],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [],
  "missedShots": 0
}
```

**Interpretação do LLM:** Uma Barca e uma Caravela foram afundadas. Resta apenas o Galeão.

### Rajada 10

Mensagem escolhida pelo LLM:

```json
[
  {
    "row": "C",
    "column": 9
  },
  {
    "row": "D",
    "column": 9
  },
  {
    "row": "E",
    "column": 9
  }
]
```

Comando introduzido no programa:

```text
rajada C9 D9 E9
```

Resposta real do programa:

```json
{
  "validShots": 3,
  "sunkBoats": [],
  "repeatedShots": 0,
  "outsideShots": 0,
  "hitsOnBoats": [
    {
      "hits": 3,
      "type": "Galeao"
    }
  ],
  "missedShots": 0
}
```

**Interpretação do LLM:** Três acertos num Galeão, ainda a flutuar.

### Rajada 11

Mensagem escolhida pelo LLM:

```json
[
  {
    "row": "E",
    "column": 8
  },
  {
    "row": "E",
    "column": 10
  },
  {
    "row": "B",
    "column": 1
  }
]
```

Comando introduzido no programa:

```text
rajada E8 E10 B1
```

Resposta real do programa:

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
  "missedShots": 1
}
```

**Interpretação do LLM:** O Galeão foi afundado e houve um tiro na água. O programa confirmou zero navios a flutuar e onze afundados.

## Verificação final

| Métrica | Valor |
|---|---:|
| Rajadas | 11 |
| Tiros | 33 |
| Tiros válidos | 29 |
| Acertos, incluindo tiros que afundaram | 25 |
| Água | 4 |
| Repetidos | 3 |
| Exteriores | 1 |
| Navios afundados | 11 |
| Navios a flutuar | 0 |

Afundamentos confirmados: 4 Barcas, 3 Caravelas, 2 Naus, 1 Fragata e 1 Galeão.
Cada resposta foi conferida com os três tiros persistidos para a respetiva jogada. As onze respostas cumpriram a soma dos contadores e os resultados registados.

## Declaração do LLM

Compreendo o protocolo praticado. Consigo produzir rajadas no formato acordado e interpretar os resultados reais do programa, incluindo repetições, tiros exteriores, água, acertos e afundamentos. Uma nova rajada deve ser escolhida depois de receber a resposta da anterior. A melhoria da seleção de tiros contra uma frota oculta será tratada na parte D.

## Evidências

- `C-trocas-json.json`: mensagens e respostas em JSON, com totais e identificador do jogo.
- `C-sessao-consola.json`: transcrição da execução, incluindo a saída original e sequências de controlo do terminal.
- `C-tabuleiros.md` e `C-tabuleiros.json`: quatro tabuleiros de exemplo com coordenadas e validação.
  O registo descreve o ensaio efetivamente executado pelo assistente; não documenta operações realizadas manualmente pelos membros do grupo.
