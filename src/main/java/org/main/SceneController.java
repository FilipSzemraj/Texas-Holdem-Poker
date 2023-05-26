package org.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class SceneController{
    public Label PlayerName_label;
    public Button ProfileIcon_button;
    public Label AmmountOfMoney_label;
    public void setInformations(String name)
    {
        PlayerName_label.setText(name);
    }

    //@FXML
    //private TextField TextLoginField;

    /*public void Login(ActionEvent event) throws IOException {




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
        //System.out.println("Jakis tekst");
        Croupier krupier;
        krupier = Croupier.getInstance();
        krupier.firstStepInCroupier(5);
    }*/
    @FXML
    void btnAllInOnClick(ActionEvent event) {

    }
    @FXML
    void btnBetOnClick(ActionEvent event) {

    }
    @FXML
    void btnCheckOnClick(ActionEvent event) {

    }
    @FXML
    void btnFoldOnClick(ActionEvent event) {

    }
    @FXML
    void btnRaiseOnClick(ActionEvent event) {

    }
}
