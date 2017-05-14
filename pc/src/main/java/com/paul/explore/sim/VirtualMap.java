package com.paul.explore.sim;

import com.paul.explore.model.Map;

import java.awt.geom.Point2D;

import static com.paul.explore.model.GeometryHelper.move;
import static com.paul.explore.model.GeometryHelper.round;
import static com.paul.explore.model.BotConstants.MAX_OBSERVABLE_DISTANCE;

public class VirtualMap extends Map {

    private static final int DIMENSION = 200;

    public VirtualMap(){
       this(DIMENSION);
    }

    public VirtualMap(int dimension)
    {
        super(dimension);
        createObstacles();
    }

    private void createObstacles() {

        //create obstacles //TODO more real life like
        int usualObstacleSize = 30;
        int center = getWidth() / 2;
        int halfUsualObstacleSize = usualObstacleSize / 2;
        int centerLeft = center - halfUsualObstacleSize;
        int centerRight = center + halfUsualObstacleSize;
        int middle = getHeight() / 2;
        int middleTop = middle + halfUsualObstacleSize;
        int middleBottom = middle - halfUsualObstacleSize;
        int top = getHeight();
        int left = 0;
        int right = getWidth();

        for (int x = left; x < usualObstacleSize; x++) {
            for (int y = middleBottom; y < middleTop; y++) {
                map[x][y] = OBSTACLE;
            }
        }

        for (int x = centerLeft; x < centerRight; x++) {
            for (int y = top - usualObstacleSize; y < top; y++) {
                map[x][y] = OBSTACLE;
            }
        }

        for (int x = centerLeft; x < centerRight; x++) {
            for (int y = middleBottom - usualObstacleSize; y < middleTop; y++) {
                map[x][y] = OBSTACLE;
            }
        }

        for (int x = right - usualObstacleSize; x < right; x++) {
            for (int y = middleBottom; y < middleTop; y++) {
                map[x][y] = OBSTACLE;
            }
        }
    }

    int getDistance(Point2D sensorPosition, float orientation) {
        for (int i = 0; i < MAX_OBSERVABLE_DISTANCE; i++) {
            Point2D sensedPoint = move(sensorPosition, i, orientation);
            int x = round(sensedPoint.getX());
            int y = round(sensedPoint.getY());
            if (x < 1 || x > getHeight() || y < 1 || y > getWidth() //outside of map
                    || map[x][y] == OBSTACLE) { //actual obstacle
                return i;
            }
        }
        return MAX_OBSERVABLE_DISTANCE;
    }
}