package net;

import Game.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.main.SceneController;

import java.io.File;
import java.io.IOException;
import java.net.*;

public class GameClient extends Thread{

    private InetAddress ipAddress;
    private DatagramSocket socket;
    SceneController controller;
    public int playerId;
    public String playerNick;
    private boolean runningFlag=true;

    public GameClient(String ipAddress)
    {
        //this.game = game;
        try {
            this.socket = new DatagramSocket();
            this.ipAddress = InetAddress.getByName(ipAddress);
        } catch (SocketException | UnknownHostException e) {
            //e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void run()
    {
        System.out.println("RUN GAMECLIENT:"+Thread.currentThread().getName());
        while(runningFlag)
        {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                //e.printStackTrace();
                //throw new RuntimeException(e);
            }
            String message = new String(packet.getData()).trim();
            System.out.println("Server > "+message);
            String[] partedMessage = message.split("-");
            switch(partedMessage[0])
            {
                case "playerAction":
                    if(Integer.valueOf(partedMessage[1])==playerId)
                    {
                        controller.enableButtonEventHandling();
                        Runnable disableButtonsTask = () -> {
                            synchronized (Croupier.getInstance().waitForMessage)
                            {
                                try {
                                    Croupier.getInstance().waitForMessage.wait(30000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            controller.disableButtonEventHandling();
                        };
                        Thread disableButtonsThread = new Thread(disableButtonsTask);
                        disableButtonsThread.start();
                    }
                    break;
                case "croupierInformations":
                    break;
                case "initializeInformations":
                    //"initializeInformations-numberOfPlayers-"+numberOfPlayers"-playerId-"+playersHand[i].playerId+"-playerName-"+playersHand[i].playerName+"-amountOfMoney-"+playersHand[i].amountOfMoney+"-"
                    controller.setOtherPlayersInterfaces(Integer.valueOf(partedMessage[3]));
                    controller.setPlayerInformations(Integer.valueOf(partedMessage[3]), partedMessage);
                    break;
                case "playerInformations":
                    break;
                case "newPlayer":
                    break;
            }
        }
    }
    public void sendData(byte[] data)
    {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 1331);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void closeRunningFlag()
    {
        sendData(("playerAction-logout-playerId-"+playerId+"-playerNick-"+playerNick+"-ipAddress-"+ipAddress.getHostAddress()+"-").getBytes());
        runningFlag=false;
    }
    public void closeTheSocket()
    {
        socket.close();
    }

    public void initializeWindow(String name, int Id, int amountOfMoney) throws IOException {
        System.out.println("initialize window, dla:"+name+" "+Thread.currentThread().getName());
        playerId=Id;
        playerNick=name;
        URL url_fxml = new File("src/main/resources/fxml/MainWindow.fxml").toURI().toURL();
        FXMLLoader loader = new FXMLLoader(url_fxml);
        Parent root = loader.load();
        controller = loader.getController();
        Scene scene = new Scene(root, 714, 441);
        scene.getStylesheets().add(getClass().getResource("/css/MainPage.css").toExternalForm());
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Texas holdem");
        primaryStage.setScene(scene);
        primaryStage.show();
        controller.initialize(loader.getLocation(), loader.getResources(), name, Id, amountOfMoney);
    }
    public int getPlayerId()
    {
        return playerId;
    }
}
