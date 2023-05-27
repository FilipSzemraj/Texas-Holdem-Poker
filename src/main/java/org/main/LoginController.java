package org.main;

import Game.Croupier;
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

    @FXML
    private Button loginButton, cancelButton;
    @FXML
    Label messageLabel;
    @FXML
    TextField loginTextField, passwordTextField;
    DatabaseConnection connectNow;
    Connection connectDB;
    GameServer gameServer;
    Thread serverThread;
    public static List<GameClient> Players;
    public static List<Thread> ThreadsOfPlayers;

    private volatile boolean serverRunning = true;

    public LoginController(){

    try {
        connectNow = new DatabaseConnection();
        connectDB = connectNow.getConnection();
    }catch(Exception e)
    {
        e.printStackTrace();
        messageLabel.setText("Bład połączenia z baza danych!");
    }
        gameServer = GameServer.getInstance();
        serverThread = new Thread(gameServer);
        serverThread.start();
        Players = new ArrayList<>();
        ThreadsOfPlayers = new ArrayList<>();
    }

    public void logged() throws IOException, RuntimeException, SQLException {

        int amountOfMoney=0;
        String serverIp = "127.0.0.1"; // Adres IP serwera
        Players.add(new GameClient(serverIp));
        ThreadsOfPlayers.add(new Thread(Players.get(Players.size()-1)));


        String checkAmountOfMoney = "SELECT amountOfMoney FROM user_account WHERE login='"+loginTextField.getText()+"';";
        Statement statement = connectDB.createStatement();
        ResultSet queryResult = statement.executeQuery(checkAmountOfMoney);
        if(queryResult.next())
        {
            amountOfMoney = queryResult.getInt("amountOfMoney");
            System.out.println(amountOfMoney);
        }

        Players.get(Players.size()-1).initializeWindow(loginTextField.getText(), Players.size()-1, amountOfMoney);
        //ThreadsOfPlayers.get(Players.size()-1).start();
    }
    public static void closePlayerSocket(int id)
    {
        Players.get(id).closeTheSocket();
    }
    public static void deletePlayer(int id)
    {
        Iterator<GameClient> iterator = Players.iterator();
        while(iterator.hasNext())
        {
            GameClient player = iterator.next();
            if(player.playerId == id)
            {
                iterator.remove();
                break;
            }
        }

    }


    public void loginButtonOnAction()
    {
        if(loginTextField.getText().isBlank() == false && passwordTextField.getText().isBlank() == false)
        {
            validateLogin();
        } else
        messageLabel.setText("Oba pola muszą być uzupełnione");
    }

    public void cancelButtonOnAction(ActionEvent event) throws InterruptedException, UnknownHostException, SocketException {
        gameServer.closeRunningFlag();
        gameServer.closeTheSocket();
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        serverThread.join();
        stage.close();
    }

    public void validateLogin()
    {


        String verifyLogin="SELECT Count(1) FROM user_account WHERE login = '"+loginTextField.getText()+"' AND password = '"+passwordTextField.getText()+"';";

        try{
            Statement statement = connectDB.createStatement();
            ResultSet queryResult = statement.executeQuery(verifyLogin);

            while(queryResult.next())
            {
                if(queryResult.getInt(1) == 1)
                {
                    messageLabel.setText("Gratulacje! Zalogowano pomyślnie.");
                    if(gameServer.checkCurrentPlayingPlayers()<5) {
                        logged();
                    }else{
                        messageLabel.setText("Twoje dane są poprawne, jednak w grze już jest 5 osób. Musisz poczekać, aż zwolni się miejsce przy stole.");
                    }
                }else {
                    messageLabel.setText("Niepoprawne dane, spróbuj ponownie...");
                }
            }

        }catch(Exception e)
        {
            e.printStackTrace();
            e.getCause();
        }
    }
}
