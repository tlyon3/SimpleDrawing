package cs355.model.drawing;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Add your triangle code here. You can add fields, but you cannot
 * change the ones that already exist. This includes the names!
 */
public class Triangle extends Shape {

	// The three points of the triangle.
	private Point2D.Double a;
	private Point2D.Double b;
	private Point2D.Double c;

	/**
	 * Basic constructor that sets all fields.
	 * @param color the color for the new shape.
	 * @param center the center of the new shape.
	 * @param a the first point, relative to the center.
	 * @param b the second point, relative to the center.
	 * @param c the third point, relative to the center.
	 */
	public Triangle(Color color, Point2D.Double center, Point2D.Double a,
					Point2D.Double b, Point2D.Double c) {

		// Initialize the superclass.
		super(color, center);

		// Set fields.
        Point2D.Double translatedA = new Point2D.Double(a.x - center.x, a.y - center.y);
        Point2D.Double translatedB = new Point2D.Double(b.x - center.x, b.y - center.y);
        Point2D.Double translatedC = new Point2D.Double(c.x - center.x, c.y - center.y);

        this.a = translatedA;
		this.b = translatedB;
		this.c = translatedC;
	}

	/**
	 * Getter for the first point.
	 * @return the first point as a Java point.
	 */
	public Point2D.Double getA() {
		return a;
	}

	/**
	 * Setter for the first point.
	 * @param a the new first point.
	 */
	public void setA(Point2D.Double a) {
		this.a = a;
	}

	/**
	 * Getter for the second point.
	 * @return the second point as a Java point.
	 */
	public Point2D.Double getB() {
		return b;
	}

	/**
	 * Setter for the second point.
	 * @param b the new second point.
	 */
	public void setB(Point2D.Double b) {
		this.b = b;
	}

	/**
	 * Getter for the third point.
	 * @return the third point as a Java point.
	 */
	public Point2D.Double getC() {
		return c;
	}

	/**
	 * Setter for the third point.
	 * @param c the new third point.
	 */
	public void setC(Point2D.Double c) {
		this.c = c;
	}

	/**
	 * Add your code to do an intersection test
	 * here. You shouldn't need the tolerance.
	 * @param pt = the point to test against.
	 * @param tolerance = the allowable tolerance.
	 * @return true if pt is in the shape,
	 *		   false otherwise.
	 */
	@Override
	public boolean pointInShape(Point2D.Double pt, double tolerance, AffineTransform viewToWorld) {
        AffineTransform viewToObj = new AffineTransform();
        Point2D.Double objCoord = new Point2D.Double();
        AffineTransform worldToObj = new AffineTransform();
        AffineTransform invRotate = new AffineTransform(Math.cos(-rotation), Math.sin(-rotation),-Math.sin(-rotation),Math.cos(-rotation), 0, 0);
        AffineTransform invTranslate = new AffineTransform(1,0,0,1,-center.x, -center.y);
        worldToObj.concatenate(invRotate);
        worldToObj.concatenate(invTranslate);

        viewToObj.concatenate(worldToObj);
        viewToObj.concatenate(viewToWorld);

        viewToObj.transform(pt, objCoord);
        boolean b1 = sign(objCoord, a, b) < 0.0;
        boolean b2 = sign(objCoord, b, c) < 0.0;
        boolean b3 = sign(objCoord, c, a) < 0.0;
        return ((b1 == b2) && (b2 == b3));
	}

	private double sign(Point2D.Double p1, Point2D.Double p2, Point2D.Double p3){
        return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);
    }

    public double getHeight(){
        double max = 0;
        if(a.y > max){
            max = a.y;
        }
        if(b.y > max){
            max = b.y;
        }
        if(c.y > max){
            max = c.y;
        }
        return max;
    }
}
