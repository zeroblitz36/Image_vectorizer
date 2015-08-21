package vectorizer;

import utils.ImagePanel;
import utils.Utility;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Locale;
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
            int size = 32;
            Font myFont = new Font("Serif",Font.BOLD, size);
            g.setFont(myFont);
            g.setColor(Color.RED);
            g.drawString("SVG: "+svgStringBuilder.length() + " B",1,size+1);
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
            lastSavedTriangleList = triangles;
            constructStringSVG();
            drawTriangles(triangles);
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

    public void exportToOutputStream(OutputStream os) {
        try {
            ObjectOutput oo = new ObjectOutputStream(os);
            oo.writeObject(lastSavedTriangleList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void importFromInputStream(InputStream is) {
        try {
            ObjectInput oi = new ObjectInputStream(is);
            ArrayList<Triangle> list = (ArrayList<Triangle>) oi.readObject();
            lastSavedTriangleList = list;
            drawTriangles(lastSavedTriangleList);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exportToHTML(OutputStream os) {

    }

    @Override
    public void exportToSVG(OutputStream os) {
        BufferedOutputStream bos = new BufferedOutputStream(os);
        Locale.setDefault(Locale.US);

        try {
            /*Utility.writeTo(String.format("<svg xmlns='http://www.w3.org/2000/svg' version='1.1' width='%d' height='%d'>", w, h), bos);
            for(Triangle t : lastSavedTriangleList){
                String s = String.format("<polyline points='%f,%f %f,%f %f,%f' style='fill:#%06X' />\n",
                        t.x0,t.y0,t.x1,t.y1,t.x2,t.y2,t.color&0xffffff);
                Utility.writeTo(s,bos);
            }
            Utility.writeTo("</svg>",bos);*/
            Utility.writeTo(svgStringBuilder.toString(),bos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            os.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void constructStringSVG() {
        Locale.setDefault(Locale.US);
        svgStringBuilder.setLength(0);
        svgStringBuilder.append(String.format("<svg xmlns='http://www.w3.org/2000/svg' version='1.1' width='%d' height='%d'>", w, h));
        for(Triangle t:lastSavedTriangleList){
            svgStringBuilder.append(String.format("<polyline points='%f,%f %f,%f %f,%f' style='fill:#%06X' />\n",
                    t.x0,t.y0,t.x1,t.y1,t.x2,t.y2,t.color&0xffffff));
        }
        svgStringBuilder.append("</svg>");
    }
}
