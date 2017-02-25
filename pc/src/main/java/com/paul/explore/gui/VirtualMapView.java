package com.paul.explore.gui;

import com.paul.explore.sim.VirtualBot;
import com.paul.explore.sim.VirtualMap;

/**
 * Used for simulating the map view.
 * Useful for experimenting with configurations without the use of the actual bot
 * The virtualBot it's used for simulation, the getRightMotorRotations and getLeftMotorRotations
 * can be alternated in the same way those methods would be altered on the code running on the real bot
 */
public class VirtualMapView extends MapView {


    private VirtualMapView(VirtualBot virtualBot) {
        super(virtualBot);
    }

    public static void main(String[] args) throws Exception {

        int hitCounts = 0;
        VirtualBot virtualBot = new VirtualBot(15, 15, 90, new VirtualMap());
        new VirtualMapView(virtualBot);

        for (int i = 0; i < 10000; i++) {
            int[] distances = virtualBot.getDistances();
            virtualBot.move(virtualBot.getRightMotorRotation(distances), virtualBot.getLeftMotorRotations(distances));
            if (virtualBot.isTouchingObstacle()) {
                hitCounts++;
                System.out.println("this individual should have died");
            }
        }
        System.out.println(hitCounts);
    }
}