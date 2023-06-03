package org.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.File;
import java.net.URL;



public class serverMain extends Application {
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //primaryStage.initStyle(StageStyle.UNDECORATED);
        URL url_fxml = new File("src/main/resources/fxml/serverWindow.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(url_fxml);
        Scene scene = new Scene(root, 200,200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Serwer dla gry PokerTexasHoldem.");
        primaryStage.show();
    }
}
