package battleship;

import javafx.application.Platform;

/** Starts JavaFX once, while console input and simulation stay on the main thread. */
public final class GuiMain {
    private GuiMain() {}

    public static void main(String[] args) {
        Platform.startup(() -> Platform.setImplicitExit(false));
        try {
            BoardGUI board = new BoardGUI();
            board.open(null);
            Tasks.menu(board);
        } finally {
            // Also closes the GUI if console input or persistence fails.
            Platform.exit();
        }
    }
}
