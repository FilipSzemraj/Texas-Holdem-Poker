package org.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class SceneController{
    public Label[] actualBet;
    public TextField raiseAmount;
    public Label actualBet_Player4;
    public Label actualBet_Player5;
    private int playerId;
    private int amountOfMoney;
    public AnchorPane wholeScene;
    public AnchorPane interfaceOne;
    public Label actualBet_Player1;
    public Circle player1Blind;
    public ImageView firstCardInHand1;
    public ImageView secondCardInHand1;
    public Label playerName_Player1;
    public Button ProfileIcon_Player1;
    public Label AmmountOfMoney_Player1;
    public Button AllIn_button;
    public Button Check_button;
    public Button Fold_button;
    public Button Raise_button;
    public Button Bet_button;
    public AnchorPane InterfaceTwo;
    public Label playerName_Player2;
    public Button profileIcon_Player2;
    public Label AmmountOfMoney_Player2;
    public ImageView secondCardInHand2;
    public ImageView firstCardInHand2;
    public Label actualBet_Player2;
    public Circle player2Blind;
    public Label player2Action;
    public AnchorPane InterfaceThree;
    public ImageView secondCardInHand3;
    public ImageView firstCardInHand3;
    public Label actualBet_Player3;
    public Circle player3Blind;
    public Label player3Action;
    public Label playerName_Player3;
    public Button profileIcon_Player3;
    public Label AmmountOfMoney_Player3;
    public AnchorPane InterfaceFour;
    public Label playerName_Player4;
    public Button profileIcon_Player4;
    public Label AmmountOfMoney_Player4;
    public Label player4Action;
    public ImageView firstCardInHand4;
    public ImageView secondCardInHand4;
    public Circle player4Blind;
    public AnchorPane InterfaceFive;
    public Label playerName_Player5;
    public Button profileIcon_Player5;
    public Label AmmountOfMoney_Player5;
    public Label player5Action;
    public ImageView firstCardInHand5;
    public ImageView secondCardInHand5;
    public Circle player5Blind;
    public VBox interfaceCroupier;
    public Circle croupierIcon;
    public Label messageToTable;
    public Label messageToPlayer;
    public VBox centerOfTable;
    public ImageView firstCardOnTable;
    public ImageView secondCardOnTable;
    public ImageView ThirdCardOnTable;
    public ImageView fourthCardOnTable;
    public ImageView fifthCardOnTable;
    public Label pot;
    public Circle potIcon;

    public void initialize(URL location, ResourceBundle resources, String name, int Id, int amountOfMoney)
    {
        playerName_Player1.setText(name);
        playerId=Id;
        this.amountOfMoney=amountOfMoney;
        AmmountOfMoney_Player1.setText(String.valueOf(amountOfMoney));
        actualBet = new Label[]{actualBet_Player1, actualBet_Player2, actualBet_Player3, actualBet_Player4, actualBet_Player5};

        login();
        Stage stage = (Stage) wholeScene.getScene().getWindow();
        stage.setOnCloseRequest((WindowEvent event) ->{
            LoginController.Players.get(playerId).closeRunningFlag();
            LoginController.closePlayerSocket(playerId);
            LoginController.deletePlayer(playerId);
            stage.close();
        });
    }
    private void login()
    {
        LoginController.Players.get(playerId).sendData(("playerAction-login-"+playerId+"-"+playerName_Player1.getText()+"-"+amountOfMoney+"-").getBytes());
    }
    @FXML
    void btnAllInOnClick(ActionEvent event) {
        LoginController.Players.get(playerId).sendData(("playerAction-allIn-"+playerId+"-"+playerName_Player1.getText()+"-").getBytes());
        System.out.println("allin");
    }
    @FXML
    void btnBetOnClick(ActionEvent event) {
        if(checkMaxBet()-Integer.valueOf(actualBet_Player1.getText())<amountOfMoney) {
            LoginController.Players.get(playerId).sendData(("playerAction-bet-" + playerId + "-" + playerName_Player1.getText() + "-").getBytes());
            System.out.println("bet");
        }
        else {
            messageToPlayer.setText("Możesz zagrać tylko 'All In'. Masz nie wystarczajaco srodkow na koncie.");
        }
    }
    @FXML
    void btnCheckOnClick(ActionEvent event) {
        if(checkMaxBet()==Integer.valueOf(actualBet_Player1.getText())) {
            LoginController.Players.get(playerId).sendData(("playerAction-check-" + playerId + "-" + playerName_Player1.getText() + "-").getBytes());
            System.out.println("check");
        }else{
            messageToPlayer.setText("Nie mozesz czekac, poniewaz nie masz maksymalnego zakladu na stole.");
        }
    }
    @FXML
    void btnFoldOnClick(ActionEvent event) {
        LoginController.Players.get(playerId).sendData(("playerAction-fold-"+playerId+"-"+playerName_Player1.getText()+"-").getBytes());
        System.out.println("fold");
    }
    @FXML
    void btnRaiseOnClick(ActionEvent event) {
        if(!raiseAmount.getText().isBlank() && Integer.valueOf(raiseAmount.getText())>checkMaxBet()) {
            LoginController.Players.get(playerId).sendData(("playerAction-raise-" + playerId + "-" + playerName_Player1.getText()+"-"+raiseAmount.getText()+"-").getBytes());
            System.out.println("raise" + playerName_Player1.getText());
        }
        else
        {
            messageToPlayer.setText("Kwota do przebicia powinna byc wieksza od maksymalnego zakladu na stole.");
        }

    }
    private int checkMaxBet()
    {
        int maxBet=0;
        for (int i = 0; i < 5; i++) {
            int tempBet = Integer.valueOf((actualBet[i].getText()));
            if(maxBet<tempBet)
                maxBet=tempBet;
        }
        return maxBet;
    }

}
