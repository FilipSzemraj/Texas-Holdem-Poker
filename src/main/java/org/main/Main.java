package org.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.GameClient;
import net.GameServer;

import java.io.File;
import java.net.URL;

public class Main extends Application {

    //private GameClient socketClient;
    //private GameServer socketClient;

    @Override
    public void start(Stage primaryStage) {
        try{
            //stage=primaryStage;
            URL url_fxml = new File("src/main/resources/fxml/LoginWindow.fxml").toURI().toURL();
            Parent root = FXMLLoader.load(url_fxml);
            Scene scene = new Scene(root, 520, 400);
            scene.getStylesheets().add(getClass().getResource("/css/LoginPage.css").toExternalForm());
            primaryStage.setTitle("Texas holdem - logowanie");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        launch(args);
    }

    //public static void setStageOption(String newTitle)
    //{
        //stage.setTitle(newTitle);
        //stage.setMaximized(true);
        //stage.setFullScreen(true);
        //Image icon = new Image("icon.png") Zmiana ikony okna.
        //primaryStage.getIcons().add(icon);
    //}



}