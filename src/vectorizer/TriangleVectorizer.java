package vectorizer;

import utils.ImagePanel;
import utils.Utility;

import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

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
            setIsDone(false);
            lastJob = new Job();
            aproxCompletedPixelCount.set(0);
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
            setIsDone(true);
        }
    }

    private void drawTriangles(ArrayList<Triangle> list){
        if(destImagePanel!=null || isInBenchmark){
            Graphics2D g = destImage.createGraphics();
            for(Triangle t:list){
                g.setColor(new Color(t.color));
                g.fill(t.getClonePath());
            }

            if(!isInBenchmark) {
                /*int size = 32;
                Font myFont = new Font("Serif", Font.BOLD, size);
                g.setFont(myFont);
                g.setColor(Color.RED);
                g.drawString("SVG: " + svgStringBuilder.length() + " B", 1, size + 1);

                //get compressed size
                ByteArrayOutputStream baos = new ByteArrayOutputStream(size / 2);
                exportToSVG(baos, true);
                int compressedSize = baos.size();
                g.drawString("SVGZ: " + compressedSize + " B", 1, 2 * size + 2);
                */
                destImagePanel.setImage(destImage);
            }
        }
    }

    private class Job extends JobThread{
        private ArrayList<Triangle> triangles = new ArrayList<>();
        @Override
        public void run() {
            if(canceled)return;
            final Triangle t1 = new Triangle(0,0,w-1,0,w-1,h-1);
            final Triangle t2 = new Triangle(0,0,0,h-1,w-1,h-1);

            ArrayList<Triangle> triangleArray1 = new ArrayList<>();
            ArrayList<Triangle> triangleArray2 = new ArrayList<>();
            Thread th = new Thread(() -> {
                recTriangulation(t1,triangleArray1);
            });
            th.start();

            recTriangulation(t2, triangleArray2);

            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            triangles.ensureCapacity(triangleArray1.size() + triangleArray2.size());
            triangles.addAll(triangleArray1);
            triangles.addAll(triangleArray2);

            if(canceled)return;
            lastSavedTriangleList = triangles;
            th = new Thread(new Runnable() {
                @Override
                public void run() {
                    constructStringSVG();
                    updateDetails(String.format("SVG:%s SVGZ:%s",
                            Utility.aproximateDataSize(svgStringBuilder.length()),
                            Utility.aproximateDataSize(svgzStringBuilder.length())));
                }
            });
            th.start();
            drawTriangles(triangles);
            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setIsDone(true);
        }

        public void recTriangulation(Triangle triangle,ArrayList<Triangle> triangles){
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
            int rTotal=0,gTotal=0,bTotal=0,count=0;
            int rMed=0,gMed=0,bMed=0;
            int m;

            rMed += redOrig((int)x0,(int)y0);
            rMed += redOrig((int)x1,(int)y1);
            rMed += redOrig((int)x2,(int)y2);

            bMed += blueOrig((int) x0, (int) y0);
            bMed += blueOrig((int) x1, (int) y1);
            bMed += blueOrig((int) x2, (int) y2);

            gMed += greenOrig((int) x0, (int) y0);
            gMed += greenOrig((int) x1, (int) y1);
            gMed += greenOrig((int) x2, (int) y2);

            rMed/=3;
            gMed/=3;
            bMed/=3;

            boolean fail = false;

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
                    rTotal += redOrig(x, y);
                    gTotal += greenOrig(x, y);
                    bTotal += blueOrig(x, y);
                    m = Math.abs(rMed - redOrig(x, y)) +
                            Math.abs(gMed - greenOrig(x, y)) +
                            Math.abs(bMed - blueOrig(x, y));
                    if(m > threshold)
                        fail = true;
                }
            }

            if(canceled)return;
            if(count==0)
                return;
            else {
                rTotal /= count;
                gTotal /= count;
                bTotal /= count;
            }
            if(!fail || triangle.area<=3){
                triangle.color = 0xff000000 | (rTotal<<16) | (gTotal<<8) | bTotal;
                triangles.add(triangle);
                int x = aproxCompletedPixelCount.addAndGet((int)Math.ceil(triangle.area * 100));
                updateDetails(String.format("Progress : %.1f%%",1.f*x/area));
            }else{

                double dist0 = Math.sqrt((x0-x1)*(x0-x1) + (y0-y1)*(y0-y1));
                double dist1 = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
                double dist2 = Math.sqrt((x0-x2)*(x0-x2) + (y0-y2)*(y0-y2));

                float r = random.nextFloat()*0.6f + 0.2f;
                float delta = 0.01f;
                delta = 0;
                Triangle t1,t2;
                if(dist0>=dist1 && dist0>=dist2){
                    t1 = new Triangle(Utility.interpolate(x0, x1, r),Utility.interpolate(y0, y1, r),x2,y2,x0,y0);
                    r -= delta;
                    t2 = new Triangle(Utility.interpolate(x0, x1, r),Utility.interpolate(y0, y1, r),x2,y2,x1,y1);
                }else if(dist1>=dist0 && dist1>=dist2){
                    t1 = new Triangle(Utility.interpolate(x2, x1, r),Utility.interpolate(y2, y1, r),x0,y0,x2,y2);
                    r -= delta;
                    t2 = new Triangle(Utility.interpolate(x2, x1, r),Utility.interpolate(y2, y1, r),x0,y0,x1,y1);
                }else{
                    t1 = new Triangle(Utility.interpolate(x2, x0, r),Utility.interpolate(y2, y0, r),x1,y1,x2,y2);
                    r -= delta;
                    t2 = new Triangle(Utility.interpolate(x2, x0, r),Utility.interpolate(y2, y0, r),x1,y1,x0,y0);
                }

                if(t1.area>0.5)recTriangulation(t1,triangles);
                if(t2.area>0.5)recTriangulation(t2,triangles);
            }
        }
    }

    @Override
    protected void constructStringSVG() {
        Locale.setDefault(Locale.US);
        svgStringBuilder.setLength(0);
        svgStringBuilder.append(String.format("<svg xmlns='http://www.w3.org/2000/svg' version='1.1' width='%d' height='%d'>\n", w, h));
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        svgStringBuilder.append(String.format("<g stroke-width='0.5'>\n"));
        for(Triangle t:lastSavedTriangleList){
            svgStringBuilder.append(String.format("<path d='M%s,%sL%s,%sL%s,%sZ' fill='#%06X' stroke='#%06X'/>\n",
                    decimalFormat.format(t.x0),
                    decimalFormat.format(t.y0),
                    decimalFormat.format(t.x1),
                    decimalFormat.format(t.y1),
                    decimalFormat.format(t.x2),
                    decimalFormat.format(t.y2),
                    t.color&0xffffff,
                    t.color&0xffffff));
        }
        svgStringBuilder.append("</g>\n");
        svgStringBuilder.append("</svg>");


        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(svgStringBuilder.length());
            GZIPOutputStream gzos = new GZIPOutputStream(baos,true);
            gzos.write(svgStringBuilder.toString().getBytes());
            gzos.flush();
            svgzStringBuilder.setLength(0);
            svgzStringBuilder.append(baos.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
