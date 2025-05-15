package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class SimpleServer extends AbstractServer {

	private static final ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();
	private final String[][] Board = new String[3][3];
	private boolean Turn = true;

	public SimpleServer(int port) {
		super(port);
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		if (msg == null) return;

		String msgString = msg.toString();

		switch (msgString.split(" ")[0]) {
			case "#warning" -> handleWarning(client);
			case "add" -> handleAddClient(client);
			case "remove" -> handleRemoveClient(client);
			case "player" -> {
				if (msgString.startsWith("player joined")) handlePlayerJoined();
				else if (msgString.startsWith("player moved")) handleMove(msgString, client);
			}
		}
	}

	private void handleWarning(ConnectionToClient client) {
		try {
			client.sendToClient(new Warning("Warning from server!"));
			System.out.format("Sent warning to client %s%n", client.getInetAddress().getHostAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleAddClient(ConnectionToClient client) {
		SubscribersList.add(new SubscribedClient(client));
		try {
			client.sendToClient("client added successfully");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void handleRemoveClient(ConnectionToClient client) {
		Iterator<SubscribedClient> iterator = SubscribersList.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getClient().equals(client)) {
				iterator.remove();
				break;
			}
		}
	}

	private void handlePlayerJoined() {
		if (SubscribersList.size() == 2) {
			sendToAllClients("start game");
		}
	}

	private void handleMove(String msgString, ConnectionToClient client) {
		String[] parts = msgString.split(" ");
		if (parts.length < 4) return;

		int row = Integer.parseInt(parts[2]);
		int col = Integer.parseInt(parts[3]);

		if (!validMove(row, col, client)) return;

		String player = Turn ? "O" : "X";
		Board[row][col] = player;

		String moveMsg = row + " " + col + " " + player;
		sendToAllClients("update board " + moveMsg + " Turn " + (player.equals("O") ? "X" : "O"));

		if (winCheck()) {
			sendToAllClients("done " + moveMsg);
		} else if (fullBoard()) {
			sendToAllClients("over " + moveMsg);
		} else {
			Turn = !Turn;
		}
	}

	private boolean validMove(int row, int col, ConnectionToClient client) {
		if (Board[row][col] != null || SubscribersList.size() < 2) return false;
		String currentClientName = client.getName();
		String expectedClientName = Turn ? SubscribersList.get(0).getClient().getName()
				: SubscribersList.get(1).getClient().getName();
		return currentClientName.equals(expectedClientName);
	}

	private boolean fullBoard() {
		for (String[] row : Board) {
			for (String cell : row) {
				if (cell == null) return false;
			}
		}
		return true;
	}

	private boolean winCheck() {
		for (int i = 0; i < 3; i++) {
			if (isLineEqual(Board[i][0], Board[i][1], Board[i][2]) ||  // Row
					isLineEqual(Board[0][i], Board[1][i], Board[2][i])) {  // Column
				return true;
			}
		}
		return isLineEqual(Board[0][0], Board[1][1], Board[2][2]) ||  // Diagonal
				isLineEqual(Board[0][2], Board[1][1], Board[2][0]);    // Anti-diagonal
	}

	private boolean isLineEqual(String a, String b, String c) {
		return a != null && a.equals(b) && b.equals(c);
	}

	public void sendToAllClients(String message) {
		for (SubscribedClient subscribedClient : SubscribersList) {
			try {
				subscribedClient.getClient().sendToClient(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
