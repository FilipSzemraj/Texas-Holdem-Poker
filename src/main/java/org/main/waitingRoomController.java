package org.main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.GameClient;

import java.util.Iterator;

public class waitingRoomController {
    private final Object waitingRoomObject = new Object();
    public TableColumn<String, String> playerId_column;
    public TableColumn<String, String> waiting_column;
    public TableColumn<String, String> inGame_column;
    public TableView<String> tableWithAllPlayers;
    /*public void setWaitingRoom(String[] partedMessage)
    {
        //"waitingRoomReceive-"+playerId+"-playersInGame-0-waitingPlayers-0-"
        int playersInGame = Integer.valueOf(partedMessage[3]);
        ObservableList<String> data = FXCollections.observableArrayList();
        if(playersInGame>0)
        {
            for (int i = 0; i < playersInGame; i++) {
                String player = partedMessage[6+i];
                data.add(player);
            }
        }
        int playersInWaitingRoom = Integer.valueOf(partedMessage[5]);
        if(playersInWaitingRoom>0)
        {
            for(int i=0;i<playersInWaitingRoom;i++)
            {
                String waitingPlayer = partedMessage[6+playersInGame+i];
                data.add(waitingPlayer);
            }
        }
        tableWithAllPlayers.setItems(data);
    }
    public void exitWaitingRoom()
    {
        isRunning=true;
        synchronized (waitingRoomObject)
        {
            waitingRoomObject.notifyAll();
        }
    }
    private void waitingRoom() throws InterruptedException {
        do {
            boolean wasMessageSend = false;
            Iterator<GameClient> iterator = LoginController.Players.iterator();
            while (iterator.hasNext()) {
                GameClient player = iterator.next();
                if (player.getPlayerId() == playerId) {
                    String message = "playerReceive-waitingRoom-";
                    player.sendData(message.getBytes());
                    wasMessageSend = true;
                    break;
                }
            }
            if (wasMessageSend) {
                synchronized (waitingRoomObject) {
                    waitingRoomObject.wait(5000);
                }
            }
        }while(!isRunning);
    }
    Thread waitingRoom = new Thread(() -> {
        try {
            waitingRoom();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    });
        waitingRoom.start();

        waitingRoom.join();*/
}
