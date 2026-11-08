package battleship;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Exports the shots and results of the current game to a CSV file. */
public final class MoveCsvExporter {
    private static final CSVFormat EXPORT_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("rajada", "linha", "coluna", "resultado")
            .get();

    private MoveCsvExporter() {
    }

    /**
     * Writes one row per shot, preserving the order of moves and shots.
     *
     * @param moves moves from the current game
     * @param directory directory in which to create a uniquely named CSV file
     * @return path of the created file
     * @throws IOException if the file cannot be created or written
     */
    public static Path export(List<IMove> moves, Path directory) throws IOException {
        if (moves == null || moves.isEmpty()) {
            throw new IllegalArgumentException("Não existem rajadas para exportar.");
        }

        Files.createDirectories(directory);
        Path stagingFile = Files.createTempFile(directory, "rajadas-", ".tmp");
        String stagingName = stagingFile.getFileName().toString();
        Path csvFile = stagingFile.resolveSibling(stagingName.replace(".tmp", ".csv"));
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(stagingFile, StandardCharsets.UTF_8);
                 CSVPrinter printer = new CSVPrinter(writer, EXPORT_FORMAT)) {
                writeMoves(moves, printer);
            }
            return Files.move(stagingFile, csvFile);
        } catch (IOException | RuntimeException | Error failure) {
            try {
                Files.deleteIfExists(stagingFile);
            } catch (IOException | RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    private static void writeMoves(List<IMove> moves, CSVPrinter printer) throws IOException {
        for (IMove move : moves) {
            List<IPosition> shots = move.getShots();
            List<IGame.ShotResult> results = move.getShotResults();
            if (shots.size() != results.size()) {
                throw new IllegalArgumentException("A rajada não tem um resultado por tiro.");
            }
            for (int i = 0; i < shots.size(); i++) {
                IPosition shot = shots.get(i);
                printer.printRecord(move.getNumber(), shot.getClassicRow(),
                        shot.getClassicColumn(), formatOutcome(results.get(i)));
            }
        }
    }

    private static String formatOutcome(IGame.ShotResult result) {
        if (!result.valid()) return "INVALID";
        if (result.repeated()) return "REPEATED";
        if (result.ship() == null) return "MISS";
        if (result.sunk()) return "SUNK";
        return "HIT";
    }
}
