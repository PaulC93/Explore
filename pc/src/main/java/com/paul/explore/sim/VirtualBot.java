package com.paul.explore.sim;

import com.paul.explore.model.GeometryHelper;
import com.paul.explore.model.Map;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

import static com.paul.explore.model.BotConstants.*;
import static com.paul.explore.model.GeometryHelper.*;

public class VirtualBot
{
    private static final int MIN_ROTATION_ANGLE = 3; //smaller angle than 3 does nothing while using int coordinates

    private Point2D leftMotorPosition;
    private Point2D rightMotorPosition;
    private Point2D center;
    private RotatingSensor rotatingSensor;
    private Map map;
    private Path2D contour;
    private Rectangle footprint;
    private float orientation; //angle between 0-360 degrees
    private Runnable repaintTrigger;
    private boolean touchingObstacle;

    public VirtualBot(int centerX, int centerY, float orientation, Map map)
    {

        this.center = new Point2D.Float(centerX, centerY);
        this.orientation = orientation;
        this.map = map;

        initMotorAndSensorPositions();
        updateBotPoints();
        map.markAsVisited(footprint);
    }

    private void initMotorAndSensorPositions()
    {
        leftMotorPosition = GeometryHelper.move(center, CENTER_TO_MOTOR_DISTANCE, orientation + 90);
        rightMotorPosition = GeometryHelper.move(center, CENTER_TO_MOTOR_DISTANCE, orientation - 90);
        Point2D sensorRotationPoint = GeometryHelper.move(center, SENSOR_ROTATION_RADIUS, orientation);
        rotatingSensor = new RotatingSensor(sensorRotationPoint, CENTER_TO_IR_SENSOR_ROTATION_POINT_DISTANCE, orientation);
    }

    public int getRightMotorRotation(int[] d)
    {
        return (d[14] - (d[4] - (d[14] - (d[4] - (((d[14] - (d[4] - d[4])) + d[12]) + d[12]))))) + d[14];
    }

    public int getLeftMotorRotations(int[] d)
    {
        return d[4] - (d[10] - (d[4] - (((d[10] - (d[4] - ((d[10] - d[4]) - d[4]))) - d[4]) - d[4])));
    }

    public void move(int rightMotorRotation, int leftMotorRotation)
    {
        if (rightMotorRotation == 0 && leftMotorRotation == 0)
        {
            return;
        }
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

        Consumer<Integer> rotation = rotateAroundCenter();
        rotate(rotation, direction, angle);

        angle = direction * toAngle(Math.abs(absRightMotorRotation - absLeftMotorRotation));
        rotateAroundOneMotor(absRightMotorRotation, absLeftMotorRotation, direction, angle);
    }

    private void rotateAroundOneMotor(int absRightMotorRotation, int absLeftMotorRotation, int direction, float angle)
    {
        Consumer<Integer> rotation;
        rotation = absRightMotorRotation > absLeftMotorRotation ? rotateAroundLeftMotor() : rotateAroundRightMotor();
        rotate(rotation, direction, angle);
    }

    private void rotate(Consumer<Integer> rotation, int unitAngle, float angle)
    {
        //unit by unit to prevent entering an obstacle
        int abs = Math.round(Math.abs(angle));
        for (int i = 0; i < abs; i += MIN_ROTATION_ANGLE)
        {
            rotation.accept(unitAngle);
            updateBotPoints();
            if (repaintTrigger != null)
            {
                repaintTrigger.run();
            }
            map.markAsVisited(footprint);
            updateIsTouchingObstacle();
            if (isTouchingObstacle())
            {
                break;
            }
        }
    }

    private void updateBotPoints()
    {
        updateBotCenter();
        updateRotatingSensor();
        updateBotContourAndFootprint();
    }

    private void updateBotCenter()
    {
        center.setLocation((rightMotorPosition.getX() + leftMotorPosition.getX()) / 2, (rightMotorPosition.getY() + leftMotorPosition.getY()) / 2);
    }

    private void updateRotatingSensor()
    {
        rotatingSensor.reset(GeometryHelper.move(center, CENTER_TO_IR_SENSOR_ROTATION_POINT_DISTANCE, orientation), orientation);
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
        int unitDistance = distance < 0 ? -1 : 1;
        //unit by unit movement for prevent entering an obstacle
        for (int i = 0; i < Math.abs(distance); i++)
        {
            leftMotorPosition = GeometryHelper.move(leftMotorPosition, unitDistance, orientation);
            rightMotorPosition = GeometryHelper.move(rightMotorPosition, unitDistance, orientation);
            updateBotPoints();
            if (repaintTrigger != null)
            {
                repaintTrigger.run();
            }
            map.markAsVisited(footprint);
            updateIsTouchingObstacle();
            if (isTouchingObstacle())
                break;
        }
    }

    private void rotate(int rightMotorRotation, int leftMotorRotation)
    {
        int absRightMotorRotation = Math.abs(rightMotorRotation);
        int absLeftMotorRotation = Math.abs(leftMotorRotation);
        int direction = rightMotorRotation < leftMotorRotation ? -1 : 1; //clockwise or counterclockwise rotation
        float angle = toAngle(rightMotorRotation - leftMotorRotation);

        rotateAroundOneMotor(absRightMotorRotation, absLeftMotorRotation, direction, angle);
    }


    private Consumer<Integer> rotateAroundCenter()
    {
        return (d) ->
        {
            rightMotorPosition = rotateAround(center, rightMotorPosition, d * MIN_ROTATION_ANGLE);
            leftMotorPosition = rotateAround(center, leftMotorPosition, d * MIN_ROTATION_ANGLE);
            setOrientation(orientation + d * MIN_ROTATION_ANGLE);
        };
    }

    private Consumer<Integer> rotateAroundRightMotor()
    {
        return (d) ->
        {
            leftMotorPosition = rotateAround(rightMotorPosition, leftMotorPosition, d * MIN_ROTATION_ANGLE);
            setOrientation(orientation + d * MIN_ROTATION_ANGLE);
        };
    }

    private Consumer<Integer> rotateAroundLeftMotor()
    {
        return (d) ->
        {
            rightMotorPosition = rotateAround(leftMotorPosition, rightMotorPosition, d * MIN_ROTATION_ANGLE);
            setOrientation(orientation + d * MIN_ROTATION_ANGLE);
        };
    }

    /**
     * only for simulation, uses VirtualMap for distances, normal map will throw exception
     */
    public int[] scan()
    {
        float shift = SENSOR_INITIAL_SHIFT;
        int[] distances = new int[NO_OF_DISTANCES_READ];
        for (int i = 0; i < distances.length; i++)
        {
            rotatingSensor.rotate(shift);
            distances[i] = ((VirtualMap) map).getDistance(rotatingSensor.getPosition(), rotatingSensor.getOrientation());
            shift -= SENSOR_MOVEMENT_ANGLE; //22.5 degrees for each side
        }
        return distances;
    }

    public Point2D getLeftMotorPosition()
    {
        return leftMotorPosition;
    }

    public Point2D getRightMotorPosition()
    {
        return rightMotorPosition;
    }

    public float getOrientation()
    {
        return orientation;
    }

    private void setOrientation(float angle)
    {
        orientation = normalizeAngle(angle);
    }

    public Map getMap()
    {
        return map;
    }

    private void updateBotContourAndFootprint()
    {
        contour = new Path2D.Float();
        Point2D frontCenter = GeometryHelper.move(center, HALF_BOT_DIMENSION, orientation); //front
        Point2D frontLeft = GeometryHelper.move(frontCenter, HALF_BOT_DIMENSION, orientation + 90);
        Point2D frontRight = GeometryHelper.move(frontCenter, HALF_BOT_DIMENSION, orientation - 90);

        float backOrientation = this.orientation + 180;
        Point2D backCenter = GeometryHelper.move(center, HALF_BOT_DIMENSION, backOrientation);
        Point2D backLeft = GeometryHelper.move(backCenter, HALF_BOT_DIMENSION, backOrientation - 90);
        Point2D backRight = GeometryHelper.move(backCenter, HALF_BOT_DIMENSION, backOrientation + 90);


        contour.moveTo(frontLeft.getX(), frontLeft.getY());
        contour.lineTo(frontRight.getX(), frontRight.getY());
        contour.lineTo(backRight.getX(), backRight.getY());
        contour.lineTo(backLeft.getX(), backLeft.getY());
        contour.closePath();

        Rectangle2D bounds2D = contour.getBounds2D();
        footprint = new Rectangle(round(bounds2D.getX()), round(bounds2D.getY()), round(bounds2D.getWidth()), round(bounds2D.getHeight()));
    }

    public Point2D getSensorRotationPoint()
    {
        return rotatingSensor.getAnchor();
    }

    public Path2D getContour()
    {
        return contour;
    }

    public void registerRepaintCallback(Runnable runnable)
    {
        repaintTrigger = runnable;
    }

    public boolean isTouchingObstacle()
    {
        return touchingObstacle;
    }

    private void updateIsTouchingObstacle()
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
                return;
            }
        }

        x = maxX;
        for (int y = minY; y < maxY; y++)
        {
            if (map.isObstacle(x, y))
            {
                touchingObstacle = true;
                return;
            }
        }

        int y = minY;
        for (int xi = minX; xi < maxX; xi++)
        {
            if (map.isObstacle(xi, y))
            {
                touchingObstacle = true;
                return;
            }
        }

        y = maxY;
        for (int xi = minX; xi < maxX; xi++)
        {
            if (map.isObstacle(xi, y))
            {
                touchingObstacle = true;
                return;
            }
        }
    }

    public RotatingSensor getRotatingSensor()
    {
        return rotatingSensor;
    }
}
