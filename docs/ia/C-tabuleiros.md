# Parte C — verificação das regras e tabuleiros

Tabuleiros de ensaio produzidos pelo assistente. As quatro orientações usam rotações da mesma frota válida.
Água representada por espaços. Legenda: G = Galeão, F = Fragata, N = Nau, C = Caravela, B = Barca.

## Galeão orientado a Norte

```text
     1  2  3  4  5  6  7  8  9 10
A |  G  G  G        N  N  N       |
B |     G                         |
C |     G        B                |
D |                    N          |
E |  F  F  F  F        N          |
F |                    N        B |
G |                               |
H |  C  C        C  C             |
I |                          C    |
J |  B           B           C    |
```

| Navio | Coordenadas |
|---|---|
| Galeao G1 | A1, A2, A3, B2, C2 |
| Fragata F1 | E1, E2, E3, E4 |
| Nau N1 | A6, A7, A8 |
| Nau N2 | D7, E7, F7 |
| Caravela C1 | H1, H2 |
| Caravela C2 | H5, H6 |
| Caravela C3 | I9, J9 |
| Barca B1 | C5 |
| Barca B2 | F10 |
| Barca B3 | J1 |
| Barca B4 | J5 |

Verificação: 11 navios, 25 posições, sem sobreposições nem contactos entre navios diferentes, incluindo diagonais.

## Galeão orientado a Sul

```text
     1  2  3  4  5  6  7  8  9 10
A |     C           B           B |
B |     C                         |
C |              C  C        C  C |
D |                               |
E |  B        N                   |
F |           N        F  F  F  F |
G |           N                   |
H |                 B        G    |
I |                          G    |
J |        N  N  N        G  G  G |
```

| Navio | Coordenadas |
|---|---|
| Galeao G1 | J10, J9, J8, I9, H9 |
| Fragata F1 | F10, F9, F8, F7 |
| Nau N1 | J5, J4, J3 |
| Nau N2 | G4, F4, E4 |
| Caravela C1 | C10, C9 |
| Caravela C2 | C6, C5 |
| Caravela C3 | B2, A2 |
| Barca B1 | H6 |
| Barca B2 | E1 |
| Barca B3 | A10 |
| Barca B4 | A6 |

Verificação: 11 navios, 25 posições, sem sobreposições nem contactos entre navios diferentes, incluindo diagonais.

## Galeão orientado a Este

```text
     1  2  3  4  5  6  7  8  9 10
A |  B     C        F           G |
B |        C        F     G  G  G |
C |                 F           G |
D |                 F             |
E |  B     C              B       |
F |        C                    N |
G |              N  N  N        N |
H |                             N |
I |  C  C                         |
J |              B                |
```

| Navio | Coordenadas |
|---|---|
| Galeao G1 | A10, B10, C10, B9, B8 |
| Fragata F1 | A6, B6, C6, D6 |
| Nau N1 | F10, G10, H10 |
| Nau N2 | G7, G6, G5 |
| Caravela C1 | A3, B3 |
| Caravela C2 | E3, F3 |
| Caravela C3 | I2, I1 |
| Barca B1 | E8 |
| Barca B2 | J5 |
| Barca B3 | A1 |
| Barca B4 | E1 |

Verificação: 11 navios, 25 posições, sem sobreposições nem contactos entre navios diferentes, incluindo diagonais.

## Galeão orientado a Oeste

```text
     1  2  3  4  5  6  7  8  9 10
A |                 B             |
B |                          C  C |
C |  N                            |
D |  N        N  N  N             |
E |  N                    C       |
F |        B              C     B |
G |              F                |
H |  G           F                |
I |  G  G  G     F        C       |
J |  G           F        C     B |
```

| Navio | Coordenadas |
|---|---|
| Galeao G1 | J1, I1, H1, I2, I3 |
| Fragata F1 | J5, I5, H5, G5 |
| Nau N1 | E1, D1, C1 |
| Nau N2 | D4, D5, D6 |
| Caravela C1 | J8, I8 |
| Caravela C2 | F8, E8 |
| Caravela C3 | B9, B10 |
| Barca B1 | F3 |
| Barca B2 | A6 |
| Barca B3 | J10 |
| Barca B4 | F10 |

Verificação: 11 navios, 25 posições, sem sobreposições nem contactos entre navios diferentes, incluindo diagonais.
