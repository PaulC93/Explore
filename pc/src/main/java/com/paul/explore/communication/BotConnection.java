package com.paul.explore.communication;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

import static com.paul.explore.model.BotConstants.NO_OF_DISTANCES_READ;

public class BotConnection
{
    private static final int MULTI_CAST_PORT = 4446;
    private static final String IP_GROUP = "224.0.0.0";
    private static final int SYNC_PORT = 5000;
    private static final String EXPECTED_STRING = "I am MindStorm";
    private Socket socket;
    private int[] distances = new int[NO_OF_DISTANCES_READ];
    private int rightMotorRotations;
    private int leftMotorRotations;
    private DataInputStream dataInputStream;

    public boolean connect()
    {
        try
        {
            String receivedString = null;
            MulticastSocket multicastSocket = new MulticastSocket(MULTI_CAST_PORT);
            InetAddress group = InetAddress.getByName(IP_GROUP);
            multicastSocket.joinGroup(group);
            byte[] buffer = new byte[EXPECTED_STRING.length()];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (!EXPECTED_STRING.equals(receivedString))
            {
                multicastSocket.receive(packet);
                receivedString = new String(packet.getData());
            }
            multicastSocket.leaveGroup(group);
            multicastSocket.close();
            String hostAddress = packet.getAddress().getHostAddress();
            System.out.println("Attempting to connect on " + hostAddress + ":" + SYNC_PORT);
            socket = new Socket(hostAddress, SYNC_PORT);
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e)
        {
           // e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean readData() throws IOException
    {

        int dummy = dataInputStream.read();
        if (dummy != -1)
        {
            for (int i = 0; i < distances.length; i++)
            {
                distances[i] = dataInputStream.readInt();
            }

            rightMotorRotations = dataInputStream.readInt();
            leftMotorRotations = dataInputStream.readInt();
            return true;
        }
        return false;
    }

    public int[] getDistances()
    {
        return distances;
    }

    public int getRightMotorRotations()
    {
        return rightMotorRotations;
    }

    public int getLeftMotorRotations()
    {
        return leftMotorRotations;
    }

    public void close() throws IOException
    {
        socket.close();
    }
}
