package restserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameRegistryTest {

    private final GameRegistry registry = new GameRegistry();

    @Test
    @DisplayName("Cria uma sessao com identificador v�lido e permite consult�-la")
    void criaEGuardaSessao() {
        GameSession session = registry.createSession("Pedro", "http://localhost:9090");

        assertNotNull(session);
        assertEquals(session.getGameId(), UUID.fromString(session.getGameId()).toString());
        assertSame(session, registry.getSession(session.getGameId()));
        assertEquals("Pedro", session.getPlayerName());
        assertEquals("http://localhost:9090", session.getCallbackUrl());
    }

    @Test
    @DisplayName("Devolve null quando o identificador nao corresponde a uma sessao")
    void consultaSessaoInexistente() {
        assertNull(registry.getSession("sessao-inexistente"));
    }

    @Test
    @DisplayName("Dois registos do mesmo jogador criam partidas independentes")
    void criaSessoesDistintasParaOMesmoJogador() {
        GameSession first = registry.createSession("Pedro", "http://localhost:9090");
        GameSession second = registry.createSession("Pedro", "http://localhost:9090");

        assertNotEquals(first.getGameId(), second.getGameId());
        assertNotSame(first.getGame(), second.getGame());
        assertSame(first, registry.getSession(first.getGameId()));
        assertSame(second, registry.getSession(second.getGameId()));

        first.markAiWins();

        assertTrue(registry.getSession(first.getGameId()).isGameOver());
        assertFalse(registry.getSession(second.getGameId()).isGameOver());
    }

    @Test
    @DisplayName("Remove apenas a sessao indicada e permite repetir a remo�ao")
    void removeSessaoSemAfetarOutraPartida() {
        GameSession first = registry.createSession("Pedro", "http://localhost:9090");
        GameSession second = registry.createSession("Outro jogador", "http://localhost:9091");

        registry.removeSession(first.getGameId());

        assertNull(registry.getSession(first.getGameId()));
        assertSame(second, registry.getSession(second.getGameId()));
        assertDoesNotThrow(() -> registry.removeSession(first.getGameId()));
        assertSame(second, registry.getSession(second.getGameId()));
    }

    @Test
    @DisplayName("Remover um identificador desconhecido preserva as sessoes existentes")
    void ignoraRemocaoDeSessaoInexistente() {
        GameSession session = registry.createSession("Pedro", "http://localhost:9090");

        assertDoesNotThrow(() -> registry.removeSession("sessao-inexistente"));

        assertSame(session, registry.getSession(session.getGameId()));
    }
}
