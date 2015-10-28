package mainpackage;

import utils.ImagePanel;
import vectorizer.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;


public class MainForm {

    private JPanel mainPanel;
    private JButton btnLoadPhoto;
    private ImagePanel imagePanel21;
    private JSlider slider1;
    private JButton chooseFileButton;
    private JComboBox<String> cbVectorizerTechnique;
    private JButton btnStart;
    private JButton btnExport;
    private JButton btnTest;
    private JButton curveEditorButton;

    File mainImageFile = new File("photo3.bmp");

    private BufferedImage mainBufferedImage;
    //private int lastSliderValue=-1;
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

                updateCurrentVectorizer();
            } catch (IOException err) {
                err.printStackTrace();
            }
        }).start());

        chooseFileButton.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser("C:\\Vectorizer");
            fc.setDialogTitle("Choose image file");
            int returnedValue = fc.showOpenDialog(chooseFileButton);
            if (returnedValue == JFileChooser.APPROVE_OPTION) {
                mainImageFile = fc.getSelectedFile();
            }
        });
        cbVectorizerTechnique.addItemListener(e -> {
            /*String s = e.getItem().toString();

            BaseVectorizer vect = indexHashMap.get(s);

            if (e.getStateChange() == ItemEvent.SELECTED) {
                System.out.println("Selected: " + e.getItem());
                if (vect == null) {
                    imagePanel21.setImage(mainBufferedImage);
                    currentVectorizer = null;
                } else {
                    vect.setOriginalImage(mainBufferedImage);
                    vect.setDestImagePanel(imagePanel21);
                    currentVectorizer = vect;
                }
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                System.out.println("Deselected: " + e.getItem());
                if (vect != null) {
                    vect.cancelLastJob();
                }
            }   */

            updateCurrentVectorizer();
        });
        updateCurrentVectorizer();
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentVectorizer != null) {
                    int x = slider1.getValue();
                    currentVectorizer.threshold = x;
                    currentVectorizer.isInBenchmark = false;
                    currentVectorizer.startJob();
                }
            }
        });
        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentVectorizer != null) {
                    final JFileChooser fc = new JFileChooser("C:\\Vectorizer");

                    fc.setDialogTitle("Export vectorized image");
                    int returnedValue = fc.showSaveDialog(btnExport);
                    if (returnedValue == JFileChooser.APPROVE_OPTION) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    File file = fc.getSelectedFile();
                                    FileOutputStream fos = new FileOutputStream(file);
                                    DataOutputStream dos = new DataOutputStream(fos);
                                    System.out.println("Exporting to output stream");
                                    String fileName = file.getName();
                                    if (fileName.endsWith(".svg")) {
                                        currentVectorizer.exportToSVG(dos, false);
                                    } else if (fileName.endsWith(".svgz")) {
                                        currentVectorizer.exportToSVG(dos, true);
                                    } else if (fileName.endsWith(".html")) {
                                        //currentVectorizer.exportToHTML(dos);
                                    } else {
                                        //currentVectorizer.exportToOutputStream(dos);
                                    }
                                    System.out.println("Exporting done");
                                    dos.close();
                                } catch (FileNotFoundException e1) {
                                    e1.printStackTrace();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
            }
        });
        /*
        curveEditorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("Curve Editor");
                frame.setContentPane(new BSplineEditor().mainPanel);
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.setSize(640,480);
                frame.setVisible(true);
            }
        });*/
        slider1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(currentVectorizer!=null) {
                    int x = slider1.getValue();
                    currentVectorizer.threshold = x;
                    currentVectorizer.isInBenchmark = false;
                    currentVectorizer.startJob();
                }
            }
        });
        btnTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentVectorizer!=null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            BenchmarkTool benchmarkTool = new BenchmarkTool();
                            benchmarkTool.test(currentVectorizer);
                        }
                    }).start();
                }
            }
        });
    }

    private void updateCurrentVectorizer(){
        String s = cbVectorizerTechnique.getSelectedItem().toString();

        BaseVectorizer vect = indexHashMap.get(s);
        //if(currentVectorizer==vect) return;
        if(currentVectorizer!=null){
            currentVectorizer.cancelLastJob();
        }
        currentVectorizer = vect;
        if(currentVectorizer==null){
            imagePanel21.setImage(mainBufferedImage);
        }else{
            currentVectorizer.setOriginalImage(mainBufferedImage);
            currentVectorizer.setDestImagePanel(imagePanel21);
        }
    }



    public static void main(String[] args) {
        JFrame frame = new JFrame("MainForm");
        frame.setContentPane(new MainForm().mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
