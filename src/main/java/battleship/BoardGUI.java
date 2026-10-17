package battleship;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/** Read-only board: all game actions remain in the existing console. */
public final class BoardGUI implements BoardView {
    private static final int CELL_SIZE = 38;
    private final Rectangle[][] cells = new Rectangle[Game.BOARD_SIZE][Game.BOARD_SIZE];
    private final Label[][] symbols = new Label[Game.BOARD_SIZE][Game.BOARD_SIZE];
    private Stage stage;
    private Label status;

    @Override
    public void update(IGame game) {
        // Never send the mutable fleet or history to the JavaFX thread.
        BoardSnapshot snapshot = BoardSnapshot.capture(game);
        Platform.runLater(() -> render(snapshot));
    }

    @Override
    public void open(IGame game) {
        BoardSnapshot snapshot = game == null ? null : BoardSnapshot.capture(game);
        Platform.runLater(() -> {
            render(snapshot);
            stage.show();
            stage.toFront();
        });
    }

    private void createWindow() {
        stage = new Stage();
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(2);
        grid.setVgap(2);
        for (int col = 0; col < Game.BOARD_SIZE; col++) {
            Label heading = new Label(String.valueOf(col + 1));
            heading.setMinWidth(CELL_SIZE);
            heading.setAlignment(Pos.CENTER);
            grid.add(heading, col + 1, 0);
        }
        for (int row = 0; row < Game.BOARD_SIZE; row++) {
            grid.add(new Label(String.valueOf((char) ('A' + row))), 0, row + 1);
            for (int col = 0; col < Game.BOARD_SIZE; col++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE, Color.LIGHTSKYBLUE);
                cell.setStroke(Color.WHITE);
                Label symbol = new Label();
                symbol.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
                cells[row][col] = cell;
                symbols[row][col] = symbol;
                grid.add(new StackPane(cell, symbol), col + 1, row + 1);
            }
        }
        Label title = new Label("Tabuleiro da Batalha Naval");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        status = new Label();
        Label legend = new Label("■ Navio (azul-escuro)   × Acerto (vermelho)\n"
                + "○ Água atingida (cinzento)   · Água (azul-claro)");
        Label instructions = new Label("Usa os comandos na consola. Para reabrir esta janela: gui");
        VBox root = new VBox(12, title, status, grid, legend, instructions);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        stage.setTitle("Batalha Naval — Simulação");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
    }

    private void render(BoardSnapshot snapshot) {
        if (stage == null) createWindow();
        status.setText(snapshot == null ? "Começa na consola com gerafrota ou lefrota."
                : "Rajadas: " + snapshot.moves() + "  |  Navios restantes: " + snapshot.remainingShips()
                + (snapshot.remainingShips() == 0 ? "  |  Partida terminada" : ""));
        for (int row = 0; row < Game.BOARD_SIZE; row++) {
            for (int col = 0; col < Game.BOARD_SIZE; col++) {
                BoardSnapshot.Cell state = snapshot == null ? BoardSnapshot.Cell.WATER : snapshot.cell(row, col);
                cells[row][col].setFill(switch (state) {
                    case WATER -> Color.LIGHTSKYBLUE;
                    case SHIP -> Color.STEELBLUE;
                    case HIT -> Color.CRIMSON;
                    case MISS -> Color.LIGHTGRAY;
                });
                symbols[row][col].setText(switch (state) {
                    case WATER -> "·";
                    case SHIP -> "■";
                    case HIT -> "×";
                    case MISS -> "○";
                });
                symbols[row][col].setTextFill(state == BoardSnapshot.Cell.HIT
                        || state == BoardSnapshot.Cell.SHIP ? Color.WHITE : Color.BLACK);
            }
        }
    }
}
