package cs355.view;

import cs355.GUIFunctions;
import cs355.model.Model;
import cs355.model.drawing.*;
import cs355.model.drawing.Rectangle;
import cs355.model.drawing.Shape;
import cs355.model.scene.CS355Scene;
import cs355.model.scene.Instance;
import cs355.model.scene.Line3D;
import cs355.model.scene.Point3D;
import org.jblas.DoubleMatrix;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Console;
import java.util.ArrayList;
import java.util.Observable;

/**
 * Created by tlyon on 1/21/17.
 * Handles the drawing of the images.
 * Is observer of the Model and draws when it is notified by the model.
 */
public class View implements ViewRefresher {

    private Model model;
    private AffineTransform viewToWorld;
    private AffineTransform worldToView;
    private double scale;
    private Point2D.Double position;
    private int size = 512; //2048 when at .25 scale
    private boolean drawingHandles = false;
    private boolean render3D = false;
    private Point3D cameraPosition;
    private double cameraAngle = 0;
    private final double fov = 50;
    private final double nearPlane = 2;
    private final double farPlane = 300;
    private final double height = 2048;
    private final double width = 2048;
    private DoubleMatrix CR;
    private DoubleMatrix CT;
    private DoubleMatrix C;
    private DoubleMatrix WC;
    private DoubleMatrix VP = new DoubleMatrix(new double[][]
            {
                    {width / 2, 0, height / 2, 0},
                    {0, -height / 2, width / 2, 0},
                    {0, 0, 1, 0},
                    {0, 0, 0, 1}
            });

    /*
    * CONSTRUCTORS
    */
    public View() {
        this.model = Model.getInstance();
        this.model.addObserver(this);
        this.scale = 1.0;
        this.position = new Point2D.Double(0, 0);
        updateTransformations();
        cameraPosition = new Point3D(0, 8, -100);
        update3DMatrices();
    }

    /*
    * PUBLIC METHODS
    */
    public void zoomIn() {
        double prevSize = size / scale;
        if (this.scale == 4.0) {
            return;
        }
        this.scale *= 2;
        GUIFunctions.setZoomText(scale);
        double newSize = size / scale;
        Point2D.Double prevCenter = new Point2D.Double(position.x + prevSize / 2, position.y + prevSize / 2);
        Point2D.Double newTopLeft = new Point2D.Double(prevCenter.x - newSize / 2, prevCenter.y - newSize / 2);
        if (newTopLeft.x + newSize > 2048) {
            newTopLeft.x = 2048 - newSize;
        }
        if (newTopLeft.y + newSize > 2048) {
            newTopLeft.y = 2048 - newSize;
        }

        if (prevSize == 2048) {
            GUIFunctions.setHScrollBarKnob((int) (size / scale));
            GUIFunctions.setVScrollBarKnob((int) (size / scale));
        }
        this.position = newTopLeft;
        resetScrollbarPos();
        GUIFunctions.setHScrollBarKnob((int) (size / scale));
        GUIFunctions.setVScrollBarKnob((int) (size / scale));
        updateTransformations();
        GUIFunctions.refresh();
    }

    public void zoomOut() {
        double prevSize = size / scale;
        if (this.scale == 0.25) {
            return;
        }
        this.scale /= 2;

        GUIFunctions.setZoomText(scale);
        double newSize = size / scale;
        Point2D.Double prevCenter = new Point2D.Double(position.x + prevSize / 2, position.y + prevSize / 2);
        Point2D.Double newTopLeft = new Point2D.Double(prevCenter.x - newSize / 2, prevCenter.y - newSize / 2);
        if (newTopLeft.x < 0) {
            newTopLeft.x = 0;
        }
        if (newTopLeft.y < 0) {
            newTopLeft.y = 0;
        }
        if (scale == 0.25) {
            newTopLeft.x = 0;
            newTopLeft.y = 0;
        }
        this.position = newTopLeft;
        resetScrollbarPos();
        GUIFunctions.setHScrollBarKnob((int) (size / scale));
        GUIFunctions.setVScrollBarKnob((int) (size / scale));
        updateTransformations();
        GUIFunctions.refresh();
    }

    public void moveX(int newX) {
        this.position.x = newX;

        updateTransformations();
        GUIFunctions.refresh();
    }

    public void moveY(int newY) {
        this.position.y = newY;

        updateTransformations();
        GUIFunctions.refresh();
    }

    public void update3DMatrices() {
        //Update world-to-camera matrix
        CR = new DoubleMatrix(new double[][]
                {
                        {Math.cos(Math.toRadians(cameraAngle)), 0, -Math.sin(Math.toRadians(cameraAngle)), 0},
                        {0, 1, 0, 0},
                        {Math.sin(Math.toRadians(cameraAngle)), 0, Math.cos(Math.toRadians(cameraAngle)), 0},
                        {0, 0, 0, 1}
                });
        CT = new DoubleMatrix(new double[][]
                {
                        {1, 0, 0, -cameraPosition.x},
                        {0, 1, 0, -cameraPosition.y},
                        {0, 0, 1, -cameraPosition.z},
                        {0, 0, 0, 1}
                });
        WC = CR.mmul(CT);

        //Update clip matrix
        double radians = Math.toRadians(fov);
        double zoom = 1 / Math.tan(radians / 2);
        C = new DoubleMatrix(new double[][]
                {
                        //do I use the zoom or the scale here???
                        {zoom, 0, 0, 0},
                        {0, zoom, 0, 0},
                        {0, 0, (farPlane + nearPlane) / (farPlane - nearPlane), (-2 * nearPlane * farPlane) / (farPlane - nearPlane)},
                        {0, 0, 1, 0}
                });
    }

    public boolean toggle3D() {
        render3D = !render3D;
        update3DMatrices();
        return render3D;
    }

    public void moveForward() {
        cameraPosition.x += Math.sin(Math.toRadians(cameraAngle));
        cameraPosition.z += Math.cos(Math.toRadians(cameraAngle));
    }

    public void moveLeft() {
        cameraPosition.x += Math.sin(Math.toRadians(cameraAngle - 90));
        cameraPosition.z += Math.cos(Math.toRadians(cameraAngle - 90));
    }

    public void moveBack() {
        cameraPosition.x -= Math.sin(Math.toRadians(cameraAngle));
        cameraPosition.z -= Math.cos(Math.toRadians(cameraAngle));
    }

    public void moveRight() {
        cameraPosition.x -= Math.sin(Math.toRadians(cameraAngle - 90));
        cameraPosition.z -= Math.cos(Math.toRadians(cameraAngle - 90));
    }

    public void moveUp() {
        cameraPosition.y += 1.0;
    }

    public void moveDown() {
        cameraPosition.y -= 1.0;
    }

    public void turnRight() {
        cameraAngle += 1.5;

        if (cameraAngle >= 360) {
            cameraAngle = 0;
        }
    }

    public void turnLeft() {
        cameraAngle -= 1.5;
        if (cameraAngle <= 0) {
            cameraAngle = 360;
        }
    }

    public void resetCamera() {
        cameraAngle = 0;
        cameraPosition.y = 8;
        cameraPosition.x = 0;
        cameraPosition.z = -100;
        update3DMatrices();
    }

    /*
    * PRIVATE METHODS
    */
    private void resetScrollbarPos() {
        GUIFunctions.setHScrollBarPosit((int) position.x);
        GUIFunctions.setVScrollBarPosit((int) position.y);
    }

    private void updateTransformations() {
        AffineTransform transpose = new AffineTransform(1, 0, 0, 1, -position.x, -position.y);
        AffineTransform s = new AffineTransform(scale, 0, 0, scale, 0, 0);
        this.worldToView = (AffineTransform) s.clone();
        this.worldToView.concatenate(transpose);

        AffineTransform t2 = new AffineTransform(1, 0, 0, 1, position.x, position.y);
        AffineTransform s2 = new AffineTransform(1 / scale, 0, 0, 1 / scale, 0, 0);
        this.viewToWorld = (AffineTransform) t2.clone();
        this.viewToWorld.concatenate(s2);
    }

    private void drawBoundingShape(Shape shape, Graphics2D g2d) {
        String className = shape.getClass().getName();
        g2d.setColor(Color.white);

        //check if the selected shape is a line
        drawingHandles = true;
        if (className.equals("cs355.model.drawing.Line")) {
            drawShape(model.getHandle1(), g2d);
            drawingHandles = true;
            drawShape(model.getHandle2(), g2d);
        } else {
            drawShape(model.getHandle1(), g2d);
        }

        //set transform
        AffineTransform objToWorld = new AffineTransform();
        AffineTransform translateW = new AffineTransform(1, 0, 0, 1, shape.getCenter().x, shape.getCenter().y);
        AffineTransform rotateW = new AffineTransform(Math.cos(shape.getRotation()), Math.sin(shape.getRotation()),
                -Math.sin(shape.getRotation()), Math.cos(shape.getRotation()), 0, 0);
        objToWorld.concatenate(translateW);
        objToWorld.concatenate(rotateW);

        AffineTransform objToView;
        objToView = (AffineTransform) worldToView.clone();
        objToView.concatenate(objToWorld);

        g2d.setTransform(objToView);

        switch (className) {
            case "cs355.model.drawing.Square":
                g2d.drawRect(-(int) ((Square) shape).getSize() / 2, -(int) ((Square) shape).getSize() / 2,
                        (int) ((Square) shape).getSize(), (int) ((Square) shape).getSize());
                break;
            case "cs355.model.drawing.Circle":
                g2d.drawOval(0 - (int) ((Circle) shape).getRadius(), 0 - (int) ((Circle) shape).getRadius(),
                        (int) ((Circle) shape).getRadius() * 2, (int) ((Circle) shape).getRadius() * 2);
                break;
            case "cs355.model.drawing.Triangle":
                int[] xpoints = {(int) ((Triangle) shape).getA().x, (int) ((Triangle) shape).getB().x, (int) ((Triangle) shape).getC().x};
                int[] ypoints = {(int) ((Triangle) shape).getA().y, (int) ((Triangle) shape).getB().y, (int) ((Triangle) shape).getC().y};
                g2d.drawPolygon(xpoints, ypoints, 3);
                break;
            case "cs355.model.drawing.Rectangle":
                g2d.drawRect(-(int) ((Rectangle) shape).getWidth() / 2, -(int) ((Rectangle) shape).getHeight() / 2,
                        (int) ((Rectangle) shape).getWidth(), (int) ((Rectangle) shape).getHeight());
                break;
            case "cs355.model.drawing.Ellipse":
                g2d.drawOval(0 - (int) ((Ellipse) shape).getWidth() / 2, 0 - (int) ((Ellipse) shape).getHeight() / 2,
                        (int) ((Ellipse) shape).getWidth(), (int) ((Ellipse) shape).getHeight());
                break;
        }
    }

    private void drawShape(Shape shape, Graphics2D g2d) {
        try {
            String className = shape.getClass().getName();
            if (className.equals("cs355.model.drawing.Line")) {
                //reset transformation
                g2d.setTransform(worldToView);
                g2d.setColor(shape.getColor());
                g2d.drawLine((int) (shape).getCenter().x, (int) (shape).getCenter().y, (int) ((Line) shape).getEnd().x, (int) ((Line) shape).getEnd().y);
                return;
            }
            //create objToWorld transformation
            AffineTransform objToWorld = new AffineTransform();
            AffineTransform translateW = new AffineTransform(1, 0, 0, 1, shape.getCenter().x, shape.getCenter().y);
            AffineTransform rotateW = new AffineTransform(Math.cos(shape.getRotation()), Math.sin(shape.getRotation()),
                    -Math.sin(shape.getRotation()), Math.cos(shape.getRotation()), 0, 0);
            objToWorld.concatenate(translateW);
            objToWorld.concatenate(rotateW);
            AffineTransform objToView;
            objToView = (AffineTransform) worldToView.clone();
            objToView.concatenate(objToWorld);
            g2d.setTransform(objToView);
            switch (className) {
                case "cs355.model.drawing.Square":
                    g2d.setColor(shape.getColor());
                    int size = (int) ((Square) shape).getSize();
                    g2d.fillRect(-size / 2, -size / 2, size, size);
                    break;
                case "cs355.model.drawing.Circle":
                    g2d.setColor(shape.getColor());
                    int radius = (int) ((Circle) shape).getRadius();
                    //don't scale the handles
                    if (drawingHandles) {
                        radius /= scale;
                        drawingHandles = false;
                    }
                    g2d.fillOval(-radius, -radius, radius * 2, radius * 2);
                    break;
                case "cs355.model.drawing.Rectangle":
                    g2d.setColor(shape.getColor());
                    int heightR = (int) ((Rectangle) shape).getHeight();
                    int widthR = (int) ((Rectangle) shape).getWidth();
                    g2d.fillRect(-widthR / 2, -heightR / 2, widthR, heightR);
                    break;
                case "cs355.model.drawing.Ellipse":
                    g2d.setColor(shape.getColor());
                    int width = (int) ((Ellipse) shape).getWidth();
                    int height = (int) ((Ellipse) shape).getHeight();
                    g2d.fillOval(-width / 2, -height / 2, width, height);
                    break;
                case "cs355.model.drawing.Triangle":
                    g2d.setColor(shape.getColor());
                    int[] xPoints = new int[3];
                    int[] yPoints = new int[3];
                    xPoints[0] = (int) ((Triangle) shape).getA().x;
                    xPoints[1] = (int) ((Triangle) shape).getB().x;
                    xPoints[2] = (int) ((Triangle) shape).getC().x;
                    yPoints[0] = (int) ((Triangle) shape).getA().y;
                    yPoints[1] = (int) ((Triangle) shape).getB().y;
                    yPoints[2] = (int) ((Triangle) shape).getC().y;
                    g2d.fillPolygon(xPoints, yPoints, 3);
                    break;
            }
        } catch (NullPointerException ex) {
            System.out.print("Null pointer!");
        }
    }

    /*
    * OVERRIDE METHODS
    */
    @Override
    public void update(Observable o, Object arg) {
        o.notifyObservers();
    }

    @Override
    public void refreshView(Graphics2D g2d) {
        if (model.getImage() != null) {
            g2d.setTransform(worldToView);
            BufferedImage bufferedImage = model.getImage().getImage();
            g2d.drawImage(bufferedImage, null, (2048 - bufferedImage.getWidth()) / 2, (2048 - bufferedImage.getHeight()) / 2);
        }
        ArrayList<cs355.model.drawing.Shape> shapes = model.getShapes();
        //reset transformation
        g2d.setTransform(new AffineTransform());
        //draw every shape in model
        for (Shape shape : shapes) {
            drawShape(shape, g2d);
        }
        //if we are currently drawing a new shape
        if (model.getNewShape() != null) {
            drawShape(model.getNewShape(), g2d);
        }
        //if we have selected a shape, draw a bounding shape
        if (model.getSelectedShape() != null) {
            Shape shape = model.getSelectedShape();
            drawBoundingShape(shape, g2d);
        }

        if (render3D && model.getScene() != null) {
            CS355Scene scene = model.getScene();
            //Handle each Instance in scene
            for (Instance inst : scene.instances()) {
                //Create object-to-world matrix
                DoubleMatrix OR = new DoubleMatrix(new double[][]
                        {
                                {Math.cos(Math.toRadians(inst.getRotAngle() - 180)), 0, -Math.sin(Math.toRadians(inst.getRotAngle() - 180)), 0},
                                {0, 1, 0, 0},
                                {Math.sin(Math.toRadians(inst.getRotAngle() - 180)), 0, Math.cos(Math.toRadians(inst.getRotAngle() - 180)), 0},
                                {0, 0, 0, 1}
                        });
                DoubleMatrix OT = new DoubleMatrix(new double[][]
                        {
                                {1, 0, 0, -inst.getPosition().x},
                                {0, 1, 0, -inst.getPosition().y},
                                {0, 0, 1, -inst.getPosition().z},
                                {0, 0, 0, 1}
                        });
                DoubleMatrix OW = OT.mmul(OR);

                for (Line3D line : inst.getModel().getLines()) {
                    //Convert to (X,Y,Z,1) homogeneous coordinate
                    Point3D a = line.start;
                    Point3D b = line.end;
                    DoubleMatrix A = new DoubleMatrix(new double[]{a.x, a.y, a.z, 1});
                    DoubleMatrix B = new DoubleMatrix(new double[]{b.x, b.y, b.z, 1});
                    //Apply object-to-world matrix
                    DoubleMatrix worldCoordinatesA = OW.mmul(A);
                    DoubleMatrix worldCoordinatesB = OW.mmul(B);

                    //Apply to world-to-camera matrix
                    DoubleMatrix cameraCoordA = WC.mmul(worldCoordinatesA);
                    DoubleMatrix cameraCoordB = WC.mmul(worldCoordinatesB);

                    //Apply clip matrix
                    DoubleMatrix clipCoordA = C.mmul(cameraCoordA);
                    DoubleMatrix clipCoordB = C.mmul(cameraCoordB);

                    //Apply clipping tests.
                    // Reject if either point fails the near-plane test
                    if (clipCoordA.get(2) < -clipCoordA.get(3) || clipCoordA.get(2) > clipCoordA.get(3)) {
                        continue;
                    }
                    if (clipCoordB.get(2) < -clipCoordA.get(3) || clipCoordB.get(2) > clipCoordB.get(3)) {
                        continue;
                    }
                    // OR
                    // if both points fail the same view frustum test
                    //left
                    if (clipCoordA.get(0) < -clipCoordA.get(3) && clipCoordB.get(0) < clipCoordB.get(3)) {
                        continue;
                    }
                    //right
                    if (clipCoordA.get(0) > clipCoordA.get(3) && clipCoordB.get(0) > clipCoordB.get(3)) {
                        continue;
                    }
                    //bottom
                    if (clipCoordA.get(1) < -clipCoordA.get(3) && clipCoordB.get(1) < -clipCoordB.get(3)) {
                        continue;
                    }
                    //top
                    if (clipCoordA.get(1) > clipCoordA.get(3) && clipCoordB.get(1) > clipCoordB.get(3)) {
                        continue;
                    }
                    //Normalize 3D homogeneous coordinate (div by w)
                    DoubleMatrix normCoordA = clipCoordA.div(clipCoordA.get(3));
                    DoubleMatrix normCoordB = clipCoordB.div(clipCoordB.get(3));
                    //Apply viewport transformation
                    DoubleMatrix vpCoordA = VP.mmul(normCoordA);
                    DoubleMatrix vpCoordB = VP.mmul(normCoordB);
                    //Draw line to screen
                    //change color
                    g2d.setColor(inst.getColor());
                    //Apply same viewing transformations used to implement 2D
                    g2d.setTransform(worldToView);
                    g2d.drawLine((int) vpCoordA.get(0), (int) vpCoordA.get(1), (int) vpCoordB.get(0), (int) vpCoordB.get(1));
                }
            }
        }
    }

    /*
    * GETTERS AND SETTERS
    */
    public AffineTransform getViewToWorld() {
        return viewToWorld;
    }

    public AffineTransform getWorldToView() {
        return worldToView;
    }

    public double getScale() {
        return scale;
    }

    public boolean isRender3D() {
        return render3D;
    }
}