package battleship;

/** Presentation hook; the console-only game does not load JavaFX. */
public interface BoardView {
    BoardView NONE = new BoardView() {};

    default void update(IGame game) {}
    default void open(IGame game) {}
}
