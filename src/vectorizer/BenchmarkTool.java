package vectorizer;

import utils.Utility;

import java.util.Locale;

/**
 * Created by Zero on 27.10.2015.
 */
public class BenchmarkTool {
    private boolean isCanceled = false;
    private int originalColors[];
    private int destinationColors[];
    private int w,h;
    public void test(BaseVectorizer vectorizer){
        int svgSize,svgzSize;
        float perc;
        float time;
        Locale.setDefault(Locale.US);
        String results = "Threshold,Time,SvgSize,SvgzSize,Percentage\n";
        long start,end;
        originalColors = new int[vectorizer.area];
        destinationColors = new int[vectorizer.area];
        w = vectorizer.w;
        h = vectorizer.h;
        vectorizer.originalImage.getRGB(0,0,w,h,originalColors,0,w);
        for(int i=0;i<=512 && !isCanceled();i++){
            vectorizer.threshold = i;
            vectorizer.isInBenchmark = true;
            start = System.currentTimeMillis();
            vectorizer.startJob();
            while(!vectorizer.isDone()){
                Thread.yield();
            }
            end = System.currentTimeMillis();
            time = (end-start)/1000.f;
            System.out.printf("Vectorization :%.3fs\n",time);

            start = System.currentTimeMillis();
            vectorizer.destImage.getRGB(0,0,w,h,destinationColors,0,w);
            end = System.currentTimeMillis();
            System.out.printf("Conversion :%.3fs\n",(end-start)/1000.f);

            start = System.currentTimeMillis();
            svgSize = vectorizer.getSvgSize();
            svgzSize = vectorizer.getSvgzSize();
            end = System.currentTimeMillis();
            System.out.printf("FileSize :%.3fs\n",(end-start)/1000.f);

            start = System.currentTimeMillis();
            perc = calculateComparison() * 100;
            end = System.currentTimeMillis();
            System.out.printf("Comparison :%.3fs\n",(end-start)/1000.f);

            String s = String.format("%d,%.3f,%d,%d,%.3f\n",i,time,svgSize,svgzSize,perc);
            System.out.print(s);
            results += s;
            System.out.format("Progress... %d out of 512\n",i);
        }

        System.out.println(results);
    }

    private float calculateComparison(){
        int area = w*h;
        int total=0;
        for(int i=0;i<area;i++) {
            total += Utility.manhattanDistance(originalColors[i], destinationColors[i]);
        }
        return 1 - 1.f * total / (area*255*3);
    }

    public synchronized boolean isCanceled() {
        return isCanceled;
    }

    public synchronized void setIsCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }
}
