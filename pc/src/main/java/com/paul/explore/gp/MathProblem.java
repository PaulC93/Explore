package com.paul.explore.gp;

import com.paul.explore.sim.VirtualBot;
import com.paul.explore.sim.VirtualMap;
import org.jgap.InvalidConfigurationException;
import org.jgap.gp.CommandGene;
import org.jgap.gp.GPFitnessFunction;
import org.jgap.gp.GPProblem;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.function.*;
import org.jgap.gp.impl.DefaultGPFitnessEvaluator;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.terminal.Terminal;
import org.jgap.gp.terminal.Variable;

import java.util.Arrays;

import static com.paul.explore.sim.VirtualBot.BOT_SIZE;

public class MathProblem extends GPProblem {

    private static Variable[] d = new Variable[16];
    private static Variable t;
    private static int noOfInputs;

    public MathProblem(GPConfiguration a_conf)
            throws InvalidConfigurationException {
        super(a_conf);
    }

    @Override
    public GPGenotype create() throws InvalidConfigurationException {
        return create(noOfInputs);
    }

    /**
     * This method is used for setting up the commands and terminals that can be
     * used to solve the problem.
     * Please notice, that the variables types, argTypes and nodeSets correspond
     * to each other: they have the same number of elements and the element at
     * the i'th index of each variable corresponds to the i'th index of the other
     * variables!
     *
     * @return GPGenotype
     * @throws InvalidConfigurationException
     */
    public GPGenotype create(int noOfInputs)
            throws InvalidConfigurationException {
        GPConfiguration conf = getGPConfiguration();
        // At first, we define the return type of the GP program.
        // ------------------------------------------------------
        Class[] types = {
                // Return type of result-producing chromosome
                CommandGene.IntegerClass, CommandGene.IntegerClass};
        Class[][] argTypes = {
                // Arguments of result-producing chromosome: none
                {}, {}
        };
        // Next, we define the set of available GP commands and terminals to use.
        // Please see package org.jgap.gp.function and org.jgap.gp.terminal
        // ----------------------------------------------------------------------
        // We use 17 variables that can be set in the fitness function.
        // 16 for distances around
        // 1 for the touch sensor
        for (int i = 0; i < 16; i++) {
            d[i] = Variable.create(conf, "d[" + i + "]", CommandGene.IntegerClass);
        }
        t = Variable.create(conf, "t", CommandGene.BooleanClass);

        Add add = new Add(conf, CommandGene.IntegerClass);
        Subtract subtract = new Subtract(conf, CommandGene.IntegerClass);
        Multiply multiply = new Multiply(conf, CommandGene.IntegerClass);
        Xor xor = new Xor(conf);
        Or or = new Or(conf);
        And and = new And(conf);
        Terminal terminal = new Terminal(conf, CommandGene.IntegerClass, 0.0d, 55.0d, true);
        CommandGene[] commandGenes = {d[0], d[2], d[4], d[6], d[8], d[10], d[12], d[14], t, add, subtract, multiply, xor, or, and, terminal};
        if (noOfInputs == 4) {
            commandGenes = new CommandGene[]{d[2], d[6], d[10], d[14], t, add, subtract, multiply, xor, or, and, terminal};
        } else if (noOfInputs == 8) {
            commandGenes = new CommandGene[]{d[0], d[2], d[4], d[6], d[8], d[10], d[12], d[14], t, add, subtract, multiply, xor, or, and, terminal};
        } else if (noOfInputs == 16) {
            commandGenes = new CommandGene[]{d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7], d[8], d[9], d[10], d[11], d[12], d[13], d[14], d[15], t, add, subtract, multiply, xor, or, and, terminal};
        }
        CommandGene[][] nodeSets = {commandGenes, commandGenes};
        // Create genotype with initial population. Here, we use the declarations
        // made above:
        // Use one result-producing chromosome (index 0) with return type float
        // (see types[0]), no argument (argTypes[0]) and several valid commands and
        // terminals (nodeSets[0]).
        // ------------------------------------------------------------------------
        return GPGenotype.randomInitialGenotype(conf, types, argTypes, nodeSets, 20, true);
    }

    public static void run(int populationSize, int noOfGenerations, int noOfInputs)
            throws Exception {
        MathProblem.noOfInputs = noOfInputs;
        // Setup the algorithm's parameters.
        // ---------------------------------
        GPConfiguration config = new GPConfiguration();
        // ----------------------------------------------------------------------
        config.setGPFitnessEvaluator(new DefaultGPFitnessEvaluator());
        config.setMaxInitDepth(4);
        config.setPopulationSize(populationSize);
        config.setMaxCrossoverDepth(8);
        config.setFitnessFunction(new MathProblem.FormulaFitnessFunction(50));
        config.setStrictProgramCreation(true);
        MathProblem problem = new MathProblem(config);
        // Create the genotype of the problem, i.e., define the GP commands and
        // terminals that can be used, and constrain the structure of the GP
        // program.
        // --------------------------------------------------------------------
        GPGenotype gp = problem.create(noOfInputs);
        gp.setVerboseOutput(true);
        // --------------------------------------------------------------------
        gp.evolve(noOfGenerations);
        // Print the best solution so far to the console.
        // ----------------------------------------------
        gp.outputSolution(gp.getAllTimeBest());
    }

    /**
     * Fitness function for evaluating the produced fomulas, represented as GP
     * programs. The fitness is computed by calculating the result (Y) of the
     * function/formula for integer inputs 0 to 20 (X). The sum of the differences
     * between expected Y and actual Y is the fitness, the lower the better (as
     * it is a defect rate here).
     */
    public static class FormulaFitnessFunction extends GPFitnessFunction {

        private int noOfSteps;

        public FormulaFitnessFunction(int noOfSteps)
        {
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
                for (int j = 0; j < 16; j++) {
                    d[j].set(distances[j]);
                }
                t.set(touchSensorIsTouchingObstacle);
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
                    System.out.println("d = " + Arrays.toString(d));
                    System.out.println("t = " + touchSensorIsTouchingObstacle);
                    System.out.println(a_subject);
                    throw ex;
                }
            }
            return fitness;
        }

    }
}
