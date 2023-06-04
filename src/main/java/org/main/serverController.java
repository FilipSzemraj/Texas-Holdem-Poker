package org.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.GameClient;
import net.GameServer;

import java.sql.SQLException;
import java.util.Iterator;

public class serverController {

    public AnchorPane wholeScene;
    GameServer gameServer;
    Thread serverThread;
    public Button stopServerButton;
    public Button runServerButton;
    private boolean isRunning=false;
    public serverController()
    {
        gameServer = GameServer.getInstance();
        serverThread = new Thread(gameServer);
    }
    /*public void initialize()
    {
        Stage stage = (Stage) wholeScene.getScene().getWindow();
        stage.setOnCloseRequest((WindowEvent event) ->{
            Iterator<GameClient> iterator = LoginController.Players.iterator();
            while(iterator.hasNext()) {
                GameClient player = iterator.next();
                if(player.getPlayerId() == playerId)
                {
                    player.closeRunningFlag();
                    player.closeTheSocket();
                    //LoginController.closePlayerSocket(player.getPlayerId());
                    iterator.remove();
                    break;
                }
            }

            stage.close();
        });
    }*/

    @FXML
    void runServer(ActionEvent actionEvent)
    {
        if(!isRunning) {
            serverThread.start();
            isRunning=true;
            runServerButton.setDisable(true);
            stopServerButton.setDisable(false);
        }
        else {
            System.out.println("Serwer zostal juz uruchomiony");
        }
    }

    public void stopServer(ActionEvent actionEvent) throws InterruptedException, SQLException {
        if(isRunning) {
            gameServer.closeRunningFlag();
            gameServer.closeTheSocket();
            Stage stage = (Stage) stopServerButton.getScene().getWindow();
            serverThread.join();
            stage.close();
        }
        else {
            System.out.println("Serwer nie zostal uruchomiony.");
        }
    }
}
