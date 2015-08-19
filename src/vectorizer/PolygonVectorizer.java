package vectorizer;

import utils.StaticPointArray;
import utils.Utility;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Locale;

public class PolygonVectorizer extends BaseVectorizer {
    private StaticPointArray list;

    private ArrayList<ColoredPolygon> lastSavedPolygonList;

    public void setOriginalImage(BufferedImage image){
        super.setOriginalImage(image);
        list = new StaticPointArray(area);
    }
    private static final int DIR_X[] = new int[]{1,1,0,-1,-1,-1,0,1};
    private static final int DIR_Y[] = new int[]{0,1,1,1,0,-1,-1,-1};


    private void drawFunctionSequentcial(ArrayList<ColoredPolygon> coloredPolygonList){
        if(destImagePanel==null)return;
        Graphics2D g = destImage.createGraphics();
        g.setStroke(new BasicStroke(0.5f));
        for (ColoredPolygon c : coloredPolygonList) {
            g.setColor(new Color(c.color));
            g.fill(c.getPath());
        }
        destImagePanel.setImage(destImage);
    }
    private void drawFunction(ArrayList<ColoredPolygon> coloredPolygonList){
        if(destImagePanel!=null) {
            Graphics2D g = destImage.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0,0,w-1,h-1);
            g.setStroke(new BasicStroke(0.5f));
            for (ColoredPolygon c : coloredPolygonList) {
                g.setColor(new Color(c.color));
                g.fill(c.getPath());
            }
            destImagePanel.setImage(destImage);
        }
    }
    private class Job extends JobThread{
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
        private long startTime,endTime;
        private long workMatrixResetTime,coverSearchTime,perimeterSearchTime,workMatrixTransferTime;

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
                        g.fill(c.getPath());
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
                }
                destImagePanel.setImage(destImage);
            }
        }
        @Override
        public void run() {
            workMatrixResetTime=0;
            coverSearchTime=0;
            perimeterSearchTime=0;
            workMatrixTransferTime=0;

            ArrayList<ColoredPolygon> localList = new ArrayList<>();
            visitMatrix = new char[h*w];
            workMatrix = new char[h*w];

            short x0,y0;
            int pixel;
            boolean flag;
            long timeOfLastUpdate = System.currentTimeMillis();
            drawFunction();
            for(y0=0;y0<h;y0++){
                flag = false;
                for(x0=0;x0<w;x0++){
                    pixel = y0*w+x0;
                    if(canceled)return;
                    if (visitMatrix[pixel] == 0) {
                        flag = true;
                        ColoredPolygon coloredPolygon = findShape(x0,y0);
                        localList.add(coloredPolygon);
                    }
                }
                if(System.currentTimeMillis()-timeOfLastUpdate>16 && flag)
                {
                    timeOfLastUpdate = System.currentTimeMillis();
                    drawFunctionSequentcial(localList);
                    coloredPolygons.addAll(localList);
                    localList.clear();
                }
            }
            if(localList.size()>0) {
                drawFunctionSequentcial(localList);
                coloredPolygons.addAll(localList);
            }
            lastSavedPolygonList = coloredPolygons;

            System.out.format("workMatrixResetTime = %d\n" +
                            "coverSearchTime = %d\n" +
                            "perimeterSearchTime = %d\n" +
                            "workMatrixTransferTime = %d\n" +
                            "-----------------------------------------\n",
                    workMatrixResetTime,
                    coverSearchTime,
                    perimeterSearchTime,
                    workMatrixTransferTime);
        }

        private char getWorkPixel(int x,int y){
            if(x<0 || x>=w || y<0 || y>=h)return 0;
            return workMatrix[y*w+x];
        }

        private boolean isWorkPixelNotVisited(int x,int y) {
            return !(x < 0 || x >= w || y < 0 || y >= h) && workMatrix[y * w + x] == 0;
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
        private ColoredPolygon findShape(short x,short y){
            ColoredPolygon coloredPolygon = new ColoredPolygon();
            Path2D.Float path = new Path2D.Float();
            int startColor = originalImage.getRGB(x,y);
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
            list.push((short) (x0+1),y0);
            list.push(x0, (short) (y0+1));
            list.push(x0, (short) (y0-1));

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
                        rTotal += redOrig(x0,y0);
                        gTotal += greenOrig(x0,y0);
                        bTotal += blueOrig(x0,y0);
                        count++;
                    }
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

            rTotal /= count;
            gTotal /= count;
            bTotal /= count;
            int averageColor = 0xff000000 | (rTotal<<16) | (gTotal<<8) | bTotal;

            int dir,dir2;


            for(y0=minY;y0<=maxY;y0++)
                for(x0=minX;x0<=maxX;x0++)
                    if(getWorkPixel(x0,y0)==2)
                    {
                        x = x0;
                        y = y0;
                        x0 = (short) (maxX+1);
                        y0 = (short) (maxY+1);
                    }

            list.push(x,y);
            workMatrix[y * w + x] = 3;
            boolean done = false;
            dir=0;
            startTime = System.currentTimeMillis();
            do {
                if(canceled)return coloredPolygon;
                x1 = list.getLastX();
                y1 = list.getLastY();
                for(dir2=dir;dir2<8+dir;dir2++){
                    x0 = (short) (x1 + DIR_X[dir2%8]);
                    y0 = (short) (y1 + DIR_Y[dir2%8]);
                    if(x0<0 || x0>=w || y0<0 || y0>=h)continue;
                    if(y0==y && x0==x) {
                        done = true;
                        break;
                    }else if(workMatrix[y0*w+x0]==2){
                        if(isThereAnyEmptySpaces(x0,y0))
                        {
                            workMatrix[y0 * w + x0] = 3;
                            int i = list.size()-2;
                            int j = list.size()-1;
                            if(i>=0 && x0-list.getX(j) == list.getX(i)-list.getX(j) && y0-list.getY(j) == list.getY(i)-list.getY(j)) {
                                list.setXY(j,x0,y0);
                            }else {
                                list.push(x0, y0);
                            }
                            dir = (dir2 + 5) % 8;
                            //dir2 = dir;
                            break;
                        }
                        else
                        {
                            workMatrix[y0 * w + x0] = 1;
                        }
                    }
                }
                if(done)break;
            }while(!done);


            //list pruning
            for(int i=0;i<list.size()-2;i++){
                float xa = list.getX(i);
                float ya = list.getY(i);
                float xb = list.getX(i + 1);
                float yb = list.getY(i + 1);
                float xc = list.getX(i + 2);
                float yc = list.getY(i + 2);

                float dist = Utility.distanceFromPointToLine(xb,yb,xa,ya,xc,yc);
                if(dist<0.001){
                    list.delete(i+1);
                    i--;
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

            coloredPolygon.color = averageColor;


            return coloredPolygon;
        }
    }

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

    public void exportToOutputStream(OutputStream os){
        try{
            ObjectOutput oo = new ObjectOutputStream(os);
            oo.writeObject(lastSavedPolygonList);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void importFromInputStream(InputStream is){
        try{
            ObjectInput oi = new ObjectInputStream(is);
            ArrayList<ColoredPolygon> list = (ArrayList<ColoredPolygon>) oi.readObject();
            lastSavedPolygonList = list;
            drawFunction(lastSavedPolygonList);
        }catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exportToHTML(OutputStream os) {

    }

    @Override
    public void exportToSVG(OutputStream os) {
        BufferedOutputStream bos = new BufferedOutputStream(os);
        Locale.setDefault(Locale.US);
        try {
            Utility.writeTo(String.format("<svg xmlns='http://www.w3.org/2000/svg' version='1.1' width='%d' height='%d'>", w, h), bos);
            /*for(SquareFragment sf : lastSavedSquareList){
                Utility.writeTo(String.format("<rect x='%f' y='%f' width='%f' height='%f' style='fill:#%06X'/>\n",
                        sf.l-0.5,
                        sf.t-0.5,
                        sf.r-sf.l+1.5,
                        sf.d-sf.t+1.5
                        ,sf.color&0xffffff),bos);
            }*/
            for(ColoredPolygon c : lastSavedPolygonList){
                StaticPointArray spa = c.pointArray;
                String points = "";
                for(int i=0;i<spa.size();i++){
                    points+=String.format("%d,%d ",spa.getX(i),spa.getY(i));
                }
                String s = String.format("<polyline points='%s'  style='fill:#%06X'/>\n",
                        points,c.color&0xffffff);
                Utility.writeTo(s,bos);
            }
            Utility.writeTo("</svg>",bos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
