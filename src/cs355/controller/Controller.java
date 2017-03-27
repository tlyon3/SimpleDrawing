package cs355.controller;

import cs355.GUIFunctions;
import cs355.model.Model;
import cs355.model.drawing.*;
import cs355.model.drawing.Rectangle;
import cs355.model.drawing.Shape;
import cs355.model.scene.CS355Scene;
import cs355.solution.CS355;
import cs355.view.View;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;

/**
 * Created by tlyon on 1/21/17.
 * Handles all user input.
 * Handles switching to different shapes.
 * Handles creation of shapes: when the user begins drawing, the controller creates
 * a new shape and sets the newShape member in the model using model.setNewShape().
 * When the shape is done being created, it calls model.commit(), which adds the shape
 * model.
 */
public class Controller implements CS355Controller {

    enum Shape {
        Rectangle,
        Square,
        Triangle,
        Circle,
        Ellipse,
        Line,
        None
    }

    private Model model;
    private Color currentColor;
    private Shape shapeType;
    private cs355.model.drawing.Shape selectedShape;
    private int selectedShapeIndex = -1;
    private BoundingRectangle bounds;
    private Point2D.Double firstPoint = null;
    private Point2D.Double secondPoint = null;
    private Point2D.Double thirdPoint = null;
    private Point2D.Double previousPoint = null;
    private boolean movingHandle1 = false;
    private boolean movingHandle2 = false;
    private Circle handle1;
    //this handle will only be used for lines
    private Circle handle2;
    private View view;
    private boolean movingScroll = false;

    public Controller() {
        this.shapeType = Shape.None;
        this.currentColor = Color.white;
        this.model = Model.getInstance();
        this.bounds = new BoundingRectangle();
        this.selectedShape = null;
    }

    private void selectShape(cs355.model.drawing.Shape s) {
        GUIFunctions.changeSelectedColor(s.getColor());
        this.selectedShape = s;
        model.setSelectedShape(s);
        this.selectedShapeIndex = model.getShapes().indexOf(s);
        setUpHandles();
        GUIFunctions.refresh();
    }

    private void setUpHandles() {
        String className = selectedShape.getClass().getName();

        if (className.equals("cs355.model.drawing.Line")) {
            Circle first = new Circle(Color.white, selectedShape.getCenter(), 4.0);
            handle1 = first;
            model.setHandle1(first);

            Circle second = new Circle(Color.white, ((Line) selectedShape).getEnd(), 4.0);
            handle2 = second;
            model.setHandle2(second);
        }
        Point2D.Double oldCenter = new Point2D.Double();
        Point2D.Double newCenter = new Point2D.Double();
        AffineTransform objToWorld = new AffineTransform();
        switch (className) {
            case "cs355.model.drawing.Square":
                oldCenter = new Point2D.Double(0, -((Square) selectedShape).getSize() / 2 - 20);
                break;
            case "cs355.model.drawing.Rectangle":
                oldCenter = new Point2D.Double(0, -((Rectangle) selectedShape).getHeight() / 2 - 20);
                break;
            case "cs355.model.drawing.Circle":
                oldCenter = new Point2D.Double(0, -((Circle) selectedShape).getRadius() - 20);
                break;
            case "cs355.model.drawing.Ellipse":
                oldCenter = new Point2D.Double(0, -((Ellipse) selectedShape).getHeight() / 2 - 20);
                break;
            case "cs355.model.drawing.Triangle":
                oldCenter = new Point2D.Double(0, -((Triangle) selectedShape).getHeight() - 25);
                break;
        }
        AffineTransform translate = new AffineTransform(1, 0, 0, 1, selectedShape.getCenter().x, selectedShape.getCenter().getY());
        AffineTransform rotate = new AffineTransform(Math.cos(selectedShape.getRotation()), Math.sin(selectedShape.getRotation()),
                -Math.sin(selectedShape.getRotation()), Math.cos(selectedShape.getRotation()), 0, 0);
        objToWorld.concatenate(translate);
        objToWorld.concatenate(rotate);
//        objToWorld.translate(selectedShape.getCenter().x, selectedShape.getCenter().y);
//        objToWorld.rotate(selectedShape.getRotation());
        objToWorld.transform(oldCenter, newCenter);
        Circle result = new Circle(Color.white, newCenter, 4.0);
        model.setHandle1(result);
        this.handle1 = result;
    }

    @Override
    public void colorButtonHit(Color c) {
        this.currentColor = c;
        GUIFunctions.changeSelectedColor(c);
        if (selectedShape != null) {
            model.getShapes().get(selectedShapeIndex).setColor(c);
            GUIFunctions.refresh();
        }
    }

    @Override
    public void lineButtonHit() {
        System.out.println("Line selected.");
        this.shapeType = Shape.Line;
        //reset triangle points and selected shape
        reset();
    }

    @Override
    public void squareButtonHit() {
        System.out.println("Square selected.");
        this.shapeType = Shape.Square;
        //reset triangle points and selected shape
        reset();
    }

    @Override
    public void rectangleButtonHit() {
        System.out.println("Rectangle selected.");
        this.shapeType = Shape.Rectangle;
        //reset triangle points and selected shape
        reset();
    }

    @Override
    public void circleButtonHit() {
        System.out.println("Circle selected.");
        this.shapeType = Shape.Circle;
        //reset triangle points and selected shape
        reset();
    }

    @Override
    public void ellipseButtonHit() {
        System.out.println("Ellipse selected.");
        this.shapeType = Shape.Ellipse;
        //reset triangle points and selected shape
        reset();
    }

    @Override
    public void triangleButtonHit() {
        System.out.println("Triangle selected.");
        this.shapeType = Shape.Triangle;
        //reset triangle points and selected shape
        reset();
    }

    private void reset() {
        model.setSelectedShape(null);
        this.selectedShape = null;
        handle1 = null;
        handle2 = null;
        model.setHandle1(null);
        model.setHandle2(null);
        firstPoint = null;
        secondPoint = null;
        thirdPoint = null;
    }

    @Override
    public void selectButtonHit() {
        this.shapeType = Shape.None;
    }

    @Override
    public void zoomInButtonHit() {
        view.zoomIn();
    }

    @Override
    public void zoomOutButtonHit() {
        view.zoomOut();
    }

    @Override
    public void hScrollbarChanged(int value) {
        if (!movingScroll) {
            movingScroll = true;
            view.moveX(value);
        }
        movingScroll = false;
    }

    @Override
    public void vScrollbarChanged(int value) {
        if (!movingScroll) {
            movingScroll = true;
            view.moveY(value);
        }
        movingScroll = false;
    }

    @Override
    public void openScene(File file) {
        model.openScene(file);
    }

    @Override
    public void toggle3DModelDisplay() {
        view.toggle3D();
    }

    @Override
    public void keyPressed(Iterator<Integer> iterator) {
        if(view.isRender3D()){
            return;
        }
        switch (iterator.next()){
            case (int)'W':
                break;
            case (int)'A':
                break;
            case (int)'S':
                break;
            case (int)'D':
                break;
        }
    }

    @Override
    public void openImage(File file) {
        if (model.open(file)) {
            System.out.println("Opened file: " + file.toString());
            // TODO: 1/21/17 Display success message
        } else {
            System.out.println("Error opening file: " + file.toString());
            // TODO: 1/21/17 Display error message
        }
    }

    @Override
    public void saveImage(File file) {
        if (model.save(file)) {
            System.out.println("Saved file: " + file.toString());
            // TODO: 1/21/17 Display success message
        } else {
            System.out.println("Error saving file: " + file.toString());
            // TODO: 1/21/17 Display error message
        }
    }

    @Override
    public void toggleBackgroundDisplay() {

    }

    @Override
    public void saveDrawing(File file) {
        model.save(file);
    }

    @Override
    public void openDrawing(File file) {
        model.open(file);
    }

    @Override
    public void doDeleteShape() {
        if (this.selectedShape != null) {
            int index = model.getShapes().indexOf(selectedShape);
            model.deleteShape(index);
            this.selectedShape = null;
            model.setSelectedShape(null);
            GUIFunctions.changeSelectedColor(this.currentColor);
            GUIFunctions.refresh();
        }
    }

    @Override
    public void doEdgeDetection() {

    }

    @Override
    public void doSharpen() {

    }

    @Override
    public void doMedianBlur() {

    }

    @Override
    public void doUniformBlur() {

    }

    @Override
    public void doGrayscale() {

    }

    @Override
    public void doChangeContrast(int contrastAmountNum) {

    }

    @Override
    public void doChangeBrightness(int brightnessAmountNum) {

    }

    @Override
    public void doMoveForward() {
        int index = model.getShapes().indexOf(selectedShape);
        model.moveForward(index);
        GUIFunctions.refresh();
    }

    @Override
    public void doMoveBackward() {
        int index = model.getShapes().indexOf(selectedShape);
        model.moveBackward(index);
        GUIFunctions.refresh();
    }

    @Override
    public void doSendToFront() {
        int index = model.getShapes().indexOf(selectedShape);
        model.moveToFront(index);
        GUIFunctions.refresh();
    }

    @Override
    public void doSendtoBack() {
        int index = model.getShapes().indexOf(selectedShape);
        model.movetoBack(index);
        GUIFunctions.refresh();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //convert to world coord
        Point2D.Double viewPt = new Point2D.Double(e.getX(), e.getY());
        Point2D.Double worldPt = new Point2D.Double();
        view.getViewToWorld().transform(viewPt, worldPt);
        //check for triangle
        if (this.shapeType == Shape.Triangle) {
            System.out.println("Mouse clicked at: (" + worldPt.getX() + ", " + worldPt.getY() + ")");
            if (firstPoint == null) {
                firstPoint = worldPt;
            } else if (secondPoint == null) {
                secondPoint = worldPt;
            } else if (thirdPoint == null) {
                thirdPoint = worldPt;
                double centerX = (firstPoint.x + secondPoint.x + thirdPoint.x) / 3;
                double centerY = (firstPoint.y + secondPoint.y + thirdPoint.y) / 3;
                Point2D.Double center = new Point2D.Double(centerX, centerY);
                model.setNewShape(new Triangle(currentColor, center, firstPoint, secondPoint, thirdPoint));
                GUIFunctions.refresh();
                model.commit();
                firstPoint = null;
                secondPoint = null;
                thirdPoint = null;
            }

        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point2D.Double viewPoint = new Point2D.Double(e.getX(), e.getY());

        //check for selection
        if (this.shapeType == Shape.None) {
            //check for handle selection
            if (selectedShape != null && selectedShape.getClass().getName().equals("cs355.model.drawing.Line")) {
                if (handle1 != null && handle1.pointInShape(viewPoint, 2.0/view.getScale(), view.getViewToWorld())) {
                    movingHandle1 = true;
                    return;
                } else if (handle2 != null && handle2.pointInShape(viewPoint, 2.0/view.getScale(), view.getViewToWorld())) {
                    movingHandle2 = true;
                    return;
                }
            }
            if (handle1 != null && handle1.pointInShape(viewPoint, 2.0/view.getScale(), view.getViewToWorld())) {
                movingHandle1 = true;
                return;
            }
            //check for shape selection
            ArrayList<cs355.model.drawing.Shape> shapes = model.getShapes();
            //go in reverse order
            for (int i = shapes.size() - 1; i >= 0; i--) {
                if (shapes.get(i).pointInShape(viewPoint, 4.0/view.getScale(), view.getViewToWorld())) {
                    System.out.println("Is in shape");
                    selectShape(shapes.get(i));
                    //pass view points in
                    previousPoint = new Point2D.Double(e.getX(), e.getY());
                    return;
                }
            }
            //if no shape was selected, reset to null
            selectedShape = null;
            model.setSelectedShape(null);
            GUIFunctions.refresh();
            return;
        }
        //if we are not building a triangle, set the begin point
        if (shapeType != Shape.Triangle) {
            System.out.println("Mouse pressed at [" + e.getX() + "," + e.getY() + "]");
            Point2D.Double viewPt = new Point2D.Double(e.getX(), e.getY());
            Point2D.Double worldPt = new Point2D.Double();
            view.getViewToWorld().transform(viewPt, worldPt);
            bounds.beginX = (int) worldPt.getX();
            bounds.beginY = (int) worldPt.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //reset the moving handle flags
        this.movingHandle1 = false;
        this.movingHandle2 = false;
        //if we aren't building a triangle or selecting a shape, commit the shape
        if (this.shapeType != Shape.Triangle && this.shapeType != Shape.None) {
            System.out.println("Mouse released at [" + e.getX() + "," + e.getY() + "]");
            model.commit();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point2D.Double viewPt = new Point2D.Double(e.getX(), e.getY());
        Point2D.Double worldPt = new Point2D.Double();
        view.getViewToWorld().transform(viewPt, worldPt);
        //check for moving handles
        if (selectedShape != null && selectedShape.getClass().getName().equals("cs355.model.drawing.Line")) {
            int index = model.getShapes().indexOf(selectedShape);
            if (movingHandle1) {
                model.getShapes().get(index).setCenter(worldPt);
                setUpHandles();
                GUIFunctions.refresh();
                return;
            } else if (movingHandle2) {
                ((Line) model.getShapes().get(index)).setEnd(worldPt);
                setUpHandles();
                GUIFunctions.refresh();
                return;
            }
        }
        //if we are moving handle1
        if (selectedShape != null && movingHandle1) {
            int index = model.getShapes().indexOf(selectedShape);
            Point2D.Double worldCenter = selectedShape.getCenter();
            Point2D.Double viewCenter = new Point2D.Double();
            view.getWorldToView().transform(worldCenter, viewCenter);
            double diffx = viewCenter.getX() - e.getX();
            double diffy = viewCenter.getY() - e.getY();
            double rotation = Math.atan2(diffy, diffx) - Math.PI / 2;
            model.getShapes().get(index).setRotation(rotation);
            setUpHandles();
            GUIFunctions.refresh();
            return;
        }
        //if we are moving an object
        if (shapeType == Shape.None && selectedShape != null) {
            double diffX = e.getX() - previousPoint.getX();
            double diffY = e.getY() - previousPoint.getY();
            previousPoint = new Point2D.Double(e.getX(), e.getY());
            Point2D.Double newCenter = new Point2D.Double(selectedShape.getCenter().getX() + (diffX / view.getScale()), selectedShape.getCenter().getY() + (diffY / view.getScale()));
            selectedShape.setCenter(newCenter);
            if (selectedShape.getClass().getName().equals("cs355.model.drawing.Line")) {
                Point2D.Double newEnd = new Point2D.Double(((Line) selectedShape).getEnd().x + (diffX / view.getScale()), ((Line) selectedShape).getEnd().y + (diffY / view.getScale()));
                ((Line) selectedShape).setEnd(newEnd);
            }
            model.getShapes().set(selectedShapeIndex, selectedShape);
            setUpHandles();
            GUIFunctions.refresh();
            return;
        }
        //we are drawing and object
        bounds.endY = (int) worldPt.getY();
        bounds.endX = (int) worldPt.getX();

        if (bounds.endX == bounds.beginX && bounds.endY == bounds.beginY) {
            return;
        }
        switch (shapeType) {
            case Square:
                model.setNewShape(new Square(this.currentColor, getCenter(), getShorterDistance()));
                break;
            case Rectangle:
                model.setNewShape(new Rectangle(this.currentColor, getCenter(), getWidth(), getHeight()));
                break;
            case Circle:
                int radius = getShorterDistance() / 2;
                Point2D.Double topLeftC = getTopLeftCorner();
                Point2D.Double centerC = new Point2D.Double(topLeftC.x + radius, topLeftC.y + radius);
                model.setNewShape(new Circle(this.currentColor, centerC, radius));
                break;
            case Ellipse:
                Point2D.Double topLeftE = getTopLeftCorner();
                int width = getWidth();
                int height = getHeight();
                Point2D.Double centerE = new Point2D.Double(topLeftE.x + (width / 2), topLeftE.y + (height / 2));
                model.setNewShape(new Ellipse(this.currentColor, centerE, width, height));
                break;
            case Line:
                model.setNewShape(new Line(this.currentColor, new Point2D.Double(bounds.beginX, bounds.beginY),
                        new Point2D.Double(bounds.endX, bounds.endY)));
                break;
            default:
                break;
        }
        GUIFunctions.refresh();
    }

    private void setRotation(cs355.model.drawing.Shape s, double theta) {
        int index = model.getShapes().indexOf(s);
        model.getShape(index).setRotation(theta);
        setUpHandles();
        GUIFunctions.refresh();
    }

    private int getHeight() {
        return Math.abs(bounds.beginY - bounds.endY);
    }

    private int getWidth() {
        return Math.abs(bounds.beginX - bounds.endX);
    }

    private Point2D.Double getTopLeftCorner() {
        int y;
        int x;
        if (shapeType == Shape.Circle || shapeType == Shape.Square) {
            int change = getShorterDistance();
            //left and up
            if (bounds.beginX > bounds.endX && bounds.beginY > bounds.endY) {
                x = bounds.beginX - change;
                y = bounds.beginY - change;
            }
            //left and down
            else if (bounds.beginX > bounds.endX && bounds.beginY < bounds.endY) {
                x = bounds.beginX - change;
                y = bounds.beginY;
            }
            //right and up
            else if (bounds.beginX < bounds.endX && bounds.beginY > bounds.endY) {
                x = bounds.beginX;
                y = bounds.beginY - change;
            }
            //right and down
            else {
                x = bounds.beginX;
                y = bounds.beginY;
            }
            return new Point2D.Double(x, y);
        } else {
            if (bounds.beginY < bounds.endY) {
                y = bounds.beginY;
            } else {
                y = bounds.endY;
            }

            if (bounds.beginX < bounds.endX) {
                x = bounds.beginX;
            } else {
                x = bounds.endX;
            }
            return new Point2D.Double(x, y);
        }
    }

    private Point2D.Double getCenter() {
        if (shapeType == Shape.Circle || shapeType == Shape.Square) {
            Point2D.Double topLeft = getTopLeftCorner();
            int w = getShorterDistance();
            double x = topLeft.x + w / 2;
            double y = topLeft.y + w / 2;
            return new Point2D.Double(x, y);
        }
        double x = getTopLeftCorner().x + getWidth() / 2;
        double y = getTopLeftCorner().y + getHeight() / 2;
        return new Point2D.Double(x, y);
    }

    private int getShorterDistance() {
        int distY = Math.abs(bounds.beginY - bounds.endY);
        int distX = Math.abs(bounds.beginX - bounds.endX);
        if (distX < distY) {
            return distX;
        } else {
            return distY;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    public void setView(View view) {
        this.view = view;
    }
}
