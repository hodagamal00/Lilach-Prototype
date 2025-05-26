package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;

public class SimpleServer extends AbstractServer {
	private static ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();
	private String[][] Board = new String[3][3];
	private boolean T = true;
	public SimpleServer(int port) {
		super(port);

	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		String msgString = msg.toString();
		if (msgString.startsWith("#warning")) {
			Warning warning = new Warning("Warning from server!");
			try {
				client.sendToClient(warning);
				System.out.format("Sent warning to client %s\n", client.getInetAddress().getHostAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(msgString.startsWith("add client")){
			SubscribedClient connection = new SubscribedClient(client);
			SubscribersList.add(connection);
			try {
				client.sendToClient("client added successfully");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else if(msgString.startsWith("remove client")){
			if(!SubscribersList.isEmpty()){
				for(SubscribedClient subscribedClient: SubscribersList){
					if(subscribedClient.getClient().equals(client)){
						SubscribersList.remove(subscribedClient);
						break;
					}
				}
			}
		}
		//
		else if (msgString.startsWith("player joined")) {
			if (SubscribersList.size() == 2) {
				try {
					SubscribersList.get(0).getClient().sendToClient("you are O");
					SubscribersList.get(1).getClient().sendToClient("you are X");
				} catch (IOException e) {
					e.printStackTrace();
				}
				sendToAllClients("start game");
			}
		}
		else if (msgString.startsWith("player moved ")) {
			handleMove(msgString, client);
		}
		else if (msgString.equals("restart game")) {
			Board = new String[3][3];  // אפס את הלוח
			T = true; // שחקן ראשון מתחיל
			sendToAllClients("restart");
		}

	}

	//saves the move and checks if someone won/  game over
	private void handleMove(String msgString, ConnectionToClient client) {
		//get the moves indexes
		String[] parts = msgString.split(" ");
		int row = Integer.parseInt(parts[2]);
		int col = Integer.parseInt(parts[3]);
        //first, update the board
		String player = T ? "O" : "X";
		String moveMsg = row + " " + col + " " + player;

		//check that player can actually make a move
		if(!validMove(row, col, client)){
			return;
		}

		Board[row][col] = player;
		sendToAllClients("update board " + moveMsg + " Turn " + (player.equals("O") ? "X" : "O"));
		if (winCheck()) {
			sendToAllClients("done " + moveMsg);
			return;
		}
		if (fullBoard()) {
			sendToAllClients("over " + moveMsg);
			return;
		}
		//game still on: flip turn
		T = !T;
	}
	private boolean fullBoard() {
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(Board[i][j] == null){
					return false;
				}
			}
		}
		return true;
	}

	private boolean winCheck() {
		// Check rows and columns
		for (int i = 0; i < 3; i++) {
			if (isLineEqual(Board[i][0], Board[i][1], Board[i][2]) || // Row
					isLineEqual(Board[0][i], Board[1][i], Board[2][i])) { // Column
				return true;
			}
		}
		// Check diagonals
		return isLineEqual(Board[0][0], Board[1][1], Board[2][2]) || // Main diagonal
				isLineEqual(Board[0][2], Board[1][1], Board[2][0]);   // Anti-diagonal
	}
	//HELPER FUNCTION FOR WINCHECK
	private boolean isLineEqual(String a, String b, String c) {
		return a != null && b != null && c != null && a.equals(b) && b.equals(c);
	}

	// ANOTHER HELPER FUNC
	private boolean validMove(int row, int col, ConnectionToClient client) {
		if(Board[row][col] != null){
			return false;
		}
		if(T){
			return client.getName().equals(SubscribersList.get(0).getClient().getName());
		}else {
			return client.getName().equals(SubscribersList.get(1).getClient().getName());
		}
	}


	//help update all clients on the current status(new game/ new move/ someone won/ game over)

	public void sendToAllClients(String message) {
		try {
			for (SubscribedClient subscribedClient : SubscribersList) {
				subscribedClient.getClient().sendToClient(message);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
