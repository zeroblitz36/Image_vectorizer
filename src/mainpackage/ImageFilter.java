package mainpackage;

import utils.Utility;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

/**
 * Created by GeorgeRoscaneanu on 16.04.2015.
 */
public class ImageFilter {

    private static float x;

    public static BufferedImage filter(BufferedImage src,float f[][]) {
        int w = src.getWidth(null);
        int h = src.getHeight(null);
        int f_h = f.length;
        int f_w = f[0].length;
        if(f_h%2==0 || f_h != f_w)throw new RuntimeException("Filter should be a square matrix of odd size");
        BufferedImage dst = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);

        float r,g,b;
        int color;
        char w_r,w_g,w_b;
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                r=0;
                g=0;
                b=0;
                for(int f_y=0;f_y<f_h;f_y++){
                    for(int f_x=0;f_x<f_w;f_x++){
                        int w_x = x + (f_x - f_w/2);
                        if(w_x<0 || w_x>=w)continue;
                        int w_y = y + (f_y - f_h/2);
                        if(w_y<0 || w_y>=h)continue;

                        color = src.getRGB(w_x,w_y);
                        w_b = (char)(color & 255);
                        color >>= 8;
                        w_g = (char)(color & 255);
                        color >>= 8;
                        w_r = (char)(color & 255);

                        r += w_r * f[f_y][f_x];
                        g += w_g * f[f_y][f_x];
                        b += w_b * f[f_y][f_x];
                    }
                }
                w_r = trun(r);
                w_g = trun(g);
                w_b = trun(b);

                color = (255<<24) | (w_r<<16) | (w_g<<8) | w_b;
                dst.setRGB(x,y,color);
            }
        }

        return dst;
    }

    private static char trun(float x){
        ImageFilter.x = x;
        if(x<0)return 0;
        if(x>255)return 255;
        return (char)x;
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }


    public static BufferedImage edgeDetect(BufferedImage src){
        int w = src.getWidth(null);
        int h = src.getHeight(null);

        BufferedImage dst = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);

        int d = 2;
        int data[] = new int[w*h];
        int max = -1;
        int numberOfLines = 3;
        int minDist = 1;
        int numberOfDists = 2;
        int distStep = 1;


        ArrayList<Point> pointList = new ArrayList<>(200);
        for(d=minDist;d<=numberOfDists*distStep;d+=distStep) {
            for (int a = 0; a < numberOfLines; a++) {
                double angle = Math.PI / (numberOfLines+1) * a;
                pointList.add(new Point((int) (Math.cos(angle) * d),(int) (Math.sin(angle) * d)));
            }
        }
        int xa1,xa2,ya1,ya2;
        int n = pointList.size();
        Point p;
        for (int x = d; x < w - d; x++) {
            for (int y = d; y < h - d; y++) {
                for (int i = 0; i < n; i++) {
                    p = pointList.get(i);
                    xa1 = x + p.x;
                    ya1 = y + p.y;
                    xa2 = x - p.x;
                    ya2 = y - p.y;
                    try {
                        data[w * y + x] += Utility.manhattanDistance(src.getRGB(xa1, ya1), src.getRGB(x, y)) + Utility.manhattanDistance(src.getRGB(xa2, ya2), src.getRGB(x, y));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                if (max < data[w * y + x]) {
                    max = data[w * y + x];
                    //System.out.format("Found new max = %d\n",max);
                }
            }
        }

        float c;
        for(int i=0;i<w*h;i++){
            c = 1.f*data[i]/max;
            //n = 0xff000000 | (c<<16) | (c<<8) | c;
            n = Utility.rainbow(c);
            data[i]=n;
        }

        //Arrays.fill(data,0xffff0000);
        dst.setRGB(0,0,w,h,data,0,w);
        return dst;
    }

}
