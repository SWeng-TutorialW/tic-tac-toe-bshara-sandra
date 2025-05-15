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

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }

    @FXML
    private void clicked(ActionEvent event) {
        Button button = (Button) event.getSource();
        String buttonId = button.getId();
        int row = Character.getNumericValue(buttonId.charAt(6));
        int col = Character.getNumericValue(buttonId.charAt(7));
        try {
            SimpleClient.getClient().sendToServer("player moved " + row + " " + col);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void handleMessage(Object msg) {
        Platform.runLater(() -> {

            //game over / winner case
            if (msg instanceof String) {
                gameStatus.setText(msg.toString());

                //switch turns
            } else if (msg instanceof Object[]) {
                Object[] update = (Object[]) msg;
                int row = (int) update[0];
                int col = (int) update[1];
                String move = (String) update[2];

                //update the label
                gameStatus.setText("Player : " + update[3].toString());

                //update the button's value
                Board[row][col] = move;
                Button button = buttons[row][col];
                //only change if the button is free
                if (button != null) {
                    button.setText(move);
                }
            }
        });
    }
}