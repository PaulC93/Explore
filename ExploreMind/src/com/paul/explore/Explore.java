package com.paul.explore;

import lejos.hardware.Button;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;
import lejos.utility.Delay;

import java.io.IOException;
import java.util.Arrays;

public class Explore
{

    private static final int NO_OF_DISTANCE_SAMPLES = 16;
    private static final int SPEED = 360;
    private static final RegulatedMotor leftMotor = Motor.B;
    private static final RegulatedMotor rightMotor = Motor.C;
    private static final RegulatedMotor sensorMotor = Motor.A;
    private static final EV3IRSensor proximitySensor = new EV3IRSensor(SensorPort.S4);
    private static final EV3TouchSensor touchSensor = new EV3TouchSensor(SensorPort.S1);
    private static boolean sensorMotorRotates;
    private static boolean rightMotorRotates, leftMotorRotates;

    public static void main(String[] args)
    {
        try
        {
            setupSpeedsAndMotorsCallbacks();
            System.out.println("Waiting PC connection...");
            PcConnection connection = new PcConnection();

            while (Button.ENTER.isUp())
            {
                int distances[] = scan(); //sense

                int rightMotorRotation = getRightMotorRotation(distances);
                int leftMotorRotations = getLeftMotorRotations(distances);
                connection.sendSensorsAndMotorData(distances, rightMotorRotation, leftMotorRotations);

                move(rightMotorRotation, leftMotorRotations); //act

                while (rightMotorRotates || leftMotorRotates)
                { //fail safe
                    ifIsTouchingObstacleStopMotors();
                }
            }
            connection.close();
        } catch (IOException e)
        {
            stopAllMovement();
            System.out.println("Connection problems, please retry");
        } catch (LowBatteryException e)
        {
            stopAllMovement();
            System.out.println("Battery level to low, please change the battery and try again");
        }
    }

    /**
     * some async motor movement could still take place
     * stop it
     */
    private static void stopAllMovement()
    {
        rightMotor.stop();
        leftMotor.stop();
        sensorMotor.stop();
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

    public static int getRightMotorRotation(int[] d)
    {
        return ((d[0] + d[0]) + ((d[0] + (((d[0] + d[0]) - d[4]) - d[4])) - d[4])) + (d[0] + d[0]);
    }

    public static int getLeftMotorRotations(int[] d)
    {
        return d[10] + (d[2] + ((d[10] + (d[2] + (d[2] + (((d[10] - d[14]) - d[14]) + d[4])))) - d[14]));
    }

    private static void setupSpeedsAndMotorsCallbacks() throws LowBatteryException
    {

        if (rightMotor.getMaxSpeed() < SPEED
                || leftMotor.getMaxSpeed() < SPEED
                || sensorMotor.getMaxSpeed() < SPEED)
        {
            throw new LowBatteryException();
        }
        rightMotor.setSpeed(SPEED);
        leftMotor.setSpeed(SPEED);
        sensorMotor.setSpeed(SPEED);

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

        sensorMotor.addListener(new RegulatedMotorListener()
        {
            @Override
            public void rotationStarted(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp)
            {
                sensorMotorRotates = true;
            }

            @Override
            public void rotationStopped(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp)
            {
                sensorMotorRotates = false;
            }
        });
    }

    public static void move(int rightMotorRotation, int leftMotorRotation)
    {
        rightMotor.rotate(rightMotorRotation, true);
        leftMotor.rotate(leftMotorRotation, true);
    }

    private static int[] scan()
    {

        //sensor rotates clockwise
        sensorMotor.rotate(-45); //assume sensor face straight ahead, move slightly left
        sensorMotor.rotate(360, true);

        float[] samples = new float[NO_OF_DISTANCE_SAMPLES];
        int offset = 0;
        while (sensorMotorRotates)
        {
            if (offset >= NO_OF_DISTANCE_SAMPLES)
            {
                break;
            }
            proximitySensor.fetchSample(samples, offset);
            Delay.msDelay(85);
            offset++;
        }

        sensorMotor.waitComplete();
        sensorMotor.rotate(-315, true); //-360 + 45 to face straight ahead again

        int[] distances = toIntArray(samples, offset);

        sensorMotor.waitComplete();
        return distances;
    }

    private static int[] toIntArray(float[] samples, int length)
    {
        int[] distances = new int[length];
        for (int i = 0; i < length; i++)
        {
            distances[i] = samples[i] < 55 ? Math.round(samples[i]) : 55;
        }
        System.out.println(Arrays.toString(distances));
        return distances;
    }
}
