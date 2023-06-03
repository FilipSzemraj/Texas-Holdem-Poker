package net;

import Game.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.main.SceneController;

import java.io.File;
import java.io.IOException;
import java.net.*;

public class GameClient extends Thread{
    public final Object waitForMessage = new Object();

    private InetAddress ipAddress;
    private DatagramSocket socket;
    SceneController controller;
    public int playerId;
    public String playerNick;
    private boolean runningFlag=true;

    public GameClient(String ipAddress)
    {
        //this.game = game;
        try {
            this.socket = new DatagramSocket();
            this.ipAddress = InetAddress.getByName(ipAddress);
        } catch (SocketException | UnknownHostException e) {
            //e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void run()
    {
        System.out.println("RUN GAMECLIENT:"+Thread.currentThread().getName());
        while(runningFlag)
        {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                //e.printStackTrace();
                //throw new RuntimeException(e);
            }
            String message = new String(packet.getData()).trim();
            System.out.println("Wiadomosc odebrana przez klienta > "+message);
            String[] partedMessage = message.split("-");
            switch(partedMessage[0])
            {
                case "playerAction":
                    if(Integer.valueOf(partedMessage[1])==playerId)
                    {
                        controller.enableButtonEventHandling();
                        Runnable disableButtonsTask = () -> {
                            synchronized (waitForMessage)
                            {
                                try {
                                    waitForMessage.wait();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            controller.disableButtonEventHandling();
                        };
                        Thread disableButtonsThread = new Thread(disableButtonsTask);
                        disableButtonsThread.start();
                    }
                    break;
                case "croupierInformations":
                    break;
                case "initializeInformations":
                    //"initializeInformations-numberOfPlayers-"+numberOfPlayers"-playerId-"+playersHand[i].playerId+"-playerName-"+playersHand[i].playerName+"-amountOfMoney-"+playersHand[i].amountOfMoney+"-"
                    controller.setOtherPlayersInterfaces(Integer.valueOf(partedMessage[3]));
                    controller.setPlayerInformations(Integer.valueOf(partedMessage[3]), partedMessage);
                    break;
                case "amountOfMoney":
                    //"amountOfMoney-0-playerName-"+playerName+"-"
                    controller.changeAmountOfMoney(partedMessage[1], partedMessage[3]);
                    break;
                case "allIn":
                    //"allIn-playerName-"+playerName+"-"
                    controller.changeIsAllIn(partedMessage[2]);
                    break;
                case "fold":
                    //"fold-playerName-"+playersHand[activePlayer].playerName+"-"
                    controller.changeIsFold(partedMessage[2]);
                    break;
                case "exitFromGame":
                    //"exitFromGame-playerName-"+playersHand[i].playerName+"-"
                    controller.playerExitFromGame(partedMessage[2]);
                    break;
                case "actualBet":
                    //"actualBet-"+actualBet+"-playerName-"+playerName+"-"
                    controller.changeActualBet(partedMessage[3], partedMessage[1]);
                    break;
                case "setActivePlayer":
                        //"setActivePlayer-playerName-"+playersHand[x].playerName+"-"
                    controller.setActivePlayer(partedMessage[2]);
                    break;
                case "setMaxBet":
                    //"setMaxBet-"+x+"-playerName-"+playersHand[activePlayer].playerName+"-"
                    controller.setMaxBet(partedMessage[1], partedMessage[3]);
                case "changePot":
                    //"changePot-"+pot+"-"
                    controller.changePot(partedMessage[1]);
                    break;
                case "bigBlindPosition":
                    //"bigBlindPosition-"+bigBlindPosition+"-bigBlind-"+bigBlind+"-smallBlindPosition-"+smallBlindPosition+"-"
                    controller.setBlindPosition(Integer.valueOf(partedMessage[1]), partedMessage[3], Integer.valueOf(partedMessage[5]));
                    break;
                case "endOfRound":
                    try {
                        controller.resetButtonsAndMessages();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "winners":
                    //"winners-"+winnersCount-playerName-
                    controller.setMessageAboutWinners(Integer.valueOf(partedMessage[1]), partedMessage);
                    break;
                case "card":
                    //"card-"+i+"-forPlayerName-"+playersHand[j].playerName+"-numberOfCard-"+random+"-"
                    System.out.println(message);
                    if(partedMessage[3].equals(playerNick))
                    {
                        controller.setCard(Integer.valueOf(partedMessage[1]), partedMessage[3], partedMessage[5]);
                    }
                    else {
                        controller.setCard(Integer.valueOf(partedMessage[1]), partedMessage[3]);
                    }
                    break;
                case "rankOfHandFor":
                    //"rankOfHandFor-"+playerId+"-Is-"+CheckHand.ranksOfHand[x-1]+"-"
                    if(Integer.valueOf(partedMessage[1])==playerId){
                        controller.setMessageAboutRankOfHand(partedMessage[3]);
                    }
                    break;
                case "CardsOnTable":
                    //"CardsOnTable-"+table.size()
                    controller.setCardsOnTable(Integer.valueOf(partedMessage[1]), partedMessage);
                    break;
                case "endOfGame":
                    System.out.println("koniec gry");
                    break;
                case "waitingRoomReceive":
                    //"waitingRoomReceive-"+playerId+"-playersInGame-0-waitingPlayers-0-"
                    System.out.println(message);
                    //controller.setWaitingRoom(partedMessage);
                    break;
                case "newPlayer":
                    break;
            }
        }
    }
    public void sendData(byte[] data)
    {
        System.out.println(data.toString());
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 1331);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void closeRunningFlag()
    {
        sendData(("playerAction-logout-playerId-"+playerId+"-playerNick-"+playerNick+"-ipAddress-"+ipAddress.getHostAddress()+"-").getBytes());
        runningFlag=false;
    }
    public void closeTheSocket()
    {
        socket.close();
    }

    public void initializeWindow(String name, int Id, int amountOfMoney) throws IOException, InterruptedException {
        System.out.println("initialize window, dla:"+name+" "+Thread.currentThread().getName());
        playerId=Id;
        playerNick=name;
        URL url_fxml = new File("src/main/resources/fxml/MainWindow.fxml").toURI().toURL();
        FXMLLoader loader = new FXMLLoader(url_fxml);
        Parent root = loader.load();
        controller = loader.getController();
        Scene scene = new Scene(root, 714, 441);
        scene.getStylesheets().add(getClass().getResource("/css/MainPage.css").toExternalForm());
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Texas holdem - poczekalnia");
        primaryStage.setScene(scene);
        primaryStage.show();
        controller.initialize(loader.getLocation(), loader.getResources(), name, Id, amountOfMoney);
    }
    public int getPlayerId()
    {
        return playerId;
    }
}
