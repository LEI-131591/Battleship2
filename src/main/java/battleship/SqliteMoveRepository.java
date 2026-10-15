package battleship;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Guarda jogos, rajadas e resultados dos tiros num ficheiro SQLite. */
public final class SqliteMoveRepository {
    private final String url;

    public record SavedGame(String id, String startedAt) {}

    public record SavedShot(int moveNumber, String playedAt, int shotOrder,
                            String row, int column, String outcome, String shipType) {}

    public static final class PersistenceException extends RuntimeException {
        private PersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** Cria a pasta e as tabelas, preservando os dados existentes. */
    public SqliteMoveRepository(Path file) {
        Path absoluteFile = file.toAbsolutePath().normalize();
        try {
            Files.createDirectories(absoluteFile.getParent());
        } catch (IOException e) {
            throw new PersistenceException("Nao foi possivel criar a pasta da base de dados.", e);
        }
        this.url = "jdbc:sqlite:" + absoluteFile;
        initialize();
    }

    private Connection open() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeError) {
                e.addSuppressed(closeError);
            }
            throw e;
        }
        return connection;
    }

    private void initialize() {
        try (Connection connection = open();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS games (
                    id TEXT PRIMARY KEY,
                    started_at TEXT NOT NULL
                )
                """);
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS moves (
                    id INTEGER PRIMARY KEY,
                    game_id TEXT NOT NULL REFERENCES games(id),
                    move_number INTEGER NOT NULL,
                    played_at TEXT NOT NULL,
                    UNIQUE (game_id, move_number)
                )
                """);
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS shots (
                    id INTEGER PRIMARY KEY,
                    move_id INTEGER NOT NULL REFERENCES moves(id),
                    shot_order INTEGER NOT NULL CHECK (shot_order BETWEEN 1 AND 3),
                    row_label TEXT NOT NULL,
                    column_number INTEGER NOT NULL,
                    outcome TEXT NOT NULL CHECK (
                        outcome IN ('INVALID', 'REPEATED', 'MISS', 'HIT', 'SUNK')
                    ),
                    ship_type TEXT,
                    UNIQUE (move_id, shot_order)
                )
                """);
        } catch (SQLException e) {
            throw new PersistenceException("Nao foi possivel inicializar a base de dados.", e);
        }
    }

    public String createGame() {
        String id = UUID.randomUUID().toString();
        String sql = "INSERT INTO games (id, started_at) VALUES (?, ?)";
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, Instant.now().toString());
            statement.executeUpdate();
            return id;
        } catch (SQLException e) {
            throw new PersistenceException("Nao foi possivel guardar o novo jogo.", e);
        }
    }

    /** A rajada e os tres tiros sao gravados na mesma transacao. */
    public void saveMove(String gameId, IMove move) {
        if (move.getShots().size() != 3 || move.getShotResults().size() != 3) {
            throw new IllegalArgumentException("Uma jogada deve conter tres tiros e tres resultados.");
        }
        String insertMove = """
            INSERT INTO moves (game_id, move_number, played_at) VALUES (?, ?, ?)
            """;
        String insertShot = """
            INSERT INTO shots
                (move_id, shot_order, row_label, column_number, outcome, ship_type)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = open()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(insertMove)) {
                    statement.setString(1, gameId);
                    statement.setInt(2, move.getNumber());
                    statement.setString(3, Instant.now().toString());
                    statement.executeUpdate();
                }

                // O ID e obtido na mesma ligacao em que a jogada foi inserida.
                long moveId;
                try (Statement statement = connection.createStatement();
                     ResultSet result = statement.executeQuery("SELECT last_insert_rowid()")) {
                    if (!result.next()) {
                        throw new SQLException("Nao foi devolvido o identificador da jogada.");
                    }
                    moveId = result.getLong(1);
                }

                try (PreparedStatement statement = connection.prepareStatement(insertShot)) {
                    for (int i = 0; i < 3; i++) {
                        IPosition position = move.getShots().get(i);
                        IGame.ShotResult result = move.getShotResults().get(i);
                        statement.setLong(1, moveId);
                        statement.setInt(2, i + 1);
                        statement.setString(3, String.valueOf(position.getClassicRow()));
                        statement.setInt(4, position.getClassicColumn());
                        statement.setString(5, outcome(result));
                        if (result.ship() == null) {
                            statement.setNull(6, Types.VARCHAR);
                        } else {
                            statement.setString(6, result.ship().getCategory());
                        }
                        statement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw new PersistenceException("Nao foi possivel guardar a jogada.", e);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Erro na ligacao a base de dados.", e);
        }
    }

    private static String outcome(IGame.ShotResult result) {
        if (!result.valid()) return "INVALID";
        if (result.repeated()) return "REPEATED";
        if (result.ship() == null) return "MISS";
        if (result.sunk()) return "SUNK";
        return "HIT";
    }

    public List<SavedGame> findGames() {
        List<SavedGame> games = new ArrayList<>();
        String sql = "SELECT id, started_at FROM games ORDER BY started_at, id";
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                games.add(new SavedGame(result.getString("id"), result.getString("started_at")));
            }
            return games;
        } catch (SQLException e) {
            throw new PersistenceException("Nao foi possivel consultar os jogos.", e);
        }
    }

    public List<SavedShot> findHistory(String gameId) {
        List<SavedShot> shots = new ArrayList<>();
        String sql = """
            SELECT m.move_number, m.played_at, s.shot_order, s.row_label,
                   s.column_number, s.outcome, s.ship_type
            FROM moves m
            JOIN shots s ON s.move_id = m.id
            WHERE m.game_id = ?
            ORDER BY m.move_number, s.shot_order
            """;
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, gameId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    shots.add(new SavedShot(
                            result.getInt("move_number"), result.getString("played_at"),
                            result.getInt("shot_order"), result.getString("row_label"),
                            result.getInt("column_number"), result.getString("outcome"),
                            result.getString("ship_type")
                    ));
                }
            }
            return shots;
        } catch (SQLException e) {
            throw new PersistenceException("Nao foi possivel consultar o historico.", e);
        }
    }
}
