package net;

import Game.*;
import sql.DatabaseConnection;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ClientInfo{
    private String name;
    private InetAddress ipAddress;
    private int port;
    private int playerId;
    public ClientInfo(InetAddress ipAddress, int port, int playerId, String name)
    {
        this.name=name;
        this.ipAddress=ipAddress;
        this.port=port;
        this.playerId=playerId;
    }
    public InetAddress getIpAddress()
    {
        return ipAddress;
    }
    public int getPort()
    {
        return port;
    }
    public int getPlayerId()
    {
        return playerId;
    }
    public String getName()
    {
        return name;
    }
}

public class GameServer extends Thread {

    private static GameServer instance;
    private DatagramSocket socket;
    private Croupier game;
    private List<ClientInfo> connectedClients = new ArrayList<>();
    private volatile static boolean runningFlag = true;
    private static DatabaseConnection connectNow;
    private static Connection connectDB;

    private GameServer() {
    }

    public static synchronized GameServer getInstance() {
        if (instance == null) {
            instance = new GameServer();
            instance.game = Croupier.getInstance();
            connectNow = new DatabaseConnection();
            connectDB = connectNow.getConnection();
            try {
                instance.socket = new DatagramSocket(1331);
            } catch (SocketException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public void handleClientDisconnection(InetAddress ipAddress, int port) {
        connectedClients.removeIf(client -> client.getIpAddress().equals(ipAddress) && client.getPort() == port);
    }

    public void closeRunningFlag() {
        runningFlag = false;
    }

    public void closeTheSocket() {
        socket.close();
    }

    public int checkCurrentPlayingPlayers() {
        return game.numberOfPlayers;
    }

    public void run() {
        System.out.println("RUN GAMESERVER:" + Thread.currentThread().getName());
        Thread startGame = new Thread(() -> {
            try {
                game.initializeCroupier();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        startGame.start();
        while (runningFlag) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            String message = "";
            String[] partedMessage = {"pusty-pusty"};
            boolean clientExists = false;
            InetAddress clientIP = null;
            int clientPort = 0;
            try {
                socket.receive(packet);
                message = new String(packet.getData(), packet.getOffset(), packet.getLength()).trim();
                //message = new String(packet.getData()).trim();
                partedMessage = message.split("-");
                System.out.println("Wiadomosc odebrana przez serwer > " + message);
                //throw new IOException("Błąd wejścia-wyjścia");

                clientIP = packet.getAddress();
                clientPort = packet.getPort();

                // Sprawdzenie, czy klient jest już na liście connectedClients
                clientExists = false;
                for (ClientInfo client : connectedClients) {
                    if (client.getIpAddress().equals(clientIP) && client.getPort() == clientPort) {
                        clientExists = true;
                        break;
                    }
                }

                // Dodanie klienta do listy connectedClients, jeśli nie istnieje


            } catch (IOException e) {
                //throw new RuntimeException(e);
            }


            switch (partedMessage[0]) {
                case "playerAction":
                    switch (partedMessage[1]) {
                        case "login":
                            try {
                                game.addPlayerToQueue(Integer.parseInt(partedMessage[2]), partedMessage[3], Integer.parseInt(partedMessage[4]));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            if (!clientExists) {
                                connectedClients.add(new ClientInfo(clientIP, clientPort, Integer.valueOf(partedMessage[2]), partedMessage[3]));
                            }
                            //System.out.println("logowanie");
                            break;
                        case "bet":
                            //"playerAction-bet-" + playerId + "-" + playerName_Player1.getText() + "-"
                            synchronized (Croupier.getInstance().waitForMessage) {
                                if (Croupier.getInstance().returnActivePlayerId() == Integer.valueOf(partedMessage[2])) {
                                    Croupier.playerActionMessage = "bet";
                                    Croupier.getInstance().waitForMessage.notifyAll();
                                }
                            }
                            break;
                        case "call":
                            //"playerAction-call-" + playerId + "-" + playerName_Player1.getText() + "-"
                            synchronized (Croupier.getInstance().waitForMessage) {
                                if (Croupier.getInstance().returnActivePlayerId() == Integer.valueOf(partedMessage[2])) {
                                    Croupier.playerActionMessage = "call";
                                    Croupier.getInstance().waitForMessage.notifyAll();
                                }
                            }
                            //game.
                            break;
                        case "check":
                            //"playerAction-check-" + playerId + "-" + playerName_Player1.getText() + "-"
                            synchronized (Croupier.getInstance().waitForMessage) {
                                if (Croupier.getInstance().returnActivePlayerId() == Integer.valueOf(partedMessage[2])) {
                                    Croupier.playerActionMessage = "check";
                                    Croupier.getInstance().waitForMessage.notifyAll();
                                }
                            }
                            break;
                        case "raise":
                            //"playerAction-raise-" + playerId + "-" + playerName_Player1.getText()+"-"+raiseAmount.getText()+"-"
                            synchronized (Croupier.getInstance().waitForMessage) {
                                if (Croupier.getInstance().returnActivePlayerId() == Integer.valueOf(partedMessage[2])) {
                                    Croupier.raiseAmount = Integer.valueOf(partedMessage[4]);
                                    Croupier.playerActionMessage = "raise";
                                    Croupier.getInstance().waitForMessage.notifyAll();
                                }
                            }
                            break;
                        case "allIn":
                            //"playerAction-allIn-"+playerId+"-"+playerName_Player1.getText()+"-"
                            synchronized (Croupier.getInstance().waitForMessage) {
                                if (Croupier.getInstance().returnActivePlayerId() == Integer.valueOf(partedMessage[2])) {
                                    Croupier.playerActionMessage = "allIn";
                                    Croupier.getInstance().waitForMessage.notifyAll();
                                }
                            }
                            break;
                        case "fold":
                            //"playerAction-fold-"+playerId+"-"+playerName_Player1.getText()+"-"
                            synchronized (Croupier.getInstance().waitForMessage) {
                                if (Croupier.getInstance().returnActivePlayerId() == Integer.valueOf(partedMessage[2])) {
                                    Croupier.playerActionMessage = "fold";
                                    Croupier.getInstance().waitForMessage.notifyAll();
                                }
                            }
                            break;
                        case "logout":
                            //"playerAction-logout-playerId-"+playerId+"-playerNick-"+playerNick+"-ipAddress-"+ipAddress.getHostAddress()+"-"
                            try {
                                if (game.isRunning) {
                                    game.removePlayerFromGame(Integer.valueOf(partedMessage[3]));
                                } else {
                                    game.removePlayerFromWaitingQueue(Integer.valueOf(partedMessage[3]));
                                }
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                //clientIP = InetAddress.getByName(partedMessage[7]);
                                clientIP = InetAddress.getByName(partedMessage[7]);
                                for (ClientInfo client : connectedClients) {
                                    if (client.getIpAddress().equals(clientIP) && Integer.valueOf(partedMessage[3]) == client.getPlayerId()) {
                                        clientPort = client.getPort();
                                        break;
                                    }
                                }
                                handleClientDisconnection(clientIP, clientPort);
                                //
                                //handleClientDisconnection(InetAddress.getByName(partedMessage[7]), Integer.valueOf(partedMessage[9])); //niech wyszukuje port z ClientInfo
                            } catch (UnknownHostException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        default:
                            System.out.println("Zly format wiadomosci." + message);
                            break;
                    }
                    break;
                case "playerReceive":
                    switch (partedMessage[1]) {
                        case "waitingRoom":
                            String tempMessage = "";
                            try {
                                tempMessage = game.returnPlayers();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            System.out.println("Temp message: " + tempMessage);
                            sendData(tempMessage.getBytes(), clientIP, clientPort);
                            break;
                        case "getOtherPlayersInformation":
                            break;
                        case "getPlayerAction":
                            break;
                        case "getCroupierInformation":
                            //getCroupierInformation
                            break;
                        default:
                            System.out.println("Zly format wiadomosci. " + message);
                            break;
                    }
                    break;
                case "serverAction":
                    switch (partedMessage[1]) {
                        case "endOfDelay":
                            synchronized (Croupier.getInstance().waitForEndOfDelay) {
                                Croupier.getInstance().waitForEndOfDelay.notifyAll();
                            }
                            break;
                        case "validateLogin":
                            //"serverAction-validateLogin-user-"+loginTextField.getText()+"-password-"+passwordTextField.getText()+"-"
                            String response = null;
                            try {
                                response = validateLogin(partedMessage[3], partedMessage[5]);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            //"correctData-amountOfMoney-"+amountOfMoney+"-"
                            String[] partedResponse = response.split("-");
                            if(partedResponse[0].equals("correctData"));
                                {
                                    sendData(response.getBytes(), clientIP, clientPort);
                                }
                            break;
                        default:
                            System.out.println("serverAction zly format wiadomosci. " + message);
                    }
                    break;
                default:
                    System.out.println("Ogolnie zly format wiadomosci. " + message);
                    break;
            }

            //sendData("pong".getBytes(), packet.getAddress(), packet.getPort());


        }
    }

    public String validateLogin(String login, String password) throws SQLException {
        boolean isLogged = false;
        int playerId=-1;
        String verifyLogin="SELECT account_ID FROM user_account WHERE login = '"+login+"' AND password = '"+password+"';";

        try{
            Statement statement = connectDB.createStatement();
            ResultSet queryResult = statement.executeQuery(verifyLogin);

            while(queryResult.next())
            {
                playerId=queryResult.getInt("account_ID");
                if(!queryResult.next())
                {
                    isLogged=true;
                }else {
                    return "incorrect-data";
                }
            }

        }catch(Exception e)
        {
            e.printStackTrace();
            e.getCause();
        }
        int amountOfMoney=0;

        if(isLogged){
            String checkAmountOfMoney = "SELECT amountOfMoney FROM user_account WHERE login='"+login+"';";
            Statement statement = connectDB.createStatement();
            ResultSet queryResult = statement.executeQuery(checkAmountOfMoney);
            if(queryResult.next())
            {
                amountOfMoney = queryResult.getInt("amountOfMoney");
            }
        }
        return "correctData-amountOfMoney-"+amountOfMoney+"-playerId-"+playerId+"-";

    }
public void saveDataAboutPlayer(int amountOfMoney, int accountId) throws SQLException {
    System.out.println("Zapisywanie danych gracza...");
    String saveCurrentAmountOfMoney = "UPDATE user_account SET amountOfMoney = "+amountOfMoney+" WHERE account_ID= "+accountId+";";
    Statement statement = connectDB.createStatement();
    int ifWorksFine = statement.executeUpdate(saveCurrentAmountOfMoney);
    if(ifWorksFine==1)
    {
        System.out.println("Pomyslnie zapisano dane gracza");
    }
}
public void prepareAndSendDataFromCroupierToOnePlayer(String message)
{
    System.out.println(message);
    boolean clientExists = false;
    InetAddress ipAddress = null;
    int port = 0;
    String[] partedMessage = message.split("-");
    for (ClientInfo client : connectedClients) {
        if (client.getPlayerId()==Integer.valueOf(partedMessage[1])) {
            clientExists = true;
            ipAddress=client.getIpAddress();
            port=client.getPort();
            break;
        }
    }

    if(clientExists)
    {
        sendData(message.getBytes(), ipAddress, port);
    }
    else {
        System.out.println("Błąd! Klient którego jest kolej nigdy nie był w grze.");
    }

}
public void prepareAndSendDataAboutCardFromCroupierToAllPlayers(String message){
    //"card-"+i+"-forPlayerName-"+playersHand[j].playerName+"-numberOfCard-"+random+"-"
    String[] partedMessage = message.split("-");
    String trimMessage = String.join("-", Arrays.copyOfRange(partedMessage, 0,4));
    trimMessage=trimMessage+"-";
    System.out.println(message);

    InetAddress ipAddress = null;
    int port = 0;

    for(ClientInfo client: connectedClients)
    {
        if(client.getName().equals(partedMessage[3]))
        {
            sendData(message.getBytes(), client.getIpAddress(), client.getPort());
        }
        else
        {
            sendData(trimMessage.getBytes(), client.getIpAddress(), client.getPort());
        }
    }
}
public void prepareAndSendDataFromCroupierToAllPlayers(String message)
{
    byte[] data = message.getBytes();
    System.out.println(message);
    for (ClientInfo client : connectedClients) {
        InetAddress ipAddress=client.getIpAddress();
        int port=client.getPort();
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
public int checkIfPlayerIsConnected(String name)
{
    for(ClientInfo client : connectedClients)
    {
        if(client.getName().equals(name))
        {
            return 1;
        }
    }
    return 0;
}
    public void sendData(byte[] data, InetAddress ipAddress, int port)
    {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            String text = new String(data, StandardCharsets.UTF_8);
            System.out.println(text);
            this.socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally{
            if(socket.isConnected())
                socket.close();
        }
    }

}