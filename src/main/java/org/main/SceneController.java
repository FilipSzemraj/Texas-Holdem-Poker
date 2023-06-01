package org.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.GameClient;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

public class SceneController{
    private boolean[] whichInterfaceIsTaken = {true, false, false, false ,false};
    private int numberOfPlayers=0;
    public Label[] actualBet;
    public TextField raiseAmount;
    public Label actualBet_Player4;
    public Label actualBet_Player5;
    private int playerId;
    private int amountOfMoney;
    public AnchorPane wholeScene;
    public AnchorPane InterfaceOne;
    public Label actualBet_Player1;
    public Circle player1BigBlind;
    public Circle player1SmallBlind;
    public ImageView firstCardInHand1;
    public ImageView secondCardInHand1;
    public Label playerName_Player1;
    public Button ProfileIcon_Player1;
    public Label AmountOfMoney_Player1;
    public Button AllIn_button;
    public Button Check_button;
    public Button Fold_button;
    public Button Raise_button;
    public Button Bet_button;
    public AnchorPane InterfaceTwo;
    public Label playerName_Player2;
    public Button profileIcon_Player2;
    public Label AmountOfMoney_Player2;
    public ImageView secondCardInHand2;
    public ImageView firstCardInHand2;
    public Label actualBet_Player2;
    public Circle player2BigBlind;
    public Circle player2SmallBlind;
    public Label player2Action;
    public AnchorPane InterfaceThree;
    public ImageView secondCardInHand3;
    public ImageView firstCardInHand3;
    public Label actualBet_Player3;
    public Circle player3BigBlind;
    public Circle player3SmallBlind;
    public Label player3Action;
    public Label playerName_Player3;
    public Button profileIcon_Player3;
    public Label AmountOfMoney_Player3;
    public AnchorPane InterfaceFour;
    public Label playerName_Player4;
    public Button profileIcon_Player4;
    public Label AmountOfMoney_Player4;
    public Label player4Action;
    public ImageView firstCardInHand4;
    public ImageView secondCardInHand4;
    public Circle player4BigBlind;
    public Circle player4SmallBlind;
    public AnchorPane InterfaceFive;
    public Label playerName_Player5;
    public Button profileIcon_Player5;
    public Label AmountOfMoney_Player5;
    public Label player5Action;
    public ImageView firstCardInHand5;
    public ImageView secondCardInHand5;
    public Circle player5BigBlind;
    public Circle player5SmallBlind;
    public VBox interfaceCroupier;
    public Circle croupierIcon;
    public Label messageToTable;
    public Label messageToPlayer;
    public VBox centerOfTable;
    public ImageView firstCardOnTable;
    public ImageView secondCardOnTable;
    public ImageView thirdCardOnTable;
    public ImageView fourthCardOnTable;
    public ImageView fifthCardOnTable;
    public Label pot;
    public Circle potIcon;
    Reflection reflection = new Reflection();


    public void initialize(URL location, ResourceBundle resources, String name, int Id, int amountOfMoney)
    {
        reflection.setFraction(0.75);
        reflection.setTopOpacity(0.5);

        playerName_Player1.setText(name);
        playerId=Id;
        this.amountOfMoney=amountOfMoney;
        AmountOfMoney_Player1.setText(String.valueOf(amountOfMoney));
        actualBet = new Label[]{actualBet_Player1, actualBet_Player2, actualBet_Player3, actualBet_Player4, actualBet_Player5};

        login();
        Stage stage = (Stage) wholeScene.getScene().getWindow();
        stage.setOnCloseRequest((WindowEvent event) ->{
            Iterator<GameClient> iterator = LoginController.Players.iterator();
            while(iterator.hasNext()) {
            GameClient player = iterator.next();
                if(player.getPlayerId() == playerId)
                {
                    player.closeRunningFlag();
                    player.closeTheSocket();
                    //LoginController.closePlayerSocket(player.getPlayerId());
                    iterator.remove();
                    break;
                }
            }
            /*LoginController.Players.get(playerId).closeRunningFlag();
            LoginController.closePlayerSocket(playerId);
            LoginController.deletePlayer(playerId);*/
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
        String actualBetText = actualBet_Player1.getText();

        if (!actualBetText.isEmpty() && actualBetText.matches("\\d+")) {
            int actualBetValue = Integer.parseInt(actualBetText);

            if (checkMaxBet() - actualBetValue < amountOfMoney) {
                LoginController.Players.get(playerId).sendData(("playerAction-bet-" + playerId + "-" + playerName_Player1.getText() + "-").getBytes());
                System.out.println("bet");
            } else {
                messageToPlayer.setText("Możesz zagrać tylko 'All In'. Masz nie wystarczajaco srodkow na koncie.");
            }
        } else {
            messageToPlayer.setText("Niepoprawna wartość 'actualBet'.");
        }
    }
    @FXML
    void btnCheckOnClick(ActionEvent event) {
        String actualBetText = actualBet_Player1.getText();

        if (!actualBetText.isEmpty() && actualBetText.matches("\\d+")) {
            int actualBetValue = Integer.parseInt(actualBetText);

            if (checkMaxBet() == actualBetValue) {
                LoginController.Players.get(playerId).sendData(("playerAction-check-" + playerId + "-" + playerName_Player1.getText() + "-").getBytes());
                System.out.println("check");
            } else {
                messageToPlayer.setText("Nie mozesz czekac, poniewaz nie masz maksymalnego zakladu na stole.");
            }
        }else {
            messageToPlayer.setText("Niepoprawna wartość 'actualBet'.");
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
    public void setMessageAboutRankOfHand(String x)
    {
        Platform.runLater(() -> {
            messageToPlayer.setText("Masz: "+x);
        });
    }
    public void setCardsOnTable(int cardsCounter, String[] partedMessage)
    {
        switch (cardsCounter)
        {
            case 3:
                String imagePath1 = "image/deck/"+partedMessage[2]+".png";
                String imagePath2 = "image/deck/"+partedMessage[3]+".png";
                String imagePath3 = "image/deck/"+partedMessage[4]+".png";
                Image card1 = new Image(imagePath1);
                Image card2 = new Image(imagePath2);
                Image card3 = new Image(imagePath3);
                Platform.runLater(() -> {
                    firstCardOnTable.setImage(card1);
                    secondCardOnTable.setImage(card2);
                    thirdCardOnTable.setImage(card3);
                });
                break;
            case 4:
                String imagePath4 = "image/deck/"+partedMessage[5]+".png";
                Image card4 = new Image(imagePath4);
                Platform.runLater(() -> {
                    fourthCardOnTable.setImage(card4);
                });
                break;
            case 5:
                String imagePath5 = "image/deck/"+partedMessage[6]+".png";
                Image card5 = new Image(imagePath5);
                Platform.runLater(() -> {
                    fifthCardOnTable.setImage(card5);
                });
                break;
        }
    }
    public void resetButtonsAndMessages()
    {
        Platform.runLater(() -> {
            Fold_button.setStyle("-fx-background-color: none");
            player2Action.setText("");
            player3Action.setText("");
            player4Action.setText("");
            player5Action.setText("");
        });
    }
    public void setMessageAboutWinners(int winnersCounter, String[] partedMessage)
    {
        StringBuffer sb = new StringBuffer("Runda została wygrana przez: ");
        for (int i = 0; i < winnersCounter; i++) {
            sb.append(partedMessage[i+2]);
        }
        Platform.runLater(() -> {
        messageToTable.setText(sb.toString());
        });
    }
    private int checkMaxBet()
    {
        int maxBet=0;
        for (int i = 0; i < numberOfPlayers; i++) {
            int tempBet = Integer.valueOf(actualBet[i].getText());
            if(maxBet<tempBet)
                maxBet=tempBet;
        }
        return maxBet;
    }
    public void setCard(int whichCard, String name, String idOfCard)
    {
        if(name.equals(playerName_Player1.getText()))
        {
            String imagePath = "image/deck/"+idOfCard+".png";
            Image card;
            switch(whichCard)
            {
                case 0:
                    card = new Image(imagePath);
                    Platform.runLater(() -> {
                        firstCardInHand1.setImage(card);
                    });
                    break;
                case 1:
                    card = new Image(imagePath);
                    Platform.runLater(() -> {
                        secondCardInHand1.setImage(card);
                    });
                    break;
            }
        }
    }
    public void setCard(int whichCard, String name)
    {
        String imagePath = "image/deck/53.png";
        Image card = new Image(imagePath);
        if(name.equals(playerName_Player2.getText()))
        {
            Platform.runLater(() -> {
                if(whichCard==0) {
                    firstCardInHand2.setImage(card);
                }else if(whichCard==1)
                {
                    secondCardInHand2.setImage(card);
                }
            });
        }else if(name.equals(playerName_Player3.getText()))
        {
            Platform.runLater(() -> {
                if(whichCard==0) {
                    firstCardInHand3.setImage(card);
                }else if(whichCard==1)
                {
                    secondCardInHand3.setImage(card);
                }
            });
        }else if(name.equals(playerName_Player4.getText()))
        {
            Platform.runLater(() -> {
                if(whichCard==0) {
                    firstCardInHand4.setImage(card);
                }else if(whichCard==1)
                {
                    secondCardInHand4.setImage(card);
                }
            });
        }else if(name.equals(playerName_Player5.getText()))
        {
            Platform.runLater(() -> {
                if(whichCard==0) {
                    firstCardInHand5.setImage(card);
                }else if(whichCard==1)
                {
                    secondCardInHand5.setImage(card);
                }
            });
        }else{
            System.out.println("Nie poprawny nick");
        }
    }
    public void disableButtonEventHandling()
    {
        AllIn_button.setDisable(true);
        Check_button.setDisable(true);
        Fold_button.setDisable(true);
        Raise_button.setDisable(true);
        Bet_button.setDisable(true);
    }

    public void enableButtonEventHandling()
    {
        AllIn_button.setDisable(false);
        Check_button.setDisable(false);
        Fold_button.setDisable(false);
        Raise_button.setDisable(false);
        Bet_button.setDisable(false);

    }
    public void setOtherPlayersInterfaces(int numberOfPlayers)
    {
        this.numberOfPlayers=numberOfPlayers;
        switch(numberOfPlayers)
        {
            case 1:
                InterfaceOne.setVisible(true);
                InterfaceTwo.setVisible(false);
                InterfaceThree.setVisible(false);
                InterfaceFour.setVisible(false);
                InterfaceFive.setVisible(false);
                break;
            case 2:
                InterfaceOne.setVisible(true);
                InterfaceTwo.setVisible(true);
                InterfaceThree.setVisible(false);
                InterfaceFour.setVisible(false);
                InterfaceFive.setVisible(false);
                break;
            case 3:
                InterfaceOne.setVisible(true);
                InterfaceTwo.setVisible(true);
                InterfaceThree.setVisible(true);
                InterfaceFour.setVisible(false);
                InterfaceFive.setVisible(false);
                break;
            case 4:
                InterfaceOne.setVisible(true);
                InterfaceTwo.setVisible(true);
                InterfaceThree.setVisible(true);
                InterfaceFour.setVisible(true);
                InterfaceFive.setVisible(false);
                break;
            case 5:
                InterfaceOne.setVisible(true);
                InterfaceTwo.setVisible(true);
                InterfaceThree.setVisible(true);
                InterfaceFour.setVisible(true);
                InterfaceFive.setVisible(true);
                break;
            default:
                System.out.println("Bledne dane przy zmianie widocznych interfejsow.");
                break;
        }
    }
    private Label getPlayerNameLabel(int playerNumber)
    {
        switch(playerNumber)
        {
            case 1:
                return playerName_Player1;
            case 2:
                return playerName_Player2;
            case 3:
                return playerName_Player3;
            case 4:
                return playerName_Player4;
            case 5:
                return playerName_Player5;
            default:
                return null;
        }
    }
    /*for (int x = 1; x <= numberOfPlayers; x++) {
            Label playerNameLabel = getPlayerNameLabel(x);
            String playerName = playerNameLabel.getText();
            if(playerName.equals(name))
            {
                VBox temp = (VBox) playerNameLabel.getParent();

            }
            this.*/
    public void playerExitFromGame(String name)
    {
        if(name.equals(playerName_Player1.getText())){
            Platform.runLater(() -> {
                InterfaceOne.setDisable(true);
            });
            whichInterfaceIsTaken[0]=false;
            numberOfPlayers--;
        } else if(name.equals(playerName_Player2.getText()))
        {
            Platform.runLater(() -> {
                player2Action.setText("Exit the game");
                InterfaceTwo.setVisible(false);
            });
            whichInterfaceIsTaken[1]=false;
            numberOfPlayers--;
        }else if(name.equals(playerName_Player3.getText()))
        {
            Platform.runLater(() -> {
                player3Action.setText("Exit the game");
                InterfaceThree.setVisible(false);
            });
            whichInterfaceIsTaken[2]=false;
            numberOfPlayers--;
        }else if(name.equals(playerName_Player4.getText()))
        {
            Platform.runLater(() -> {
                player4Action.setText("Exit the game");
                InterfaceFour.setVisible(false);
            });
            whichInterfaceIsTaken[3]=false;
            numberOfPlayers--;
        }else if(name.equals(playerName_Player5.getText()))
        {
            Platform.runLater(() -> {
                player5Action.setText("Exit the game");
                InterfaceTwo.setVisible(false);
            });
            whichInterfaceIsTaken[4]=false;
            numberOfPlayers--;
        }else{
            System.out.println("Nie poprawny nick");
        }
    }
    public void changeIsFold(String name)
    {
        if(name.equals(playerName_Player1.getText())){
            Platform.runLater(() -> {
                Fold_button.setStyle("-fx-background-color: yellow;");
            });
        } else if(name.equals(playerName_Player2.getText()))
        {
            Platform.runLater(() -> {
                player2Action.setText("Folds");
            });
        }else if(name.equals(playerName_Player3.getText()))
        {
            Platform.runLater(() -> {
                player3Action.setText("Folds");
            });
        }else if(name.equals(playerName_Player4.getText()))
        {
            Platform.runLater(() -> {
                player4Action.setText("Folds");
            });
        }else if(name.equals(playerName_Player5.getText()))
        {
            Platform.runLater(() -> {
                player5Action.setText("Folds");
            });
        }else{
            System.out.println("Nie poprawny nick");
        }
    }
    public void changeIsAllIn(String name)
    {
        if(name.equals(playerName_Player1.getText())){
            Platform.runLater(() -> {
                AllIn_button.setStyle("-fx-background-color: yellow;");
            });
        } else if(name.equals(playerName_Player2.getText()))
        {
            Platform.runLater(() -> {
                player2Action.setText("All In");
            });
        }else if(name.equals(playerName_Player3.getText()))
        {
            Platform.runLater(() -> {
                player3Action.setText("All In");
            });
        }else if(name.equals(playerName_Player4.getText()))
        {
            Platform.runLater(() -> {
                player4Action.setText("All In");
            });
        }else if(name.equals(playerName_Player5.getText()))
        {
            Platform.runLater(() -> {
                player5Action.setText("All In");
            });
        }else{
            System.out.println("Nie poprawny nick");
        }
    }
    public void changeActualBet(String name, String actualBetAsParameter)
    {
        if(name.equals(playerName_Player1.getText())){
            Platform.runLater(() -> {
                actualBet_Player1.setText(actualBetAsParameter);
            });
        } else if(name.equals(playerName_Player2.getText()))
        {
            Platform.runLater(() -> {
                actualBet_Player2.setText(actualBetAsParameter);
            });
        }else if(name.equals(playerName_Player3.getText()))
        {
            Platform.runLater(() -> {
                actualBet_Player3.setText(actualBetAsParameter);
            });
        }else if(name.equals(playerName_Player4.getText()))
        {
            Platform.runLater(() -> {
                actualBet_Player4.setText(actualBetAsParameter);
            });
        }else if(name.equals(playerName_Player5.getText()))
        {
            Platform.runLater(() -> {
                actualBet_Player5.setText(actualBetAsParameter);
            });
        }else{
            System.out.println("Nie poprawny nick");
        }
    }

    public void setActivePlayer(String name)
    {
        if(name.equals(playerName_Player1.getText())){
            Platform.runLater(() -> {
                ProfileIcon_Player1.setEffect(reflection);
                profileIcon_Player2.setEffect(null);
                profileIcon_Player3.setEffect(null);
                profileIcon_Player4.setEffect(null);
                profileIcon_Player5.setEffect(null);
            });
        } else if(name.equals(playerName_Player2.getText()))
        {
            Platform.runLater(() -> {
                ProfileIcon_Player1.setEffect(null);
                profileIcon_Player2.setEffect(reflection);
                profileIcon_Player3.setEffect(null);
                profileIcon_Player4.setEffect(null);
                profileIcon_Player5.setEffect(null);
            });
        }else if(name.equals(playerName_Player3.getText()))
        {
            Platform.runLater(() -> {
                ProfileIcon_Player1.setEffect(null);
                profileIcon_Player2.setEffect(null);
                profileIcon_Player3.setEffect(reflection);
                profileIcon_Player4.setEffect(null);
                profileIcon_Player5.setEffect(null);
            });
        }else if(name.equals(playerName_Player4.getText()))
        {
            Platform.runLater(() -> {
                ProfileIcon_Player1.setEffect(null);
                profileIcon_Player2.setEffect(null);
                profileIcon_Player3.setEffect(null);
                profileIcon_Player4.setEffect(reflection);
                profileIcon_Player5.setEffect(null);
            });
        }else if(name.equals(playerName_Player5.getText()))
        {
            Platform.runLater(() -> {
                ProfileIcon_Player1.setEffect(null);
                profileIcon_Player2.setEffect(null);
                profileIcon_Player3.setEffect(null);
                profileIcon_Player4.setEffect(null);
                profileIcon_Player5.setEffect(reflection);
            });
        }else{
            System.out.println("Nie poprawny nick");
        }
    }
    public void setMaxBet(String maxBet, String name)
    {
            Platform.runLater(() -> {
                messageToTable.setText("Maksymalny zaklad wniosl gracz: "+name+", i wynosi on: "+maxBet);
            });
    }
    public void changePot(String money)
    {
        Platform.runLater(() -> {
        pot.setText(money);
        });
    }
    public void setBlindPosition(int bigBlindPosition, String bigBlind, int smallBlindPosition)
    {
        setBlinds(bigBlindPosition, player1BigBlind, player2BigBlind, player3BigBlind, player4BigBlind, player5BigBlind);
        setBlinds(smallBlindPosition, player1SmallBlind, player2SmallBlind, player3SmallBlind, player4SmallBlind, player5SmallBlind);

    }

    private void setBlinds(int bigBlindPosition, Circle player1Blind, Circle player2Blind, Circle player3Blind, Circle player4Blind, Circle player5Blind) {
        switch(bigBlindPosition)
        {
            case 0:
                Platform.runLater(() -> {
                player1Blind.setVisible(true);
                player2Blind.setVisible(false);
                player3Blind.setVisible(false);
                player4Blind.setVisible(false);
                player5Blind.setVisible(false);

                });
                break;
            case 1:
                Platform.runLater(() -> {
                    player1Blind.setVisible(false);
                    player2Blind.setVisible(true);
                    player3Blind.setVisible(false);
                    player4Blind.setVisible(false);
                    player5Blind.setVisible(false);
                });
                break;
            case 2:
                Platform.runLater(() -> {
                    player1Blind.setVisible(false);
                    player2Blind.setVisible(false);
                    player3Blind.setVisible(true);
                    player4Blind.setVisible(false);
                    player5Blind.setVisible(false);
                });
                break;
            case 3:
                Platform.runLater(() -> {
                    player1Blind.setVisible(false);
                    player2Blind.setVisible(false);
                    player3Blind.setVisible(false);
                    player4Blind.setVisible(true);
                    player5Blind.setVisible(false);
                });
                break;
            case 4:
                Platform.runLater(() -> {
                    player1Blind.setVisible(false);
                    player2Blind.setVisible(false);
                    player3Blind.setVisible(false);
                    player4Blind.setVisible(false);
                    player5Blind.setVisible(true);
                });
                break;
        }
    }

    public void changeAmountOfMoney(String amountOfMoney, String name) {
        if(name.equals(playerName_Player1.getText())){
            Platform.runLater(() -> {
                AmountOfMoney_Player1.setText(amountOfMoney);
            });
        } else if(name.equals(playerName_Player2.getText()))
        {
            Platform.runLater(() -> {
                AmountOfMoney_Player2.setText(amountOfMoney);
            });
        }else if(name.equals(playerName_Player3.getText()))
        {
            Platform.runLater(() -> {
                AmountOfMoney_Player3.setText(amountOfMoney);
            });
        }else if(name.equals(playerName_Player4.getText()))
        {
            Platform.runLater(() -> {
                AmountOfMoney_Player4.setText(amountOfMoney);
            });
        }else if(name.equals(playerName_Player5.getText()))
        {
            Platform.runLater(() -> {
                AmountOfMoney_Player5.setText(amountOfMoney);
            });
        }else{
            System.out.println("Nie poprawny nick");
        }
        /*for(Node node : wholeScene.getChildren()){
            if(node instanceof AnchorPane){
                boolean ifCorrectPlayer = false;
                for(Node label : ((AnchorPane) node).getChildren()){
                    if(label instanceof Label)
                    {
                        String playerName = ((Label) label).getText();
                        if(playerName.equals(name))
                        {
                            ifCorrectPlayer=true;
                        }
                    }
                }
                if(ifCorrectPlayer)
                {
                    for(Node label : ((AnchorPane) node).getChildren())
                    {
                        if(label instanceof Label)
                        {
                            String temp = ((Label) label).getText();
                            if(temp.matches("\\d+"))
                            {
                                ((Label) label).setText(amountOfMoney);
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }*/


    }
    public void setPlayerInformations(int numberOfPlayers, String[] partedMessage)
    {
        //"initializeInformations-"+playersHand[i].playerId+"-numberOfPlayers-"+numberOfPlayers+"-playerId-"+playersHand[y].playerId+"-playerName-"+playersHand[y].playerName+"-amountOfMoney-"+
        //                        playersHand[y].amountOfMoney
        switch(numberOfPlayers) {
            case 2:
                Platform.runLater(() -> {
                    playerName_Player2.setText(partedMessage[7]);
                    AmountOfMoney_Player2.setText(partedMessage[9]);
                });
                whichInterfaceIsTaken[1]=true;
                numberOfPlayers=2;
                break;
            case 3:
                Platform.runLater(() -> {
                    playerName_Player2.setText(partedMessage[7]);
                    AmountOfMoney_Player2.setText(partedMessage[9]);
                    playerName_Player3.setText(partedMessage[13]);
                    AmountOfMoney_Player3.setText(partedMessage[15]);
                });
                whichInterfaceIsTaken[1]=true;
                whichInterfaceIsTaken[2]=true;
                numberOfPlayers=3;
                break;
            case 4:
                Platform.runLater(() -> {
                    playerName_Player2.setText(partedMessage[7]);
                    AmountOfMoney_Player2.setText(partedMessage[9]);
                    playerName_Player3.setText(partedMessage[13]);
                    AmountOfMoney_Player3.setText(partedMessage[15]);
                    playerName_Player4.setText(partedMessage[19]);
                    AmountOfMoney_Player4.setText(partedMessage[21]);
                });
                whichInterfaceIsTaken[1]=true;
                whichInterfaceIsTaken[2]=true;
                whichInterfaceIsTaken[3]=true;
                numberOfPlayers=4;
                break;
            case 5:
                Platform.runLater(() -> {
                    playerName_Player2.setText(partedMessage[7]);
                    AmountOfMoney_Player2.setText(partedMessage[9]);
                    playerName_Player3.setText(partedMessage[13]);
                    AmountOfMoney_Player3.setText(partedMessage[15]);
                    playerName_Player4.setText(partedMessage[19]);
                    AmountOfMoney_Player4.setText(partedMessage[21]);
                    playerName_Player5.setText(partedMessage[27]);
                    AmountOfMoney_Player5.setText(partedMessage[29]);
                });
                whichInterfaceIsTaken[1]=true;
                whichInterfaceIsTaken[2]=true;
                whichInterfaceIsTaken[3]=true;
                whichInterfaceIsTaken[4]=true;
                numberOfPlayers=5;
            default:
                System.out.println("Za duza ilosc graczy.");
                break;
        }
    }

}
