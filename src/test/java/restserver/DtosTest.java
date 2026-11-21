package restserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtosTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Lê o nome do jogador e o URL no pedido de registo")
    void lePedidoDeRegisto() throws Exception {
        Dtos.RegistrationRequest pedido = mapper.readValue("""
                {"playerName":"Alice","callbackUrl":"http://127.0.0.1:9090"}
                """, Dtos.RegistrationRequest.class);

        assertEquals("Alice", pedido.playerName);
        assertEquals("http://127.0.0.1:9090", pedido.callbackUrl);
    }

    @Test
    @DisplayName("Serializa a confirmação de registo com os nomes do protocolo")
    void escreveRespostaDeRegisto() throws Exception {
        Dtos.RegistrationResponse resposta = new Dtos.RegistrationResponse();
        resposta.gameId = "jogo-1";
        resposta.status = "REGISTERED";
        resposta.shotsPerTurn = 3;
        resposta.message = "Jogo pronto";

        assertEquals(mapper.readTree("""
                {"gameId":"jogo-1","status":"REGISTERED",
                 "shotsPerTurn":3,"message":"Jogo pronto"}
                """), mapper.valueToTree(resposta));
    }

    @Test
    @DisplayName("Lê as posições da rajada na mesma ordem do JSON")
    void lePedidoDeTiros() throws Exception {
        Dtos.ShotRequest pedido = mapper.readValue("""
                {"shots":[{"row":"A","column":1},
                          {"row":"J","column":10},
                          {"row":"B","column":3}]}
                """, Dtos.ShotRequest.class);

        assertEquals(3, pedido.shots.size());
        assertEquals("A", pedido.shots.get(0).row);
        assertEquals(1, pedido.shots.get(0).column);
        assertEquals("J", pedido.shots.get(1).row);
        assertEquals(10, pedido.shots.get(1).column);
        assertEquals("B", pedido.shots.get(2).row);
        assertEquals(3, pedido.shots.get(2).column);
    }

    @Test
    @DisplayName("Lê o vencedor, os navios restantes e os resultados de tiro")
    void leRespostaDeTiros() throws Exception {
        Dtos.ShotResponse resposta = mapper.readValue("""
                {
                  "results":[
                    {"row":"B","column":3,"outcome":"MISS"},
                    {"row":"G","column":7,"outcome":"SUNK","shipType":"caravela"}
                  ],
                  "shipsRemaining":0,
                  "gameStatus":"GAME_OVER",
                  "winner":"AI_WINS"
                }
                """, Dtos.ShotResponse.class);

        assertEquals(0, resposta.shipsRemaining);
        assertEquals("GAME_OVER", resposta.gameStatus);
        assertEquals("AI_WINS", resposta.winner);
        assertEquals(2, resposta.results.size());

        Dtos.ShotResult falha = resposta.results.get(0);
        assertEquals("B", falha.row);
        assertEquals(3, falha.column);
        assertEquals("MISS", falha.outcome);
        assertNull(falha.shipType);

        Dtos.ShotResult afundado = resposta.results.get(1);
        assertEquals("G", afundado.row);
        assertEquals(7, afundado.column);
        assertEquals("SUNK", afundado.outcome);
        assertEquals("caravela", afundado.shipType);
    }

    @Test
    @DisplayName("Lê uma partida em curso sem vencedor")
    void lePartidaSemVencedor() throws Exception {
        Dtos.ShotResponse resposta = mapper.readValue("""
                {"results":[],"shipsRemaining":4,"gameStatus":"ONGOING"}
                """, Dtos.ShotResponse.class);

        assertTrue(resposta.results.isEmpty());
        assertEquals(4, resposta.shipsRemaining);
        assertEquals("ONGOING", resposta.gameStatus);
        assertNull(resposta.winner);
    }

    @Test
    @DisplayName("Serializa o texto da resposta de erro na propriedade error")
    void escreveRespostaDeErro() throws Exception {
        Dtos.ErrorResponse resposta = new Dtos.ErrorResponse("Jogo inexistente");

        assertEquals(mapper.readTree("""
                {"error":"Jogo inexistente"}
                """), mapper.valueToTree(resposta));
    }
}