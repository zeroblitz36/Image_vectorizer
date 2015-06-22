package vectorizer;

import utils.ColoredPolygon;
import utils.ImagePanel;
import utils.Utility;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by GeorgeRoscaneanu on 28.04.2015.
 */
public class PolygonVectorizer {
    private BufferedImage originalImage;
    private BufferedImage destImage;
    private char[] originalRedArray;
    private char[] originalGreenArray;
    private char[] originalBlueArray;
    private int[] originalColorArray;
    private int w,h,area;
    public int threshold;
    private ImagePanel destImagePanel;

    public PolygonVectorizer(BufferedImage image){
        originalImage = image;
        w = originalImage.getWidth();
        h = originalImage.getHeight();
        area = w*h;
        destImage = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);
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
    private static final int DIR_X[] = new int[]{1,1,0,-1,-1,-1,0,1};
    private static final int DIR_Y[] = new int[]{0,1,1,1,0,-1,-1,-1};
    private Job lastJob;
    private class Job extends Thread{
        private boolean canceled = false;
        /*
        0 = unvisited
        1 = visited , not-wall
        2 = visited , wall
        3 = visited , wall , added to the polygon list
         */
        private char visitMatrix[];
        private char workMatrix[];
        boolean notDebug = true;
        private ArrayList<ColoredPolygon> coloredPolygons = new ArrayList<>(1000);
        private void drawFunction(){
            int x0,y0;
            if(destImagePanel!=null) {
                Graphics2D g = destImage.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0,0,w-1,h-1);

                if(notDebug) {
                    g.setStroke(new BasicStroke(0.5f));
                    for (ColoredPolygon c : coloredPolygons) {
                        if (canceled) return;
                        g.setColor(new Color(c.color));
                        g.fill(c.path);


                        //g.draw(c.path);
                    }
                }else {
                    for (int pixel = 0; pixel < h * w; pixel++) {
                        y0 = pixel / w;
                        x0 = pixel % w;
                        if (canceled) return;
                        switch (visitMatrix[pixel]) {
                            case 0:
                                g.setColor(Color.black);
                                break;
                            case 1:
                                g.setColor(Color.BLUE);
                                break;
                            case 2:
                                g.setColor(Color.RED);
                                break;
                            case 3:
                                g.setColor(Color.GREEN);
                                break;
                            default:
                                g.setColor(Color.PINK);
                        }
                        g.drawLine(x0, y0, x0, y0);
                    }
                    //Arrays.fill(visitMatrix,(char)0);
                }
                destImagePanel.setImage(destImage);
            }
        }

        @Override
        public void run() {

            visitMatrix = new char[h*w];
            workMatrix = new char[h*w];
            int x0,y0;
            int counter=0;
            for(int pixel=0;pixel<h*w;pixel++){
                y0 = pixel/w;
                x0 = pixel%w;
                if(canceled)return;
                if (visitMatrix[pixel] == 0) {
                    ColoredPolygon coloredPolygon = findShape(x0,y0);
                    coloredPolygons.add(coloredPolygon);
                    counter++;

                    if(counter%2500==0) {
                        drawFunction();
                        try {
                            Thread.sleep(15);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //if(!notDebug)
                    //    break;
                }
            }
            if(canceled)return;
            drawFunction();
        }
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        private char getWorkPixel(int x,int y){
            if(x<0 || x>=w || y<0 || y>=h)return 0;
            return workMatrix[y*w+x];
        }

        private boolean isThereAnyEmptySpaces(int x0,int y0){
            return getWorkPixel(x0+1,y0)==0||
                    getWorkPixel(x0+1,y0+1)==0||
                    getWorkPixel(x0,y0+1)==0||
                    getWorkPixel(x0-1,y0+1)==0||
                    getWorkPixel(x0-1,y0)==0||
                    getWorkPixel(x0-1,y0-1)==0||
                    getWorkPixel(x0,y0-1)==0||
                    getWorkPixel(x0+1,y0-1)==0;
        }
        private ColoredPolygon findShape(int x,int y){
            ColoredPolygon coloredPolygon = new ColoredPolygon();
            Path2D.Float path = new Path2D.Float();
            LinkedList<Point> list = new LinkedList<>();
            int startColor = originalImage.getRGB(x,y);
            int rTotal=0,gTotal=0,bTotal=0;
            int count=0;
            int currentColor;

            Arrays.fill(workMatrix,(char)0);
            workMatrix[y*w+x] = 2;
            int x0=x,y0=y;

            list.add(new Point(x0-1,y0));
            list.add(new Point(x0+1,y0));
            list.add(new Point(x0,y0-1));
            list.add(new Point(x0,y0+1));

            Point point;
            rTotal += redOrig(x, y);
            gTotal += greenOrig(x, y);
            bTotal += blueOrig(x,y);
            count++;

            int minX=x,maxX=x,minY=y,maxY=y;
            while(!list.isEmpty()){
                if(canceled)return coloredPolygon;
                point = list.remove();
                x0 = point.x;
                y0 = point.y;
                int index = y0*w+x0;
                if(x0<0 || x0>=w || y0<0 || y0>=h)continue;
                if(workMatrix[index]!=0)continue;
                currentColor = originalImage.getRGB(x0,y0);

                if(minX>x0)minX = x0;
                if(minY>y0)minY = y0;
                if(maxX<x0)maxX = x0;
                if(maxY<y0)maxY = y0;

                if(visitMatrix[index]!=0
                        || Utility.manhattanDistance(startColor, currentColor)>threshold){
                    workMatrix[index]=2;
                }else{
                    if(x0==0 || x0==w-1 || y0==0 || y0==h-1) {
                        workMatrix[index]=2;
                    }else {
                        workMatrix[index]=1;
                    }
                    rTotal += redOrig(x0,y0);
                    gTotal += greenOrig(x0,y0);
                    bTotal += blueOrig(x0,y0);
                    count++;
                    list.add(new Point(x0-1,y0));
                    list.add(new Point(x0+1,y0));
                    list.add(new Point(x0,y0-1));
                    list.add(new Point(x0,y0+1));
                }
            }

            rTotal /= count;
            gTotal /= count;
            bTotal /= count;
            int averageColor = 0xff000000 | (rTotal<<16) | (gTotal<<8) | bTotal;

            for(y0=minY;y0<=maxY;y0++)
                for(x0=minX;x0<=maxX;x0++)
                {
                    if(getWorkPixel(x0,y0)==2 && !isThereAnyEmptySpaces(x0,y0))
                        workMatrix[y0*w+x0]=1;
                }
            int dir,dir2;

            for(y0=minY;y0<=maxY;y0++)
                for(x0=minX;x0<=maxX;x0++)
                    if(getWorkPixel(x0,y0)==2)
                    {
                        x = x0;
                        y = y0;
                        x0 = maxX+1;
                        y0 = maxY+1;
                    }
            list.add(new Point(x,y));
            workMatrix[y * w + x] = 3;
            boolean done = false;
            dir=0;
            do {
                if(canceled)return coloredPolygon;
                point = list.getLast();
                for(dir2=dir;dir2<8+dir;dir2++){
                    x0 = point.x + DIR_X[dir2%8];
                    y0 = point.y + DIR_Y[dir2%8];
                    if(x0<0 || x0>=w || y0<0 || y0>=h)continue;
                    if(y0==y && x0==x) {
                        done = true;
                        break;
                    }else if(workMatrix[y0*w+x0]==2){
                        if(isThereAnyEmptySpaces(x0,y0))
                        {
                            workMatrix[y0 * w + x0] = 3;
                            list.add(new Point(x0, y0));
                            dir = (dir2 + 5) % 8;
                            //dir = 0;
                            dir2 = dir;
                            break;
                        }
                        else
                        {
                            workMatrix[y0 * w + x0] = 1;
                        }
                    }
                }
                if(done)break;
                if(dir2==8+dir){
                    if(list.size() == 1){
                        done = true;
                    }else {
                        point = list.removeLast();
                        x0 = point.x;
                        y0 = point.y;
                        workMatrix[y0 * w + x0] = 1;
                    }
                }
            }while(!done);

            if(notDebug) {
                int i = 0;

                for (Point p : list) {
                    if (canceled) return coloredPolygon;
                    if (i == 0) {
                        path.moveTo(p.x, p.y);
                    } else {
                        path.lineTo(p.x, p.y);
                    }
                    i++;
                }
                point = list.getFirst();
                path.lineTo(point.x, point.y);
            }

            int index;
            for(y0=minY;y0<=maxY;y0++)
                for(x0=minX;x0<=maxX;x0++) {
                    if(canceled)return coloredPolygon;
                    index = y0 * w + x0;
                    if(workMatrix[index]==1 || (workMatrix[index]==3 && (x0==0||x0==w-1||y0==0||y==h-1))){
                        visitMatrix[index] = workMatrix[index];
                    }
                }
            coloredPolygon.path = path;
            coloredPolygon.color = averageColor;
            return coloredPolygon;
        }
    }


    public void setDestImagePanel(ImagePanel p){
        destImagePanel = p;
    }

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


    public void startJob(){
        if(lastJob!=null){
            lastJob.setCanceled(true);
        }
        lastJob = new Job();
        lastJob.start();
    }

}
