package battleship;

import java.io.IOException;
import java.util.Scanner;
import java.util.Locale;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * The type Tasks.
 */
public class Tasks {
	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The constant GOODBYE_MESSAGE.
	 */
	private static final String GOODBYE_MESSAGE = "Bons ventos!";

	/**
	 * Strings to be used by the user
	 */
	private static final String AJUDA = "ajuda";
	private static final String GERAFROTA = "gerafrota";
	private static final String LEFROTA = "lefrota";
	private static final String DESISTIR = "desisto";
	private static final String RAJADA = "rajada";
	private static final String TIROS = "tiros";
	private static final String MAPA = "mapa";
	private static final String STATUS = "estado";
	private static final String SIMULA = "simula";
	private static final String EXPORTAR = "exportar";

	/**
	 * This task also tests the fighting element of a round of three shots
	 */
	public static void menu() {
		menu(BoardView.NONE);
	}

	/** Runs the existing simulation with an optional read-only board view. */
	public static void menu(BoardView boardView) {

		Path databaseFile = Path.of("data", "battleship.db");

		SqliteMoveRepository repository =
				new SqliteMoveRepository(databaseFile);

		System.out.println(
				"Base de dados: " + databaseFile.toAbsolutePath().normalize()
		);

		IFleet myFleet = null;
		IGame game = null;
		StopWatch moveTimer = new StopWatch();
		menuHelp();

		System.out.print("> ");
		Scanner in = new Scanner(System.in);
		String command = in.next();
		while (!command.equals(DESISTIR)) {

			switch (command) {
				case GERAFROTA:
					moveTimer.reset();
					myFleet = Fleet.createRandom();
					game = new Game(myFleet, repository);
					game.printMyBoard(false, true);
					boardView.update(game);
					break;
				case LEFROTA:
					moveTimer.reset();
					myFleet = buildFleet(in);
					game = new Game(myFleet, repository);
					game.printMyBoard(false, true);
					boardView.update(game);
					break;
				case STATUS:
					if (myFleet != null)
						myFleet.printStatus();
					break;
				case MAPA:
					if (myFleet != null)
						game.printMyBoard(false, true);
					break;
				case RAJADA:
					if (game != null) {
						if (game.getRemainingShips() == 0) {
							System.out.println("A partida terminou. Usa 'exportar' ou inicia uma nova frota.");
							break;
						}
						game.readEnemyFire(in);
						printMoveTime(moveTimer, game);
						myFleet.printStatus();
						game.printMyBoard(true, false);
						boardView.update(game);

						if (game.getRemainingShips() == 0) {
							game.over();
							System.out.println("Usa 'exportar' para guardar as rajadas desta partida.");
						}
					}
					break;
				case SIMULA:
					if (game != null) {
						if (game.getRemainingShips() == 0) {
							System.out.println("A partida terminou. Usa 'exportar' ou inicia uma nova frota.");
							break;
						}
						while (game.getRemainingShips() > 0){
							moveTimer.reset();
							moveTimer.start();
							game.randomEnemyFire();
							printMoveTime(moveTimer, game);
							myFleet.printStatus();
							game.printMyBoard(true, false);
							boardView.update(game);
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt(); // Best practice: restore interrupt status
							}
						}

						if (game.getRemainingShips() == 0) {
							game.over();
							System.out.println("Usa 'exportar' para guardar as rajadas desta partida.");
						}
					}
					break;
				case TIROS:
					if (game != null)
						game.printMyBoard(true, true);
					break;
				case "gui":
					if (boardView == BoardView.NONE)
						System.out.println("Inicia o modo gráfico com mvn javafx:run.");
					else
						boardView.open(game);
					break;
				case EXPORTAR:
					if (game == null || game.getAlienMoves().isEmpty()) {
						System.out.println("Ainda não existem rajadas para exportar.");
						break;
					}
					try {
						Path file = MoveCsvExporter.export(game.getAlienMoves(), Path.of("data", "exports"));
						System.out.println("Rajadas exportadas para: " + file.toAbsolutePath().normalize());
					} catch (IOException | IllegalArgumentException e) {
						System.err.println("Não foi possível exportar as rajadas: " + e.getMessage());
					}
					break;
				case AJUDA:
					menuHelp();
					break;
				case "jogos": {
					var savedGames = repository.findGames();

					if (savedGames.isEmpty()) {
						System.out.println("Ainda não existem jogos guardados.");
					} else {
						for (var savedGame : savedGames) {
							System.out.printf(
									"ID: %s | Início: %s%n",
									savedGame.id(),
									savedGame.startedAt()
							);
						}
					}

					break;
				}
				case "historico": {
					String requestedGameId = in.nextLine().trim();

					if (requestedGameId.isEmpty()) {
						System.out.println("Utilização: historico ID_DO_JOGO");
						break;
					}

					var history = repository.findHistory(requestedGameId);

					if (history.isEmpty()) {
						System.out.println(
								"Não existem jogadas para esse ID. Usa 'jogos' para verificar os IDs."
						);
					} else {
						for (var shot : history) {
							String shipInfo = shot.shipType() == null
									? ""
									: " — " + shot.shipType();

							System.out.printf(
									"Jogada %d | Tiro %d | %s%d | %s%s%n",
									shot.moveNumber(),
									shot.shotOrder(),
									shot.row(),
									shot.column(),
									shot.outcome(),
									shipInfo
							);
						}
					}

					break;
				}
				default:
					System.out.println("Que comando é esse??? Repete ...");
			}

			if (game != null && game.getRemainingShips() > 0 && !moveTimer.isStarted()) {
				moveTimer.reset();
				moveTimer.start();
			}
			System.out.print("> ");
			command = in.next();
		}
		System.out.println(GOODBYE_MESSAGE);
	}

	/**
	 * Apresenta a duração de uma rajada concluída, antes de mostrar o tabuleiro.
	 * A medição termina depois da leitura/geração e do processamento dos tiros.
	 * A duração é apresentada em segundos, com três casas decimais.
	 *
	 * @param moveTimer cronómetro em execução para a rajada concluída
	 * @param game partida cujo histórico contém a rajada acabada de processar
	 */
	private static void printMoveTime(StopWatch moveTimer, IGame game) {
		moveTimer.stop();
		int moveNumber = game.getAlienMoves().get(game.getAlienMoves().size() - 1).getNumber();
		System.out.printf(Locale.ROOT, "Tempo da jogada %d: %.3f s%n",
				moveNumber, moveTimer.getDuration().toMillis() / 1000.0);
	}

	/**
	 * This function provides help information about the menu commands.
	 */
	public static void menuHelp() {
		System.out.println("======================= AJUDA DO MENU =========================");
		System.out.println("Digite um dos comandos abaixo para interagir com o jogo:");
		System.out.println("- " + GERAFROTA + ": Gera uma frota aleatória de navios.");
		System.out.println("- " + LEFROTA + ": Permite criar e carregar uma frota personalizada.");
		System.out.println("- " + STATUS + ": Mostra o status atual da frota.)");
		System.out.println("- " + MAPA + ": Exibe o mapa da frota.");
		System.out.println("- " + RAJADA + ": Realiza uma rajada de disparos.");
		System.out.println("- " + SIMULA + ": Simula um jogo completo.");
		System.out.println("- " + TIROS + ": Lista os tiros válidos realizados (* = tiro em navio, o = tiro na água)");
		System.out.println("- " + EXPORTAR + ": Exporta as rajadas da partida atual para CSV.");
		System.out.println("- " + DESISTIR + ": Encerra o jogo.");
		System.out.println("- gui: Reabre o tabuleiro no modo gráfico (mvn javafx:run).");
		System.out.println("- jogos: Lista os jogos guardados.");
		System.out.println("- historico ID_DO_JOGO: Mostra as jogadas desse jogo.");
		System.out.println("===============================================================");
	}
	/**
	 * This operation allows the build up of a fleet, given user data
	 *
	 * @param in The scanner to read from
	 * @return The fleet that has been built
	 */
	public static Fleet buildFleet(Scanner in) {

		assert in != null;

		Fleet fleet = new Fleet();
		int i = 0; // i represents the total of successfully created ships
		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);
			if (s != null) {
				boolean success = fleet.addShip(s);
				if (success)
					i++;
				else
					LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
			} else {
				LOGGER.info("Navio desconhecido!");
			}
		}
		LOGGER.info("{} navios adicionados com sucesso!", i);
		return fleet;
	}

	/**
	 * This operation reads data about a ship, build it and returns it
	 *
	 * @param in The scanner to read from
	 * @return The created ship based on the data that has been read
	 */
	public static Ship readShip(Scanner in) {

		assert in != null;

		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);
		return Ship.buildShip(shipKind, bearing, pos);
	}

	/**
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The position that has been read
	 */
	public static Position readPosition(Scanner in) {

		assert in != null;

		int row = in.nextInt();
		int column = in.nextInt();
		return new Position(row, column);
	}

	/**
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The classic position that has been read
	 */
	public static IPosition readClassicPosition(@NotNull Scanner in) {
		// Verifica se ainda há tokens disponíveis
		if (!in.hasNext()) {
			throw new IllegalArgumentException("Nenhuma posição válida encontrada!");
		}

		String part1 = in.next(); // Primeiro token
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next(); // Segundo token, se disponível
		}

		String input = (part2 != null) ? part1 + part2 : part1;

		// Normalizar o input para tratar letras maiúsculas e minúsculas
		input = input.toUpperCase();

		// Verificar os dois formatos possíveis: compactos e com espaço
		if (input.matches("[A-Z]\\d+")) {
			char column = input.charAt(0); // Extrair a coluna
			int row = Integer.parseInt(input.substring(1)); // Extrair a linha
			return new Position(column, row);
		} else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
			char column = part1.charAt(0); // Extrair a coluna
			int row = Integer.parseInt(part2); // Extrair a linha
			return new Position(column, row);
		} else {
			throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
		}
	}

}
