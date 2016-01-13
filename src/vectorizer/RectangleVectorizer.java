package vectorizer;

import utils.Utility;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class RectangleVectorizer extends BaseVectorizer{
    private ArrayList<RectangleFragment> lastRectangleSquareList;
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

    public void drawFunction(ArrayList<RectangleFragment> list){
        if(destImage!=null || isInBenchmark) {
            Graphics2D g = destImage.createGraphics();
            for (RectangleFragment s : list) {
                g.setColor(new Color(s.color));
                g.fillRect(s.l, s.t, s.r - s.l + 1, s.d - s.t + 1);
            }
            if(!isInBenchmark) {
                destImagePanel.setImage(destImage);
            }
        }
    }

    private class Job extends JobThread{
        private ArrayList<RectangleFragment> fragList = new ArrayList<>();
        @Override
        public void run() {
            if (originalImage == null || canceled) return;
            RectangleFragment rectangleFragment = new RectangleFragment((short)0,(short) (w - 1),(short) 0, (short)(h - 1), -1);
            startTime = System.currentTimeMillis();
            RectangleFragment s1 = new RectangleFragment();
            RectangleFragment s2 = new RectangleFragment();
            RectangleFragment s3 = new RectangleFragment();
            RectangleFragment s4 = new RectangleFragment();
            splitRectangleFragmentInFour(rectangleFragment,s1,s2,s3,s4);
            LinkedList<RectangleFragment> fragList1 = new LinkedList<>();
            LinkedList<RectangleFragment> fragList2 = new LinkedList<>();
            LinkedList<RectangleFragment> fragList3 = new LinkedList<>();
            LinkedList<RectangleFragment> fragList4 = new LinkedList<>();
            Thread t1 = new Thread(() -> {
                if (s1.isValid()) recFragCheck(s1,fragList1);
            });
            Thread t2 = new Thread(() -> {
                if (s2.isValid()) recFragCheck(s2,fragList2);
            });
            Thread t3 = new Thread(() -> {
                if (s3.isValid()) recFragCheck(s3,fragList3);
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
            lastRectangleSquareList = fragList;
            if (canceled) return;
            Thread th = new Thread(() -> {
                constructStringSVG();
                updateDetails(String.format("SVG:%s SVGZ:%s",
                        Utility.aproximateDataSize(svgStringBuilder.length()),
                        Utility.aproximateDataSize(svgzStringBuilder.length())));
            });
            th.start();
            drawFunction(lastRectangleSquareList);
            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setIsDone(true);
        }

        private void recFragCheck(RectangleFragment s, Collection<RectangleFragment> localFragList) {
            int rTotal, gTotal, bTotal, avgColor;
            rTotal=(redOrig(s.l,s.t)+redOrig(s.r,s.t)+redOrig(s.l,s.d)+redOrig(s.r,s.d))/4;
            gTotal=(greenOrig(s.l, s.t)+greenOrig(s.r, s.t)+greenOrig(s.l, s.d)+greenOrig(s.r, s.d))/4;
            bTotal=(blueOrig(s.l, s.t)+blueOrig(s.r, s.t)+blueOrig(s.l, s.d)+blueOrig(s.r, s.d))/4;
            avgColor = 0xff000000 | (rTotal << 16) | (gTotal << 8) | bTotal;

            boolean fail = false;
            rTotal = 0; gTotal = 0; bTotal = 0;
            for (int y = s.t; y <= s.d && !fail; y++)
                for (int x = s.l; x <= s.r && !fail; x++) {
                    if (canceled) return;
                    rTotal += redOrig(x,y); gTotal += greenOrig(x, y); bTotal += blueOrig(x,y);
                    if (Utility.manhattanDistance(colorOrig(x, y), avgColor) > threshold)
                        fail = true;
                }
            if (!fail) {
                int count = (s.d-s.t+1)*(s.r-s.l+1);
                rTotal /= count; bTotal /= count; gTotal /= count;
                avgColor = 0xff000000 | (rTotal << 16) | (gTotal << 8) | bTotal;
                s.color = avgColor;
                localFragList.add(s);
                int x = aproxCompletedPixelCount.addAndGet(s.area());
                updateDetails(String.format("Progress : %.1f%%", 100.f * x / area));
            } else {
                RectangleFragment s1 = new RectangleFragment();
                RectangleFragment s2 = new RectangleFragment();
                RectangleFragment s3 = new RectangleFragment();
                RectangleFragment s4 = new RectangleFragment();
                splitRectangleFragmentInFour(s,s1,s2,s3,s4);

                if (s1.isValid()) recFragCheck(s1,localFragList);
                if (s2.isValid()) recFragCheck(s2,localFragList);
                if (s3.isValid()) recFragCheck(s3,localFragList);
                if (s4.isValid()) recFragCheck(s4,localFragList);
            }
        }

        private void splitRectangleFragmentInFour(RectangleFragment s, RectangleFragment s1, RectangleFragment s2, RectangleFragment s3, RectangleFragment s4){
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
        for(RectangleFragment sf : lastRectangleSquareList) {
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
