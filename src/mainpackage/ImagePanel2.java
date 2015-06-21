package mainpackage;

import javax.swing.*;
import java.awt.*;

/**
 * Created by GeorgeRoscaneanu on 15.04.2015.
 */
public class ImagePanel2 extends JPanel{
    private Image image;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }

    public void setImage(Image img){
        image = img;
        repaint();
    }



    @Override
    public boolean isOpaque() {
        return true;
    }


}
