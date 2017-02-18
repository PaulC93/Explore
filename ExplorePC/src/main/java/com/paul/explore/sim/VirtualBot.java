package com.paul.explore.sim;

import com.paul.explore.Map;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.IntFunction;

import static com.paul.explore.sim.GeometryHelper.*;

public class VirtualBot
{

    public static final int SENSOR_ROTATION_RADIUS = 5;

    public static final int BOT_SIZE = 24;
    private static final int MIN_ROTATION_ANGLE = 3; //smaller angle than 3 does nothing while using int coordinates
    private static final int ROTATIONS_PER_90_DEGREE_ANGLE = 1077;
    private static final int ROTATIONS_PER_CENTIMETER = 36;

    private Point2D leftMotorPosition;
    private Point2D rightMotorPosition;
    private Point2D previousCenter;
    private Point2D center;
    private Point2D sensorRotationPoint;
    private Map map;
    private Path2D contour;
    private Rectangle footprint;
    private float orientation; //angle between 0-360 degrees
    private int noOfNewVisitedPoints; //no of points changed as visited in the last move (move(rr,lr) called from outside)
    private Runnable repaintTrigger;
    private boolean touchingObstacle;

    public VirtualBot(int centerX, int centerY, float orientation, Map map)
    {

        this.center = new Point2D.Float(centerX, centerY);
        this.previousCenter = center;
        this.orientation = orientation;
        this.map = map;

        initMotorPositions();
        updateBotPoints();
        map.markAsVisited(footprint);
    }

    private void initMotorPositions()
    {
        leftMotorPosition = GeometryHelper.move(center, 10, orientation + 90);
        rightMotorPosition = GeometryHelper.move(center, 10, orientation - 90);
    }

    public int getRightMotorRotation(int[] d)
    {
        return (((d[0] + d[0]) + ((d[0] + (((d[0] + d[0]) - d[4]) - d[4])) - d[4])) + (d[0] + d[0]));
    }

    public int getLeftMotorRotations(int[] d)
    {
        return (d[10] + (d[2] + ((d[10] + (d[2] + (d[2] + (((d[10] - d[14]) - d[14]) + d[4])))) - d[14])));
    }

    public void move(int rightMotorRotation, int leftMotorRotation)
    {
        previousCenter = new Point2D.Float((float) center.getX(), (float) center.getY());
        if (rightMotorRotation == 0 && leftMotorRotation == 0)
        {
            return;
        }

        noOfNewVisitedPoints = 0;
        if ((rightMotorRotation > 0 && leftMotorRotation > 0) || (rightMotorRotation < 0 && leftMotorRotation < 0))
        {
            sameSignMovement(rightMotorRotation, leftMotorRotation);
        } else
        {
            oppositeSignMovement(rightMotorRotation, leftMotorRotation);
        }
    }

    private void oppositeSignMovement(int rightMotorRotation, int leftMotorRotation)
    {

        int absRightMotorRotation = Math.abs(rightMotorRotation);
        int absLeftMotorRotation = Math.abs(leftMotorRotation);
        int rotations = Math.min(absRightMotorRotation, absLeftMotorRotation);
        int direction = rightMotorRotation < leftMotorRotation ? -1 : 1; //clockwise or counterclockwise rotation
        float angle = 2 * toAngle(rotations); // * 2 because both motors rotate in opposite direction

        IntFunction rotation = rotateAroundCenter();
        rotate(rotation, direction, angle);

        if (absRightMotorRotation > absLeftMotorRotation)
        {
            angle = direction * toAngle(absRightMotorRotation - absLeftMotorRotation);
            rotation = rotateAroundLeftMotor();

        } else
        {
            angle = direction * toAngle(absLeftMotorRotation - absRightMotorRotation);
            rotation = rotateAroundRightMotor();
        }
        rotate(rotation, direction, angle);
    }

    private void rotate(IntFunction rotation, int direction, float angle)
    {
        //unit by unit to prevent entering an obstacle
        int abs = Math.round(Math.abs(angle));
        for (int i = 0; i < abs; i += MIN_ROTATION_ANGLE)
        {
            rotation.apply(direction);
            updateBotPoints();
            if (repaintTrigger != null) repaintTrigger.run();
            markNewAreaAsVisited();
            updateIsTouchingObstacle();
            if (isTouchingObstacle())
            {
                break;
            }
        }
    }

    private void markNewAreaAsVisited()
    {
        noOfNewVisitedPoints += map.markAsVisited(footprint);
    }

    private void updateBotPoints()
    {
        updateBotCenter();
        updateSensorRotationPoint();
        updateBotContourAndFootprint();
    }

    private void updateBotCenter()
    {
        center.setLocation((rightMotorPosition.getX() + leftMotorPosition.getX()) / 2, (rightMotorPosition.getY() + leftMotorPosition.getY()) / 2);
    }

    private void updateSensorRotationPoint()
    {
        sensorRotationPoint = GeometryHelper.move(center, SENSOR_ROTATION_RADIUS, orientation);
    }

    private void sameSignMovement(int rightMotorRotation, int leftMotorRotation)
    {
        if (rightMotorRotation > 0 && leftMotorRotation > 0)
        {
            moveAheadAndRotate(rightMotorRotation, leftMotorRotation);
        } else if (rightMotorRotation < 0 && leftMotorRotation < 0)
        {
            moveBackAndRotate(rightMotorRotation, leftMotorRotation);
        }
    }

    private void moveAheadAndRotate(int rightMotorRotation, int leftMotorRotation)
    {
        move(Math.min(rightMotorRotation, leftMotorRotation));
        rotate(rightMotorRotation, leftMotorRotation);
    }

    private void moveBackAndRotate(int rightMotorRotation, int leftMotorRotation)
    {
        move(Math.max(rightMotorRotation, leftMotorRotation));
        rotate(rightMotorRotation, leftMotorRotation);
    }

    private void move(int distance)
    {
        distance = toCentimeters(distance);
        int direction = distance < 0 ? -1 : 1;
        //unit by unit movement for prevent entering an obstacle
        for (int i = 0; i < Math.abs(distance); i++)
        {
            leftMotorPosition = GeometryHelper.move(leftMotorPosition, direction, orientation);
            rightMotorPosition = GeometryHelper.move(rightMotorPosition, direction, orientation);
            updateBotPoints();
            if (repaintTrigger != null) repaintTrigger.run();
            markNewAreaAsVisited();
            updateIsTouchingObstacle();
            if (isTouchingObstacle())
                break;
        }
    }

    private int toCentimeters(int distance)
    {
        return distance / ROTATIONS_PER_CENTIMETER; //360 degrees -> 10 cm
    }

    private void rotate(int rightMotorRotation, int leftMotorRotation)
    {

        int absRightMotorRotation = Math.abs(rightMotorRotation);
        int absLeftMotorRotation = Math.abs(leftMotorRotation);
        int direction = rightMotorRotation < leftMotorRotation ? -1 : 1; //clockwise or counterclockwise rotation
        float angle = toAngle(rightMotorRotation - leftMotorRotation);

        IntFunction rotation;
        if (absRightMotorRotation > absLeftMotorRotation)
        {
            rotation = rotateAroundLeftMotor();
        } else
        {
            rotation = rotateAroundRightMotor();
        }

        rotate(rotation, direction, angle);
    }


    private IntFunction rotateAroundCenter()
    {
        return (d) ->
        {
            rightMotorPosition = rotateAround(center, rightMotorPosition, d * MIN_ROTATION_ANGLE);
            leftMotorPosition = rotateAround(center, leftMotorPosition, d * MIN_ROTATION_ANGLE);
            setOrientation(orientation + d * MIN_ROTATION_ANGLE);
            return 0; //ignored
        };
    }

    private IntFunction rotateAroundRightMotor()
    {
        return (d) -> {
            leftMotorPosition = rotateAround(rightMotorPosition, leftMotorPosition, d * MIN_ROTATION_ANGLE);
            setOrientation(orientation + d * MIN_ROTATION_ANGLE);
            return 0; //ignored
        };
    }

    private IntFunction rotateAroundLeftMotor()
    {
        return (d) -> {
            rightMotorPosition = rotateAround(leftMotorPosition, rightMotorPosition, d * MIN_ROTATION_ANGLE);
            setOrientation(orientation + d * MIN_ROTATION_ANGLE);
            return 0; //ignored
        };
    }

    private int toAngle(int rotations)
    {
        return (90 * rotations / ROTATIONS_PER_90_DEGREE_ANGLE);
    }

    public int[] scan()
    {
        return getDistances();
    }

    /**
     * only for simulation, uses VirtualMap for distances, normal map will throw exception
     */
    public int[] getDistances()
    {
        Point2D sensorPosition = GeometryHelper.move(sensorRotationPoint, SENSOR_ROTATION_RADIUS, orientation);
        float sensorOrientation = orientation + 45;
        sensorPosition = rotateAround(sensorRotationPoint, sensorPosition, sensorOrientation); //sensor face straight ahead (same orientation as bot), move slightly left

        int[] distances = new int[16];
        for (int i = 0; i < distances.length; i++)
        {
            distances[i] = ((VirtualMap) map).getDistance(sensorPosition, sensorOrientation);
            sensorPosition = rotateAround(sensorRotationPoint, sensorPosition, 22.5f);
            sensorOrientation -= 22.5; //22.5 degrees for each side
        }
        return distances;
    }

    public Point2D getLeftMotorPosition()
    {
        return leftMotorPosition;
    }

    public void setLeftMotorPosition(Point2D p)
    {
        leftMotorPosition = p;
    }

    public Point2D getRightMotorPosition()
    {
        return rightMotorPosition;
    }

    public void setRightMotorPosition(Point2D p)
    {
        rightMotorPosition = p;
    }

    public float getOrientation()
    {
        return orientation;
    }

    public void setOrientation(float angle)
    {
        orientation = normalizeAngle(angle);
    }

    public boolean touchSensorIsTouchingObstacle()
    {
        Point2D sensorPosition = GeometryHelper.move(center, 11, orientation); //11cm from bot center till touch sensor
        return map.isObstacle(round(sensorPosition.getX()), round(sensorPosition.getY()));
    }

    public boolean isInTheSameSpotAsBefore(int tolerance)
    {

        if (previousCenter.equals(center))
        {
            return true;
        }
        int px = round(previousCenter.getX());
        int py = round(previousCenter.getY());
        int minX = px - tolerance;
        int minY = px - tolerance;
        int maxX = px + tolerance;
        int maxY = py + tolerance;
        for (int x = minX; x < maxX; x++)
        {
            for (int y = minY; y < maxY; y++)
            {
                if ((new Point(x, y)).equals(center))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public Map getMap()
    {
        return map;
    }

    private void updateBotContourAndFootprint()
    {
        contour = new Path2D.Float();
        Point2D frontCenter = GeometryHelper.move(center, BOT_SIZE / 2, orientation); //front
        Point2D frontLeft = GeometryHelper.move(frontCenter, BOT_SIZE / 2, orientation + 90);
        Point2D frontRight = GeometryHelper.move(frontCenter, BOT_SIZE / 2, orientation - 90);

        float backOrientation = this.orientation + 180;
        Point2D backCenter = GeometryHelper.move(center, BOT_SIZE / 2, backOrientation);
        Point2D backLeft = GeometryHelper.move(backCenter, BOT_SIZE / 2, backOrientation - 90);
        Point2D backRight = GeometryHelper.move(backCenter, BOT_SIZE / 2, backOrientation + 90);


        contour.moveTo(frontLeft.getX(), frontLeft.getY());
        contour.lineTo(frontRight.getX(), frontRight.getY());
        contour.lineTo(backRight.getX(), backRight.getY());
        contour.lineTo(backLeft.getX(), backLeft.getY());
        contour.closePath();

        Rectangle2D bounds2D = contour.getBounds2D();
        footprint = new Rectangle(round(bounds2D.getX()), round(bounds2D.getY()), round(bounds2D.getWidth()), round(bounds2D.getHeight()));
    }

    public int getNoOfNewVisitedPoints()
    {
        return noOfNewVisitedPoints;
    }

    public Rectangle getFootprint()
    {
        return footprint;
    }

    public Point2D getSensorRotationPoint()
    {
        return sensorRotationPoint;
    }

    public Point2D getCenter()
    {
        return center;
    }

    public Path2D getContour()
    {
        return contour;
    }

    public void registerRepaintCallBack(Runnable runnable)
    {
        repaintTrigger = runnable;
    }

    public boolean isTouchingObstacle()
    {
        return touchingObstacle;
    }

    public boolean updateIsTouchingObstacle()
    {
        touchingObstacle = false;

        int minX = round(footprint.getMinX());
        int minY = round(footprint.getMinY());
        int maxY = round(footprint.getMaxY());
        int maxX = round(footprint.getMaxX());

        int x = minX;
        for (int y = minY; y < maxY; y++)
        {
            if (map.isObstacle(x, y))
            {
                touchingObstacle = true;
                return true;
            }
        }

        x = maxX;
        for (int y = minY; y < maxY; y++)
        {
            if (map.isObstacle(x, y))
            {
                touchingObstacle = true;
                return true;
            }
        }

        int y = minY;
        for (int xi = minX; xi < maxX; xi++)
        {
            if (map.isObstacle(xi, y))
            {
                touchingObstacle = true;
                return true;
            }
        }

        y = maxY;
        for (int xi = minX; xi < maxX; xi++)
        {
            if (map.isObstacle(xi, y))
            {
                touchingObstacle = true;
                return true;
            }
        }
        return touchingObstacle;
    }
}
