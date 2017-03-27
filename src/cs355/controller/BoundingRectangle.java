package cs355.controller;

/**
 * Created by tlyon on 1/21/17.
 */
class BoundingRectangle {
    int beginX;
    int beginY;
    int endX;
    int endY;

    public BoundingRectangle(int topLeftX, int topLeftY, int bottomLeftX, int bottomLeftY) {
        this.beginX = topLeftX;
        this.beginY = topLeftY;
        this.endX = bottomLeftX;
        this.endY = bottomLeftY;
    }

    BoundingRectangle() {
    }
}
