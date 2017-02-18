package com.paul.explore.gp

import org.jgap.gp.IGPProgram
import org.jgap.gp.impl.GPConfiguration
import spock.lang.Specification

class MathProblemTest extends Specification {

    IGPProgram ind = Mock(IGPProgram)
    MathProblem problem
    MathProblem.FormulaFitnessFunction fitnessFunction = new MathProblem.FormulaFitnessFunction()

    def 'setup'()
    {
        GPConfiguration config = new GPConfiguration()
        config.setFitnessFunction(fitnessFunction)
        config.setPopulationSize(1)
        problem = new MathProblem(config)
    }


    def 'should ComputeRawFitness'() {
        given:
        ind.execute_int(0, _) >> 1000
        ind.execute_int(1, _) >> 1000
        problem.create()

        when:
        def fitness = fitnessFunction.computeRawFitness(ind)

        then:
        fitness > 0
    }
}
