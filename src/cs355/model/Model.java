package cs355.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import cs355.GUIFunctions;
import cs355.JsonShape;
import cs355.model.drawing.Circle;
import cs355.model.drawing.Shape;
import cs355.model.image.Image;
import cs355.model.scene.CS355Scene;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tlyon on 1/21/17.
 * Singleton of model. Stores all shapes.
 * When the controller begins creating a new shape, it calls setNewShape.
 * Once the shape is done being created, the controller will call Commit and the
 * shape will be added to the model.
 */

public class Model extends Observable {
    private ArrayList<Shape> shapes;
    private Shape newShape;
    private Shape selectedShape;
    private static Model instance;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Shape.class, new JsonShape()).create();
    private Circle handle1;
    private Circle handle2;
    private CS355Scene scene;
    private Image image;
    private Image originalImage;

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
            return instance;
        } else {
            return instance;
        }
    }

    private Model() {
        this.shapes = new ArrayList<>();
    }

    public void setNewShape(Shape newShape) {
        this.newShape = newShape;
        this.setChanged();
        notifyObservers();
    }

    public void commit() {
        if (newShape == null) {
            return;
        }
        shapes.add(newShape);
        System.out.println("Added new shape with center at: (" + newShape.getCenter().x + "," + newShape.getCenter().y + ")");
        newShape = null;
        this.setChanged();
        notifyObservers();
    }

    public Shape getNewShape() {
        return newShape;
    }

    public void deleteShape(int index) {
        shapes.remove(index);
        setChanged();
        notifyObservers();
    }

    public void moveToFront(int index) {
        Shape shapeToMove = shapes.get(index);
        shapes.remove(index);
        shapes.add(shapeToMove);
        setChanged();
        notifyObservers();
    }

    public void movetoBack(int index) {
        Shape shapeToMove = shapes.get(index);
        shapes.remove(index);
        shapes.add(0, shapeToMove);
        setChanged();
        notifyObservers();
    }

    public void moveForward(int index) {
        if (index != shapes.size() - 1 && shapes.size() != 1) {
            cs355.model.drawing.Shape temp = shapes.get(index);
            shapes.remove(index);
            shapes.add(index + 1, temp);
            setChanged();
            notifyObservers();
        }
    }

    public void moveBackward(int index) {
        if (index != 0 && shapes.size() != 1) {
            cs355.model.drawing.Shape temp = shapes.get(index);
            shapes.remove(index);
            shapes.add(index - 1, temp);
            setChanged();
            notifyObservers();
        }
    }

    public ArrayList<Shape> getShapes() {
        return shapes;
    }

    private void setShapes(List<Shape> shapes) {
        this.shapes = new ArrayList<>(shapes);
        this.setChanged();
        notifyObservers();
        GUIFunctions.refresh();
    }

    public boolean save(File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {

            // Get the list from the concrete class.
            List<Shape> data = getShapes();

            // Convert the List to an array.
            Shape[] shapes = new Shape[data.size()];
            data.toArray(shapes);

            // Convert to JSON text and write it out.
            String json = gson.toJson(shapes, Shape[].class);
            fos.write(json.getBytes());
        }
        catch (IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    public boolean open(File file) {
        // Make a blank list.
        List<Shape> shapes;

        try {
            // Read the entire file in. I hate partial I/O.
            byte[] b = Files.readAllBytes(file.toPath());

            // Validation.
            if (b == null) {
                throw new IOException("Unable to read drawing");
            }

            // Convert it to text.
            String data = new String(b, StandardCharsets.UTF_8);

            // Use Gson to convert the text to a list of Shapes.
            Shape[] list = gson.fromJson(data, Shape[].class);
            shapes = new ArrayList<>(Arrays.asList(list));
        }
        catch (IOException | JsonSyntaxException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        // Set the new shape list.
        setShapes(shapes);

        return true;
    }

    public Shape getSelectedShape() {
        return selectedShape;
    }

    public void setSelectedShape(Shape selectedShape) {
        this.selectedShape = selectedShape;
    }

    public Circle getHandle1() {
        return handle1;
    }

    public void setHandle1(Circle handle1) {
        this.handle1 = handle1;
        setChanged();
        notifyObservers();
        GUIFunctions.refresh();
    }

    public Circle getHandle2() {
        return handle2;
    }

    public void setHandle2(Circle handle2) {
        this.handle2 = handle2;
        setChanged();
        notifyObservers();
        GUIFunctions.refresh();
    }

    public void openScene(File file){
        this.scene = new CS355Scene();
        this.scene.open(file);
    }

    public CS355Scene getScene() {
        return scene;
    }

    public Image getImage(){
        return this.image;
    }

    public void openImage(File f){
        this.image = new Image();
        this.originalImage = new Image();
        this.image.open(f);
        this.originalImage.open(f);
    }

    public void saveImage(File f){
        this.image.save(f);
    }
}
