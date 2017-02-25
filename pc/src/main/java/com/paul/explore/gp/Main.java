package com.paul.explore.gp;

import org.jgap.gp.impl.DefaultGPFitnessEvaluator;
import org.apache.log4j.Logger;
import org.jgap.gp.impl.DeltaGPFitnessEvaluator;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.GPGenotype;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        int noOfGenerations = 800;
        int populationSize = 1000;
        int noOfInputs = 8;
        if (args.length == 3) {
            populationSize = Integer.parseInt(args[0]);
            noOfGenerations = Integer.parseInt(args[1]);
            noOfInputs = Integer.parseInt(args[2]);
        }
        LOGGER.info("Running with  " + populationSize + " populationSize " + noOfGenerations + " generations " + noOfInputs + " inputs");
        run(populationSize, noOfGenerations, noOfInputs);
    }

    private static void run(int populationSize, int noOfGenerations,int noOfInputs) throws Exception {
        // Setup the algorithm's parameters.
        // ---------------------------------
        GPConfiguration config = new GPConfiguration();
        // ----------------------------------------------------------------------
        config.setGPFitnessEvaluator(new DefaultGPFitnessEvaluator());
        config.setMaxInitDepth(4);
        config.setPopulationSize(populationSize);
        config.setMaxCrossoverDepth(8);
        config.setFitnessFunction(new RobotFitnessFunction(noOfInputs, 50));
        config.setStrictProgramCreation(true);
        ControlProblem problem = new ControlProblem(config, noOfInputs);
        // Create the genotype of the problem, i.e., define the GP commands and
        // terminals that can be used, and constrain the structure of the GP
        // program.
        // --------------------------------------------------------------------
        GPGenotype gp = problem.create();
        gp.setVerboseOutput(true);
        // --------------------------------------------------------------------
        gp.evolve(noOfGenerations);
        // Print the best solution so far to the console.
        // ----------------------------------------------
        gp.outputSolution(gp.getAllTimeBest());
    }
}
