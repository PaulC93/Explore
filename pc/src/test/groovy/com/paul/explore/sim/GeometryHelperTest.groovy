package com.paul.explore.sim

import spock.lang.Specification

import java.awt.*
import java.awt.geom.Point2D

import static com.paul.explore.sim.GeometryHelper.normalizeAngle
import static com.paul.explore.sim.GeometryHelper.rotateAround

class GeometryHelperTest extends Specification {

    def 'should normalize angle'() {
        expect:
        normalizeAngle(angle) == normalized

        where:
        angle | normalized
        360   | 0
        720   | 0
        365   | 5
        735   | 15
        -270  | 90
    }

    def 'should rotate around'() {

        expect:
        rotateAround(new Point(0, 0), new Point(0, -10), 90) == new Point2D.Double(10, 0)
    }
}
