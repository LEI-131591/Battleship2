package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ResourceLock(Resources.SYSTEM_OUT)
class MoveTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final IPosition POSITION = new Position('A', 1);
    private static final IShip BARGE = new Barge(Compass.NORTH, POSITION);
    private static final IGame.ShotResult OUTSIDE = new IGame.ShotResult(false, false, null, false);
    private static final IGame.ShotResult REPEATED = new IGame.ShotResult(true, true, null, false);
    private static final IGame.ShotResult MISS = new IGame.ShotResult(true, false, null, false);
    private static final IGame.ShotResult HIT = new IGame.ShotResult(true, false, BARGE, false);
    private static final IGame.ShotResult SUNK = new IGame.ShotResult(true, false, BARGE, true);

    @Test
    @DisplayName("Expõe o número, os tiros e os resultados da rajada")
    void exposesMoveDetails() {
        Move move = move(MISS, REPEATED, OUTSIDE);

        assertEquals(7, move.getNumber());
        assertEquals(List.of(POSITION, POSITION, POSITION), move.getShots());
        assertEquals(List.of(MISS, REPEATED, OUTSIDE), move.getShotResults());
        assertEquals("Move{number=7, shots=3, results=3}", move.toString());
    }

    @Test
    @DisplayName("Conta tiros na água, repetidos e exteriores separadamente")
    void countsMissesRepeatsAndOutsideShots() throws Exception {
        Summary summary = summarize(false, MISS, REPEATED, OUTSIDE);

        assertCounts(summary.json(), 1, 1, 1, 1);
        assertTrue(summary.json().get("sunkBoats").isEmpty());
        assertTrue(summary.json().get("hitsOnBoats").isEmpty());
        assertFalse(summary.output().contains("Jogada nº"));
    }

    @Test
    @DisplayName("Agrupa navios afundados e exclui os acertos dessa categoria")
    void groupsSunkShipsAndOmitsTheirHits() throws Exception {
        Summary summary = summarize(true, SUNK, SUNK, HIT);

        assertCounts(summary.json(), 3, 0, 0, 0);
        JsonNode sunk = summary.json().get("sunkBoats");
        assertEquals(1, sunk.size());
        assertEquals(BARGE.getCategory(), sunk.get(0).get("type").asText());
        assertEquals(2, sunk.get(0).get("count").asInt());
        assertTrue(summary.json().get("hitsOnBoats").isEmpty());
        assertTrue(summary.output().contains("2 " + BARGE.getCategory() + "s ao fundo"));
    }

    @Test
    @DisplayName("Agrupa acertos em navios a flutuar e imprime o tiro na água")
    void groupsHitsWithoutSinking() throws Exception {
        Summary summary = summarize(true, HIT, HIT, MISS);

        JsonNode hits = summary.json().get("hitsOnBoats");
        assertEquals(1, hits.size());
        assertEquals(BARGE.getCategory(), hits.get(0).get("type").asText());
        assertEquals(2, hits.get(0).get("hits").asInt());
        assertTrue(summary.output().contains("2 tiros num(a) " + BARGE.getCategory() + " + 1 tiro na água"));
    }

    @Test
    @DisplayName("Apresenta formas singular e plural nos resumos da consola")
    void displaysSingularAndPluralCounts() throws Exception {
        assertTrue(summarize(true, REPEATED, OUTSIDE, OUTSIDE).output()
                .contains("1 tiro repetido, 2 tiros exteriores"));
        assertTrue(summarize(true, REPEATED, REPEATED, OUTSIDE).output()
                .contains("2 tiros repetidos, 1 tiro exterior"));
        assertTrue(summarize(true, OUTSIDE, OUTSIDE, OUTSIDE).output()
                .contains("Jogada nº7 -> 3 tiros exteriores"));
        assertTrue(summarize(true, MISS, MISS, REPEATED).output()
                .contains("2 tiros válidos: 2 tiros na água, 1 tiro repetido"));
        assertTrue(summarize(true, MISS, REPEATED, REPEATED).output()
                .contains("1 tiro válido: 1 tiro na água, 2 tiros repetidos"));
        assertTrue(summarize(true, HIT, REPEATED, OUTSIDE).output()
                .contains("1 tiro num(a) " + BARGE.getCategory()));
        assertTrue(summarize(true, SUNK, MISS, OUTSIDE).output()
                .contains("1 " + BARGE.getCategory() + " ao fundo"));
    }

    private static void assertCounts(JsonNode json, int valid, int repeated, int outside, int missed) {
        assertAll(
                () -> assertEquals(valid, json.get("validShots").asInt()),
                () -> assertEquals(repeated, json.get("repeatedShots").asInt()),
                () -> assertEquals(outside, json.get("outsideShots").asInt()),
                () -> assertEquals(missed, json.get("missedShots").asInt()));
    }

    private static Move move(IGame.ShotResult... results) {
        return new Move(7, List.of(POSITION, POSITION, POSITION), List.of(results));
    }

    private static Summary summarize(boolean verbose, IGame.ShotResult... results) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            String json = move(results).processEnemyFire(verbose);
            assertTrue(output.toString(StandardCharsets.UTF_8).contains(json));
            return new Summary(JSON.readTree(json), output.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(original);
        }
    }

    private record Summary(JsonNode json, String output) {}
}
