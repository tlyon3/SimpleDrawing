package cs355.model.drawing;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Add your circle code here. You can add fields, but you cannot
 * change the ones that already exist. This includes the names!
 */
public class Circle extends Shape {

    // The radius.
    private double radius;

    /**
     * Basic constructor that sets all fields.
     *
     * @param color  the color for the new shape.
     * @param center the center of the new shape.
     * @param radius the radius of the new shape.
     */
    public Circle(Color color, Point2D.Double center, double radius) {

        // Initialize the superclass.
        super(color, center);

        // Set the field.
        this.radius = radius;
    }

    /**
     * Getter for this Circle's radius.
     *
     * @return the radius of this Circle as a double.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Setter for this Circle's radius.
     *
     * @param radius the new radius of this Circle.
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Add your code to do an intersection test
     * here. You shouldn't need the tolerance.
     *
     * @param pt        = the point to test against.
     * @param tolerance = the allowable tolerance.
     * @return true if pt is in the shape,
     * false otherwise.
     */
    @Override
    public boolean pointInShape(Point2D.Double pt, double tolerance, AffineTransform viewToWorld) {
        AffineTransform viewToObj = new AffineTransform();
        //this works for right now, but won't work for rotation
        Point2D.Double objCoord = new Point2D.Double();
        AffineTransform worldToObj = new AffineTransform();
        AffineTransform invTranslate = new AffineTransform(1,0,0,1,-center.x, -center.y);
        worldToObj.concatenate(invTranslate);

        viewToObj.concatenate(worldToObj);
        viewToObj.concatenate(viewToWorld);

        viewToObj.transform(pt, objCoord);
        return (Math.pow(objCoord.x, 2) + Math.pow(objCoord.y, 2)) <= Math.pow(radius + tolerance, 2);
    }

}
