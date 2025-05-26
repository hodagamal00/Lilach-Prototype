package il.cshaifasweng.OCSFMediatorExample.client;

import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;

public class SimpleClient extends AbstractClient {
	private String playerSymbol = ""; // "O" or "X"
	public void setPlayerSymbol(String symbol) {
		this.playerSymbol = symbol;
	}
	public String getPlayerSymbol() {
		return playerSymbol;
	}

	private static SimpleClient client = null;


	private SimpleClient(String host, int port) {
		super(host, port);

	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		if (msg.getClass().equals(Warning.class)) {
			EventBus.getDefault().post(new WarningEvent((Warning) msg));
		}
		else
		{

			String message = msg.toString();
			//System.out.println(message);
			//case1: two players are present => start the game
			if (message != null && message.startsWith("start game")) {
				javafx.application.Platform.runLater(() -> {
					try {
						PrimaryController.setWaitingForPlayer(false);
						PrimaryController.switchToSecondary();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

				//case2: one player made a move => use server to update it to all clients
			} else if (message != null && message.startsWith("update")) {
				String[] strArray = message.split(" ");
				int row = Integer.parseInt(strArray[2]);
				int col = Integer.parseInt(strArray[3]);
				EventBus.getDefault().post(new Object[]{row, col, strArray[4], strArray[6]});


				//case3: one of the players won according to the server => update everyone with a msg
			} else if (message != null && message.startsWith("done")) {
				String[] strArray = message.split(" ");
				EventBus.getDefault().post("Winner : " + strArray[3]);

				//case4: the game ended in a tie
			} else if (message != null && message.startsWith("over")) {
				EventBus.getDefault().post("Game Ended");
			}
			else if (message != null && message.equals("restart")) {
				System.out.println("Restart message received from server");
				EventBus.getDefault().post("restart");
			}
		    else if (message.startsWith("you are")) {
				String  symbol  = message.split(" ")[2];
				setPlayerSymbol(symbol);

			}
		}
	}


	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
		}
		return client;
	}

}
