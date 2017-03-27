package cs355.model.drawing;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Add your line code here. You can add fields, but you cannot
 * change the ones that already exist. This includes the names!
 */
public class Line extends Shape {

	// The ending point of the line.
	private Point2D.Double end;

	/**
	 * Basic constructor that sets all fields.
	 * @param color the color for the new shape.
	 * @param start the starting point.
	 * @param end the ending point.
	 */
	public Line(Color color, Point2D.Double start, Point2D.Double end) {

		// Initialize the superclass.
		super(color, start);

		// Set the field.
		this.end = end;
	}

	/**
	 * Getter for this Line's ending point.
	 * @return the ending point as a Java point.
	 */
	public Point2D.Double getEnd() {
		return end;
	}

	/**
	 * Setter for this Line's ending point.
	 * @param end the new ending point for the Line.
	 */
	public void setEnd(Point2D.Double end) {
		this.end = end;
	}

	/**
	 * Add your code to do an intersection test
	 * here. You <i>will</i> need the tolerance.
	 * @param viewPt = the point to test against in view coordinates.
	 * @param tolerance = the allowable tolerance.
	 * @return true if pt is in the shape,
	 *		   false otherwise.
	 */
	@Override
	public boolean pointInShape(Point2D.Double viewPt, double tolerance, AffineTransform viewToWorld) {
        //convert to world
        Point2D.Double worldPt = new Point2D.Double();
        viewToWorld.transform(viewPt, worldPt);
        //check bounding box first
        double leftX;
        double rightX;
        if(center.x < end.x){
            leftX = center.x;
            rightX = end.x;
        }
        else {
            leftX = end.x;
            rightX = center.x;
        }
        double leftY;
        double rightY;
        if(center.y < end.y){
            leftY = center.y;
            rightY = end.y;
        }
        else {
            leftY = end.y;
            rightY = center.y;
        }
        //check bounding box. Give a little room for ends of line
        if(worldPt.x < leftX-tolerance || worldPt.x > rightX+tolerance || worldPt.y < leftY-tolerance || worldPt.y > rightY+tolerance){
            return false;
        }
        return distanceFromPoint(worldPt) <= tolerance;
	}

	public double distanceFromPoint(Point2D.Double pt){
        double normal = Math.sqrt((end.x - center.x) * (end.x - center.x) + (end.y - center.y) * (end.y - center.y));
        return Math.abs((pt.x - center.x) * (end.y - center.y) - (pt.y - center.y) * (end.x - center.x))/normal;
    }

}
