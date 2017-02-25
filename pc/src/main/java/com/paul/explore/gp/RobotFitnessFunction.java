package com.paul.explore.gp;

import com.paul.explore.sim.VirtualBot;
import com.paul.explore.sim.VirtualMap;
import org.jgap.gp.CommandGene;
import org.jgap.gp.GPFitnessFunction;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.terminal.Variable;

import static com.paul.explore.sim.VirtualBot.BOT_SIZE;

public class RobotFitnessFunction extends GPFitnessFunction {

    private int noOfInputs;
    private int noOfSteps;
    private static final Object[] NO_ARGS = new Object[0];
    private static final int TOLERANCE = BOT_SIZE / 2;

    public RobotFitnessFunction(int noOfInputs, int noOfSteps) {
        this.noOfInputs = noOfInputs;
        this.noOfSteps = noOfSteps;
    }

    protected double evaluate(IGPProgram a_subject) {
        double fitness = 0;
        VirtualBot virtualBot = new VirtualBot(15, 15, 0, new VirtualMap());
        int[] distances = virtualBot.scan();
        boolean touchSensorIsTouchingObstacle = virtualBot.touchSensorIsTouchingObstacle();
        // Evaluate function for noOfSteps steps
        // ---------------------- -------------------
        for (int i = 0; i < noOfSteps; i++) {
            // Provide the sensory input
            CommandGene[] variables = a_subject.getNodeSets()[0];
            for (int j = 0; j < noOfInputs; j++) {
                ((Variable) variables[j]).set(distances[j]);
            }
            ((Variable) variables[noOfInputs]).set(touchSensorIsTouchingObstacle);
            // Execute the GP program representing the functions to be evolved.
            int rightMotorRotations = a_subject.execute_int(0, NO_ARGS);
            int leftMotorRotations = a_subject.execute_int(1, NO_ARGS);
            virtualBot.move(rightMotorRotations, leftMotorRotations);
            distances = virtualBot.scan();
            touchSensorIsTouchingObstacle = virtualBot.touchSensorIsTouchingObstacle();
            if (virtualBot.isInTheSameSpotAsBefore(TOLERANCE) || virtualBot.isTouchingObstacle()) {
                return 0;
            }
            fitness += virtualBot.getNoOfNewVisitedPoints();
        }
        return fitness;
    }
}

