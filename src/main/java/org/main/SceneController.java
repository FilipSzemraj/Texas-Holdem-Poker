package org.main;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SceneController{

    //@FXML
    //private TextField TextLoginField;

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

    @FXML
    void btnAllIN2OnClick(ActionEvent event) {

    }

    @FXML
    void btnAllINOnClick(ActionEvent event) {

    }

    @FXML
    void btnBetOnClick(ActionEvent event) {

    }

    @FXML
    void btnCheckOnClick(ActionEvent event) {

    }

    @FXML
    void btnFold2OnClick(ActionEvent event) {

    }

    @FXML
    void btnFoldOnClick(ActionEvent event) {

    }

    @FXML
    void btnRaiseOnClick(ActionEvent event) {

    }
}
