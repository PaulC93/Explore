package com.paul.explore.gp;

import org.jgap.InvalidConfigurationException;
import org.jgap.gp.CommandGene;
import org.jgap.gp.GPProblem;
import org.jgap.gp.function.*;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.terminal.Terminal;
import org.jgap.gp.terminal.Variable;

import static com.paul.explore.model.BotConstants.NO_OF_DISTANCES_READ;

public class ControlProblem extends GPProblem {

    private Variable[] distances;
    private int noOfInputs;

    public ControlProblem(GPConfiguration a_conf, int noOfInputs) throws InvalidConfigurationException {
        super(a_conf);
        this.noOfInputs = noOfInputs;
        distances = new Variable[NO_OF_DISTANCES_READ];
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
    public GPGenotype create() throws InvalidConfigurationException {
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
        // We use NO_OF_DISTANCES_READ variables that can be set in the fitness function.
        for (int i = 0; i < NO_OF_DISTANCES_READ; i++) {
            distances[i] = Variable.create(conf, "d[" + i + "]", CommandGene.IntegerClass);
        }

        Add add = new Add(conf, CommandGene.IntegerClass);
        Subtract subtract = new Subtract(conf, CommandGene.IntegerClass);
        Multiply multiply = new Multiply(conf, CommandGene.IntegerClass);
        Divide divide = new Divide(conf, CommandGene.IntegerClass);
        Terminal terminal = new Terminal(conf, CommandGene.IntegerClass, 0.0d, 55.0d, true);
        CommandGene[] commandGenes;
        switch (noOfInputs) {
            case 4:
                commandGenes = new CommandGene[]{distances[2], distances[6], distances[10], distances[14], add, subtract, multiply, divide, terminal};
                break;
            case 16:
                commandGenes = new CommandGene[]{distances[0], distances[1], distances[2], distances[3], distances[4], distances[5], distances[6], distances[7], distances[8], distances[9], distances[10], distances[11], distances[12], distances[13], distances[14], distances[15], add, subtract, multiply, divide, terminal};
                break;
            case 8:
            default:
                commandGenes = new CommandGene[]{distances[0], distances[2], distances[4], distances[6], distances[8], distances[10], distances[12], distances[14], add, subtract, multiply, divide, terminal};
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
}
