package org.main;

import Game.Croupier;
import javafx.stage.Modality;
import net.GameClient;
import net.GameServer;
import sql.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class LoginController {

    public Button registerButton;
    @FXML
    private Button loginButton, cancelButton;
    @FXML
    Label messageLabel;
    @FXML
    TextField loginTextField, passwordTextField;
    DatabaseConnection connectNow;
    Connection connectDB;
    private InetAddress serverAddress;
    private DatagramSocket socket;
    private int serverPort;
    public static volatile List<GameClient> Players;

    public LoginController() throws SocketException, UnknownHostException {
    /*try {
        connectNow = new DatabaseConnection();
        connectDB = connectNow.getConnection();
    }catch(Exception e)
    {
        e.printStackTrace();
        messageLabel.setText("Bład połączenia z baza danych!");
    }*/
        socket = new DatagramSocket();
        serverAddress = InetAddress.getByName("127.0.0.1");
        serverPort = 1331;
        Players = new ArrayList<>();
    }

    public void sendRequest(String message) throws Exception {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
        socket.send(sendPacket);
    }

    public String receiveResponse() throws Exception {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
        return response;
    }

    public static int returnIndexOfPlayerById(int value) {
        int i = 0;
        for (GameClient client : Players) {
            if (client.playerId == value) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public void logged(String money, int Id) throws IOException, RuntimeException, SQLException, InterruptedException {

        int amountOfMoney = Integer.valueOf(money);
        String serverIp = "127.0.0.1"; // Adres IP serwera
        Players.add(new GameClient(serverIp));
        Thread newPlayer=new Thread(Players.get(Players.size()-1));


        System.out.println("LOGGED DLA:" + Players.get(Players.size() - 1) + Thread.currentThread().getName());
        Players.get(Players.size() - 1).initializeWindow(loginTextField.getText(), Id, amountOfMoney);
        newPlayer.start();

    }

    public static void closePlayerSocket(int id) {
        Players.get(id).closeTheSocket();
    }

    public static void deletePlayer(int id) {
        Iterator<GameClient> iterator = Players.iterator();
        while (iterator.hasNext()) {
            GameClient player = iterator.next();
            if (player.playerId == id) {
                iterator.remove();
                break;
            }
        }

    }


    public void loginButtonOnAction() throws Exception {
        if (loginTextField.getText().isBlank() == false && passwordTextField.getText().isBlank() == false) {
            validateLogin();
        } else
            messageLabel.setText("Oba pola muszą być uzupełnione");
    }

    public void cancelButtonOnAction(ActionEvent event) throws InterruptedException, UnknownHostException, SocketException {
        //gameServer.closeRunningFlag();
        //gameServer.closeTheSocket();
        socket.close();
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        //serverThread.join();
        stage.close();
    }

    public void validateLogin() throws Exception {

        String messageToServer = "serverAction-validateLogin-user-" + loginTextField.getText() + "-password-" + passwordTextField.getText() + "-";
        sendRequest(messageToServer);
        String response="";
        do{
        response = receiveResponse();
        }while(response.equals(""));
        String[] partedResponse = response.split("-");
        if (partedResponse[0].equals("correctData")) {
            //"correctData-amountOfMoney-"+amountOfMoney+"-playerId-"+playerId+"-"
            logged(partedResponse[2], Integer.valueOf(partedResponse[4]));
        }
        else if(partedResponse[0].equals("wrongData")) {
            messageLabel.setText("Nie poprawne dane.");
        }
    }

    public void registerButtonOnAction(ActionEvent actionEvent) throws IOException {
        Stage registerStage = new Stage();
        registerStage.setTitle("Rejestracja");

        // Utworzenie FXMLLoadera i załadowanie widoku dla nowego okna
        URL url_fxml = new File("src/main/resources/fxml/registerWindow.fxml").toURI().toURL();
        FXMLLoader fxmlLoader = new FXMLLoader(url_fxml);
        Parent root = fxmlLoader.load();
        registerController registerController = fxmlLoader.getController();
        registerController.setLoginController(this);

        // Ustawienie właściciela (okna-rodzica) dla nowego okna
        Stage primaryStage = (Stage) registerButton.getScene().getWindow();
        registerStage.initOwner(primaryStage);

        // Ustawienie wyłączenia interakcji z oknem rodzica
        registerStage.initModality(Modality.APPLICATION_MODAL);

        // Utworzenie sceny dla nowego okna i ustawienie jej widoku
        Scene scene = new Scene(root);
        registerStage.setScene(scene);

        // Wyświetlenie nowego okna
        registerStage.show();
    }
}
