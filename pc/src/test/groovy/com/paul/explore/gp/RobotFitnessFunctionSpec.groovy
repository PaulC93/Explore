package com.paul.explore.gp

import org.jgap.gp.IGPProgram
import org.jgap.gp.terminal.Variable
import spock.lang.Specification

class RobotFitnessFunctionSpec extends Specification {

    IGPProgram ind = Mock(IGPProgram)
    Variable d0 = Mock(Variable)
    Variable t = Mock(Variable)
    RobotFitnessFunction fitnessFunction = new RobotFitnessFunction(1,1)

    def 'should evaluate'() {
        given:
        ind.execute_int(0, _) >> rmr
        ind.execute_int(1, _) >> lmr
        ind.getNodeSets() >> [[d0, t],[]]

        expect:
        fitnessFunction.evaluate(ind) == expectedFitness

        where:
        rmr  | lmr  | expectedFitness
        1000 | 1000 | 648
        0    | 0    | 0
        9000 | 9000 | 0
    }
}
