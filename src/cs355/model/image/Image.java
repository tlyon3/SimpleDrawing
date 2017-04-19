package cs355.model.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.WritableRaster;
import java.util.Map;

/**
 * Created by tlyon on 4/18/17.
 */
public class Image extends CS355Image {

    BufferedImage bufferedImage = null;

    @Override
    public BufferedImage getImage() {
        if (bufferedImage != null) {
            return bufferedImage;
        } else {
            int width = super.getWidth();
            int height = super.getHeight();
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            WritableRaster writableRaster = bufferedImage.getRaster();
            int[] rgb = new int[3];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    writableRaster.setPixel(i, j, super.getPixel(i, j, rgb));
                }
            }
            bufferedImage.setData(writableRaster);
            return bufferedImage;
        }
    }

    @Override
    public void edgeDetection() {
        // TODO: 4/18/17 do it
        int[] rgb = new int[3];
        float[] hsb = new float[3];
        int[] sobelX = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
        int[] sobelY = {-1, -2, -1, 0, 0, 0, 1, 2, 1};
        int[] xIndex = new int[9];
        int[] yIndex = new int[9];

        Image bufferImage = new Image();
        bufferImage.setPixels(getImage());

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                //get neighbor indexes. account for edges
                int prevX = i - 1;
                if (prevX < 0) prevX = 0;
                int prevY = j - 1;
                if (prevY < 0) prevY = 0;
                int nextX = i + 1;
                int nextY = j + 1;
                if (nextX > super.getWidth() - 1) nextX = i;
                if (nextY > super.getHeight() - 1) nextY = j;

                //set x indexes
                xIndex[0] = prevX;
                xIndex[1] = i;
                xIndex[2] = nextX;
                xIndex[3] = prevX;
                xIndex[4] = i;
                xIndex[5] = nextX;
                xIndex[6] = prevX;
                xIndex[7] = i;
                xIndex[8] = nextX;

                //set y indexes
                yIndex[0] = prevY;
                yIndex[1] = prevY;
                yIndex[2] = prevY;
                yIndex[3] = j;
                yIndex[4] = j;
                yIndex[5] = j;
                yIndex[6] = nextY;
                yIndex[7] = nextY;
                yIndex[8] = nextY;

                //apply kernel(s)
                //compute sum
                double xSum = 0;
                double ySum = 0;
                for (int k = 0; k < 9; k++) {
                    rgb = getPixel(xIndex[k], yIndex[k], rgb);
                    hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsb);
                    xSum += (sobelX[k] * hsb[2]);
                    ySum += (sobelY[k] * hsb[2]);
                }

                //divide by 8
                xSum /= 8;
                ySum /= 8;

                //get gradient
                double gradient = Math.sqrt(Math.pow(xSum, 2) + Math.pow(ySum, 2));
                int color = Math.min((int) (gradient * 255), 255);
                //brighten the edges a little bit
                if (color > 50) {
                    color += 50;
                }

                bufferImage.setPixel(i, j, new int[]{color, color, color});
            }
        }
        this.setPixels(bufferImage.getImage());
        this.bufferedImage = null;
    }

    @Override
    public void sharpen() {
        // TODO: 4/18/17 do it
        int[] rgb = new int[3];
        float[] hsb = new float[3];
        int[] unsharpMaskKernel = {0, -1, 0, -1, 6, -1, 0, -1, 0};
        int[] xIndex = new int[9];
        int[] yIndex = new int[9];

        Image bufferImage = new Image();
        bufferImage.setPixels(getImage());

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                //get neighbor indexes. account for edges
                int prevX = i - 1;
                if (prevX < 0) prevX = 0;
                int prevY = j - 1;
                if (prevY < 0) prevY = 0;
                int nextX = i + 1;
                int nextY = j + 1;
                if (nextX > super.getWidth() - 1) nextX = i;
                if (nextY > super.getHeight() - 1) nextY = j;

                //set x indexes
                xIndex[0] = prevX;
                xIndex[1] = i;
                xIndex[2] = nextX;
                xIndex[3] = prevX;
                xIndex[4] = i;
                xIndex[5] = nextX;
                xIndex[6] = prevX;
                xIndex[7] = i;
                xIndex[8] = nextX;

                //set y indexes
                yIndex[0] = prevY;
                yIndex[1] = prevY;
                yIndex[2] = prevY;
                yIndex[3] = j;
                yIndex[4] = j;
                yIndex[5] = j;
                yIndex[6] = nextY;
                yIndex[7] = nextY;
                yIndex[8] = nextY;

                double sumR = 0;
                double sumG = 0;
                double sumB = 0;
                //apply kernel(s)
                for (int k = 0; k < 9; k++) {
                    rgb = getPixel(xIndex[k], yIndex[k], rgb);
                    sumR += (unsharpMaskKernel[k] * rgb[0]);
                    sumG += (unsharpMaskKernel[k] * rgb[1]);
                    sumB += (unsharpMaskKernel[k] * rgb[2]);
                }
                sumR /= 2;
                sumB /= 2;
                sumG /= 2;
                sumR = Math.max(Math.min(sumR, 255), 0);
                sumG = Math.max(Math.min(sumG, 255), 0);
                sumB = Math.max(Math.min(sumB, 255), 0);
                rgb[0] = (int) sumR;
                rgb[1] = (int) sumG;
                rgb[2] = (int) sumB;
                bufferImage.setPixel(i, j, rgb);
            }
        }
        this.setPixels(bufferImage.getImage());
        this.bufferedImage = null;
    }

    @Override
    public void medianBlur() {
        // TODO: 4/18/17 do it
    }

    @Override
    public void uniformBlur() {
        // TODO: 4/18/17 do it
    }

    @Override
    public void grayscale() {
        int[] rgb = new int[3];
        float[] hsb = new float[3];
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                rgb = getPixel(i, j, rgb);
                hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsb);
                //contrast operations
                hsb[1] = 0.0f;
                Color color = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
                rgb[0] = color.getRed();
                rgb[1] = color.getGreen();
                rgb[2] = color.getBlue();

                setPixel(i, j, rgb);
            }
        }
        //reset buffered image
        bufferedImage = null;
    }

    @Override
    public void contrast(int amount) {
        int[] rgb = new int[3];
        float[] hsb = new float[3];
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                rgb = getPixel(i, j, rgb);
                hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsb);
                //contrast operations
                // TODO: 4/18/17 Check this. Not sure if this what I'm supposed to do
                hsb[2] = (float) Math.pow(((amount + 100.0f) / 100.0f), 4.0f) * (hsb[2] - 0.5f) + 0.5f;

                hsb[2] = Math.min(Math.max(hsb[2], 0.0f), 1.0f);
                Color color = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
                rgb[0] = color.getRed();
                rgb[1] = color.getGreen();
                rgb[2] = color.getBlue();

                setPixel(i, j, rgb);
            }
        }
        //reset buffered image
        bufferedImage = null;
    }

    @Override
    public void brightness(int amount) {
        float floatAmount = amount / 100.0f;
        floatAmount = Math.min(Math.max(floatAmount, -1.0f), 1.0f);
        int[] rgb = new int[3];
        float[] hsb = new float[3];
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                rgb = getPixel(i, j, rgb);
                hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsb);
                //brightness operations
                hsb[2] += floatAmount;

                hsb[2] = Math.min(Math.max(hsb[2], 0.0f), 1.0f);
                Color color = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
                rgb[0] = color.getRed();
                rgb[1] = color.getGreen();
                rgb[2] = color.getBlue();

                setPixel(i, j, rgb);
            }
        }
        //reset buffered image
        bufferedImage = null;
    }

    void applyKernel(float[][] kernel) {

    }
}
