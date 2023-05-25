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
    //private Croupier game;

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
        while(true)
        {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String message = new String(packet.getData());
            System.out.println("Server > "+message);
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
    public void initializeWindow(String name) throws IOException {
        URL url_fxml = new File("src/main/resources/fxml/MainWindow.fxml").toURI().toURL();
        FXMLLoader loader = new FXMLLoader(url_fxml);
        Parent root = loader.load();
        SceneController controller = loader.getController();
        controller.setInformations(name);
        Scene scene = new Scene(root, 714, 441);
        scene.getStylesheets().add(getClass().getResource("/css/MainPage.css").toExternalForm());
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Texas holdem");
        primaryStage.setScene(scene);
        //primaryStage.setMaximized(true);
        //primaryStage.setFullScreen(true);
        primaryStage.show();
        //loginButton.getScene().getWindow().hide();
    }
}
