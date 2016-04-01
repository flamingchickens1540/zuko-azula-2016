package org.team1540.vision;

import java.awt.Point;

public class Goal {
    public final Point ul;
    public final Point ur;
    public final Point lr;
    public final Point ll;
    public final Shape shape;

    public Goal(Point _ul, Point _ur, Point _lr, Point _ll, Shape _shape) {
        ul = _ul;
        ur = _ur;
        lr = _lr;
        ll = _ll;
        shape = _shape;
    }

    public float getLowerDistance(float conversionConstant) {
        return (float) (conversionConstant / Math.tan(lr.distance(ll)));
    }

    public float getUpperDistance(float conversionConstant) {
        return (float) (conversionConstant / Math.tan(ul.distance(ur)));
    }
}
