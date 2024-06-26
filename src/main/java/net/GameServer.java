package net;

import Game.*;
import sql.DatabaseConnection;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

    private static volatile GameServer instance;
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

    public static synchronized void closeRunningFlag() {
        System.out.println("zamykanie flagi");
        runningFlag = false;
    }

    public void closeTheSocket() throws SQLException {
        System.out.println("Zamykanie soketa");
        socket.close();
        connectDB.close();
        connectNow.closeConnection();
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
                            int x=Integer.parseInt(partedMessage[2]);
                            String y=partedMessage[3];
                            int z=Integer.parseInt(partedMessage[4]);
                            if(z<=0){
                             sendData("notEnoughMoney-".getBytes(), clientIP, clientPort);
                             break;
                            }
                                Thread addPlayer = new Thread(() -> {
                                    try {
                                        game.addPlayerToQueue(x, y, z);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                                addPlayer.start();
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
                            int w=Integer.valueOf(partedMessage[3]);
                            if (game.isRunning) {
                                Thread removePlayer = new Thread(() -> {
                                    try {
                                        game.removePlayerFromGame(w);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                                removePlayer.start();
                            } else {
                                Thread removePlayer = new Thread(() -> {
                                    try {
                                        game.removePlayerFromWaitingQueue(w);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                                removePlayer.start();
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
                            InetAddress tempClientIp = clientIP;
                            int tempClientPort = clientPort;
                            Thread returnPlayers = new Thread(() -> {
                            String tempMessage=" ";
                                try {
                                    tempMessage = game.returnPlayers();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            System.out.println("Temp message: " + tempMessage);
                            sendData(tempMessage.getBytes(), tempClientIp, tempClientPort);
                            });
                            returnPlayers.start();
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
                            String response = "wrongData";
                            try {
                                response = validateLogin(partedMessage[3], partedMessage[5]);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            //"correctData-amountOfMoney-"+amountOfMoney+"-"
                            sendData(response.getBytes(), clientIP, clientPort);
                            break;
                        case "registerNewUser":
                            //"serverAction-registerNewUser-"+nameField.getText()+"-"+surnameField.getText()+"-"+loginField.getText()+"-"+passwordField.getText()+"-"
                            String query = "INSERT INTO user_account (firstname, lastname, login, password, amountOfMoney) VALUES (?, ?, ?, ?, 1000)";
                            try {
                                PreparedStatement statement = connectDB.prepareStatement(query);
                                statement.setString(1, partedMessage[2]);
                                statement.setString(2, partedMessage[3]);
                                statement.setString(3, partedMessage[4]);
                                statement.setString(4, partedMessage[5]);
                                int rowsAffected = statement.executeUpdate();
                                String responseToRegister = null;
                                if (rowsAffected > 0) {
                                    responseToRegister = "new-user-registered-";
                                } else {
                                    responseToRegister = "error-inRegister-";
                                }
                                sendData(responseToRegister.getBytes(), clientIP, clientPort);
                                statement.close();
                            } catch(SQLException e)
                            {
                                e.printStackTrace();
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
        game.stopWaitingFor2Players();
        synchronized (game.waitFor2Players) {
            game.waitFor2Players.notifyAll();
        }
        try {
            startGame.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //startGame.interrupt();
    }

    public String validateLogin(String login, String password) throws SQLException {
        boolean isLogged = false;
        int playerId=-1;
        String verifyLogin="SELECT account_ID FROM user_account WHERE login = '"+login+"' AND password = '"+password+"';";

        try{
            Statement statement = connectDB.createStatement();
            ResultSet queryResult = statement.executeQuery(verifyLogin);

            if(queryResult.next())
            {
                playerId=queryResult.getInt("account_ID");
                isLogged=true;
            }
            queryResult.close();
            statement.close();
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
            queryResult.close();
            statement.close();
            return "correctData-amountOfMoney-"+amountOfMoney+"-playerId-"+playerId+"-";
        }
        return "wrongData";

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
    statement.close();
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