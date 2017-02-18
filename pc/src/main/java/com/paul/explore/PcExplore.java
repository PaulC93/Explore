package com.paul.explore;

import com.paul.explore.gui.MapView;
import com.paul.explore.sim.VirtualBot;

import javax.swing.*;
import java.io.IOException;

public class PcExplore
{
    private static String botIp = "192.168.43.140";
    private static final int MAX_NO_OF_ATTEMPTS = 50;

    public static void main(String[] args)
    {

        System.out.println("Attempting to connect, please wait...");
        BotConnection botConnection = null;
        int noOfAttempts = 0;

        while (botConnection == null && noOfAttempts < MAX_NO_OF_ATTEMPTS)
        {
            noOfAttempts++;
            try
            {
                botConnection = new BotConnection(botIp);
            } catch (IOException e)
            {
                System.out.println(e.getMessage());
                botIp = "";
                while (invalidIp())
                {
                    botIp = JOptionPane.showInputDialog("Bot IP:");
                }
            }
        }
        if (botConnection == null)
        {
            JOptionPane.showMessageDialog(null, "Unable to connect after " + MAX_NO_OF_ATTEMPTS + " attempts. Aborting");
            return;
        }
        System.out.println("Connected");

        Map map = new Map();
        VirtualBot virtualBot = new VirtualBot(400, 400, 90, map);

        new MapView(virtualBot);

        try
        {
            while (botConnection.readData())
            {
                map.addArea(virtualBot.getCenter(), virtualBot.getOrientation(), botConnection.getDistances());
                virtualBot.move(botConnection.getRightMotorRotations(), botConnection.getLeftMotorRotations());
            }
            botConnection.close();
        }catch (IOException e)
        {
            System.out.println("Connection problems, please retry");
            e.printStackTrace();
        }
    }

    private static boolean invalidIp()
    {
        String[] ipv4 = botIp.split("\\.");
        for (String anInt : ipv4)
        {
            try
            {
                //noinspection ResultOfMethodCallIgnored
                Integer.parseInt(anInt);
            } catch (NumberFormatException e)
            {
                return true;
            }
        }
        return ipv4.length != 4;
    }
}
