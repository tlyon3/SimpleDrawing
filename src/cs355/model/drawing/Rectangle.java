package cs355.model.drawing;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Add your rectangle code here. You can add fields, but you cannot
 * change the ones that already exist. This includes the names!
 */
public class Rectangle extends Shape {

	// The width of this shape.
	private double width;

	// The height of this shape.
	private double height;

	/**
	 * Basic constructor that sets all fields.
	 * @param color the color for the new shape.
	 * @param center the center of the new shape.
	 * @param width the width of the new shape.
	 * @param height the height of the new shape.
	 */
	public Rectangle(Color color, Point2D.Double center, double width, double height) {

		// Initialize the superclass.
		super(color, center);

		// Set fields.
		this.width = width;
		this.height = height;
	}

	/**
	 * Getter for this shape's width.
	 * @return this shape's width as a double.
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Setter for this shape's width.
	 * @param width the new width.
	 */
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * Getter for this shape's height.
	 * @return this shape's height as a double.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Setter for this shape's height.
	 * @param height the new height.
	 */
	public void setHeight(double height) {
		this.height = height;
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
        return objCoord.x < width/2 && objCoord.x > -(width)/2 && objCoord.y < height/2 && objCoord.y > -(height/2);
	}

}
