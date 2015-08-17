package mainpackage;

import utils.StaticPointArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Created by Zero on 17.08.2015.
 */
public class CurvePanel extends JPanel implements MouseMotionListener,MouseListener{
    ArrayList<Point2D.Float> D = new ArrayList<Point2D.Float>(64);
    ArrayList<Point2D.Float> P = new ArrayList<Point2D.Float>(64);
    ArrayList<Point2D.Float> Q = new ArrayList<Point2D.Float>(64);
    float [][] N = new float[64][64];
    float [][] NTranspose = new float[64][64];
    float [][] M = new float[64][64];
    float[] t = new float[64];
    int h; // h+1 = number of unknown control points
    int p; //degree
    int n;

    public void generatePArray(){
        while(P.size()<h+1){
            P.add(new Point2D.Float());
        }
        while(Q.size()<n+1){
            Q.add(new Point2D.Float());
        }
        P.get(0).setLocation(D.get(0).x,D.get(0).y);
        P.get(h).setLocation(D.get(n).x,D.get(n).y);

        for(int k=1;k<=n-1;k++){
            float a = N(0,p,t[k]);
            float b = N(h,p,t[k]);
            float x = D.get(k).x-a*D.get(0).x-b*D.get(n).x;
            float y = D.get(k).y-a*D.get(0).y-b*D.get(n).y;
            Q.get(k).setLocation(x,y);
        }
        generateNArray();


    }



    public void generateNArray(){
        for(int j=1;j<=n-1;j++){
            for(int i=1;i<=h-1;i++){
                N[j][i]=N(i,p,t[j]);
                NTranspose[i][j] = N[j][i];
            }
        }
        for(int i=1;i<=h-1;i++)
            for(int j=1;j<=h-1;j++){
                M[i][j]=0;
                for(int k=1;k<n-1;k++){
                    M[i][j] += NTranspose[i][k] * N[k][j];
                }
            }
    }

    private void generateTArray(){
        for(int i=0;i<=n;i++){
            t[i] = 1.f*i/n;
        }
    }
    private float N(int i,int j,float u){
        if(j==0){
            if(t[i] <= u && u <= t[i+1] && t[i] < t[i+1]){
                return 1;
            }else{
                return 0;
            }
        }else {
            float a = ((u - t[i]) / (t[i + j] - t[i])) * N(i, j - 1, u);
            float b = ((t[i + j + 1] - u) / (t[i + j + 1] - t[i + 1])) * N(i + 1, j - 1, u);
            return a + b;
        }
    }

    private Point2D.Float C(float u){
        float x=0,y=0;
        float a = N(0,p,u);
        x += a*D.get(0).x;
        y += a*D.get(0).y;

        for(int i=1;i<=h-1;i++){
            float b = N(i,p,u);
            x += b*P.get(i).x;
            y += b*P.get(i).y;
        }

        float c = N(h,p,u);
        x += c*D.get(h).x;
        y += c*D.get(h).y;

        return new Point2D.Float(x,y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0x6495ED));
        g.fillRect(0,0,getWidth(),getHeight());

    }

    @Override
    public void mouseClicked(MouseEvent e) {

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

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
