package vectorizer;

import utils.ImagePanel;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class BaseVectorizer {
    protected BufferedImage originalImage;
    protected BufferedImage destImage;
    protected char[] originalRedArray;
    protected char[] originalGreenArray;
    protected char[] originalBlueArray;
    protected int[] originalColorArray;
    protected short w,h;
    protected int area;
    public int threshold=-1;
    protected ImagePanel destImagePanel;
    protected JobThread lastJob;
    protected final Object jobLock=new Object();
    protected StringBuilder svgStringBuilder = new StringBuilder(2000000);



    protected final static byte NONE_TYPE = 0;
    protected final static byte SQUARE_TYPE = 1;
    protected final static byte TRIANGLE_TYPE = 2;
    protected final static byte POLYGON_TYPE = 3;

    public abstract void startJob();
    public abstract void cancelLastJob();

    public abstract void exportToOutputStream(OutputStream os);
    public abstract void importFromInputStream(InputStream is);
    public abstract void exportToHTML(OutputStream os);
    public abstract void exportToSVG(OutputStream os,boolean isCompressed);

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

    protected char abs(char a,char b){
        return (char) (a>b ? a-b : b-a);
    }

    private int averageColor(int r,int g,int b,int count){
        return 0xff000000 | (r<<16) | (g<<8) | b;
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

    public void setOriginalImage(BufferedImage image){
        if(image == null){
            cancelLastJob();
            originalImage = null;
            w = -1;
            h = -1;
            area = -1;
            destImage = null;
            return;
        }
        if(originalImage==null || image!=originalImage) {
            cancelLastJob();
            originalImage = image;
            w = (short) originalImage.getWidth();
            h = (short) originalImage.getHeight();
            area = w * h;
            destImage = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
            initialize();
        }
    }

    public void setDestImagePanel(ImagePanel p){
        destImagePanel = p;
    }

    public class JobThread extends Thread{
        protected boolean canceled = false;
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    protected abstract void constructStringSVG();
}
