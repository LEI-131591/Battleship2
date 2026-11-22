package restserver;

import battleship.IGame;
import battleship.IPosition;
import battleship.IShip;
import battleship.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import restserver.Dtos.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link GameController}.
 */
class GameControllerTest {

	private GameRegistry registry;
	private CallbackClient callbackClient;
	private AiShotStrategy aiStrategy;
	private GameController controller;

	@BeforeEach
	void setUp() {
		registry = Mockito.mock(GameRegistry.class);
		callbackClient = Mockito.mock(CallbackClient.class);
		aiStrategy = Mockito.mock(AiShotStrategy.class);
		controller = new GameController(registry, callbackClient, aiStrategy);
	}

	private static ShotPosition shot(String row, int column) {
		ShotPosition sp = new ShotPosition();
		sp.row = row;
		sp.column = column;
		return sp;
	}

	@Test
	@DisplayName("Rejeita registo quando playerName é nulo ou em branco")
	void registerRejectsMissingPlayerName() {
		RegistrationRequest nullName = new RegistrationRequest();
		nullName.playerName = null;
		nullName.callbackUrl = "http://127.0.0.1:9090";

		ResponseEntity<?> respNull = controller.register(nullName);
		assertEquals(HttpStatus.BAD_REQUEST, respNull.getStatusCode());
		assertEquals("playerName is required", ((ErrorResponse) respNull.getBody()).error);

		RegistrationRequest blankName = new RegistrationRequest();
		blankName.playerName = "   ";
		blankName.callbackUrl = "http://127.0.0.1:9090";

		ResponseEntity<?> respBlank = controller.register(blankName);
		assertEquals(HttpStatus.BAD_REQUEST, respBlank.getStatusCode());
		assertEquals("playerName is required", ((ErrorResponse) respBlank.getBody()).error);
	}

	@Test
	@DisplayName("Rejeita registo quando callbackUrl é nulo ou em branco")
	void registerRejectsMissingCallbackUrl() {
		RegistrationRequest nullUrl = new RegistrationRequest();
		nullUrl.playerName = "Alice";
		nullUrl.callbackUrl = null;

		ResponseEntity<?> respNull = controller.register(nullUrl);
		assertEquals(HttpStatus.BAD_REQUEST, respNull.getStatusCode());
		assertEquals("callbackUrl is required", ((ErrorResponse) respNull.getBody()).error);

		RegistrationRequest blankUrl = new RegistrationRequest();
		blankUrl.playerName = "Alice";
		blankUrl.callbackUrl = "  ";

		ResponseEntity<?> respBlank = controller.register(blankUrl);
		assertEquals(HttpStatus.BAD_REQUEST, respBlank.getStatusCode());
		assertEquals("callbackUrl is required", ((ErrorResponse) respBlank.getBody()).error);
	}

	@Test
	@DisplayName("Regista jogador válido e devolve dados iniciais da sessão")
	void registerCreatesSessionSuccessfully() {
		GameSession session = new GameSession("game-124708", "Alice", "http://127.0.0.1:9090");
		Mockito.when(registry.createSession("Alice", "http://127.0.0.1:9090")).thenReturn(session);

		RegistrationRequest req = new RegistrationRequest();
		req.playerName = "Alice";
		req.callbackUrl = "http://127.0.0.1:9090";

		ResponseEntity<?> resp = controller.register(req);
		assertEquals(HttpStatus.OK, resp.getStatusCode());
		RegistrationResponse body = assertInstanceOf(RegistrationResponse.class, resp.getBody());
		assertEquals("game-124708", body.gameId);
		assertEquals("REGISTERED", body.status);
		assertEquals(3, body.shotsPerTurn);
		assertTrue(body.message.contains("Alice"));
	}

	@Test
	@DisplayName("Devolve 404 quando a sessão não existe e 410 quando já terminou")
	void receiveShotsHandlesMissingAndFinishedSessions() {
		ShotRequest req = new ShotRequest();
		req.shots = List.of(shot("A", 1), shot("B", 2), shot("C", 3));

		Mockito.when(registry.getSession("inexistente")).thenReturn(null);
		ResponseEntity<?> notFound = controller.receiveShots("inexistente", req);
		assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
		assertTrue(((ErrorResponse) notFound.getBody()).error.contains("inexistente"));

		GameSession overSession = new GameSession("terminado", "Alice", "http://127.0.0.1:9090");
		overSession.markAiWins();
		Mockito.when(registry.getSession("terminado")).thenReturn(overSession);
		ResponseEntity<?> gone = controller.receiveShots("terminado", req);
		assertEquals(HttpStatus.GONE, gone.getStatusCode());
		assertTrue(((ErrorResponse) gone.getBody()).error.contains("AI_WINS"));
	}

	@Test
	@DisplayName("Rejeita rajadas com lista de tiros nula, tamanho incorreto ou coordenadas inválidas")
	void receiveShotsValidatesShotListAndPositions() {
		GameSession session = new GameSession("val-1", "Alice", "http://127.0.0.1:9090");
		Mockito.when(registry.getSession("val-1")).thenReturn(session);

		ShotRequest nullShots = new ShotRequest();
		nullShots.shots = null;
		assertEquals(HttpStatus.BAD_REQUEST, controller.receiveShots("val-1", nullShots).getStatusCode());

		ShotRequest wrongSize = new ShotRequest();
		wrongSize.shots = List.of(shot("A", 1), shot("B", 2));
		assertEquals(HttpStatus.BAD_REQUEST, controller.receiveShots("val-1", wrongSize).getStatusCode());

		List<ShotPosition> invalidPositions = List.of(
				shot(null, 5),
				shot("  ", 5),
				shot("A", 0),
				shot("A", 11)
		);
		for (ShotPosition bad : invalidPositions) {
			ShotRequest badReq = new ShotRequest();
			badReq.shots = List.of(bad, shot("B", 2), shot("C", 3));
			ResponseEntity<?> resp = controller.receiveShots("val-1", badReq);
			assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
			assertTrue(((ErrorResponse) resp.getBody()).error.startsWith("Invalid position:"));
		}
	}

	@Test
	@DisplayName("Avalia todos os tipos de resultado de tiro e trata sucesso e falha do callback")
	void receiveShotsEvaluatesAllOutcomesAndHandlesCallback() {
		GameSession session = Mockito.mock(GameSession.class);
		IGame game = Mockito.mock(IGame.class);
		IShip ship = Mockito.mock(IShip.class);
		Mockito.when(ship.getCategory()).thenReturn("barca");

		Mockito.when(registry.getSession("game-1")).thenReturn(session);
		Mockito.when(session.isGameOver()).thenReturn(false);
		Mockito.when(session.getGame()).thenReturn(game);
		Mockito.when(session.getCallbackUrl()).thenReturn("http://127.0.0.1:9090");
		Mockito.when(game.getRemainingShips()).thenReturn(4);

		// 1ª rajada: INVALID, REPEATED (mesma posição repetida na rajada) e MISS
		Mockito.when(game.fireSingleShot(Mockito.any(), Mockito.anyBoolean()))
				.thenReturn(
						new IGame.ShotResult(false, false, null, false),
						new IGame.ShotResult(true, true, null, false),
						new IGame.ShotResult(true, false, null, false)
				);

		List<IPosition> aiShots = List.of(new Position('A', 1), new Position('A', 2), new Position('A', 3));
		Mockito.when(aiStrategy.computeShots(game)).thenReturn(aiShots);
		Mockito.when(callbackClient.sendShots(session, aiShots)).thenReturn(true);

		ShotRequest req1 = new ShotRequest();
		req1.shots = List.of(shot("a", 1), shot("A", 1), shot("B", 2));
		ResponseEntity<?> resp1 = controller.receiveShots("game-1", req1);
		assertEquals(HttpStatus.OK, resp1.getStatusCode());
		ShotResponse body1 = assertInstanceOf(ShotResponse.class, resp1.getBody());
		assertEquals("ONGOING", body1.gameStatus);
		assertNull(body1.winner);
		assertEquals("INVALID", body1.results.get(0).outcome);
		assertEquals("REPEATED", body1.results.get(1).outcome);
		assertEquals("MISS", body1.results.get(2).outcome);

		// 2ª rajada: HIT, SUNK, MISS e callback a falhar (devolve false)
		Mockito.when(game.fireSingleShot(Mockito.any(), Mockito.anyBoolean()))
				.thenReturn(new IGame.ShotResult(true, false, ship, false))
				.thenReturn(new IGame.ShotResult(true, false, ship, true))
				.thenReturn(new IGame.ShotResult(true, false, null, false));
		Mockito.when(callbackClient.sendShots(session, aiShots)).thenReturn(false);

		ShotRequest req2 = new ShotRequest();
		req2.shots = List.of(shot("C", 1), shot("D", 2), shot("E", 3));
		ResponseEntity<?> resp2 = controller.receiveShots("game-1", req2);
		assertEquals(HttpStatus.OK, resp2.getStatusCode());
		ShotResponse body2 = assertInstanceOf(ShotResponse.class, resp2.getBody());
		assertEquals("HIT", body2.results.get(0).outcome);
		assertEquals("barca", body2.results.get(0).shipType);
		assertEquals("SUNK", body2.results.get(1).outcome);
		assertEquals("barca", body2.results.get(1).shipType);
		assertEquals("MISS", body2.results.get(2).outcome);
	}

	@Test
	@DisplayName("Declara vitória do estudante quando não restam navios na frota da IA")
	void receiveShotsDeclaresStudentVictoryWhenZeroShipsRemain() {
		GameSession session = Mockito.mock(GameSession.class);
		IGame game = Mockito.mock(IGame.class);
		IShip ship = Mockito.mock(IShip.class);
		Mockito.when(ship.getCategory()).thenReturn("galeao");

		Mockito.when(registry.getSession("win-1")).thenReturn(session);
		Mockito.when(session.isGameOver()).thenReturn(false);
		Mockito.when(session.getGame()).thenReturn(game);
		Mockito.when(session.getPlayerName()).thenReturn("Alice");
		Mockito.when(game.getRemainingShips()).thenReturn(0);
		Mockito.when(game.fireSingleShot(Mockito.any(), Mockito.anyBoolean()))
				.thenReturn(new IGame.ShotResult(true, false, ship, true));

		ShotRequest req = new ShotRequest();
		req.shots = new ArrayList<>(List.of(shot("A", 1), shot("A", 2), shot("A", 3)));

		ResponseEntity<?> resp = controller.receiveShots("win-1", req);
		assertEquals(HttpStatus.OK, resp.getStatusCode());
		ShotResponse body = assertInstanceOf(ShotResponse.class, resp.getBody());
		assertEquals("GAME_OVER", body.gameStatus);
		assertEquals("STUDENT_WINS", body.winner);
		assertEquals(0, body.shipsRemaining);
		Mockito.verify(session).markStudentWins();
		Mockito.verifyNoInteractions(aiStrategy, callbackClient);
	}
}
