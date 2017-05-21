package com.paul.explore.model;

import com.paul.explore.sim.RotatingSensor;

import java.awt.*;
import java.awt.geom.Point2D;

import static com.paul.explore.model.BotConstants.SENSOR_INITIAL_SHIFT;
import static com.paul.explore.model.BotConstants.SENSOR_MOVEMENT_ANGLE;
import static com.paul.explore.model.GeometryHelper.move;
import static com.paul.explore.model.GeometryHelper.round;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class Map
{
    private static final int UNKNOWN = 0;   //000 //default
    private static final int FREE = 1;      //001
    private static final int VISITED = 2;   //010
    protected static final int OBSTACLE = 4;//100
    protected static final int OBSERVED_AS_OBSTACLE = 8;//1000
    protected byte[][] map;
    private final int dimension;

    public Map(int dimension)
    {
        this.dimension = dimension;
        map = new byte[dimension][dimension];
    }

    public void markObservedArea(RotatingSensor rotatingSensor, int[] distances)
    {
        Polygon freeArea = new Polygon();
        int freeAreaMinX = Integer.MAX_VALUE;
        int freeAreaMaxX = Integer.MIN_VALUE;
        int freeAreaMinY = Integer.MAX_VALUE;
        int freeAreaMaxY = Integer.MIN_VALUE;

        float shift = SENSOR_INITIAL_SHIFT;

        for (int distance : distances)
        {
            rotatingSensor.rotate(shift);
            Point2D sensedPoint = move(rotatingSensor.getPosition(), distance, rotatingSensor.getOrientation());

            int x = round(sensedPoint.getX());
            int y = round(sensedPoint.getY());

            freeArea.addPoint(x, y);
            freeAreaMinX = forceInMapBoundaries(min(freeAreaMinX, x));
            freeAreaMaxX = forceInMapBoundaries(max(freeAreaMaxX, x));
            freeAreaMinY = forceInMapBoundaries(min(freeAreaMinY, y));
            freeAreaMaxY = forceInMapBoundaries(max(freeAreaMaxY, y));

            if (distance < 55)
            {
                markObservedAsObstacle(sensedPoint);
            }
            shift -= SENSOR_MOVEMENT_ANGLE;
        }

        //FREE
        for (int x = freeAreaMinX; x < freeAreaMaxX; x++)
        {
            for (int y = freeAreaMinY; y < freeAreaMaxY; y++)
            {
                if (map[x][y] == UNKNOWN)
                {
                    if (freeArea.contains(x, y))
                    {
                        markAsFree(x, y);
                    }
                }
            }
        }
    }

    private void markObservedAsObstacle(Point2D obstaclePoint)
    {
        int x = round(obstaclePoint.getX());
        int y = round(obstaclePoint.getY());
        if (!isOutsideOfMap(x, y))
        {
            map[x][y] |= OBSERVED_AS_OBSTACLE;
        }
    }

    public boolean isObservedAsObstacle(int x, int y)
    {
        return (map[x][y] & OBSERVED_AS_OBSTACLE) == OBSERVED_AS_OBSTACLE;
    }

    private void markAsFree(int x, int y)
    {
        map[x][y] |= FREE;
    }

    private int forceInMapBoundaries(int p)
    {
        if (p < 0) return 0;
        if (p > dimension) return dimension - 1;
        return p;
    }

    public boolean isObstacle(Point sensedPoint)
    {
        //while trying to sense something outside the map return true
        return isOutsideOfMap(sensedPoint.x, sensedPoint.y) || (map[sensedPoint.x][sensedPoint.y] & OBSTACLE) == OBSTACLE;
    }

    public int getHeight()
    {
        return map.length - 1;
    }

    public int getWidth()
    {
        return map[0].length - 1;
    }

    public boolean isObstacle(int x, int y)
    {
        return isObstacle(new Point(x, y));
    }

    public boolean isVisited(int x, int y)
    {
        //while trying to sense something outside the map return true
        return isOutsideOfMap(x, y) || (map[x][y] & VISITED) == VISITED;
    }

    public boolean isFree(int x, int y)
    {
        //while trying to sense something outside the map return false
        return !isOutsideOfMap(x, y) && (map[x][y] & FREE) == FREE;
    }

    private boolean isOutsideOfMap(int x, int y)
    {
        return x < 0 || y < 0 || x > getWidth() || y > getHeight();
    }

    public void markAsVisited(Rectangle footprint)
    {

        int w = getWidth();
        int h = getHeight();

        int startX = footprint.x;
        int endX = round(footprint.getMaxX());
        if (startX < 0) startX = 0;
        if (startX > w) return;
        if (endX < 0) return;
        if (endX > w) endX = w;

        int startY = footprint.y;
        int endY = round(footprint.getMaxY());
        if (startY < 0) startY = 0;
        if (startY > h) return;
        if (endY < 0) return;
        if (endY > h) endY = h;

        markAreaAsVisited(startX, endX, startY, endY);
    }

    private void markAreaAsVisited(int startX, int endX, int startY, int endY)
    {
        for (int x = startX; x < endX; x++)
        {
            for (int y = startY; y < endY; y++)
            {
                if (map[x][y] < 4)
                {  //different than obstacle should always be true
                    markAsVisited(x, y);    // the VirtualBot could be improved when bot moves don't stop when the contour reaches
                    // obstacle but when a corner touches an obstacle the rest of the bot continues
                }
            }
        }
    }

    private void markAsVisited(int x, int y)
    {
        map[x][y] |= VISITED;
    }

    public int getNoOfPointsObservedAsFree()
    {
        int noOfPointsObservedAsFree = 0;
        for (int i = 0; i < map.length; i++)
        {
            for (int j = 0; j < map[0].length; j++)
            {
                noOfPointsObservedAsFree += isFree(i, j) ? 1 : 0;
            }
        }
        return noOfPointsObservedAsFree;
    }

    public int getNoOfNonObstaclePoints()
    {
        int noOfNonObstaclesPoints = 0;
        for (int i = 0; i < map.length; i++)
        {
            for (int j = 0; j < map[0].length; j++)
            {
                noOfNonObstaclesPoints += isObstacle(i, j) ? 0 : 1;
            }
        }
        return noOfNonObstaclesPoints;
    }
}
