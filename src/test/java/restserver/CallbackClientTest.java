package restserver;

import battleship.IGame;
import battleship.IMove;
import battleship.IPosition;
import battleship.Position;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CallbackClientTest {

    private record Pedido(String metodo, String caminho, String contentType, String corpo) {}

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<IPosition> tiros = List.of(
            new Position('B', 3),
            new Position('G', 7),
            new Position('D', 10)
    );

    private HttpServer servidor;
    private volatile Pedido pedido;

    private GameSession prepararResposta(int status, String corpo) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", exchange -> {
            try (exchange) {
                pedido = new Pedido(
                        exchange.getRequestMethod(),
                        exchange.getRequestURI().getPath(),
                        exchange.getRequestHeaders().getFirst("Content-Type"),
                        new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)
                );
                byte[] resposta = corpo.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(status, resposta.length);
                exchange.getResponseBody().write(resposta);
            }
        });
        servidor.start();

        String url = "http://127.0.0.1:" + servidor.getAddress().getPort();
        return new GameSession("teste-131056", "Jogador de teste", url);
    }

    @AfterEach
    void pararServidor() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    @Test
    @DisplayName("Envia os tiros em JSON e regista os resultados de uma partida em curso")
    void enviaTirosERegistaJogada() throws Exception {
        GameSession sessao = prepararResposta(200, """
                {
                  "results": [
                    {"row":"B","column":3,"outcome":"INVALID"},
                    {"row":"G","column":7,"outcome":"REPEATED"},
                    {"row":"D","column":10,"outcome":"SUNK","shipType":"barca"}
                  ],
                  "shipsRemaining": 2,
                  "gameStatus": "ONGOING"
                }
                """);

        assertTrue(new CallbackClient().sendShots(sessao, tiros));

        assertNotNull(pedido);
        assertEquals("POST", pedido.metodo());
        assertEquals("/game/teste-131056/shots", pedido.caminho());
        assertEquals("application/json", pedido.contentType());

        JsonNode esperado = mapper.readTree("""
                {"shots":[{"row":"B","column":3},
                          {"row":"G","column":7},
                          {"row":"D","column":10}]}
                """);
        assertEquals(esperado, mapper.readTree(pedido.corpo()));

        List<IMove> jogadas = sessao.getGame().getAlienMoves();
        assertEquals(1, jogadas.size());
        IMove jogada = jogadas.getFirst();
        assertEquals(1, jogada.getNumber());
        assertEquals(tiros, jogada.getShots());
        assertEquals(List.of(
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, false, null, true)
        ), jogada.getShotResults());
        assertFalse(sessao.isGameOver());
        assertNull(sessao.getWinner());
    }

    @Test
    @DisplayName("Marca vitória da IA quando ficam zero navios")
    void marcaVitoriaComZeroNavios() throws IOException {
        GameSession sessao = prepararResposta(200, """
                {"results":[],"shipsRemaining":0,"gameStatus":"ONGOING"}
                """);

        assertTrue(new CallbackClient().sendShots(sessao, tiros));
        assertTrue(sessao.isGameOver());
        assertEquals("AI_WINS", sessao.getWinner());
    }

    @Test
    @DisplayName("Marca vitória da IA quando recebe GAME_OVER com navios restantes")
    void marcaVitoriaComGameOver() throws IOException {
        GameSession sessao = prepararResposta(200, """
                {"results":[],"shipsRemaining":1,"gameStatus":"GAME_OVER"}
                """);

        assertTrue(new CallbackClient().sendShots(sessao, tiros));
        assertTrue(sessao.isGameOver());
        assertEquals("AI_WINS", sessao.getWinner());
    }

    @Test
    @DisplayName("Aceita uma resposta sem resultados e deixa o histórico vazio")
    void trataResultadosNulos() throws IOException {
        GameSession sessao = prepararResposta(200, """
                {"results":null,"shipsRemaining":2,"gameStatus":"ONGOING"}
                """);

        assertTrue(new CallbackClient().sendShots(sessao, tiros));
        assertTrue(sessao.getGame().getAlienMoves().isEmpty());
        assertFalse(sessao.isGameOver());
    }

    @Test
    @DisplayName("Devolve falso quando recebe HTTP 400")
    void rejeitaHttp400() throws IOException {
        GameSession sessao = prepararResposta(400, """
                {"error":"Pedido inválido"}
                """);

        assertFalse(new CallbackClient().sendShots(sessao, tiros));
        assertNotNull(pedido);
        assertTrue(sessao.getGame().getAlienMoves().isEmpty());
        assertFalse(sessao.isGameOver());
    }

    @Test
    @DisplayName("Devolve falso quando recebe HTTP 200 com JSON inválido")
    void rejeitaJsonInvalido() throws IOException {
        GameSession sessao = prepararResposta(200, "{");

        assertFalse(new CallbackClient().sendShots(sessao, tiros));
        assertTrue(sessao.getGame().getAlienMoves().isEmpty());
        assertFalse(sessao.isGameOver());
    }
}