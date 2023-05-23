package net;

import Game.*;

import java.io.IOException;
import java.net.*;

public class GameClient extends Thread{

    private InetAddress ipAddress;
    private DatagramSocket socket;
    private Croupier game;

    public GameClient(Croupier game, String ipAddress)
    {
        this.game = game;
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
        while(true)
        {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String message = new String(packet.getData());
            System.out.println("Server > "+message);
        }
    }

    public void sendData(byte[] data)
    {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 1331);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
