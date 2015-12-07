package vectorizer;

import utils.Utility;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class SquareVectorizer extends BaseVectorizer{
    private ArrayList<SquareFragment> lastSavedSquareList;
    private Random random = new Random(System.currentTimeMillis());
    long startTime, endTime;


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

    public void drawFunction(ArrayList<SquareFragment> list){
        if(destImage!=null || isInBenchmark) {
            Graphics2D g = destImage.createGraphics();
            for (SquareFragment s : list) {
                g.setColor(new Color(s.color));
                g.fillRect(s.l, s.t, s.r - s.l + 1, s.d - s.t + 1);
            }
            if(!isInBenchmark) {
                destImagePanel.setImage(destImage);
            }
        }
    }

    private class Job extends JobThread{
        private ArrayList<SquareFragment> fragList = new ArrayList<>();
        @Override
        public void run() {
            if (originalImage == null || canceled) return;
            SquareFragment squareFragment = new SquareFragment((short)0,(short) (w - 1),(short) 0, (short)(h - 1), -1);
            startTime = System.currentTimeMillis();
            SquareFragment s1 = new SquareFragment();
            SquareFragment s2 = new SquareFragment();
            SquareFragment s3 = new SquareFragment();
            SquareFragment s4 = new SquareFragment();
            splitSquareFragmentInFour(squareFragment,s1,s2,s3,s4);
            LinkedList<SquareFragment> fragList1 = new LinkedList<>();
            LinkedList<SquareFragment> fragList2 = new LinkedList<>();
            LinkedList<SquareFragment> fragList3 = new LinkedList<>();
            LinkedList<SquareFragment> fragList4 = new LinkedList<>();
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (s1.isValid()) recFragCheck(s1,fragList1);
                }
            });
            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (s2.isValid()) recFragCheck(s2,fragList2);
                }
            });
            Thread t3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (s3.isValid()) recFragCheck(s3,fragList3);
                }
            });
            t1.start();
            t2.start();
            t3.start();
            if (s4.isValid()) recFragCheck(s4,fragList4);
            if(canceled) return;
            try {
                t1.join();
                t2.join();
                t3.join();
                if(canceled)return;
                fragList.ensureCapacity(fragList1.size()+fragList2.size()+
                fragList3.size()+fragList4.size());
                fragList.addAll(fragList1);
                fragList.addAll(fragList2);
                fragList.addAll(fragList3);
                fragList.addAll(fragList4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(canceled) return;
            endTime = System.currentTimeMillis();
            lastSavedSquareList = fragList;
            if (canceled) return;
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    constructStringSVG();
                    updateDetails(String.format("SVG:%s SVGZ:%s",
                            Utility.aproximateDataSize(svgStringBuilder.length()),
                            Utility.aproximateDataSize(svgzStringBuilder.length())));
                }
            });
            th.start();
            drawFunction(lastSavedSquareList);
            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setIsDone(true);
        }

        private void recFragCheck(SquareFragment s,Collection<SquareFragment> localFragList) {
            int rTotal = 0, gTotal = 0, bTotal = 0, count = 0, color;
            int r, g, b;
            int t, min = 9999, max = -9999;

            boolean fail = false;

            for (int y = s.t; y <= s.d && !fail; y++)
                for (int x = s.l; x <= s.r && !fail; x++) {
                    if (canceled) return;
                    count++;

                    r = redOrig(x,y);
                    g = greenOrig(x,y);
                    b = blueOrig(x,y);
                    t = r + g + b;
                    bTotal += b;
                    gTotal += g;
                    rTotal += r;

                    if (t < min) min = t;
                    if (t > max) max = t;

                    if (max - min > threshold) {
                        fail = true;
                    }
                }
            rTotal /= count;
            gTotal /= count;
            bTotal /= count;
            int avgColor = 0xff000000 | (rTotal << 16) | (gTotal << 8) | bTotal;
            for (int y = s.t; y <= s.d && !fail; y++)
                for (int x = s.l; x <= s.r && !fail; x++) {
                    if (canceled) return;
                    color = colorOrig(x, y);
                    if (Utility.manhattanDistance(color, avgColor) > threshold)
                        fail = true;
                }
            if (!fail) {
                s.color = avgColor;
                localFragList.add(s);
                int x = aproxCompletedPixelCount.addAndGet(s.area());
                updateDetails(String.format("Progress : %.1f%%", 100.f * x / area));
            } else {
                SquareFragment s1 = new SquareFragment();
                SquareFragment s2 = new SquareFragment();
                SquareFragment s3 = new SquareFragment();
                SquareFragment s4 = new SquareFragment();
                splitSquareFragmentInFour(s,s1,s2,s3,s4);

                if (s1.isValid()) recFragCheck(s1,localFragList);
                if (s2.isValid()) recFragCheck(s2,localFragList);
                if (s3.isValid()) recFragCheck(s3,localFragList);
                if (s4.isValid()) recFragCheck(s4,localFragList);
            }
        }

        private void splitSquareFragmentInFour(SquareFragment s,SquareFragment s1,SquareFragment s2,SquareFragment s3,SquareFragment s4){
            short midX = (short) (s.l + (random.nextFloat() / 2 + 0.25f) * (s.r - s.l));
            short midY = (short) (s.t + (random.nextFloat() / 2 + 0.25f) * (s.d - s.t));

            s1.set(s.l, midX, s.t, midY);
            s2.set((short) (midX + 1), s.r, s.t, midY);
            s3.set(s.l, midX, (short) (midY + 1), s.d);
            s4.set((short)(midX + 1), s.r, (short) (midY + 1), s.d);
        }
    }

    @Override
    protected void constructStringSVG() {
        svgStringBuilder.setLength(0);
        svgStringBuilder.append(String.format("<svg xmlns='http://www.w3.org/2000/svg' version='1.1' width='%d' height='%d'>\n", w, h));
        for(SquareFragment sf : lastSavedSquareList) {
            svgStringBuilder.append(String.format("<rect x='%d' y='%d' width='%d' height='%d' style='fill:#%06X'/>\n",
                    sf.l,
                    sf.t,
                    sf.r-sf.l+2,
                    sf.d-sf.t+2,
                    sf.color&0xffffff));
        }
        svgStringBuilder.append("</svg>");

        try {
            if(gzos==null)
                gzos = new GZIPOutputStream(baos,true);
            baos.reset();
            gzos.write(svgStringBuilder.toString().getBytes());
            gzos.flush();
            svgzStringBuilder.setLength(0);
            svgzStringBuilder.append(baos.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
