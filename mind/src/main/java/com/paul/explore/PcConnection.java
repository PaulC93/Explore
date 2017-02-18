package com.paul.explore;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PcConnection
{
    private final int PORT = 80;
    private ServerSocket serverSocket;
    private Socket socket;
    private DataOutputStream dataOutputStream;

    public PcConnection() throws IOException
    {
        serverSocket = new ServerSocket(PORT);
        socket = serverSocket.accept();
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("Connected");
    }

    public void sendSensorsAndMotorData(int[] distances, int rightMotorRotations, int leftMotorRotations) throws IOException
    {
        dataOutputStream.writeByte(1); //Dummy data to show connection hasn't been closed

        for (int distance : distances)
        {
            dataOutputStream.writeInt(distance);
        }

        dataOutputStream.writeInt(rightMotorRotations);
        dataOutputStream.writeInt(leftMotorRotations);
    }

    public void close() throws IOException
    {
        socket.close();
        serverSocket.close();
    }
}
