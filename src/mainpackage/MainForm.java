package mainpackage;

import utils.ImagePanel;
import vectorizer.BaseVectorizer;
import vectorizer.PolygonVectorizer;
import vectorizer.SquareVectorizer;
import vectorizer.TriangleVectorizer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;


public class MainForm {

    private JPanel mainPanel;
    private JButton btnLoadPhoto;
    private ImagePanel imagePanel21;
    private JSlider slider1;
    private JButton chooseFileButton;
    private JComboBox<String> cbVectorizerTechnique;

    File mainImageFile = new File("photo3.bmp");

    private BufferedImage mainBufferedImage;
    private int lastSliderValue=-1;
    private BaseVectorizer currentVectorizer = null;

    private HashMap<String, BaseVectorizer> indexHashMap = new HashMap<>(3);

    private Random random = new Random(System.currentTimeMillis());
    public MainForm() {
        indexHashMap.put("Original",null);
        indexHashMap.put("Square",new SquareVectorizer());
        indexHashMap.put("Triangle",new TriangleVectorizer());
        indexHashMap.put("Polygon", new PolygonVectorizer());

        for(String key : indexHashMap.keySet()){
            cbVectorizerTechnique.addItem(key);
        }

        btnLoadPhoto.addActionListener(e -> new Thread(() -> {
            try {
                //reset image panels
                imagePanel21.setImage(null);

                BufferedImage img = ImageIO.read(mainImageFile);
                System.out.printf("Loaded image with size: %d %d\n", img.getWidth(), img.getHeight());
                mainBufferedImage = img;

                imagePanel21.setImage(mainBufferedImage);

            } catch (IOException err) {
                err.printStackTrace();
            }
        }).start());
        slider1.addChangeListener(e -> {
            int x = slider1.getValue();
            if (x != lastSliderValue) {
                lastSliderValue = x;
                if(currentVectorizer!=null) {
                    currentVectorizer.threshold = x;
                    currentVectorizer.startJob();
                }
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
        cbVectorizerTechnique.addItemListener(e -> {
            String s = e.getItem().toString();

            BaseVectorizer vect = indexHashMap.get(s);

            if(e.getStateChange()==ItemEvent.SELECTED) {
                System.out.println("Selected: "+e.getItem());
                if(vect==null){
                    imagePanel21.setImage(mainBufferedImage);
                    currentVectorizer=null;
                }else{
                    vect.setOriginalImage(mainBufferedImage);
                    vect.setDestImagePanel(imagePanel21);
                    if(slider1.getValue()!=vect.threshold){
                        vect.threshold = slider1.getValue();
                        vect.startJob();
                    }
                    currentVectorizer = vect;
                }
            }else if(e.getStateChange()==ItemEvent.DESELECTED){
                System.out.println("Deselected: "+e.getItem());
                if(vect!=null){
                    vect.cancelLastJob();
                }
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
