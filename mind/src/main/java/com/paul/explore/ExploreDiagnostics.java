package com.paul.explore;

import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;

import java.io.IOException;

public class ExploreDiagnostics
{

    private static final int SPEED = 360;
    private static final RegulatedMotor leftMotor = Motor.B;
    private static final RegulatedMotor rightMotor = Motor.C;
    private static final EV3TouchSensor touchSensor = new EV3TouchSensor(SensorPort.S1);
    private static boolean rightMotorRotates, leftMotorRotates;
    /**
     * Make sure this constants are the same in the Virtual Bot class, otherwise the displayed map
     * will be inaccurate
     */
    private static final int ROTATIONS_PER_90_DEGREE_ANGLE = 1077;
    private static final int ROTATIONS_PER_CENTIMETER = 36;

    public static void main(String[] args) throws InterruptedException, LowBatteryException, IOException
    {
        setupSpeedsAndMotorsCallbacks();

        rotateRight90DegreesUsing1Motor();
        waitCompleteAbortIfNecessary();
        rotateLeft90DegreesUsing1Motor();
        waitCompleteAbortIfNecessary();
       /* moveAhead10Cm();
        waitCompleteAbortIfNecessary();*/
    }

    private static void waitCompleteAbortIfNecessary()
    {
        while (rightMotorRotates || leftMotorRotates)  //fail safe
        {
            ifIsTouchingObstacleStopMotors();
        }
    }

    private static void rotateRight90DegreesUsing1Motor()
    {
        move(0, ROTATIONS_PER_90_DEGREE_ANGLE);
    }

    private static void rotateLeft90DegreesUsing1Motor()
    {
        move(ROTATIONS_PER_90_DEGREE_ANGLE, 0);
    }

    private static void moveAhead10Cm()
    {
        int _10Cm = 10 * ROTATIONS_PER_CENTIMETER;
        move(_10Cm, _10Cm);
    }

    private static void ifIsTouchingObstacleStopMotors()
    {
        if (isTouchingObstacle())
        {
            rightMotor.stop();
            leftMotor.stop();
        }
    }

    private static boolean isTouchingObstacle()
    {
        float[] sample = new float[1];
        touchSensor.fetchSample(sample, 0);
        return sample[0] == 1;
    }

    private static void setupSpeedsAndMotorsCallbacks() throws LowBatteryException
    {

        if (rightMotor.getMaxSpeed() < SPEED
                || leftMotor.getMaxSpeed() < SPEED)
        {
            throw new LowBatteryException();
        }
        rightMotor.setSpeed(SPEED);
        leftMotor.setSpeed(SPEED);

        rightMotor.addListener(new RegulatedMotorListener()
        {
            @Override
            public void rotationStarted(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp)
            {
                rightMotorRotates = true;
            }

            @Override
            public void rotationStopped(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp)
            {
                rightMotorRotates = false;
            }
        });

        leftMotor.addListener(new RegulatedMotorListener()
        {
            @Override
            public void rotationStarted(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp)
            {
                leftMotorRotates = true;
            }

            @Override
            public void rotationStopped(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp)
            {
                leftMotorRotates = false;
            }
        });
    }

    public static void move(int rightMotorRotation, int leftMotorRotation)
    {
        rightMotor.rotate(rightMotorRotation, true);
        leftMotor.rotate(leftMotorRotation, true);
    }

    private static int[] toIntArray(float[] samples, int length)
    {
        int[] distances = new int[length];
        for (int i = 0; i < length; i++)
        {
            distances[i] = samples[i] < 55 ? Math.round(samples[i]) : 55;
        }
        return distances;
    }
}
