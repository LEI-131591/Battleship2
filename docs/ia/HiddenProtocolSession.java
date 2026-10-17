import battleship.*;
import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Adapter for protocol tests. Reveals no fleet coordinates before game over. */
public final class HiddenProtocolSession {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IFleet fleet = Fleet.createRandom();
        List<Map<String,Object>> initial = new ArrayList<>();
        for (IShip ship : fleet.getShips()) {
            Map<String,Object> item = new LinkedHashMap<>();
            item.put("type",ship.getCategory());
            item.put("positions",ship.getPositions().stream().map(p ->
                String.valueOf(p.getClassicRow())+p.getClassicColumn()).sorted().toList());
            initial.add(item);
        }
        String initialJson = mapper.writeValueAsString(initial);
        String salt = UUID.randomUUID().toString();
        String commitment = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest((salt+"\n"+initialJson).getBytes(StandardCharsets.UTF_8)));
        Game game = new Game(fleet);
        System.out.println("FLEET_COMMITMENT_SHA256="+commitment);
        System.out.println("READY: send a JSON array of three shots on one line.");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in,StandardCharsets.UTF_8));
        String line;
        while ((line=input.readLine()) != null) {
            if (line.equals("QUIT")) break;
            if (line.isBlank()) continue;
            JsonNode request = mapper.readTree(line);
            if (!request.isArray() || request.size()!=3) throw new IllegalArgumentException("Expected three shots");
            List<IPosition> shots = new ArrayList<>();
            for (JsonNode point : request) {
                shots.add(new Position(point.get("row").asText().charAt(0),point.get("column").asInt()));
            }
            PrintStream output = System.out;
            String response;
            try (PrintStream capture = new PrintStream(new ByteArrayOutputStream(),true,StandardCharsets.UTF_8)) {
                System.setOut(capture);
                try {
                    game.fireShots(shots);
                    response = game.getAlienMoves().getLast().processEnemyFire(false);
                } finally {
                    System.setOut(output);
                }
            }
            System.out.println("RESPONSE="+mapper.writeValueAsString(mapper.readTree(response)));
            System.out.println("REMAINING_SHIPS="+game.getRemainingShips());
            if (game.getRemainingShips()==0) {
                System.out.println("GAME_OVER");
                System.out.println("REVEAL_SALT="+salt);
                System.out.println("REVEAL_FLEET="+initialJson);
                return;
            }
        }
    }
}
