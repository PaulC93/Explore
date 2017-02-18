package com.paul.explore.gp

import org.jgap.gp.IGPProgram
import org.jgap.gp.impl.GPConfiguration
import spock.lang.Specification

class MathProblemTest extends Specification {

    IGPProgram ind = Mock(IGPProgram)
    static MathProblem problem
    static MathProblem.FormulaFitnessFunction fitnessFunction = new MathProblem.FormulaFitnessFunction(1)

    def setupSpec() {
        GPConfiguration config = new GPConfiguration()
        config.setFitnessFunction(fitnessFunction)
        config.setPopulationSize(1)
        problem = new MathProblem(config)
    }


    def 'should ComputeRawFitness'() {
        given:
        ind.execute_int(0, _) >> rmr
        ind.execute_int(1, _) >> lmr
        problem.create()

        when:
        def fitness = fitnessFunction.evaluate(ind)

        then:
        fitness == expectedFitness
        where:
        rmr  | lmr  | expectedFitness
        1000 | 1000 | 648
        0    | 0    | 0
        9000 | 9000 | 0
    }
}
