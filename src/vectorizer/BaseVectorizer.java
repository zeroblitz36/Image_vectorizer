package vectorizer;

import utils.ImagePanel;

import java.awt.image.BufferedImage;

public abstract class BaseVectorizer {
    protected BufferedImage originalImage;
    protected BufferedImage destImage;
    private char[] originalRedArray;
    private char[] originalGreenArray;
    private char[] originalBlueArray;
    private int[] originalColorArray;
    protected int w,h,area;
    public int threshold;
    protected ImagePanel destImagePanel;
    protected JobThread lastJob;
    protected final Object jobLock=new Object();

    public abstract void startJob();
    public abstract void cancelLastJob();

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
        cancelLastJob();
        originalImage = image;
        w = originalImage.getWidth();
        h = originalImage.getHeight();
        area = w*h;
        destImage = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);
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
}