package battleship;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveCsvExporterTest {
    @TempDir
    Path directory;

    @Test
    void exportsEveryShotInMoveOrderWithItsOutcome() throws IOException {
        IShip barge = new Barge(Compass.NORTH, new Position('A', 1));
        IMove first = new Move(1,
                List.of(new Position('A', 1), new Position('B', 2), new Position('K', 11)),
                List.of(new IGame.ShotResult(true, false, null, false),
                        new IGame.ShotResult(true, false, barge, true),
                        new IGame.ShotResult(false, false, null, false)));
        IMove second = new Move(2,
                List.of(new Position('A', 1), new Position('C', 3), new Position('D', 4)),
                List.of(new IGame.ShotResult(true, true, null, false),
                        new IGame.ShotResult(true, false, barge, false),
                        new IGame.ShotResult(true, false, null, false)));

        Path file = MoveCsvExporter.export(List.of(first, second), directory.resolve("exports"));

        assertEquals(List.of(
                "rajada,linha,coluna,resultado",
                "1,A,1,MISS",
                "1,B,2,SUNK",
                "1,K,11,INVALID",
                "2,A,1,REPEATED",
                "2,C,3,HIT",
                "2,D,4,MISS"), Files.readAllLines(file));
        assertNotEquals(file, MoveCsvExporter.export(List.of(first), directory.resolve("exports")));
    }

    @Test
    void rejectsAnEmptyHistoryWithoutCreatingAFile() {
        Path exports = directory.resolve("exports");

        assertThrows(IllegalArgumentException.class,
                () -> MoveCsvExporter.export(List.of(), exports));
        assertFalse(Files.exists(exports));
    }
}
