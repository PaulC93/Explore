package com.paul.explore;

import com.paul.explore.communication.BotConnection;
import com.paul.explore.gui.MapView;
import com.paul.explore.model.Map;
import com.paul.explore.sim.VirtualBot;

import javax.swing.*;
import java.io.IOException;

public class PcExplore
{
    public static void main(String[] args)
    {
        BotConnection  botConnection = new BotConnection();
        if (!botConnection.connect())
        {
            JOptionPane.showMessageDialog(null, "Unable to connect. Aborting");
            return;
        }
        System.out.println("Connected");

        Map map = new Map(800);
        VirtualBot virtualBot = new VirtualBot(400, 400, 90, map);

        new MapView(virtualBot);

        try
        {
            while (botConnection.readData())
            {
                map.markFreeArea(virtualBot.getCenter(), virtualBot.getOrientation(), botConnection.getDistances());
                virtualBot.move(botConnection.getRightMotorRotations(), botConnection.getLeftMotorRotations());
            }
            botConnection.close();
        }catch (IOException e)
        {
            System.out.println("Connection problems, please retry");
            e.printStackTrace();
        }
    }
}
