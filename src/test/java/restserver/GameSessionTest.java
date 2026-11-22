package restserver;

import battleship.Game;
import battleship.IFleet;
import battleship.IGame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    private final GameSession session = new GameSession(
            "partida-129823", "Pedro Victor", "http://localhost:9090");

    @Test
    @DisplayName("Preserva os dados do registo e come�a sem vencedor")
    void guardaDadosIniciaisDaSessao() {
        assertEquals("partida-129823", session.getGameId());
        assertEquals("Pedro Victor", session.getPlayerName());
        assertEquals("http://localhost:9090", session.getCallbackUrl());
        assertEquals(Game.NUMBER_SHOTS, session.getShotsPerTurn());
        assertFalse(session.isGameOver());
        assertNull(session.getWinner());
    }

    @Test
    @DisplayName("Cria uma partida com frota completa e hist�ricos vazios")
    void preparaPartidaParaAPrimeiraJogada() {
        IGame game = session.getGame();

        assertNotNull(game);
        assertSame(game, session.getGame());
        assertEquals(IFleet.FLEET_SIZE, game.getRemainingShips());
        assertTrue(game.getMyMoves().isEmpty());
        assertTrue(game.getAlienMoves().isEmpty());
        assertTrue(game.getAlienFleet().getShips().isEmpty());
        assertEquals(0, game.getHits());
        assertEquals(0, game.getSunkShips());
    }

    @Test
    @DisplayName("Termina a partida com vit�ria da IA e preserva a partida associada")
    void marcaVitoriaDaIa() {
        IGame game = session.getGame();

        session.markAiWins();

        assertTrue(session.isGameOver());
        assertEquals("AI_WINS", session.getWinner());
        assertSame(game, session.getGame());
        assertEquals("partida-129823", session.getGameId());
    }

    @Test
    @DisplayName("Termina a partida com vit�ria do estudante e preserva os dados do jogador")
    void marcaVitoriaDoEstudante() {
        session.markStudentWins();

        assertTrue(session.isGameOver());
        assertEquals("STUDENT_WINS", session.getWinner());
        assertEquals("Pedro Victor", session.getPlayerName());
        assertEquals("http://localhost:9090", session.getCallbackUrl());
    }

    @Test
    @DisplayName("O fim de uma sessao nao altera o estado nem a frota de outra")
    void mantemEstadoIndependenteEntreSessoes() {
        GameSession other = new GameSession(
                "outra-partida", "Outro jogador", "http://localhost:9091");

        session.markStudentWins();

        assertNotSame(session.getGame(), other.getGame());
        assertNotSame(session.getGame().getMyFleet(), other.getGame().getMyFleet());
        assertFalse(other.isGameOver());
        assertNull(other.getWinner());
        assertEquals(IFleet.FLEET_SIZE, other.getGame().getRemainingShips());
    }
}
