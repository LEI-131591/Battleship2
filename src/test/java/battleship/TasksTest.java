package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link Tasks}.
 */
class TasksTest {

	private static final String VALID_FLEET_INPUT = """
			caravela 0 0 s
			galeao 0 3 o
			barca 0 9 n
			fragata 8 6 e
			nau 9 0 e
			caravela 5 2 o
			nau 3 7 n
			barca 3 1 s
			barca 3 5 e
			caravela 7 0 e
			barca 7 4 o
			""";

	private static final String WINNING_RAJADAS_FOR_VALID_FLEET = """
			rajada A 1 B 1 A 4
			rajada B 3 B 4 B 5
			rajada C 4 A 10 I 7
			rajada I 8 I 9 I 10
			rajada J 1 J 2 J 3
			rajada F 3 F 4 D 8
			rajada E 8 F 8 D 2
			rajada D 6 H 1 H 2
			rajada H 5 A 2 A 3
			""";

	private InputStream originalIn;
	private PrintStream originalOut;
	private PrintStream originalErr;

	@BeforeEach
	void setUp() {
		originalIn = System.in;
		originalOut = System.out;
		originalErr = System.err;
	}

	@AfterEach
	void tearDown() {
		System.setIn(originalIn);
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	@Test
	@DisplayName("Permite instanciar a classe Tasks e apresenta o menu de ajuda")
	void instantiatesTasksAndPrintsMenuHelp() {
		assertNotNull(new Tasks());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));

		Tasks.menuHelp();

		String output = out.toString(StandardCharsets.UTF_8);
		assertTrue(output.contains("AJUDA DO MENU"));
		assertTrue(output.contains("gerafrota"));
		assertTrue(output.contains("exportar"));
	}

	@Test
	@DisplayName("Lê posições numéricas e posições clássicas em formato compacto, separado ou inválido")
	void readPositionAndReadClassicPositionCoverAllBranches() {
		Position pos = Tasks.readPosition(new Scanner("4 7"));
		assertEquals(4, pos.getRow());
		assertEquals(7, pos.getColumn());

		IPosition compact = Tasks.readClassicPosition(new Scanner("b5"));
		assertEquals('B', compact.getClassicRow());
		assertEquals(5, compact.getClassicColumn());

		IPosition separated = Tasks.readClassicPosition(new Scanner("C 10"));
		assertEquals('C', separated.getClassicRow());
		assertEquals(10, separated.getClassicColumn());

		IPosition compactWithTrailingToken = Tasks.readClassicPosition(new Scanner("D4 extra"));
		assertEquals('D', compactWithTrailingToken.getClassicRow());
		assertEquals(4, compactWithTrailingToken.getClassicColumn());

		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("")));
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("123")));
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("AB 3")));
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("A B")));
		assertThrows(AssertionError.class, () -> Tasks.readPosition(null));
		assertThrows(AssertionError.class, () -> Tasks.readShip(null));
		assertThrows(AssertionError.class, () -> Tasks.buildFleet(null));
	}

	@Test
	@DisplayName("Lê navios conhecidos e desconhecidos e constrói uma frota completa ignorando falhas")
	void readShipAndBuildFleetHandleValidInvalidAndCollidingShips() {
		Ship known = Tasks.readShip(new Scanner("barca 2 3 n"));
		assertNotNull(known);
		assertEquals("Barca", known.getCategory());

		Ship unknown = Tasks.readShip(new Scanner("submarino 2 3 n"));
		assertNull(unknown);

		// Inclui um navio desconhecido e um navio sobreposto antes dos 11 navios válidos
		String fleetWithRetries = "submarino 0 0 n\ncaravela 0 0 s\ncaravela 0 0 s\n" +
				VALID_FLEET_INPUT.substring(VALID_FLEET_INPUT.indexOf('\n') + 1);
		Fleet fleet = Tasks.buildFleet(new Scanner(fleetWithRetries));
		assertNotNull(fleet);
		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size());
	}

	@Test
	@DisplayName("Executa menu() por omissão e trata comandos antes e durante uma partida com vitória por rajadas")
	void menuDefaultAndInteractiveCommandsUntilVictory() throws IOException {
		java.nio.file.Files.deleteIfExists(Path.of("data", "battleship.db"));

		Fleet parsedFleet = Tasks.buildFleet(new Scanner(VALID_FLEET_INPUT));
		List<IPosition> allOccupied = new ArrayList<>();
		for (IShip ship : parsedFleet.getShips()) {
			allOccupied.addAll(ship.getPositions());
		}
		while (allOccupied.size() % Game.NUMBER_SHOTS != 0) {
			allOccupied.add(new Position(0, 1));
		}
		StringBuilder rajadas = new StringBuilder();
		for (int i = 0; i < allOccupied.size(); i += Game.NUMBER_SHOTS) {
			rajadas.append("rajada ")
					.append(allOccupied.get(i).getClassicRow()).append(' ').append(allOccupied.get(i).getClassicColumn()).append(' ')
					.append(allOccupied.get(i + 1).getClassicRow()).append(' ').append(allOccupied.get(i + 1).getClassicColumn()).append(' ')
					.append(allOccupied.get(i + 2).getClassicRow()).append(' ').append(allOccupied.get(i + 2).getClassicColumn()).append('\n');
		}

		String input =
				// Comandos antes de existir frota/jogo (ramos game == null, myFleet == null e jogos vazio)
				"jogos\nestado\nmapa\nrajada\nsimula\ntiros\nexportar\ngui\najuda\ncomandoinvalido\n" +
				"historico\nhistorico id-inexistente-124708\n" +
				// Carregar frota personalizada, testar exportar sem rajadas e comandos com frota ativa
				"lefrota\n" + VALID_FLEET_INPUT +
				"exportar\nestado\nmapa\ntiros\n" +
				// Afundar toda a frota com rajadas, testar medição de tempo e exportar CSV
				rajadas +
				"rajada\nsimula\nexportar\njogos\ndesisto\n";

		System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));

		Tasks.menu();

		String console = out.toString(StandardCharsets.UTF_8);
		assertTrue(console.contains("Que comando é esse??? Repete ..."));
		assertTrue(console.contains("Utilização: historico ID_DO_JOGO"));
		assertTrue(console.contains("Ainda não existem rajadas para exportar."));
		assertTrue(console.contains("Tempo da jogada 1:"));
		assertTrue(console.contains("A partida terminou. Usa 'exportar' ou inicia uma nova frota."));
		assertTrue(console.contains("Rajadas exportadas para:"));
		assertTrue(console.contains("Bons ventos!"));
	}

	@Test
	@DisplayName("Executa gerafrota, gui com BoardView personalizado, historico com resultados e erro de exportação")
	void menuCoversGerafrotaCustomBoardViewHistoryAndExportFailure() {
		SqliteMoveRepository repo = new SqliteMoveRepository(Path.of("data", "battleship.db"));
		IFleet randomFleet = Fleet.createRandom();
		Game seededGame = new Game(randomFleet, repo);
		seededGame.randomEnemyFire();
		String existingGameId = repo.findGames().getFirst().id();

		AtomicBoolean openedGui = new AtomicBoolean(false);
		BoardView customView = new BoardView() {
			@Override
			public void open(IGame game) {
				openedGui.set(true);
			}
		};

		String input = "gerafrota\ngui\nrajada A 1 B 2 C 3\nexportar\nhistorico " + existingGameId + "\ndesisto\n";
		System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
		System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));

		try (MockedStatic<MoveCsvExporter> mockedExporter = Mockito.mockStatic(MoveCsvExporter.class)) {
			mockedExporter.when(() -> MoveCsvExporter.export(Mockito.anyList(), Mockito.any(Path.class)))
					.thenThrow(new IOException("falha simulada no disco"));

			Tasks.menu(customView);
		}

		assertTrue(openedGui.get());
		assertTrue(out.toString(StandardCharsets.UTF_8).contains("Jogada 1 | Tiro 1"));
		assertTrue(err.toString(StandardCharsets.UTF_8).contains("Não foi possível exportar as rajadas: falha simulada no disco"));
	}

	@Test
	@DisplayName("Simula partida completa com interrupção de espera durante a simulação")
	void menuSimulaHandlesInterruptedSleepAndFinishesGame() {
		String input = "gerafrota\nsimula\ndesisto\n";
		System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));

		Thread testThread = Thread.currentThread();
		Thread interrupter = new Thread(() -> {
			try {
				Thread.sleep(3200);
			} catch (InterruptedException e) {
				return;
			}
			while (!Thread.currentThread().isInterrupted()) {
				testThread.interrupt();
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					break;
				}
			}
		});
		interrupter.setDaemon(true);
		interrupter.start();
		try {
			Tasks.menu(BoardView.NONE);
		} finally {
			interrupter.interrupt();
			Thread.interrupted(); // Limpa o estado de interrupção no fim do teste
		}

		String console = out.toString(StandardCharsets.UTF_8);
		assertTrue(console.contains("Usa 'exportar' para guardar as rajadas desta partida."));
	}
}
