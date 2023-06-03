package org.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import net.GameServer;

public class serverController {

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

    public void stopServer(ActionEvent actionEvent) throws InterruptedException {
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
