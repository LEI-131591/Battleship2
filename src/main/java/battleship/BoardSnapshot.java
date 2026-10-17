package battleship;

import java.util.ArrayList;
import java.util.List;

/** Immutable visual state captured on the same thread that processes moves. */
public record BoardSnapshot(List<Cell> cells, int moves, int remainingShips) {
    public enum Cell { WATER, SHIP, HIT, MISS }

    public BoardSnapshot {
        cells = List.copyOf(cells);
        if (cells.size() != Game.BOARD_SIZE * Game.BOARD_SIZE)
            throw new IllegalArgumentException("Invalid board size");
    }

    public Cell cell(int row, int column) {
        return cells.get(row * Game.BOARD_SIZE + column);
    }

    public static BoardSnapshot capture(IGame game) {
        List<Cell> cells = new ArrayList<>();
        for (int row = 0; row < Game.BOARD_SIZE; row++) {
            for (int col = 0; col < Game.BOARD_SIZE; col++) {
                cells.add(game.getMyFleet().shipAt(new Position(row, col)) == null
                        ? Cell.WATER : Cell.SHIP);
            }
        }
        for (IMove move : game.getAlienMoves()) {
            for (int i = 0; i < move.getShots().size(); i++) {
                IPosition shot = move.getShots().get(i);
                IGame.ShotResult result = move.getShotResults().get(i);
                if (shot.isInside() && result.valid() && !result.repeated()) {
                    cells.set(shot.getRow() * Game.BOARD_SIZE + shot.getColumn(),
                            result.ship() == null ? Cell.MISS : Cell.HIT);
                }
            }
        }
        return new BoardSnapshot(cells, game.getAlienMoves().size(), game.getRemainingShips());
    }
}
