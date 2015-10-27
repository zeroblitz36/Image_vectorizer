package vectorizer;

import utils.Utility;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * Created by Zero on 27.10.2015.
 */
public class BenchmarkTool {
    private boolean isCanceled = false;
    public void test(BaseVectorizer vectorizer){
        int svgSize,svgzSize;
        String svg,svgz;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        float perc;
        String results = "Threshold SvgSize SvgzSize Percentage\n";

        for(int i=0;i<=512 && !isCanceled();i++){
            vectorizer.threshold = i;
            vectorizer.startJob();
            while(vectorizer.isAJobRunning()){
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            baos.reset();
            vectorizer.exportToSVG(baos,false);
            svg = baos.toString();
            svgSize = svg.length();
            baos.reset();
            vectorizer.exportToSVG(baos,true);
            svgz = baos.toString();
            svgzSize = svgz.length();

            perc = calculateComparison(vectorizer) * 100;

            results += String.format("%d\t%d\t%d\t%.3f\n",i,svgSize,svgzSize,perc);
            System.out.format("Progress... %d out of 512\n",i);
        }

        System.out.println(results);
    }

    private float calculateComparison(BaseVectorizer vectorizer){
        BufferedImage orig = vectorizer.originalImage;
        BufferedImage dest = vectorizer.destImage;
        int width = orig.getWidth();
        int height = orig.getHeight();
        int a,b;
        int total=0;
        for(int i=0;i<height;i++)
            for(int j=0;j<width;j++){
                a = orig.getRGB(j,i);
                b = dest.getRGB(j,i);
                total += Utility.manhattanDistance(a,b);
            }
        float percentage = 1 - 1.f * total / (width*height*255*3);
        return percentage;
    }

    public synchronized boolean isCanceled() {
        return isCanceled;
    }

    public synchronized void setIsCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }
}
