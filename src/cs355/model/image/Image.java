package cs355.model.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.WritableRaster;

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
    }

    @Override
    public void sharpen() {
        // TODO: 4/18/17 do it
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
        for (int i = 0; i < super.getWidth(); i++) {
            for (int j = 0; j < super.getHeight(); j++) {
                rgb = super.getPixel(i, j, rgb);
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
        for (int i = 0; i < super.getWidth(); i++) {
            for (int j = 0; j < super.getHeight(); j++) {
                rgb = super.getPixel(i, j, rgb);
                hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsb);
                //contrast operations
                // TODO: 4/18/17 Check this. Not sure if this what I'm supposed to do
                hsb[2] = (float) Math.pow(((amount+100.0f)/100.0f), 4.0f) * (hsb[2] - 0.5f) + 0.5f;

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
        for (int i = 0; i < super.getWidth(); i++) {
            for (int j = 0; j < super.getHeight(); j++) {
                rgb = super.getPixel(i, j, rgb);
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

    void applyKernel(float[][] kernel){

    }
}
