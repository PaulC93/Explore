package com.paul.explore.gp;

public class Main {

    public static void main(String[] args) throws Exception {
        int noOfGenerations = 800;
        int populationSize = 1000;
        int noOfInputs = 8;
        if (args.length == 3) {
            populationSize = Integer.parseInt(args[0]);
            noOfGenerations = Integer.parseInt(args[1]);
            noOfInputs = Integer.parseInt(args[2]);
        }
        System.out.println("Running with  " + populationSize + " populationSize " + noOfGenerations + " generations " + noOfInputs + " inputs");
        MathProblem.run(populationSize, noOfGenerations, noOfInputs);
    }
}
