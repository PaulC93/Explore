package com.paul.explore.gp;

import com.paul.explore.sim.VirtualBot;
import com.paul.explore.sim.VirtualMap;
import org.apache.log4j.Logger;
import org.jgap.gp.CommandGene;
import org.jgap.gp.GPFitnessFunction;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.terminal.Variable;

public class RobotFitnessFunction extends GPFitnessFunction {

    private int noOfInputs;
    private int noOfSteps;
    private static final Object[] NO_ARGS = new Object[0];
    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private boolean maximumDeltaLogged = false;

    public RobotFitnessFunction(int noOfInputs, int noOfSteps) {
        this.noOfInputs = noOfInputs;
        this.noOfSteps = noOfSteps;
    }

    protected double evaluate(IGPProgram a_subject) {
        VirtualMap map = new VirtualMap();
        VirtualBot virtualBot = new VirtualBot(15, 15, 0, map);
        int[] distances = virtualBot.scan();
        boolean touchSensorIsTouchingObstacle = virtualBot.touchSensorIsTouchingObstacle();
        if (!maximumDeltaLogged)
        {
            LOGGER.info(" Maximum delta (noOfVisitablePoints) is " + map.getNoOfVisitablePoints());
            maximumDeltaLogged = true;
        }
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
            if (virtualBot.isTouchingObstacle()) {
                return Integer.MAX_VALUE;
            }
        }
        return map.getNoOfVisitablePoints() - map.getNoOfVisitedPoints();
    }
}

