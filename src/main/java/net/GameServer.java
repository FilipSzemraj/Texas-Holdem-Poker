package net;

import Game.*;

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
        connectedClients.removeIf(client -> client.getIpAddress().equals(ipAddress) && client.getPort() == port);
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

            while (runningFlag) {
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                String message;
                String[] partedMessage = {"pusty-pusty"};
                boolean clientExists = false;
                InetAddress clientIP = null;
                int clientPort = 0;
                try {
                    socket.receive(packet);
                    message = new String(packet.getData());
                    partedMessage = message.split("-");
                    System.out.println(message);
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
                            game.addPlayerToQueue(Integer.parseInt(partedMessage[2]), partedMessage[3], Integer.parseInt(partedMessage[4]));
                            if (!clientExists) {
                                ClientInfo client = new ClientInfo(clientIP, clientPort, Integer.parseInt(partedMessage[2]));
                                connectedClients.add(client);
                            }
                            System.out.println("logowanie");
                            break;
                        case "call":
                            //"playerAction-bet-" + playerId + "-" + playerName_Player1.getText() + "-"
                            //game.
                            break;
                        case "check":
                            //"playerAction-check-" + playerId + "-" + playerName_Player1.getText() + "-"
                            break;
                        case "raise":
                            //"playerAction-raise-" + playerId + "-" + playerName_Player1.getText()+"-"+raiseAmount.getText()+"-"
                            break;
                        case "allIn":
                            //"playerAction-allIn-"+playerId+"-"+playerName_Player1.getText()+"-"
                            break;
                        case "fold":
                            //"playerAction-fold-"+playerId+"-"+playerName_Player1.getText()+"-"
                            break;
                        case "logout":
                            //handleClientDisconnection();
                            break;
                        case "logoutFromWaiting":
                            //handleClientDisconnection();
                            break;
                        default:
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
                                System.out.println("Zly format wiadomosci.");
                                break;
                        }
                        break;
                    default:
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
        if (client.getPlayerId()==Integer.valueOf(partedMessage[2])) {
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

}
    public void sendData(byte[] data, InetAddress ipAddress, int port)
    {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally{
            if(socket.isConnected())
                socket.close();
        }
    }

}