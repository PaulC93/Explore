package com.paul.explore.sim

import spock.lang.Specification

import java.awt.geom.Point2D

import static com.paul.explore.model.GeometryHelper.round

class VirtualBotTest extends Specification {

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
        int cX = (rx + lx) / 2 + 150;
        int cY = (ry + ly) / 2 + 150;
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
        //MAP.createObstacles()
        VirtualBot virtualBot = new VirtualBot(12, 12, o, map)
        //VirtualMapView mapView = new VirtualMapView(virtualBot)

        when:
        def actualDistances = virtualBot.scan()
        //Thread.sleep(2000)

        then:
        actualDistances == expectedDistances as int[]

        where:
        o    | expectedDistances
        0    | [55, 55, 55, 44, 22, 15, 12, 11, 11, 16, 17, 21, 29, 55, 55, 55]
        22.5 | [55, 55, 55, 55, 36, 17, 11, 9, 10, 13, 23, 22, 24, 31, 54, 55]
        /*   45    | [3, 4]
           67.5  | [4, 5]
           90    | [5]
           112.5 | [6]
           135   | [7]
           157.5 | [8]
           180   | [9]
           202.5 | [10]
           225   | [11]
           247.5 | [12]
           270   | [13]
           292.5 | [14]
           315   | [15]
           337.5 | [16]
           360   | [17]*/
    }
}
