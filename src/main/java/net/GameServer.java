package net;

import Game.*;

import java.io.IOException;
import java.net.*;

public class GameServer extends Thread{

    private DatagramSocket socket;
    private Croupier game;
    private volatile static boolean runningFlag=true;

    public GameServer()
    {
        this.game = Croupier.getInstance();
        try {
            this.socket = new DatagramSocket(1331);
        } catch (SocketException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
                try {
                    socket.receive(packet);
                    message = new String(packet.getData());
                    partedMessage = message.split("-");
                    //throw new IOException("Błąd wejścia-wyjścia");
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                }

                switch (partedMessage[0]) {
                    case "playerAction":
                    switch(partedMessage[1]) {
                        case "login":
                            game.addPlayerToQueue(Integer.parseInt(partedMessage[2]), partedMessage[3], Integer.parseInt(partedMessage[4]));
                            System.out.println("logowanie");
                            break;
                        case "call":
                            break;
                        case "check":
                            break;
                        case "raise":
                            break;
                        case "allIn":
                            break;
                        case "fold":
                            break;
                        case "logout":
                            break;
                        case "logoutFromWaiting":
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
                        }
                        break;
                    default:
                        System.out.println("Nie poprawna wiadomosc");
                        break;
                }

                //sendData("pong".getBytes(), packet.getAddress(), packet.getPort());


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