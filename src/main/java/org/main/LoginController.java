package org.main;

import Game.Croupier;
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
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


public class LoginController {

    @FXML
    private Button loginButton, cancelButton;
    @FXML
    Label messageLabel;
    @FXML
    TextField loginTextField, passwordTextField;
    DatabaseConnection connectNow;
    Connection connectDB;

    //private Stage primaryStage;
    public LoginController(){

    try {
        connectNow = new DatabaseConnection();
        connectDB = connectNow.getConnection();
    }catch(Exception e)
    {
        e.printStackTrace();
        messageLabel.setText("Bład połączenia z baza danych!");
    }
    Croupier krupier = Croupier.getInstance();
    }

    public void logged() throws IOException {

        System.out.println("Jakis tekst");

        URL url_fxml = new File("src/main/resources/fxml/MainWindow.fxml").toURI().toURL();
        FXMLLoader loader = new FXMLLoader(url_fxml);
        Parent root = loader.load();
        SceneController controller = loader.getController();
        controller.setInformations(loginTextField.getText());
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



    public void loginButtonOnAction()
    {
        if(loginTextField.getText().isBlank() == false && passwordTextField.getText().isBlank() == false)
        {
            validateLogin();
        } else
        messageLabel.setText("Oba pola muszą być uzupełnione");
    }

    public void cancelButtonOnAction(ActionEvent event){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
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
                    logged();
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
