package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PrimaryController {

	private static final BooleanProperty waitingForPlayer = new SimpleBooleanProperty(false);

	@FXML
	private Label waitingLabel;

	@FXML
	void sendWarning(ActionEvent event) {
		try {
			SimpleClient.getClient().sendToServer("#warning");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void setWaitingForPlayer(boolean value) {
		waitingForPlayer.set(value);
	}

	@FXML
	public static void switchToSecondary() throws IOException {
		App.setRoot("secondary");
	}

	@FXML
	private void startClient(){
		try {
			SimpleClient.getClient().sendToServer("player joined");
			setWaitingForPlayer(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	void initialize(){
		waitingLabel.visibleProperty().bind(waitingForPlayer);
		try {
			SimpleClient.getClient().sendToServer("add client");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
