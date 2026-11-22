# Testes individuais de LEI-129823

O relat�rio de [Move e MoveCsvExporter](index.html) acompanha os testes do PR #31. O relat�rio de [GameRegistry e GameSession](sessoes/index.html) corresponde aos testes acrescentados na issue #42.

| Classe | Linhas cobertas | M�todos cobertos |
| --- | --- | --- |
| GameRegistry | 9/9 (100%) | 4/4 (100%) |
| GameSession | 22/22 (100%) | 10/10 (100%) |

As duas classes de sessoes nao cont�m decisoes condicionais, pelo que a cobertura de ramos nao se aplica. O relat�rio foi gerado com JaCoCo 0.8.14 a partir das classes compiladas em Java 21.

Os cinco testes de cada classe verificam cria�ao e consulta de sessoes, identificadores distintos, remo�ao, estado inicial, vit�rias e independ�ncia entre partidas. A sele�ao individual de vinte testes, incluindo Move e MoveCsvExporter, passou sem falhas, erros ou testes ignorados.
