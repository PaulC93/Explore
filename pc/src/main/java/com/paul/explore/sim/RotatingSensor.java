package com.paul.explore.sim;

import com.paul.explore.model.GeometryHelper;

import java.awt.geom.Point2D;

public class RotatingSensor
{
    private Point2D anchor;
    private int rotationRadius;
    private Point2D position;
    private Point2D initialPosition;
    private float initialOrientation;
    private float orientation;

    public RotatingSensor(Point2D anchor, int rotationRadius, float orientation)
    {
        this.anchor = anchor;
        this.rotationRadius = rotationRadius;
        this.orientation = orientation;
        this.initialOrientation = orientation;
        position = GeometryHelper.move(this.anchor, this.rotationRadius, this.orientation);
        initialPosition = position;
    }

    public Point2D getPosition()
    {
        return position;
    }

    public float getOrientation()
    {
        return orientation;
    }

    public Point2D getAnchor()
    {
        return anchor;
    }

    public void reset(Point2D anchor, float orientation)
    {
        this.anchor = anchor;
        this.orientation = orientation;
        initialOrientation = orientation;
        position = GeometryHelper.move(anchor, rotationRadius, orientation);
        initialPosition = position;
    }

    /**
     * Rotates the sensor with the given angle from the initial position and orientation
     *
     * @param shift the angle to rotate
     */
    public void rotate(float shift)
    {
        orientation = initialOrientation + shift;
        position = GeometryHelper.rotateAround(anchor, initialPosition, shift);
    }
}