package com.paul.explore;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class BotConnection
{
    private final int PORT = 80;

    private Socket socket;
    private int[] distances = new int[16];
    private int rightMotorRotations;
    private int leftMotorRotations;
    private DataInputStream dataInputStream;

    public BotConnection(String ip) throws IOException
    {
        socket = new Socket(ip, PORT);
        dataInputStream = new DataInputStream(socket.getInputStream());
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
