package vectorizer;

import utils.Utility;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * Created by Zero on 27.10.2015.
 */
public class BenchmarkTool {
    private boolean isCanceled = false;
    private int originalColors[];
    private int destinationColors[];
    private int w,h;
    public void test(BaseVectorizer vectorizer){
        int svgSize=-1,svgzSize=-1;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        float perc;
        String results = "Threshold SvgSize SvgzSize Percentage\n";

        originalColors = new int[vectorizer.area];
        destinationColors = new int[vectorizer.area];
        w = vectorizer.w;
        h = vectorizer.h;
        vectorizer.originalImage.getRGB(0,0,w,h,originalColors,0,w);
        for(int i=0;i<=512 && !isCanceled();i++){
            vectorizer.threshold = i;
            vectorizer.isInBenchmark = true;
            vectorizer.startJob();
            while(!vectorizer.isDone()){
                Thread.yield();
            }
            vectorizer.destImage.getRGB(0,0,w,h,destinationColors,0,w);
            /*baos.reset();
            vectorizer.exportToSVG(baos,false);
            svgSize = baos.size();
            baos.reset();
            vectorizer.exportToSVG(baos,true);
            svgzSize = baos.size();*/

            perc = calculateComparison(vectorizer) * 100;

            String s = String.format("%10d%10d%10d\t%10.3f\n",i,svgSize,svgzSize,perc);
            System.out.print(s);
            results += s;
            System.out.format("Progress... %d out of 512\n",i);
        }

        System.out.println(results);
    }

    private float calculateComparison(BaseVectorizer vectorizer){
        int area = w*h;
        int total=0;
        for(int i=0;i<area;i++) {
            total += Utility.manhattanDistance(originalColors[i], destinationColors[i]);
        }
        float percentage = 1 - 1.f * total / (area*255*3);
        return percentage;
    }

    public synchronized boolean isCanceled() {
        return isCanceled;
    }

    public synchronized void setIsCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }
}
