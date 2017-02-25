package com.paul.explore.model;

import java.awt.*;
import java.awt.geom.Point2D;

import static com.paul.explore.model.GeometryHelper.*;

public class Map {

    private static final int UNKNOWN = 0;   //000 //default
    private static final int FREE = 1;      //001
    private static final int VISITED = 2;   //010
    protected static final int OBSTACLE = 4;//100
    protected byte[][] map;
    private final int DIMENSION;

    public Map(int dimension) {
        DIMENSION = dimension;
        map = new byte[DIMENSION][DIMENSION];
    }

    public void markFreeArea(Point2D botCenter, float orientation, int[] distances) {

        Point2D sensorRotationPoint = move(botCenter, 5, orientation);
        Polygon freeArea = new Polygon();
        float sensorOrientation = orientation + 45;

        int freeAreaMinX = Integer.MAX_VALUE;
        int freeAreaMaxX = Integer.MIN_VALUE;
        int freeAreaMinY = Integer.MAX_VALUE;
        int freeAreaMaxY = Integer.MIN_VALUE;

        for (int distance : distances) {
            sensorOrientation -= 22.5;
            Point2D sensorPosition = move(sensorRotationPoint, 5, sensorOrientation);
            sensorPosition = rotateAround(sensorRotationPoint, sensorPosition, sensorOrientation);

            //FREE
            Point2D sensedPoint = move(sensorPosition, distance, sensorOrientation);
            int x = round(sensedPoint.getX());
            int y = round(sensedPoint.getY());
            freeArea.addPoint(x, y);

            freeAreaMinX = forceInMapBoundaries(updateMinIfNecessary(freeAreaMinX, x));
            freeAreaMaxX = forceInMapBoundaries(updateMaxIfNecessary(freeAreaMaxX, x));
            freeAreaMinY = forceInMapBoundaries(updateMinIfNecessary(freeAreaMinY, y));
            freeAreaMaxY = forceInMapBoundaries(updateMaxIfNecessary(freeAreaMaxY, y));
        }

        //FREE
        for (int x = freeAreaMinX; x < freeAreaMaxX; x++) {
            for (int y = freeAreaMinY; y < freeAreaMaxY; y++) {
                if (map[x][y] == UNKNOWN) {
                    if (freeArea.contains(x, y)) {
                        markAsFree(x, y);
                    }
                }
            }
        }
    }

    private void markAsFree(int x, int y) {
        map[x][y] |= FREE;
    }

    private static int updateMaxIfNecessary(int currentMax, int potentialNewMax) {
        return potentialNewMax > currentMax ? potentialNewMax : currentMax;
    }

    private static int updateMinIfNecessary(int currentMin, int potentialNewMin) {
        return potentialNewMin < currentMin ? potentialNewMin : currentMin;
    }

    private int forceInMapBoundaries(int p) {
        if (p < 0) return 0;
        if (p > DIMENSION) return DIMENSION - 1;
        return p;
    }

    public boolean isObstacle(Point sensedPoint) {
        //while trying to sense something outside the map return true
        return isOutsideOfMap(sensedPoint.x, sensedPoint.y) || map[sensedPoint.x][sensedPoint.y] == OBSTACLE;
    }

    public int getHeight() {
        return map.length - 1;
    }

    public int getWidth() {
        return map[0].length - 1;
    }

    public boolean isObstacle(int x, int y) {
        return isObstacle(new Point(x, y));
    }

    public boolean isVisited(int x, int y) {
        //while trying to sense something outside the map return true
        return isOutsideOfMap(x, y) || (map[x][y] & VISITED) == VISITED;
    }

    public boolean isFree(int x, int y) {
        //while trying to sense something outside the map return false
        return !isOutsideOfMap(x, y) && (map[x][y] & FREE) == FREE;
    }

    private boolean isOutsideOfMap(int x, int y) {
        return x < 0 || y < 0 || x > getWidth() || y > getHeight();
    }

    public void markAsVisited(Rectangle footprint) {

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

    private void markAreaAsVisited(int startX, int endX, int startY, int endY) {
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                if (map[x][y] < 4) {  //different than obstacle should always be true
                    markAsVisited(x, y);    // the VirtualBot could be improved when bot moves don't stop when the contour reaches
                   // obstacle but when a corner touches an obstacle the rest of the bot continues
                }
            }
        }
    }

    private void markAsVisited(int x, int y) {
        map[x][y] |= VISITED;
    }

    public int getNoOfPointsObservedAsFree() {
        int noOfPointsObservedAsFree = 0;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                noOfPointsObservedAsFree += isFree(i, j) ? 1 : 0;
            }
        }
        return noOfPointsObservedAsFree;
    }

    public int getNoOfNonObstaclePoints() {
        int noOfNonObstaclesPoints = 0;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                noOfNonObstaclesPoints += isObstacle(i, j) ? 0 : 1;
            }
        }
        return noOfNonObstaclesPoints;
    }
}
