package com.paul.explore.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class GeometryHelper {

    public static Point2D move(Point2D point, int distance, float orientation) {
        Point2D movedPoint = new Point2D.Double(point.getX(), point.getY());
        double angle = toRadians(orientation);
        movedPoint.setLocation(movedPoint.getX() + Math.round(distance * Math.cos(angle)), movedPoint.getY() + Math.round(distance * Math.sin(angle)));
        return movedPoint;
    }

    public static Point.Double rotateAround(Point2D pivot, Point2D point, float angle) {
        Point.Double result = new Point.Double();
        AffineTransform rotation = new AffineTransform();
        double radians = toRadians(angle);
        rotation.rotate(radians, pivot.getX(), pivot.getY());
        rotation.transform(point, result);
        return result;
    }

    public static double toRadians(float angle) {
        return angle * Math.PI / 180;
    }

    /**
     * @return then angle normalized between 0, 360
     */
    public static float normalizeAngle(float angle) {
        float reducedAngle = angle - new Float(angle / 360).intValue() * 360;
        if (reducedAngle < 0) {
            return 360 + reducedAngle;
        }
        return reducedAngle;
    }

    public static int round(double d){
        return Math.toIntExact(Math.round(d));
    }
}
