package org.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.scene.control.Button;


public class LoginController {

    @FXML
    private Button loginButton, cancelButton;
    //private Stage primaryStage;

    public void Login(ActionEvent event) throws IOException {

        System.out.println("Jakis tekst");

        URL url_fxml = new File("src/main/resources/fxml/MainWindow.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(url_fxml);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/MainPage.css").toExternalForm());
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Texas holdem");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(true);
        primaryStage.show();
        ((Node)(event.getSource())).getScene().getWindow().hide();
    }

    public void cancelButtonOnAction(ActionEvent event){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
