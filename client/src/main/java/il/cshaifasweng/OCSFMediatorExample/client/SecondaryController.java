package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class SecondaryController {
    @FXML
    private Button button00;

    @FXML
    private Button button01;

    @FXML
    private Button button02;

    @FXML
    private Button button10;

    @FXML
    private Button button11;

    @FXML
    private Button button12;

    @FXML
    private Label turnLabel;

    @FXML
    private Button button20;

    @FXML
    private Button button21;

    @FXML
    private Button button22;

    @FXML
    private Label gameStatus;

    private String[][] Board = new String[3][3];
    private final Button[][] buttons = new Button[3][3];
    public SecondaryController() {
        // Register to EventBus
    }
    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);
        buttons[0][0] = button00;
        buttons[0][1] = button01;
        buttons[0][2] = button02;
        buttons[1][0] = button10;
        buttons[1][1] = button11;
        buttons[1][2] = button12;
        buttons[2][0] = button20;
        buttons[2][1] = button21;
        buttons[2][2] = button22;
    }
    private void resetBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Board[i][j] = null;
                if (buttons[i][j] != null) {
                    buttons[i][j].setText(""); // איפוס טקסט הכפתור
                }
            }
        }
        gameStatus.setText("Game restarted. Waiting for moves...");
    }

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
    @FXML
    private void clicked(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (clickedButton == buttons[row][col]) {
                    try {
                        SimpleClient.getClient().sendToServer("player moved " + row + " " + col);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }
    }
    @FXML
    public void restartGame(ActionEvent event) {
        try {
            SimpleClient.getClient().sendToServer("restart game");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Restart button clicked");

    }



    @Subscribe
    public void handleMessage(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof String) {
                String message = msg.toString();
                if (message.equalsIgnoreCase("restart")) {
                    resetBoard();
                    return;
                }
                gameStatus.setText(message);
            } else if (msg instanceof Object[]) {
                Object[] update = (Object[]) msg;
                int row = (int) update[0];
                int col = (int) update[1];
                String move = (String) update[2];
                String nextTurn = (String) update[3];  // "X" or "O"

                // עדכון הודעת התור
                String mySymbol = SimpleClient.getClient().getPlayerSymbol();
                if (mySymbol.equals(nextTurn)) {
                    turnLabel.setText("Your turn");
                } else {
                    turnLabel.setText("Wait for the other player...");
                }

                // עדכון הלוח
                gameStatus.setText("Player: " + nextTurn);
                Board[row][col] = move;
                Button button = buttons[row][col];
                if (button != null) {
                    button.setText(move);
                }
            }
        });
    }

}
