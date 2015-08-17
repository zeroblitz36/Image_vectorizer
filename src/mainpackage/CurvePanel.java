package mainpackage;

import utils.StaticPointArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by Zero on 17.08.2015.
 */
public class CurvePanel extends JPanel implements MouseMotionListener,MouseListener{
    StaticPointArray D = new StaticPointArray(64);
    StaticPointArray P = new StaticPointArray(64);
    float[] t = new float[64];
    int h; // h+1 = number of unknown control points

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
