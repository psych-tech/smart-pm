package com.emolance.service;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageColorAnalyzer extends Component {

	private static final long serialVersionUID = 1L;

    public int printPixelARGB(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        int total = red + blue + green;
        //System.out.println("argb: " + alpha + ", " + red + ", " + green + ", " + blue);
        return total;
    }

    private Node marchThroughImage(BufferedImage image) {
        Node result;
        int w = image.getWidth();
        int h = image.getHeight();
        int ST = 0;
        int SO = 0;
        int SC = 0;

        //Calculate ST
        for (int i = 488; i <= 818; i++) {
            for (int j = 645; j <= 690; j++) {
                //System.out.println("x,y: " + j + ", " + i);
                int pixel = image.getRGB(i, j);
                //System.out.println(pixel);
                ST += printPixelARGB(pixel);
                //System.out.println("");
            }
        }

        //Calculate SO
        for (int i = 488; i <= 818; i++) {
            for (int j = 475; j <= 520; j++) {
                //System.out.println("x,y: " + j + ", " + i);
                int pixel = image.getRGB(i, j);
                //System.out.println(pixel);
                SO += printPixelARGB(pixel);
                //System.out.println("");
            }
        }

        //Calculate SC
        for (int i = 488; i <= 818; i++) {
            for (int j = 288; j <= 333; j++) {
                //System.out.println("x,y: " + j + ", " + i);
                int pixel = image.getRGB(i, j);
                //System.out.println(pixel);
                SC += printPixelARGB(pixel);
                //System.out.println("");
            }
        }

        result = new Node(ST, SO, SC);
        System.out.println("width, height: " + w + ", " + h);
        System.out.println("ST is " + result.getST());
        System.out.println("SO is " + result.getSO());
        System.out.println("SC is " + result.getSC());
        System.out.println("RT is " + (double)result.getST() / result.getSO());
        System.out.println("RC is " + (double)result.getSC() / result.getSO());
        System.out.println("Scaled RT is " + result.getScaledRT());
        System.out.println("Less number, More pressure ---> more Red");

        return result;
    }

    public Node analyzeImage(File imageFile) {
    	try {
            // File name should be modified by using your image's name
            BufferedImage image = ImageIO.read(imageFile);
            return marchThroughImage(image);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

//    public static void main(String[] args) {
//    	ImageColorAnalyzer ica = new ImageColorAnalyzer();
//    	ica.analyzeImage(new File("/Users/yusun/Desktop/test-01.jpg")); // 0.81 // white
//    	ica.analyzeImage(new File("/Users/yusun/Desktop/test-02.jpg")); // 0.75 // midlle   // 0.7
//    	ica.analyzeImage(new File("/Users/yusun/Desktop/test-03.jpg")); // 0.70 // dark     // 0.2
//    }
}
