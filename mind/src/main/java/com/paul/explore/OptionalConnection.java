package com.paul.explore;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class OptionalConnection
{
    private static final int MULTI_CAST_PORT = 4446;
    private static final String GROUP_IP = "224.0.0.0";
    private static final int SYNC_PORT = 5000;
    private DatagramPacket packet;
    private boolean initialized = false;
    private boolean connected = false;
    private DatagramSocket datagramSocket;
    private ServerSocket serverSocket;
    private Socket socket;
    private DataOutputStream dataOutputStream;

    private void init() throws IOException
    {
        serverSocket = new ServerSocket(SYNC_PORT);
        serverSocket.setSoTimeout(1000);
        byte[] buf = "I am MindStorm".getBytes();
        InetAddress group = InetAddress.getByName(GROUP_IP);
        packet = new DatagramPacket(buf, buf.length, group, MULTI_CAST_PORT);
        datagramSocket = new DatagramSocket(MULTI_CAST_PORT);
        initialized = true;
    }

    public boolean tryConnect()
    {
        if (!initialized)
        {
            try
            {
                init();
            } catch (IOException e)
            {
                //e.printStackTrace();
                connected = false;
                return false;
            }
        }
        if (!connected)
        {
            try
            {
                broadCast();
                optionalSynchronousConnect();
            } catch (IOException e)
            {
                //e.printStackTrace();
                connected = false;
            }
        }
        return connected;
    }

    private void broadCast() throws IOException
    {
        for (int i = 0; i < 3; i++)
        {
            datagramSocket.send(packet);
        }
    }

    private void optionalSynchronousConnect() throws IOException
    {
        socket = serverSocket.accept();
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        connected = true;
    }

    public void sendSensorsAndMotorData(int[] distances, int rightMotorRotations, int leftMotorRotations)
    {
        tryConnect();
        if (connected)
        {
            try
            {
                dataOutputStream.writeByte(1); //Dummy data to show connection hasn't been closed
                for (int distance : distances)
                {
                    dataOutputStream.writeInt(distance);
                }

                dataOutputStream.writeInt(rightMotorRotations);
                dataOutputStream.writeInt(leftMotorRotations);
            } catch (IOException e)
            {
                //e.printStackTrace();
                connected = false;
            }
        }
    }

    public void close()
    {
        if (connected)
        {
            try
            {
                socket.close();
            } catch (IOException e)
            {
               // e.printStackTrace();
            }
        }
        datagramSocket.close();
        connected = false;
    }
}
