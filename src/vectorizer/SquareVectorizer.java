package vectorizer;

import utils.ProtoBitSet;
import utils.Utility;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

public class SquareVectorizer extends BaseVectorizer{
    private ArrayList<SquareFragment> lastSavedSquareList;
    private ArrayList<SquareTree> lastSavedSquareTreeList;
    private ProtoBitSet lastBitSet;
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

    public void drawFunction(ArrayList<SquareFragment> list){
        if(destImage!=null) {
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = image.createGraphics();
            for (SquareFragment s : list) {
                g.setColor(new Color(s.color));
                g.fillRect(s.l, s.t, s.r - s.l + 1, s.d - s.t + 1);
            }
            destImagePanel.setImage(image);
        }
    }

    private class Job extends JobThread{
        private ArrayList<SquareFragment> fragList = new ArrayList<>();
        private ArrayList<SquareTree> squareTreeList = new ArrayList<>();
        @Override
        public void run() {
            if (originalImage == null || canceled) return;
            SquareFragment squareFragment = new SquareFragment((short)0,(short) (w - 1),(short) 0, (short)(h - 1), -1);
            startTime = System.currentTimeMillis();
            recFragCheck(squareFragment);
            //splitRecFragCheck(squareFragment);
            endTime = System.currentTimeMillis();
            lastSavedSquareList = fragList;
            lastSavedSquareTreeList = squareTreeList;
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
                squareTreeList.add(new SquareTree(s.color,-1,-1));
            } else {
                short midX = (short) (s.l + (random.nextFloat() / 2 + 0.25f) * (s.r - s.l));
                short midY = (short) (s.t + (random.nextFloat() / 2 + 0.25f) * (s.d - s.t));
                squareTreeList.add(new SquareTree(-1,midX,midY));
                SquareFragment s1 = new SquareFragment(s.l, midX, s.t, midY, -1);
                SquareFragment s2 = new SquareFragment((short) (midX + 1), s.r, s.t, midY, -1);
                SquareFragment s3 = new SquareFragment(s.l, midX, (short) (midY + 1), s.d, -1);
                SquareFragment s4 = new SquareFragment((short)(midX + 1), s.r, (short) (midY + 1), s.d, -1);

                if (s1.isValid()) recFragCheck(s1);
                if (s2.isValid()) recFragCheck(s2);
                if (s3.isValid()) recFragCheck(s3);
                if (s4.isValid()) recFragCheck(s4);
            }
        }


        private void splitRecFragCheck(SquareFragment s) {
            short midX = (short) (s.l + (random.nextFloat() / 2 + 0.25f) * (s.r - s.l));
            short midY = (short) (s.t + (random.nextFloat() / 2 + 0.25f) * (s.d - s.t));

            SquareFragment squareFragments[] = new SquareFragment[]{
                    new SquareFragment(s.l, midX, s.t, midY, -1),
                    new SquareFragment((short) (midX + 1), s.r, s.t, midY, -1),
                    new SquareFragment(s.l, midX, (short) (midY + 1), s.d, -1),
                    new SquareFragment((short)(midX + 1), s.r, (short) (midY + 1), s.d, -1)
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

    public void exportToOutputStream(OutputStream os) {
        try {
            prepareProtoBitSet();
            os.write(lastBitSet.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void prepareProtoBitSet(){
        byte coordDataSize = 0;
        int max = w>h?w:h;
        while(max!=0){
            max>>=1;
            coordDataSize++;
        }
        ProtoBitSet pbs = new ProtoBitSet();
        //coordinate data size
        pbs.push(coordDataSize,8);
        //width and height
        pbs.push(w,coordDataSize);
        pbs.push(h,coordDataSize);
        //push all square trees
        for(SquareTree st : lastSavedSquareTreeList){
            //System.out.format("BitSet size = %d\n",pbs.currentLength);
            if(st.midX!=-1){
                pbs.push(1,1);
                pbs.push(st.midX,coordDataSize);
                pbs.push(st.minY,coordDataSize);
            }else{
                pbs.push(0,1);
                pbs.push(st.color,32);
            }
        }
        //write the size
        pbs.writeSize();
        System.out.format("BitSet size = %d\n",pbs.currentLength);
        lastBitSet = pbs;
    }
    public void importFromInputStream(InputStream is) {
        try{
            ObjectInput oi = new ObjectInputStream(is);
            ArrayList<SquareFragment> list = (ArrayList<SquareFragment>) oi.readObject();
            lastSavedSquareList = list;
            drawFunction(lastSavedSquareList);
        }catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
