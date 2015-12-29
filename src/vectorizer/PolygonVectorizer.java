package vectorizer;

import utils.StaticPointArray;
import utils.Utility;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class PolygonVectorizer extends BaseVectorizer {
    private StaticPointArray list;
    private StaticPointArray workList;

    public void setOriginalImage(BufferedImage image){
        super.setOriginalImage(image);
        list = new StaticPointArray(area);
        workList = new StaticPointArray(area);
    }
    private static final int DIR_X[] = new int[]{1,1,0,-1,-1,-1,0,1};
    private static final int DIR_Y[] = new int[]{0,1,1,1,0,-1,-1,-1};
    /*
            0 = unvisited
            1 = visited , not-wall
            2 = visited , wall
            3 = visited , wall , added to the polygon list
             */
    private char visitMatrix[];
    private char workMatrix[];
    private LinkedList<ColoredPolygon> coloredPolygons = new LinkedList<>();

    public void startJob(){
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

    private void drawFunctionSequentcial(AbstractList<ColoredPolygon> coloredPolygonList){
        if(destImagePanel!=null || isInBenchmark) {
            Graphics2D g = destImage.createGraphics();
            g.setStroke(new BasicStroke(0.5f));
            for (ColoredPolygon c : coloredPolygonList) {
                g.setColor(new Color(c.color));
                g.fill(c.getPath());
            }
            if(!isInBenchmark) {
                destImagePanel.setImage(destImage);
            }
        }
    }
    private void drawFunction(AbstractList<ColoredPolygon> coloredPolygonList){
        if(destImagePanel!=null || isInBenchmark) {
            Graphics2D g = destImage.createGraphics();
            for (ColoredPolygon c : coloredPolygonList) {
                g.setColor(new Color(c.color));
                g.fill(c.getPath());
            }
            if(!isInBenchmark) {
                destImagePanel.setImage(destImage);
            }
        }
    }
    private class Job extends JobThread{

        private long startTime,endTime;
        private long workMatrixResetTime,coverSearchTime,perimeterSearchTime,workMatrixTransferTime,totalTime;
        private long exportSvgTime;
        private LinkedList<ColoredPolygon> localList = new LinkedList<>();
        @Override
        public void run() {
            workMatrixResetTime=0;
            coverSearchTime=0;
            perimeterSearchTime=0;
            workMatrixTransferTime=0;
            totalTime = System.currentTimeMillis();

            if(visitMatrix==null || visitMatrix.length < h*w) {
                visitMatrix = new char[h*w];
            }
            if(workMatrix==null || workMatrix.length < h*w){
                workMatrix = new char[h*w];
            }

            Arrays.fill(visitMatrix,0,h*w,(char)0);
            Arrays.fill(workMatrix,0,h*w,(char)0);

            short x0,y0;
            int pixel;

            for(y0=0;y0<h;y0++){
                for(x0=0;x0<w;x0++){
                    pixel = y0*w+x0;
                    if(canceled)return;
                    if (visitMatrix[pixel] == 0) {
                        ColoredPolygon coloredPolygon = findShape(x0,y0);
                        localList.add(coloredPolygon);
                    }
                }
                int k = aproxCompletedPixelCount.addAndGet(w);
                updateDetails(String.format("Progress : %.1f%%", 100.f * k / area));
            }

            if(canceled)return;
            coloredPolygons = localList;

            if(canceled)return;
            Thread th = new Thread(() -> {
                exportSvgTime = System.currentTimeMillis();
                constructStringSVG();
                exportSvgTime = System.currentTimeMillis()-exportSvgTime;
                updateDetails(String.format("SVG:%s SVGZ:%s",
                        Utility.aproximateDataSize(svgStringBuilder.length()),
                        Utility.aproximateDataSize(svgzStringBuilder.length())));
            });
            th.start();

            drawFunction(coloredPolygons);
            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setIsDone(true);
            totalTime = System.currentTimeMillis() - totalTime;
            /*System.out.format("workMatrixResetTime = %d\n" +
                            "coverSearchTime = %d\n" +
                            "perimeterSearchTime = %d\n" +
                            "workMatrixTransferTime = %d\n" +
                            "exportSvgTime = %d\n" +
                            "totalTime = %d\n"+
                            "-----------------------------------------\n",
                    workMatrixResetTime,
                    coverSearchTime,
                    perimeterSearchTime,
                    workMatrixTransferTime,
                    exportSvgTime,
                    totalTime);*/
        }

        private char getWorkPixel(int x,int y){
            if(x<0 || x>=w || y<0 || y>=h)return 0;
            return workMatrix[y*w+x];
        }

        private boolean isWorkPixelNotVisited(int x,int y) {
            return !(x < 0 || x >= w || y < 0 || y >= h) && workMatrix[y * w + x] == 0;
        }

        private boolean isThereAnyEmptySpaces(int x0,int y0){
            return  getWorkPixel(x0-1,  y0-1)   ==0  ||
                    getWorkPixel(x0,    y0-1)   ==0  ||
                    getWorkPixel(x0+1,  y0-1)   ==0  ||
                    getWorkPixel(x0-1,  y0)     ==0  ||
                    getWorkPixel(x0+1,  y0)     ==0  ||
                    getWorkPixel(x0-1,  y0+1)   ==0  ||
                    getWorkPixel(x0,    y0+1)   ==0  ||
                    getWorkPixel(x0+1,  y0+1)   ==0  ;
        }
        private ColoredPolygon findShape(short x,short y){
            ColoredPolygon coloredPolygon = new ColoredPolygon();
            int startColor = colorOrig(x,y);
            int rTotal=0,gTotal=0,bTotal=0;
            int count=0;
            int currentColor;

            startTime = System.currentTimeMillis();
            //Arrays.fill(workMatrix,(char)0);
            endTime = System.currentTimeMillis();
            workMatrixResetTime += endTime-startTime;

            workMatrix[y*w+x] = 2;
            short x0=x,y0=y,x1,y1;

            list.clearAll();
            list.push((short) (x0 - 1), y0);
            list.push(x0, (short) (y0-1));
            list.push(x0, (short) (y0+1));
            list.push((short) (x0+1),y0);


            //Point point;
            rTotal += redOrig(x, y);
            gTotal += greenOrig(x, y);
            bTotal += blueOrig(x, y);
            count++;

            startTime = System.currentTimeMillis();
            short minX=x,maxX=x,minY=y,maxY=y;
            while(!list.isEmpty()){
                if(canceled)return coloredPolygon;
                x0 = list.getLastX();
                y0 = list.getLastY();
                list.deleteLast();

                int index = y0*w+x0;
                if(x0<0 || x0>=w || y0<0 || y0>=h)continue;
                if(workMatrix[index]!=0)continue;
                currentColor = colorOrig(x0,y0);

                if(minX>x0)minX = x0;
                if(minY>y0)minY = y0;
                if(maxX<x0)maxX = x0;
                if(maxY<y0)maxY = y0;

                if(x0==0 || x0==w-1 || y0==0 || y0==h-1 ||
                        visitMatrix[index]!=0 ||
                        Utility.manhattanDistance(startColor, currentColor)>threshold)
                {
                    workMatrix[index]=2;
                }
                else
                {
                    workMatrix[index]=1;
                    rTotal += Utility.red(currentColor);
                    gTotal += Utility.green(currentColor);
                    bTotal += Utility.blue(currentColor);
                    count++;

                    if(isWorkPixelNotVisited(x0,y0+1))
                        list.push(x0, (short) (y0+1));
                    if(isWorkPixelNotVisited(x0+1,y0))
                        list.push((short) (x0+1),y0);
                    if(isWorkPixelNotVisited(x0-1,y0))
                        list.push((short) (x0-1),y0);
                    if(isWorkPixelNotVisited(x0,y0-1))
                        list.push(x0, (short) (y0-1));
                }
            }
            endTime = System.currentTimeMillis();
            coverSearchTime += endTime-startTime;

            y = minY;
            x = minX;
            while(getWorkPixel(x,y)!=2)x++;

            if(canceled)return coloredPolygon;

            list.push(x,y);
            workMatrix[y * w + x] = 3;
            boolean done = false;
            int dir=0,dir2;
            startTime = System.currentTimeMillis();
            while(!done) {
                if (canceled) return coloredPolygon;
                x1 = list.getLastX();
                y1 = list.getLastY();
                for (dir2 = dir; dir2 < 8 + dir; dir2++) {
                    x0 = (short) (x1 + DIR_X[dir2 % 8]);
                    y0 = (short) (y1 + DIR_Y[dir2 % 8]);
                    if (x0 < 0 || x0 >= w || y0 < 0 || y0 >= h) continue;
                    if (y0 == y && x0 == x) {
                        done = true;
                        break;
                    } else if (workMatrix[y0 * w + x0] == 2) {
                        if (isThereAnyEmptySpaces(x0, y0)) {
                            workMatrix[y0 * w + x0] = 3;
                            list.push(x0, y0);
                            dir = (dir2 + 5) % 8;
                            break;
                        } else {
                            workMatrix[y0 * w + x0] = 1;
                            rTotal += redOrig(x0, y0);
                            gTotal += greenOrig(x0, y0);
                            bTotal += blueOrig(x0, y0);
                            count++;
                        }
                    }
                }
            }
            rTotal/=count;
            gTotal/=count;
            bTotal/=count;
            coloredPolygon.color = 0xff000000 | (rTotal<<16) | (gTotal<<8) | bTotal;

            float comp = 0.1f;
            boolean flag = true;
            //list pruning


            while(flag) {
                flag = false;
                workList.clearAll();
                workList.push(list.getX(0),list.getY(1));
                for (int i = 0; i < list.size() - 2; i++) {
                    short xa = list.getX(i);
                    short ya = list.getY(i);
                    short xb = list.getX(i + 1);
                    short yb = list.getY(i + 1);
                    short xc = list.getX(i + 2);
                    short yc = list.getY(i + 2);

                    float dist = Utility.distanceFromPointToLine(xb, yb, xa, ya, xc, yc);
                    if (dist < comp) {
                        //list.delete(i + 1);
                        //i=i>1?i-2:i-1;
                        flag = true;
                    }else{
                        workList.push(xb,yb);
                    }
                }
                workList.push(list.getLastX(),list.getLastY());
                if(flag){
                    list.copyFrom(workList);
                }
            }

            coloredPolygon.pointArray = list.cloneUpTo(list.size());

            perimeterSearchTime += endTime-startTime;

            startTime = System.currentTimeMillis();
            int index;
            for(y0=minY;y0<=maxY;y0++)
                for(x0=minX;x0<=maxX;x0++) {
                    if(canceled)return coloredPolygon;
                    index = y0 * w + x0;
                    if(workMatrix[index]==1 || (workMatrix[index]==3 && (x0==0||x0==w-1||y0==0||y==h-1))){
                        visitMatrix[index] = workMatrix[index];
                    }
                    workMatrix[index]=0;
                }
            endTime = System.currentTimeMillis();
            workMatrixTransferTime += endTime-startTime;

            return coloredPolygon;
        }
    }



    public void constructStringSVG(){
        svgStringBuilder.setLength(0);
        svgStringBuilder.append(String.format("<svg xmlns='http://www.w3.org/2000/svg' version='1.1' width='%d' height='%d'>", w, h));
        for(ColoredPolygon c : coloredPolygons){
            StaticPointArray spa = c.pointArray;
            svgStringBuilder.append("<path d='M");
            int n = spa.size()-1;
            for(int i=0;i<n;i++){
                svgStringBuilder.append(spa.getX(i));
                svgStringBuilder.append(',');
                svgStringBuilder.append(spa.getY(i));
                svgStringBuilder.append('L');
            }
            svgStringBuilder.append(spa.getX(n));
            svgStringBuilder.append(',');
            svgStringBuilder.append(spa.getY(n));
            svgStringBuilder.append(String.format("Z' fill='#%06X'/>\n",c.color&0xffffff));
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
