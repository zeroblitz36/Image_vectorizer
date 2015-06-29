package mainpackage;

import utils.ImagePanel;
import vectorizer.PolygonVectorizer;
import vectorizer.SquareVectorizer;
import vectorizer.TriangleVectorizer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;


public class MainForm {

    private JPanel mainPanel;
    private JButton btnLoadPhoto;
    private ImagePanel imagePanel21;
    private ImagePanel imagePanel22;
    private ImagePanel imagePanel23;
    private ImagePanel imagePanel24;
    private JSlider slider1;
    private JButton chooseFileButton;

    File mainImageFile = new File("photo3.bmp");

    private float f2[][] = Filters.generateGaussian(3, 1.1f);
    private float f3[][] = Filters.generateGaussian(7,1.1f);
    private float f4[][] = Filters.generateGaussian(11,1.1f);

    private BufferedImage mainBufferedImage;
    private int lastSliderValue=-1;
    private SquareVectorizer squareVectorizer;
    private TriangleVectorizer triangleVectorizer;
    private PolygonVectorizer polygonVectorizer;

    private Random random = new Random(System.currentTimeMillis());
    public MainForm() {
        btnLoadPhoto.addActionListener(e -> new Thread(() -> {
            try {
                //reset image panels
                imagePanel21.setImage(null);
                imagePanel22.setImage(null);
                imagePanel23.setImage(null);
                imagePanel24.setImage(null);

                BufferedImage img = ImageIO.read(mainImageFile);
                System.out.printf("Loaded image with size: %d %d\n",img.getWidth(),img.getHeight());
                mainBufferedImage = img;

                imagePanel21.setImage(mainBufferedImage);

                if(squareVectorizer!=null)
                    squareVectorizer.cancelLastJob();
                squareVectorizer = new SquareVectorizer();
                squareVectorizer.setOriginalImage(mainBufferedImage);
                squareVectorizer.setDestImagePanel(imagePanel23);

                if(polygonVectorizer!=null)
                    polygonVectorizer.cancelLastJob();
                polygonVectorizer = new PolygonVectorizer();
                polygonVectorizer.setOriginalImage(mainBufferedImage);
                polygonVectorizer.initialize();
                polygonVectorizer.setDestImagePanel(imagePanel22);

                triangleVectorizer = new TriangleVectorizer(mainBufferedImage);
                triangleVectorizer.initialize();
                triangleVectorizer.setDestImagePanel(imagePanel24);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }).start());
        slider1.addChangeListener(e -> {
            int x = slider1.getValue();
            if(x!=lastSliderValue){
                //System.out.println("Changed value to "+x);
                lastSliderValue = x;

                squareVectorizer.threshold = x;
                squareVectorizer.startJob();

                triangleVectorizer.threshold = x;
                triangleVectorizer.startJob();

                polygonVectorizer.threshold = x;
                polygonVectorizer.startJob();
            }
        });

        chooseFileButton.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Choose image file");
            int returnedValue = fc.showOpenDialog(chooseFileButton);
            if(returnedValue == JFileChooser.APPROVE_OPTION){
                mainImageFile = fc.getSelectedFile();
            }
        });
    }





    public static void main(String[] args) {
        JFrame frame = new JFrame("MainForm");
        frame.setContentPane(new MainForm().mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
