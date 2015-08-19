package mainpackage;

import org.ejml.simple.SimpleMatrix;

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
    ArrayList<Point2D.Float> Qk = new ArrayList<Point2D.Float>(64);
    ArrayList<Point2D.Float> Q = new ArrayList<Point2D.Float>(64);
    float [][] N = new float[64][64];
    float [][] NTranspose = new float[64][64];
    float [][] M = new float[64][64];
    float[] t;
    float[] u;
    int h; // h+1 = number of unknown control points
    int p; //degree
    int n=-1; //n+1= number of original data points
    int m; //m+1 knots

    private static final int UNSELECTED_RED_CIRCLE_RADIUS = 10;
    private static final int UNSELECTED_GREEN_CIRCLE_RADIUS = 8;

    public CurvePanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void generatePArray(){
        while(P.size()<h+1){
            P.add(new Point2D.Float());
        }
        while(Qk.size()<n+1){
            Qk.add(new Point2D.Float());
        }
        while(Q.size()<h+1){
            Q.add(new Point2D.Float());
        }

        P.get(0).setLocation(D.get(0).x, D.get(0).y);
        P.get(h).setLocation(D.get(n).x, D.get(n).y);

        System.out.println("Qk array raw form");
        for(int k=1;k<=n-1;k++){
            float a = N(0,p,t[k]);
            float b = N(h,p,t[k]);
            float x = D.get(k).x-a*D.get(0).x-b*D.get(n).x;
            float y = D.get(k).y-a*D.get(0).y-b*D.get(n).y;
            Qk.get(k).setLocation(x, y);
            System.out.format("%.1f %.1f %.1f %.1f\n", x, y, a, b);
            System.out.format("h = %d p = %d\n",h,p);
        }
        System.out.println("Q array raw form");
        for(int i=1;i<=h-1;i++){
            float x=0,y=0;
            for(int k=1;k<=n-1;k++){
                float n = N(i,p,t[k]);
                x+= n*Qk.get(k).x;
                y+= n*Qk.get(k).y;
                //Q.get(i).setLocation(n*Qk.get(i).x,n*Qk.get(i).y);
            }
            Q.get(i).setLocation(x,y);
            System.out.format("%.1f %.1f\n",x,y);
        }



        //generateMatrixM();

        //matrix solving
        /*SimpleMatrix simpleMatrixM = new SimpleMatrix(h-1,h-1);
        for(int i=1;i<=h-1;i++)
            for(int j=1;j<=h-1;j++){
                simpleMatrixM.set(i - 1, j - 1, M[i][j]);
            }
        */
        SimpleMatrix matrixM = generateMatrixM();

        SimpleMatrix simpleMatrixQ = new SimpleMatrix(h-1,2);
        for(int i=1;i<=h-1;i++){
            simpleMatrixQ.set(i - 1, 0, Q.get(i).x);
            simpleMatrixQ.set(i - 1, 1, Q.get(i).y);
        }

        System.out.println("SimpleMatrixM\n"+matrixM.toString());
        System.out.println("SimpleMatrixQ\n" + simpleMatrixQ.toString());

        SimpleMatrix simpleMatrixP = matrixM.solve(simpleMatrixQ);
        System.out.println("SimpleMatrixP\n" + simpleMatrixP.toString());
        for(int i=1;i<=h-1;i++){
            P.get(i).setLocation(simpleMatrixP.get(i-1,0),simpleMatrixP.get(i-1,1));
        }
        while(P.size() > h+1)P.remove(P.size()-1);
    }



    public SimpleMatrix generateMatrixM(){
        SimpleMatrix matrixN = new SimpleMatrix(n-1,h-1);

        for(int j=1;j<=n-1;j++){
            for(int i=1;i<=h-1;i++){
                matrixN.set(j-1,i-1,N(i,p,t[j]));
                //N[j][i]=N(i,p,t[j]);
                //NTranspose[i][j] = N[j][i];
            }
        }
        SimpleMatrix matrixNtransposed = matrixN.transpose();

        SimpleMatrix matrixM = matrixNtransposed.mult(matrixN);
        return matrixM;
        /*
        for(int i=1;i<=h-1;i++)
            for(int j=1;j<=h-1;j++){
                M[i][j]=0;
                for(int k=1;k<n-1;k++){
                    M[i][j] += NTranspose[i][k] * N[k][j];
                }
            }*/
    }

    private void generateTArray(){
        t = new float[n+1];
        for(int i=0;i<=n;i++){
            t[i] = 1.f*i/n;
        }
    }
    private void generateUArray(){
        u = new float[m+1];
        /*for(int i=0;i<=p;i++)
            u[i] = 0;

        for(int j=1;j<=n-p;j++)
            u[j+p] = j/(n-p+1.f);

        for(int i=m-p;i<=m;i++){
            u[i]=1;
        }*/
        for(int i=0;i<=m;i++){
            u[i] = 1.f*i/m;
        }
    }

    private float N(int i,int j,float uval){
        if(j==0){
            if(u[i] <= uval && uval <= u[i+1]){
                return 1;
            }else{
                return 0;
            }
        }else {
            float a = 0;
            float b = 0;
            try {
                a = ((uval - u[i]) / (u[i + j] - u[i])) * N(i, j - 1, uval);
                b = ((u[i + j + 1] - uval) / (u[i + j + 1] - u[i + 1])) * N(i + 1, j - 1, uval);
                if(a!=a || b!=b){
                    System.out.println("LoL");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

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
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.RED);
        for(Point2D.Float point : D){
            g.fillOval((int)point.x-UNSELECTED_RED_CIRCLE_RADIUS/2,
                    (int)point.y-UNSELECTED_RED_CIRCLE_RADIUS/2,
                    UNSELECTED_RED_CIRCLE_RADIUS,
                    UNSELECTED_RED_CIRCLE_RADIUS);
        }
        g.setColor(Color.GREEN);
        for(Point2D.Float point : P){
            g.fillOval((int)point.x-UNSELECTED_GREEN_CIRCLE_RADIUS/2,
                    (int)point.y-UNSELECTED_GREEN_CIRCLE_RADIUS/2,
                    UNSELECTED_GREEN_CIRCLE_RADIUS,
                    UNSELECTED_GREEN_CIRCLE_RADIUS);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        D.add(new Point2D.Float(x,y));
        n = D.size()-1;
        h = n-1;
        p = h-1;
        //m = n+p+1;
        m = h+p+1;
        if(p>=1){
            generateTArray();
            generateUArray();
            try {
                generatePArray();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        repaint();
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
