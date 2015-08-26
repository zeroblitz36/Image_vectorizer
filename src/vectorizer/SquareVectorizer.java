package vectorizer;

import utils.ProtoBitSet;
import utils.Utility;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

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
            int size = 32;
            Font myFont = new Font("Serif",Font.BOLD, size);
            g.setFont(myFont);
            g.setColor(Color.RED);
            g.drawString("SVG: "+svgStringBuilder.length() + " B",1,size+1);
            //get compressed size
            ByteArrayOutputStream baos = new ByteArrayOutputStream(size/2);
            exportToSVG(baos, true);
            int compressedSize = baos.size();
            g.drawString("SVGZ: "+compressedSize+" B",1,2*size + 2);
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
            constructStringSVG();
            drawFunction(lastSavedSquareList);
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
                    color = colorOrig(x, y);
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
                    color = colorOrig(x, y);
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
    public void prepareProtoBitSet(){
        byte coordDataSize = 0;
        int max = w>h?w:h;
        while(max!=0){
            max>>=1;
            coordDataSize++;
        }
        ProtoBitSet pbs = new ProtoBitSet();
        pbs.resetWriteCounter();
        //coordinate data size
        pbs.push(coordDataSize,8);
        //width and height
        pbs.push(w,coordDataSize);
        pbs.push(h, coordDataSize);
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

    public void decodeSquareTreeArray(ArrayList<SquareTree> list){

    }
    private void decodeBitSet(ProtoBitSet pbs){
        pbs.resetReadCounter();
        //coordinate data size
        byte coordDataSize = pbs.readByte(8);
        //width and height
        short w = pbs.readShort(coordDataSize);
        short h = pbs.readShort(coordDataSize);
        //get all square trees
        int size = pbs.getSize();
        ArrayList<SquareTree> list = new ArrayList<>();
        while(pbs.getReadCounter()<size){
            list.add(decodeSquareTrees(pbs,coordDataSize));
        }
        lastSavedSquareTreeList = list;
    }
    private SquareTree decodeSquareTrees(ProtoBitSet pbs,int dataSize){
        //if its true then it contains further child nodes
        if(pbs.readBoolean()){
            return new SquareTree(0,pbs.readInt(dataSize),pbs.readInt(dataSize));
        }
        //if its false then this square tree is a child
        else{
            return new SquareTree(pbs.readInt(32),-1,-1);
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

    public void importFromInputStream(InputStream is) {
        try{
            ProtoBitSet pbs = new ProtoBitSet();
            byte[] buffer = new byte[1000];
            int l;
            while((l=is.read(buffer))!=-1){
                for(int i=0;i<l;i++)
                    pbs.push(buffer[i],8);
            }

            drawFunction(lastSavedSquareList);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void exportToHTML(OutputStream os) {
        BufferedOutputStream bos = new BufferedOutputStream(os);
        Locale.setDefault(Locale.US);
        try {
            Utility.writeTo("<!DOCTYPE html>\n<html>\n<body>\n", bos);
            Utility.writeTo(String.format("<svg width='%d' height='%d'>\n", w, h), bos);
            for(SquareFragment sf : lastSavedSquareList){
                Utility.writeTo(String.format("<rect x='%f' y='%f' width='%f' height='%f' style='fill:#%06X'/>\n",
                        sf.l-0.5,
                        sf.t-0.5,
                        sf.r-sf.l+1.5,
                        sf.d-sf.t+1.5
                        ,sf.color&0xffffff),bos);
            }
            Utility.writeTo("</svg>\n</body>\n</html>",bos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exportToSVG(OutputStream os,boolean isCompressed) {
        if(isCompressed){
            try {
                os = new GZIPOutputStream(os);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedOutputStream bos = new BufferedOutputStream(os);
        try {
            Utility.writeTo(svgStringBuilder.toString(),bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void constructStringSVG() {
        Locale.setDefault(Locale.US);
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
    }
}
