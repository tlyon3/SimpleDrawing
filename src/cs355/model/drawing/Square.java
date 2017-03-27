package cs355.model.drawing;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Add your square code here. You can add fields, but you cannot
 * change the ones that already exist. This includes the names!
 */
public class Square extends Shape {

    // The size of this Square.
    private double size;

    /**
     * Basic constructor that sets all fields.
     *
     * @param color  the color for the new shape.
     * @param center the center of the new shape.
     * @param size   the size of the new shape.
     */
    public Square(Color color, Point2D.Double center, double size) {

        // Initialize the superclass.
        super(color, center);

        // Set the field.
        this.size = size;
    }

    /**
     * Getter for this Square's size.
     *
     * @return the size as a double.
     */
    public double getSize() {
        return size;
    }

    /**
     * Setter for this Square's size.
     *
     * @param size the new size.
     */
    public void setSize(double size) {
        this.size = size;
    }

    /**
     * Add your code to do an intersection test
     * here. You shouldn't need the tolerance.
     *
     * @param viewPt    = the point to test against.
     * @param tolerance = the allowable tolerance.
     * @return true if pt is in the shape,
     * false otherwise.
     */
    @Override
    public boolean pointInShape(Point2D.Double viewPt, double tolerance, AffineTransform viewToWorld) {
        AffineTransform viewToObj = new AffineTransform();
        Point2D.Double objCoord = new Point2D.Double();

        AffineTransform worldToObj = new AffineTransform();
        AffineTransform invRotate = new AffineTransform(Math.cos(-rotation), Math.sin(-rotation), -Math.sin(-rotation), Math.cos(-rotation), 0, 0);
        AffineTransform invTranslate = new AffineTransform(1, 0, 0, 1, -center.x, -center.y);

        worldToObj.concatenate(invRotate);
        worldToObj.concatenate(invTranslate);

        viewToObj.concatenate(worldToObj);
        viewToObj.concatenate(viewToWorld);

        viewToObj.transform(viewPt, objCoord);

        return objCoord.x < size / 2 && objCoord.x > -(size / 2) && objCoord.y < (size / 2) && objCoord.y > (-size / 2);
    }

}
