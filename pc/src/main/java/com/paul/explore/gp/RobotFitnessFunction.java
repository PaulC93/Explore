package com.paul.explore.gp;

import com.paul.explore.sim.VirtualBot;
import com.paul.explore.sim.VirtualMap;
import org.jgap.gp.CommandGene;
import org.jgap.gp.GPFitnessFunction;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.terminal.Variable;

import java.util.Arrays;

import static com.paul.explore.sim.VirtualBot.BOT_SIZE;

public class RobotFitnessFunction extends GPFitnessFunction {

    private int noOfInputs;
    private int noOfSteps;

    public RobotFitnessFunction(int noOfInputs, int noOfSteps) {
        this.noOfInputs = noOfInputs;
        this.noOfSteps = noOfSteps;
    }

    protected double evaluate(IGPProgram a_subject) {
        double fitness = 0;
        int tolerance = BOT_SIZE / 2;
        Object[] noargs = new Object[0];

        VirtualMap map = new VirtualMap();
        map.createObstacles();
        VirtualBot virtualBot = new VirtualBot(15, 15, 0, map);

        int[] distances = virtualBot.scan();
        boolean touchSensorIsTouchingObstacle = virtualBot.touchSensorIsTouchingObstacle();
        // Evaluate function for noOfSteps steps
        // ---------------------- -------------------
        for (int i = 0; i < noOfSteps; i++) {
            // Provide the sensory input
            // See method create(), declaration of "nodeSets" for where X is
            // defined.
            // -------------------------------------------------------------
            CommandGene[] variables = a_subject.getNodeSets()[0];
            for (int j = 0; j < noOfInputs; j++) {
                ((Variable) variables[j]).set(distances[j]);
            }
            ((Variable) variables[noOfInputs]).set(touchSensorIsTouchingObstacle);
            try {
                // Execute the GP program representing the function to be evolved.
                // As in method create(), the return type is declared as float (see
                // declaration of array "types").
                // ----------------------------------------------------------------
                int rightMotorRotations = a_subject.execute_int(0, noargs);
                int leftMotorRotations = a_subject.execute_int(1, noargs);
                virtualBot.move(rightMotorRotations, leftMotorRotations);
                distances = virtualBot.scan();
                touchSensorIsTouchingObstacle = virtualBot.touchSensorIsTouchingObstacle();
                if (virtualBot.isInTheSameSpotAsBefore(tolerance) || virtualBot.isTouchingObstacle()) {
                    return 0;
                }

                fitness += virtualBot.getNoOfNewVisitedPoints();

            } catch (ArithmeticException ex) {
                // This should not happen, some illegal operation was executed.
                // ------------------------------------------------------------
                System.out.println("distances = " + Arrays.toString(distances));
                System.out.println("touchingObstacle = " + touchSensorIsTouchingObstacle);
                System.out.println(a_subject);
                throw ex;
            }
        }
        return fitness;
    }
}

