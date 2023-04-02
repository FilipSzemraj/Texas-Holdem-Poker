package org.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SceneController {

    @FXML
    private TextField TextLoginField;

    public void Login(ActionEvent event) throws IOException {

        System.out.println(TextLoginField.getText());

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

        //AnchorPane mainLayout;
        //FXMLLoader loader = new FXMLLoader();
        //loader.setLocation(Main.class.getResource("src/main/resources/fxml/MainWindow.fxml"));
        //mainLayout=loader.load();
        //stage.getScene().setRoot(mainLayout);

        //URL url_fxml = new File("src/main/resources/fxml/MainWindow.fxml").toURI().toURL();
        //root = FXMLLoader.load(url_fxml);
        //stage = (Stage)((Node)event.getSource()).getScene().getWindow();
       // scene = new Scene(root);
        //scene.getStylesheets().add(getClass().getResource("/css/MainPage.css").toExternalForm());

        //org.main.Main.setStageOption("Jakastam");
        //Image icon = new Image("icon.png") Zmiana ikony okna.
        //primaryStage.getIcons().add(icon);
        //

        //stage.setScene(scene);
        //stage.show();
    }

}
