package restserver;

import battleship.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AiShotStrategyTest {

    private final AiShotStrategy strategy = new AiShotStrategy();


    @Test
    @DisplayName("Should use safety fallback when hunt pool has no available positions")
    void shouldUseSafetyFallbackWhenHuntPoolIsEmpty() {
        Game game = new Game(new Fleet());

        List<IPosition> previousShots = new ArrayList<>();
        List<IGame.ShotResult> previousResults = new ArrayList<>();

        // Mark every position as already shot, except three odd-parity cells.
        for (int row = 0; row < Game.BOARD_SIZE; row++) {
            for (int col = 0; col < Game.BOARD_SIZE; col++) {
                if (!(row == 0 && (col == 1 || col == 3 || col == 5))) {
                    previousShots.add(new Position(row, col));
                    previousResults.add(
                            new IGame.ShotResult(false, false, null, false)
                    );
                }
            }
        }

        game.getAlienMoves().add(
                new Move(1, previousShots, previousResults)
        );

        AiShotStrategy strategy = new AiShotStrategy();

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(3, shots.size());

        assertTrue(shots.contains(new Position(0, 1)));
        assertTrue(shots.contains(new Position(0, 3)));
        assertTrue(shots.contains(new Position(0, 5)));
    }


    @Test
    @DisplayName("Deve devolver exatamente três posições válidas e distintas")
    void deveDevolverTresDisparosValidosEDistintos() {
        IGame game = new Game(new Fleet());

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        assertEquals(shots.size(), new HashSet<>(shots).size(),
                "A estratégia não deve repetir posições na mesma jogada.");
        assertTrue(shots.stream().allMatch(IPosition::isInside),
                "Todos os disparos devem estar dentro do tabuleiro.");
    }

    @Test
    @DisplayName("Deve usar o padrão de xadrez na fase HUNT")
    void deveUsarPadraoXadrezNaFaseHunt() {
        IGame game = new Game(new Fleet());

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        assertTrue(shots.stream().allMatch(p ->
                        (p.getRow() + p.getColumn()) % 2 == 0),
                "Na fase HUNT, as posições devem pertencer ao padrão de xadrez.");
    }

    @Test
    @DisplayName("Não deve voltar a disparar em posições usadas anteriormente")
    void naoDeveRepetirDisparosAnteriores() {
        IGame game = new Game(new Fleet());
        List<IPosition> previousShots = List.of(
                new Position(0, 0),
                new Position(0, 2),
                new Position(0, 4)
        );
        game.getAlienMoves().add(new Move(1, previousShots, List.of()));

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        assertTrue(shots.stream().noneMatch(previousShots::contains));
        assertEquals(shots.size(), new HashSet<>(shots).size());
    }

    @Test
    @DisplayName("Deve dar prioridade às posições adjacentes a um acerto não afundado")
    void devePriorizarPosicoesAdjacentesAUmAcerto() {
        IGame game = new Game(new Fleet());
        IShip ship = new Barge(Compass.NORTH, new Position(5, 5));
        IPosition hit = new Position(5, 5);

        IMove previousMove = new Move(
                1,
                List.of(hit),
                List.of(new IGame.ShotResult(true, false, ship, false))
        );
        game.getAlienMoves().add(previousMove);

        List<IPosition> shots = strategy.computeShots(game);
        Set<IPosition> adjacent = Set.copyOf(hit.adjacentPositions());

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        assertTrue(shots.stream().allMatch(adjacent::contains),
                "As três posições devem ser escolhidas entre as adjacentes ao acerto.");
        assertFalse(shots.contains(hit));
    }

    @Test
    @DisplayName("Deve ignorar resultados inválidos, repetidos, sem navio ou com navio afundado")
    void deveIgnorarResultadosQueNaoSaoAlvosAtivos() {
        IGame game = new Game(new Fleet());
        IShip ship = new Barge(Compass.NORTH, new Position(4, 4));

        List<IPosition> previousShots = List.of(
                new Position(4, 4),
                new Position(4, 6),
                new Position(6, 4),
                new Position(6, 6)
        );
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(false, false, ship, false),
                new IGame.ShotResult(true, true, ship, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, ship, true)
        );
        game.getAlienMoves().add(new Move(1, previousShots, results));

        List<IPosition> shots = strategy.computeShots(game);

        assertEquals(Game.NUMBER_SHOTS, shots.size());
        assertTrue(shots.stream().allMatch(p ->
                        (p.getRow() + p.getColumn()) % 2 == 0),
                "Sem alvos ativos, a estratégia deve recorrer à fase HUNT.");
        assertTrue(shots.stream().noneMatch(previousShots::contains));
    }
}
