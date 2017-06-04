package com.paul.explore.sim

import spock.lang.Specification
import spock.lang.Unroll

import java.awt.geom.Point2D

import static com.paul.explore.model.GeometryHelper.round

@Unroll
class VirtualBotSpec extends Specification {

    def 'should init motor positions'() {

        given:
        def bot = new VirtualBot(centerX, centerY, orientation, new VirtualMap())

        expect:
        bot.getLeftMotorPosition() == new Point2D.Double(lX, lY)
        bot.getRightMotorPosition() == new Point2D.Double(rX, rY)

        where:
        centerX | centerY | orientation | lX | lY | rX | rY
        15      | 15      | 90          | 5  | 15 | 25 | 15
        15      | 15      | 45          | 8  | 22 | 22 | 8
        15      | 15      | 0           | 15 | 25 | 15 | 5
    }

    def 'should move'() {

        given:
        int cX = (rx + lx) / 2 + 150
        int cY = (ry + ly) / 2 + 150
        VirtualBot virtualBot = new VirtualBot(cX, cY, initialOrientation, new VirtualMap())
        //VirtualMapView mapView = new VirtualMapView(virtualBot)

        when:
        virtualBot.move(rr, lr)
        //Thread.sleep(1000)
        //mapView.dispose()

        then:
        round(virtualBot.rightMotorPosition.x) == nrx + 150
        round(virtualBot.rightMotorPosition.y) == nry + 150
        round(virtualBot.leftMotorPosition.x) == nlx + 150
        round(virtualBot.leftMotorPosition.y) == nly + 150
        virtualBot.orientation == expectedOrientation


        where:
        initialOrientation | rr    | lr    | rx | lx | ry | ly | nrx | nlx | nry | nly | expectedOrientation
        //same sign
        0                  | 1440  | 360   | 0  | 0  | 0  | 20 | 30  | 10  | 20  | 20  | 90  //move ahead & rotate left
        0                  | 360   | 1440  | 0  | 0  | 0  | 20 | 10  | 30  | 0   | 0   | 270 //move ahead & rotate right
        0                  | 360   | 360   | 0  | 0  | 0  | 20 | 10  | 10  | 0   | 20  | 0   //move ahead
        0                  | -1440 | -360  | 0  | 0  | 0  | 20 | -30 | -10 | 20  | 20  | 270 //move back & rotate back right
        0                  | -360  | -1440 | 0  | 0  | 0  | 20 | -10 | -30 | 0   | 0   | 90  //move back & rotate back left
        0                  | -360  | -360  | 0  | 0  | 0  | 20 | -10 | -10 | 0   | 20  | 0   //move back
        90                 | 360   | 360   | 30 | 10 | 20 | 20 | 30  | 10  | 30  | 30  | 90  //move ahead 10 cm after moving ahead and rotating right (first case)
        //opposite sign
        0                  | 1080  | -1080 | 0  | 0  | 0  | 20 | 0   | 0   | 20  | 0   | 180 //180 rotation
        0                  | 2160  | -1080 | 0  | 0  | 0  | 20 | -20 | 0   | 0   | 0   | 270 //180 rotation around center + 90 rotation around left engine
        0                  | 1080  | -2160 | 0  | 0  | 0  | 20 | 0   | 20  | 20  | 20  | 270 //180 rotation around center + 90 rotation around right engine
        0                  | -1080 | 1080  | 0  | 0  | 0  | 20 | 0   | 0   | 20  | 0   | 180 //-180 rotation
        0                  | -2160 | 1080  | 0  | 0  | 0  | 20 | 20  | 0   | 0   | 0   | 90  //-180 rotation around center + 90 rotation around left engine
        0                  | -1080 | 2160  | 0  | 0  | 0  | 20 | 0   | -20 | 20  | 20  | 90  //-180 rotation around center + 90 rotation around right engine

    }

    def 'should scan'() {
        given:
        def map = new VirtualMap()
        VirtualBot virtualBot = new VirtualBot(70, 100, o, map)
        //VirtualMapView mapView = new VirtualMapView(virtualBot)

        when:
        def actualDistances = virtualBot.scan()
        map.markObservedArea(virtualBot.getRotatingSensor(), actualDistances)
        // mapView.repaint()
        //Thread.sleep(5000)

        then:
        actualDistances == expectedDistances as int[]

        where:
        o    | expectedDistances
        0    | [7, 4, 4, 4, 7, 17, 55, 55, 55, 55, 41, 55, 55, 55, 55, 55]
        22.5 | [55, 7, 4, 4, 4, 7, 17, 55, 55, 55, 55, 41, 55, 55, 55, 55]
        45   | [55, 55, 55, 5, 4, 5, 8, 20, 55, 55, 55, 43, 39, 55, 55, 55]
        67.5 | [55, 55, 55, 55, 8, 7, 8, 11, 25, 55, 55, 55, 41, 38, 55, 55]
        90   | [55, 55, 55, 55, 55, 10, 9, 10, 14, 31, 55, 55, 55, 39, 36, 55]
    }
}
