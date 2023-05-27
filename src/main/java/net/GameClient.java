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
            String message = new String(packet.getData());
            System.out.println("Server > "+message);
        }
    }
    public void closeRunningFlag()
    {
        runningFlag=false;
    }
    public void closeTheSocket()
    {
        socket.close();
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
    public void initializeWindow(String name, int Id, int amountOfMoney) throws IOException {
        playerId=Id;
        playerNick=name;
        URL url_fxml = new File("src/main/resources/fxml/MainWindow.fxml").toURI().toURL();
        FXMLLoader loader = new FXMLLoader(url_fxml);
        Parent root = loader.load();
        SceneController controller = loader.getController();
        Scene scene = new Scene(root, 714, 441);
        scene.getStylesheets().add(getClass().getResource("/css/MainPage.css").toExternalForm());
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Texas holdem");
        primaryStage.setScene(scene);
        primaryStage.show();
        controller.initialize(loader.getLocation(), loader.getResources(), name, Id, amountOfMoney);
    }
}
