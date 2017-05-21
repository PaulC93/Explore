package com.paul.explore

import com.paul.explore.model.Map
import com.paul.explore.sim.VirtualMap
import spock.lang.Specification

class MapTest extends Specification {

    def 'should set and check map point'() {
        Map map = new Map(2)
        map.markAsFree(0, 0)
        map.markAsFree(1, 1)
        map.markAsVisited(0, 0)
        map.markAsVisited(1, 0)
        map.map[0][1] = 4

        expect:
        map.isFree(0, 0)
        map.isFree(1, 1)
        map.isVisited(0, 0)
        map.isVisited(1, 0)
        !map.isVisited(1, 1)
        !map.isFree(1, 0)
        !map.isFree(0, 1)
        !map.isVisited(0, 1)
        map.isObstacle(0, 1)
    }

    def 'should get no of non-obstacle'() {
        Map map = new VirtualMap()

        expect:
        map.getNoOfNonObstaclePoints() == 35500
    }

    def 'should get no of points observed as free'() {
        Map map = new Map(2)
        map.markAsFree(0, 0)
        map.markAsVisited(0, 0)
        map.markAsFree(0, 1)

        expect:
        map.getNoOfPointsObservedAsFree() == 2
    }

}
