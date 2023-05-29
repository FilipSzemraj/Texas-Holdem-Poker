package net;

import Game.*;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

class ClientInfo{
    private InetAddress ipAddress;
    private int port;
    private int playerId;
    public ClientInfo(InetAddress ipAddress, int port, int playerId)
    {
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
}

public class GameServer extends Thread{

    private static GameServer instance;
    private DatagramSocket socket;
    private Croupier game;
    private List<ClientInfo> connectedClients = new ArrayList<>();
    private volatile static boolean runningFlag=true;

    private GameServer()
    {
    }
    public static synchronized GameServer getInstance()
    {
        if(instance==null)
        {
            instance = new GameServer();
            instance.game = Croupier.getInstance();
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
        connectedClients.removeIf(client -> client.getIpAddress().equals(ipAddress) && client.getPort()==port);
    }
    public void closeRunningFlag()
    {
        runningFlag=false;
    }
    public void closeTheSocket()
    {
        socket.close();
    }
    public int checkCurrentPlayingPlayers()
    {
        return game.numberOfPlayers;
    }

    public void run()
    {
        System.out.println("RUN GAMESERVER:"+Thread.currentThread().getName());
        Thread startGame = new Thread(() -> {
            try {
                game.initializeCroupier();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        startGame.start();
            while (runningFlag) {
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                String message="";
                String[] partedMessage = {"pusty-pusty"};
                boolean clientExists = false;
                InetAddress clientIP = null;
                int clientPort = 0;
                try {
                    socket.receive(packet);
                    message = new String(packet.getData()).trim();
                    partedMessage = message.split("-");
                    System.out.println("Wiadomosc odebrana przez serwer > "+message);
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
                    switch(partedMessage[1]) {
                        case "login":
                            try {
                                game.addPlayerToQueue(Integer.parseInt(partedMessage[2]), partedMessage[3], Integer.parseInt(partedMessage[4]));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            if (!clientExists) {
                                connectedClients.add(new ClientInfo(clientIP, clientPort, Integer.valueOf(partedMessage[2])));
                            }
                            System.out.println("logowanie");
                            break;
                        case "call":
                            //"playerAction-bet-" + playerId + "-" + playerName_Player1.getText() + "-"
                            synchronized (Croupier.getInstance().waitForMessage)
                            {
                                Croupier.playerActionMessage="call";
                                Croupier.getInstance().waitForMessage.notifyAll();
                            }
                            //game.
                            break;
                        case "check":
                            //"playerAction-check-" + playerId + "-" + playerName_Player1.getText() + "-"
                            synchronized (Croupier.getInstance().waitForMessage)
                            {
                                Croupier.playerActionMessage="check";
                                Croupier.getInstance().waitForMessage.notifyAll();
                            }
                            break;
                        case "raise":
                            //"playerAction-raise-" + playerId + "-" + playerName_Player1.getText()+"-"+raiseAmount.getText()+"-"
                            synchronized (Croupier.getInstance().waitForMessage)
                            {
                                Croupier.playerActionMessage="raise";
                                Croupier.getInstance().waitForMessage.notifyAll();
                            }
                            break;
                        case "allIn":
                            //"playerAction-allIn-"+playerId+"-"+playerName_Player1.getText()+"-"
                            synchronized (Croupier.getInstance().waitForMessage)
                            {
                                Croupier.playerActionMessage="allIn";
                                Croupier.getInstance().waitForMessage.notifyAll();
                            }
                            break;
                        case "fold":
                            //"playerAction-fold-"+playerId+"-"+playerName_Player1.getText()+"-"
                            synchronized (Croupier.getInstance().waitForMessage)
                            {
                                Croupier.playerActionMessage="fold";
                                Croupier.getInstance().waitForMessage.notifyAll();
                            }
                            break;
                        case "logout":
                            //handleClientDisconnection();
                            try {
                                game.removePlayerFromGame(Integer.valueOf(partedMessage[3]));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                //clientIP = InetAddress.getByName(partedMessage[7]);
                                clientIP = InetAddress.getByName(partedMessage[7]);
                                for (ClientInfo client : connectedClients) {
                                    if (client.getIpAddress().equals(clientIP) && Integer.valueOf(partedMessage[3]) == client.getPlayerId()) {
                                        clientPort=client.getPort();
                                        break;
                                    }
                                }
                                handleClientDisconnection(clientIP, clientPort);
                                //handleClientDisconnection(InetAddress.getByName(partedMessage[7]), Integer.valueOf(partedMessage[9])); //niech wyszukuje port z ClientInfo
                            } catch (UnknownHostException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        default:
                            System.out.println(message);
                            System.out.println("Zly format wiadomosci.");
                            break;
                    }
                    break;
                    case "playerReceive":
                        switch(partedMessage[1]) {
                            case "getOtherPlayersInformation":
                                break;
                            case "getPlayerAction":
                                break;
                            case "getCroupierInformation":
                                break;
                            default:
                                System.out.println(message);
                                System.out.println("Zly format wiadomosci.");
                                break;
                        }
                        break;
                    default:
                        System.out.println(message);
                        System.out.println("Zly format wiadomosci.");
                        break;
                }

                //sendData("pong".getBytes(), packet.getAddress(), packet.getPort());


            }
    }

public void prepareAndSendDataFromCroupierToOnePlayer(String message)
{
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
public void prepareAndSendDataFromCroupierToAllPlayers(String message)
{
    byte[] data = message.getBytes();
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
    public void sendData(byte[] data, InetAddress ipAddress, int port)
    {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            System.out.println(data.toString());
            this.socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally{
            if(socket.isConnected())
                socket.close();
        }
    }

}