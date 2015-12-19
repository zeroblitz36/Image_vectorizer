package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by GeorgeRoscaneanu on 15.04.2015.
 */
public class ImagePanel extends JPanel{
    private BufferedImage image;
    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }

    public void setImage(BufferedImage img){
        image = img;
        repaint();
    }

    @Override
    public boolean isOpaque() {
        return true;
    }

}
