package battleship;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqliteMoveRepositoryTest {
    @TempDir
    Path directory;

    @Test
    void savesResultsAndReadsThemFromANewRepository() {
        Path file = directory.resolve("test.db");
        SqliteMoveRepository repository = new SqliteMoveRepository(file);
        Game game = new Game(new Fleet(), repository);

        // Frota vazia: A1 e agua; o segundo A1 e repetido; K11 e exterior.
        game.fireShots(List.of(new Position('A', 1), new Position('A', 1), new Position('K', 11)));

        SqliteMoveRepository reopened = new SqliteMoveRepository(file);
        assertEquals(1, reopened.findGames().size());
        String gameId = reopened.findGames().getFirst().id();
        var shots = reopened.findHistory(gameId);
        assertEquals(3, shots.size());
        assertEquals(List.of("MISS", "REPEATED", "INVALID"),
                shots.stream().map(SqliteMoveRepository.SavedShot::outcome).toList());
        assertEquals(List.of(1, 2, 3),
                shots.stream().map(SqliteMoveRepository.SavedShot::shotOrder).toList());
        assertEquals("K", shots.get(2).row());
        assertEquals(11, shots.get(2).column());
    }

    @Test
    void separatesGamesAndDoesNotDuplicateMoves() {
        SqliteMoveRepository repository = new SqliteMoveRepository(directory.resolve("test.db"));
        Game first = new Game(new Fleet(), repository);
        Game second = new Game(new Fleet(), repository);
        first.fireShots(List.of(new Position('A', 1), new Position('B', 2), new Position('C', 3)));
        second.fireShots(List.of(new Position('D', 4), new Position('E', 5), new Position('F', 6)));

        var games = repository.findGames();
        assertEquals(2, games.size());
        for (var savedGame : games) {
            assertEquals(3, repository.findHistory(savedGame.id()).size());
            assertThrows(SqliteMoveRepository.PersistenceException.class,
                    () -> repository.saveMove(savedGame.id(), first.getAlienMoves().getFirst()));
            assertEquals(3, repository.findHistory(savedGame.id()).size());
        }
    }

    @Test
    void rollsBackMoveAndEarlierShotsWhenThirdInsertFails() throws Exception {
        Path file = directory.resolve("test.db");
        SqliteMoveRepository repository = new SqliteMoveRepository(file);
        String gameId = repository.createGame();
        Game game = new Game(new Fleet());
        game.fireShots(List.of(new Position('A', 1), new Position('B', 2), new Position('C', 3)));

        // Falha deliberada no terceiro INSERT para verificar a transacao inteira.
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath());
             var statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TRIGGER fail_third_shot BEFORE INSERT ON shots
                WHEN NEW.shot_order = 3
                BEGIN
                    SELECT RAISE(ABORT, 'Simulated write failure');
                END
                """);
        }
        assertThrows(SqliteMoveRepository.PersistenceException.class,
                () -> repository.saveMove(gameId, game.getAlienMoves().getFirst()));
        assertTrue(repository.findHistory(gameId).isEmpty());

        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath());
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM moves")) {
            assertTrue(result.next());
            assertEquals(0, result.getInt(1));
        }
    }
}
