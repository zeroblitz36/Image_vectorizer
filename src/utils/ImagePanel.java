package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by GeorgeRoscaneanu on 15.04.2015.
 */
public class ImagePanel extends JPanel implements MouseListener{
    private BufferedImage image;
    private boolean detectContinousColor = false;
    private int clickX,clickY,color;
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private int threshold = 128;
    @Override
    protected void paintComponent(Graphics g) {
        //super.paintComponent(g);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }

    public void setImage(BufferedImage img){
        image = img;
        repaint();
    }

    public void setDetectContinousColor(boolean b){
        if(detectContinousColor == b)return;
        detectContinousColor = b;
        if(detectContinousColor){
            addMouseListener(this);
        }else{
            removeMouseListener(this);
        }
    }

    public void setThreshold(int th){
        threshold = th;
    }


    @Override
    public boolean isOpaque() {
        return true;
    }



    @Override
    public void mouseClicked(MouseEvent e) {
        clickX = e.getX();
        clickY = e.getY();
        if(image != null){
            clickX = (int) (1.f * clickX / getWidth() * image.getWidth(null));
            clickY = (int) (1.f * clickY / getHeight() * image.getHeight(null));
            color = image.getRGB(clickX,clickY);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private static final int DIR_X[] = new int[]{1,1,0,-1,-1,-1,0,1};
    private static final int DIR_Y[] = new int[]{0,-1,-1,-1,0,1,1,1};
    private class BoundFinder implements Runnable{
        private int startX,startY;
        private int checkColor;
        private boolean isCanceled = false;
        private int visited[][] = new int[image.getHeight()][image.getWidth()];
        private LinkedList<Point> pointList = new LinkedList<>();
        public BoundFinder(int x,int y,int color){
            startX = x;
            startY = y;
            this.checkColor = color;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(isCanceled)return;

            pointList.add(new Point(startX,startY));
            Point point;
            int color;
            int x,y;
            int w = image.getWidth();
            int h = image.getHeight();
            int minX= 999999;
            int maxX=-999999;
            int minY= 999999;
            int maxY=-999999;
            while(!pointList.isEmpty()){
                point = pointList.remove();
                x = point.x;
                y = point.y;
                if(x<0 || x>=w || y<0 || y>=h)continue;
                if(visited[y][x]>0)continue;
                color = image.getRGB(x,y);
                if(x==0 || x==w-1 || y==0 || y==h-1 || Utility.manhattanDistance(color, checkColor)>threshold){
                    visited[y][x]=2;
                    if(x < minX)minX = x;
                    if(x > maxX)maxX = x;
                    if(y < minY)minY = y;
                    if(y > maxY)maxY = y;
                }else{
                    visited[y][x]=1;
                    pointList.add(new Point(x-1,y-1));
                    pointList.add(new Point(x-1,y+1));
                    pointList.add(new Point(x+1,y-1));
                    pointList.add(new Point(x+1,y+1));
                }
            }

            int dir = 0;
            int dir2 = 0;
            int x2,y2;
            for(int i = minY;i <= maxY;i++){
                for(int j = minX;j <= maxX;j++){
                    if(visited[i][j]==2){
                        x = j;
                        y = i;
                        visited[y][x]=2;

                        LinkedList<Point> points = new LinkedList<>();
                        points.add(new Point(x,y));
                        do{
                            dir2 = dir;
                            do{
                                x2 = x + DIR_X[dir2%8];
                                y2 = y + DIR_Y[dir2%8];
                                if(x2<0 || x2>=w || y2<0 || y2>=h){
                                    dir2++;
                                }else if(visited[y2][x2]==2){
                                    break;
                                }
                            }while(true);

                            if(dir2 >= dir+8)throw new RuntimeException("You fucked up here");

                            point = new Point(x2,y2);
                            points.add(point);
                            x = x2;
                            y = y2;
                            dir = dir2;
                        }while(point.equals(points.getFirst()));
                    }
                }
            }
        }
    }

}
