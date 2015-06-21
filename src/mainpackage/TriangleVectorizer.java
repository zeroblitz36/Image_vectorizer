package mainpackage;

import utils.ImagePanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by GeorgeRoscaneanu on 20.04.2015.
 */
public class TriangleVectorizer {
    private BufferedImage originalImage;
    private BufferedImage destImage;
    private char[] originalRedArray;
    private char[] originalGreenArray;
    private char[] originalBlueArray;
    private int[] originalColorArray;
    private int w,h,area;
    public int threshold;
    private ImagePanel destImagePanel;
    private Random random = new Random(System.currentTimeMillis());
    private int[] edgeDataArray;
    private Job lastJob;


    public TriangleVectorizer(BufferedImage image){
        originalImage = image;
        w = originalImage.getWidth();
        h = originalImage.getHeight();
        area = w*h;
        destImage = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);
    }

    public void initialize(){
        calculateColorArrays();
    }

    public void calculateColorArrays(){
        originalColorArray = new int[area];
        originalRedArray = new char[area];
        originalGreenArray = new char[area];
        originalBlueArray = new char[area];

        originalImage.getRGB(0,0,w,h,originalColorArray,0,w);
        int color;
        for(int i=0;i<area;i++){
            color = originalColorArray[i];
            originalBlueArray[i] = (char) (color & 0xff);
            color >>= 8;
            originalGreenArray[i] = (char) (color & 0xff);
            color >>= 8;
            originalRedArray[i] = (char) (color & 0xff);
        }
    }

    public void calculateEdgeData(){
        edgeDataArray = new int[area];

        int d = 2;
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
                        edgeDataArray[w*y+x] +=
                                abs(redOrig(xa1,ya1),redOrig(x,y)) + abs(redOrig(xa2,ya2),redOrig(x,y)) +
                                abs(greenOrig(xa1, ya1),greenOrig(x, y)) + abs(greenOrig(xa2, ya2),greenOrig(x,y)) +
                                abs(blueOrig(xa1, ya1),blueOrig(x, y)) + abs(blueOrig(xa2, ya2),blueOrig(x, y));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public void setDestImagePanel(ImagePanel p){
        destImagePanel = p;
    }

    public void startJob() {
        if(lastJob!=null){
            lastJob.setCanceled(true);
        }
        lastJob = new Job();
        lastJob.start();
    }

    private class Job extends Thread{
        private boolean canceled = false;
        private ArrayList<Triangle> triangles = new ArrayList<>();
        @Override
        public void run() {
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(canceled)return;
            final Triangle t1 = new Triangle(0,0,w-1,0,w-1,h-1);
            final Triangle t2 = new Triangle(0,0,0,h-1,w-1,h-1);

            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    recTriangulation(t1);
                }
            });
            th.start();

            recTriangulation(t2);


            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(canceled)return;
            if(destImagePanel!=null){
                Graphics2D g = destImage.createGraphics();
                for(Triangle t:triangles){
                    g.setColor(new Color(t.color));
                    g.fill(t.path);
                }

                destImagePanel.setImage(destImage);
            }
        }

        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        public void recTriangulation(Triangle triangle){
            //triangle.expand(0,h-1,0,w-1);
            float i0=0,i1=0,man;
            int flag;
            float a;
            float x0 = triangle.x0;
            float x1 = triangle.x1;
            float x2 = triangle.x2;
            float y0 = triangle.y0;
            float y1 = triangle.y1;
            float y2 = triangle.y2;
            float xMin = triangle.xMin;
            float xMax = triangle.xMax;
            float yMin = triangle.yMin;
            float yMax = triangle.yMax;
            int rTotal=0,gTotal=0,bTotal=0,count=0,color;
            int m;

            //System.out.format("triangulation (%d,%d) (%d,%d) (%d,%d)\n",x0,y0,x1,y1,x2,y2);

            rTotal += redOrig((int)x0,(int)y0);
            rTotal += redOrig((int)x1,(int)y1);
            rTotal += redOrig((int)x2,(int)y2);

            bTotal += blueOrig((int) x0, (int) y0);
            bTotal += blueOrig((int) x1, (int) y1);
            bTotal += blueOrig((int) x2, (int) y2);

            gTotal += greenOrig((int) x0, (int) y0);
            gTotal += greenOrig((int) x1, (int) y1);
            gTotal += greenOrig((int) x2, (int) y2);

            count = 3;

            boolean fail = false;

            for(int pass = 0; pass<2 && !fail; pass++) {
                for (int y = (int) yMin; y <= yMax && !fail; y++) {
                    flag = 0;
                    a = 1.f * (y - y0) / (y1 - y0);
                    flag += ((a >= 0) && (a <= 1)) ? 1 : 0;

                    if (flag == 1) {
                        i0 = (int) (x0 + (x1 - x0) * a);
                    }

                    a = 1.f * (y - y1) / (y2 - y1);
                    flag += ((a >= 0) && (a <= 1)) ? 1 : 0;

                    if (flag == 1) {
                        i0 = (int) (x1 + (x2 - x1) * a);
                    } else if (flag == 2) {
                        i1 = (int) (x1 + (x2 - x1) * a);
                    }

                    if (flag == 1) {
                        a = 1.f * (y - y2) / (y0 - y2);
                        i1 = (int) (x2 + (x0 - x2) * a);
                    }

                    if (i0 > i1) {
                        man = i0;
                        i0 = i1;
                        i1 = man;
                    }
                    i0 = (int) Math.floor(i0);
                    i1 = (int) Math.ceil(i1);
                    if(i0 < xMin) i0 = xMin;
                    if(i1 > xMax) i1 = xMax;
                    if(canceled)return;
                    //System.out.format("Points on line %d from %d to %d: ",(int)y,(int)i0,(int)i1);
                    for (int x = (int) i0; x <= i1 && !fail; x++) {
                        if(canceled)return;
                        //System.out.format("(%d,%d) ",x,(int)y);
                        count++;
                        if(pass == 0) {
                            rTotal += redOrig(x, y);
                            gTotal += greenOrig(x, y);
                            bTotal += blueOrig(x, y);
                        }else{
                            m = abs((char) rTotal,redOrig(x,y)) +
                                    abs((char) gTotal,greenOrig(x,y)) +
                                    abs((char) bTotal,blueOrig(x,y));
                            if(m > threshold)
                                fail = true;
                        }
                    }
                    //System.out.println();
                }
                if(pass==0) {
                    rTotal /= count;
                    gTotal /= count;
                    bTotal /= count;
                }
            }
            if(canceled)return;
            if(count==0)return;
            if(!fail || triangle.area<=3){
                triangle.color = 0xff000000 | (rTotal<<16) | (gTotal<<8) | bTotal;
                synchronized (triangles) {
                    triangles.add(triangle);
                }
            }else{

                double dist0 = Math.sqrt((x0-x1)*(x0-x1) + (y0-y1)*(y0-y1));
                double dist1 = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
                double dist2 = Math.sqrt((x0-x2)*(x0-x2) + (y0-y2)*(y0-y2));

                float r = random.nextFloat()*0.6f + 0.2f;
                //r = 0.5f;
                float delta = 0.01f;
                Triangle t1,t2;
                if(dist0>=dist1 && dist0>=dist2){
                    t1 = new Triangle(interpolate(x0,x1,r),interpolate(y0,y1,r),x2,y2,x0,y0);
                    r -= delta;
                    t2 = new Triangle(interpolate(x0,x1,r),interpolate(y0,y1,r),x2,y2,x1,y1);
                }else if(dist1>=dist0 && dist1>=dist2){
                    t1 = new Triangle(interpolate(x2,x1,r),interpolate(y2,y1,r),x0,y0,x2,y2);
                    r -= delta;
                    t2 = new Triangle(interpolate(x2,x1,r),interpolate(y2,y1,r),x0,y0,x1,y1);
                }else{
                    t1 = new Triangle(interpolate(x2,x0,r),interpolate(y2,y0,r),x1,y1,x2,y2);
                    r -= delta;
                    t2 = new Triangle(interpolate(x2,x0,r),interpolate(y2,y0,r),x1,y1,x0,y0);
                }

                if(t1.area>0.5)recTriangulation(t1);
                if(t2.area>0.5)recTriangulation(t2);
            }
        }
    }


    public float interpolate(float a, float b,float x){
        return a + x*(b-a);
    }

    public char redOrig(int x,int y){
        return originalRedArray[y*w+x];
    }
    public char blueOrig(int x,int y){
        return originalBlueArray[y*w+x];
    }
    public char greenOrig(int x,int y){
        return originalGreenArray[y*w+x];
    }

    private char abs(char a,char b){
        return (char) (a>b ? a-b : b-a);
    }
}
