package com.emolance.enterprise.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;

/**
 * Created by yusun on 4/10/16.
 */
public class ImageColorAnalyzer {

    private static final long serialVersionUID = 1L;

    private Bitmap bmp;

    public ImageColorAnalyzer(File imageFile) {
        this.bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        Log.i("TEST", "Image Size: " + bmp.getWidth() + " " + bmp.getHeight());
    }

    public int printPixelARGB(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        int total = red + blue + green;
        //Log.i("TEST", "argb: " + alpha + ", " + red + ", " + green + ", " + blue);
        return total;
    }

    public TestResult marchThroughImage() {
        TestResult result;
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int ST = 0;
        int SO = 0;
        int SC = 0;

        // Calculate ST
        for (int i = 10; i <= 136; i++) {
            for (int j = 47; j <= 73; j++) {
                int pixel = bmp.getPixel(i, j);
                ST += printPixelARGB(pixel);
            }
        }

        // Calculate SO
        for (int i = 10; i <= 136; i++) {
            for (int j = 108; j <= 134; j++) {
                int pixel = bmp.getPixel(i, j);
                SO += printPixelARGB(pixel);
            }
        }

        // Calculate SC
        for (int i = 184; i <= 310; i++) {
            for (int j = 47; j <= 73; j++) {
                int pixel = bmp.getPixel(i, j);
                SC += printPixelARGB(pixel);
            }
        }

        result = new TestResult(ST, SO, SC);
        System.out.println("width, height: " + w + ", " + h);
        System.out.println("ST is " + result.getST());
        System.out.println("SO is " + result.getSO());
        System.out.println("SC is " + result.getSC());
        System.out.println(
                "Cortisol: " + result.getCotisol() +
                        " Scaled: " + result.getScaledCortisol());
        System.out.println(
                "DHEA: " + result.getDHEA() +
                        " Scaled: " + result.getScaledDHEA());

        return result;
    }

}