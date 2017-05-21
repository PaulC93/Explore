package com.paul.explore.model;

public final class BotConstants
{
    public static final int SENSOR_ROTATION_RADIUS = 5;
    public static final int BOT_DIMENSION = 24; //length and width both equal 24
    public static final int HALF_BOT_DIMENSION = BOT_DIMENSION / 2;
    public static final int ROTATIONS_PER_ANGLE = 12;
    public static final int ROTATIONS_PER_CENTIMETER = 36;
    public static final int CENTER_TO_MOTOR_DISTANCE = 10;
    public static final int SENSOR_INITIAL_SHIFT = 45;
    public static final float SENSOR_MOVEMENT_ANGLE = 22.5f;
    public static final int MAX_OBSERVABLE_DISTANCE = 55;
    public static final int NO_OF_DISTANCES_READ = 16;
    public static final int CENTER_TO_TOUCH_SENSOR_DISTANCE = 11;
    public static final int CENTER_TO_IR_SENSOR_ROTATION_POINT_DISTANCE = 5;

    public static int toCentimeters(int rotations)
    {
        return rotations / ROTATIONS_PER_CENTIMETER; //360 degrees -> 10 cm
    }

    public static int toAngle(int rotations)
    {
        return (rotations / ROTATIONS_PER_ANGLE);
    }
}
