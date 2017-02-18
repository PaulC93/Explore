package com.paul.explore;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import static com.paul.explore.sim.GeometryHelper.*;

public class Map {

    private static final int UNKNOWN = 0; //default
    protected static final int FREE = 1;
    private static final int VISITED = 2;
    protected static final int OBSTACLE = 3;
    protected byte[][] map;

    public Map() {
        map = new byte[800][800];
    }

    void addArea(Point2D botCenter, float orientation, int[] distances) {

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

            freeAreaMinX = updateMinIfNecessary(freeAreaMinX, x);
            freeAreaMaxX = updateMaxIfNecessary(freeAreaMaxX, x);
            freeAreaMinY = updateMinIfNecessary(freeAreaMinY, y);
            freeAreaMaxY = updateMaxIfNecessary(freeAreaMaxY, y);
        }

        //FREE
        for (int x = freeAreaMinX; x < freeAreaMaxX; x++) {
            for (int y = freeAreaMinY; y < freeAreaMaxY; y++) {
                if (map[x][y] == UNKNOWN) {
                    if (freeArea.contains(x, y)) {
                        map[x][y] = FREE;
                    }
                }
            }
        }
    }

    private static int updateMaxIfNecessary(int currentMax, int potentialNewMax) {
        return potentialNewMax > currentMax ? potentialNewMax : currentMax;
    }

    private static int updateMinIfNecessary(int currentMin, int potentialNewMin) {
        return potentialNewMin < currentMin ? potentialNewMin : currentMin;
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
        return isOutsideOfMap(x, y) || map[x][y] == VISITED;
    }

    public boolean isFree(int x, int y) {
        //while trying to sense something outside the map return false
        return !isOutsideOfMap(x, y) && map[x][y] == FREE;
    }

    private boolean isOutsideOfMap(int x, int y) {
        return x < 0 || y < 0 || x > getWidth() || y > getHeight();
    }

    public int markAsVisited(Rectangle footprint) {

        int w = getWidth();
        int h = getHeight();

        int startX = footprint.x;
        int endX = round(footprint.getMaxX());
        if (startX < 0) startX = 0;
        if (startX > w) return 0;
        if (endX < 0) return 0;
        if (endX > w) endX = w;

        int startY = footprint.y;
        int endY = round(footprint.getMaxY());
        if (startY < 0) startY = 0;
        if (startY > h) return 0;
        if (endY < 0) return 0;
        if (endY > h) endY = h;

        return getNoOfNewVisitedPoints(startX, endX, startY, endY);
    }

    private int getNoOfNewVisitedPoints(int startX, int endX, int startY, int endY) {
        int noOfNewVisitedPoints = 0;
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                if (map[x][y] < 2) {  //different than obstacle should always be true
                    map[x][y] = VISITED;                             // the VirtualBot could be improved when bot moves don't stop when the contour reaches
                    noOfNewVisitedPoints++;                         // obstacle but when a corner touches an obstacle the rest of the bot continues
                }
            }
        }
        return noOfNewVisitedPoints;
    }
}
