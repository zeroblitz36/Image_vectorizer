package vectorizer;

import mainpackage.Triangle;
import utils.ImagePanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class TriangleVectorizer extends BaseVectorizer{

    private Random random = new Random(System.currentTimeMillis());
    private ArrayList<Triangle> lastSavedTriangleList = null;

    public void setDestImagePanel(ImagePanel p){
        destImagePanel = p;
    }

    public void startJob() {
        synchronized (jobLock) {
            if (lastJob != null) {
                lastJob.setCanceled(true);
                try {
                    lastJob.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            lastJob = new Job();
            lastJob.start();
        }
    }
    public void cancelLastJob(){
        synchronized (jobLock){
            if (lastJob != null) {
                lastJob.setCanceled(true);
                try {
                    lastJob.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void drawTriangles(ArrayList<Triangle> list){
        if(destImagePanel!=null){
            Graphics2D g = destImage.createGraphics();
            for(Triangle t:list){
                g.setColor(new Color(t.color));
                g.fill(t.getClonePath());
            }
            destImagePanel.setImage(destImage);
        }
    }

    private class Job extends JobThread{
        private ArrayList<Triangle> triangles = new ArrayList<>();
        private final Object triangleLock = new Object();
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

            Thread th = new Thread(() -> {
                recTriangulation(t1);
            });
            th.start();

            recTriangulation(t2);


            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(canceled)return;
            drawTriangles(triangles);
            lastSavedTriangleList = triangles;
        }

        public void recTriangulation(Triangle triangle){
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
            int rTotal=0,gTotal=0,bTotal=0,count;
            int m;

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
                    for (int x = (int) i0; x <= i1 && !fail; x++) {
                        if(canceled)return;
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
                synchronized (triangleLock) {
                    triangles.add(triangle);
                }
            }else{

                double dist0 = Math.sqrt((x0-x1)*(x0-x1) + (y0-y1)*(y0-y1));
                double dist1 = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
                double dist2 = Math.sqrt((x0-x2)*(x0-x2) + (y0-y2)*(y0-y2));

                float r = random.nextFloat()*0.6f + 0.2f;
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

    public void exportToOutputStream(DataOutputStream os) {
        try {
            os.writeByte(SQUARE_TYPE);
            os.writeInt(lastSavedTriangleList.size());
            for(Triangle t : lastSavedTriangleList){
                os.writeFloat(t.x0);
                os.writeFloat(t.y0);
                os.writeFloat(t.x1);
                os.writeFloat(t.y1);
                os.writeFloat(t.x2);
                os.writeFloat(t.y2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void importFromInputStream(DataInputStream is) {
        try {
            byte type = is.readByte();
            if(type!=SQUARE_TYPE)throw new RuntimeException("Incorrect vectorizer");
            int count = is.readInt();
            ArrayList<Triangle> list = new ArrayList<>(count);
            float x0, y0, x1, y1, x2, y2;
            for (int i = 0; i < count; i++) {
                x0 = is.readFloat();
                y0 = is.readFloat();
                x1 = is.readFloat();
                y1 = is.readFloat();
                x2 = is.readFloat();
                y2 = is.readFloat();
                list.add(new Triangle(x0, y0, x1, y1, x2, y2));
            }
            lastSavedTriangleList = list;
            drawTriangles(lastSavedTriangleList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
