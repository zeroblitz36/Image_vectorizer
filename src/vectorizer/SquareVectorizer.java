package vectorizer;

import mainpackage.MainForm;
import mainpackage.Triangle;
import utils.ImagePanel;
import utils.Utility;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SquareVectorizer extends BaseVectorizer{
    private ArrayList<SquareFragment> fragList = new ArrayList<>();
    private final Object fragListLock = new Object();
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
    private class Job extends JobThread{

        @Override
        public void run() {
            if (originalImage == null || canceled) return;
            int w = originalImage.getWidth();
            int h = originalImage.getHeight();
            SquareFragment squareFragment = new SquareFragment(0, w - 1, 0, h - 1, -1);
            startTime = System.currentTimeMillis();
            //recFragCheck(squareFragment);
            splitRecFragCheck(squareFragment);
            //executorService.shutdown();
            endTime = System.currentTimeMillis();
            if (canceled) return;
            startTime = System.currentTimeMillis();
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = image.createGraphics();
            for (SquareFragment s : fragList) {
                if (canceled) return;
                g.setColor(new Color(s.color));
                g.fillRect(s.l, s.t, s.r - s.l + 1, s.d - s.t + 1);
            }
            endTime = System.currentTimeMillis();
            if (canceled) return;
            if(destImagePanel!=null)
                destImagePanel.setImage(image);
        }

        private void recFragCheck(SquareFragment s) {
            int rTotal = 0, gTotal = 0, bTotal = 0, count = 0, color;
            int r, g, b;
            int t, min = 9999, max = -9999;

            boolean fail = false;

            for (int y = s.t; y <= s.d && !fail; y++)
                for (int x = s.l; x <= s.r && !fail; x++) {
                    if (canceled) return;
                    count++;
                    color = originalImage.getRGB(x, y);
                    b = color & 0xff;
                    color >>= 8;
                    g = color & 0xff;
                    color >>= 8;
                    r = color & 0xff;

                    t = r + g + b;
                    bTotal += b;
                    gTotal += g;
                    rTotal += r;

                    if (t < min) min = t;
                    if (t > max) max = t;

                    if (max - min > threshold << 1) {
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
                    color = originalImage.getRGB(x, y);
                    if (Utility.manhattanDistance(color, avgColor) > threshold)
                        fail = true;
                }
            if (!fail) {
                s.color = avgColor;
                synchronized (fragListLock) {
                    fragList.add(s);
                }
            } else {
                int midX = (int) (s.l + (random.nextFloat() / 2 + 0.25f) * (s.r - s.l));
                int midY = (int) (s.t + (random.nextFloat() / 2 + 0.25f) * (s.d - s.t));

                SquareFragment s1 = new SquareFragment(s.l, midX, s.t, midY, -1);
                SquareFragment s2 = new SquareFragment(midX + 1, s.r, s.t, midY, -1);
                SquareFragment s3 = new SquareFragment(s.l, midX, midY + 1, s.d, -1);
                SquareFragment s4 = new SquareFragment(midX + 1, s.r, midY + 1, s.d, -1);

                if (s1.isValid()) recFragCheck(s1);
                if (s2.isValid()) recFragCheck(s2);
                if (s3.isValid()) recFragCheck(s3);
                if (s4.isValid()) recFragCheck(s4);
            }
        }


        private void splitRecFragCheck(SquareFragment s) {
            int midX = (int) (s.l + (random.nextFloat() / 2 + 0.25f) * (s.r - s.l));
            int midY = (int) (s.t + (random.nextFloat() / 2 + 0.25f) * (s.d - s.t));

            SquareFragment squareFragments[] = new SquareFragment[]{
                    new SquareFragment(s.l, midX, s.t, midY, -1),
                    new SquareFragment(midX + 1, s.r, s.t, midY, -1),
                    new SquareFragment(s.l, midX, midY + 1, s.d, -1),
                    new SquareFragment(midX + 1, s.r, midY + 1, s.d, -1)
            };
            ArrayList<Thread> threads = new ArrayList<>(4);
            for (SquareFragment squareFragment : squareFragments) {
                final SquareFragment sf = squareFragment;
                if (sf.isValid()) {
                    Thread t = new Thread(() -> {
                        recFragCheck(sf);
                    });
                    threads.add(t);
                    t.start();
                }
            }
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public class SquareFragment{
        public int l,r,t,d,color;

        public SquareFragment(int l, int r, int t, int d, int color) {
            this.l = l;
            this.r = r;
            this.t = t;
            this.d = d;
            this.color = color;
        }

        public boolean isValid(){
            return l<=r && t<=d;
        }
    }
}
