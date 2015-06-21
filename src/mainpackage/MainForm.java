package mainpackage;

import utils.ImagePanel;
import utils.Utility;
import vectorizer.PolygonVectorizer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by GeorgeRoscaneanu on 15.04.2015.
 */
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
    //File file2 = new File("C:\\Users\\GeorgeRoscaneanu\\Downloads\\milky-way-galaxy-615.jpg");

    private float f[][] = new float[][]{
            {-1,-1, 0},
            {-1, 0, 1},
            { 0, 1, 1}
    };

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
        btnLoadPhoto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //reset image panels
                            imagePanel21.setImage(null);
                            imagePanel22.setImage(null);
                            imagePanel23.setImage(null);
                            imagePanel24.setImage(null);

                            BufferedImage img = ImageIO.read(mainImageFile);
                            mainBufferedImage = img;

                            imagePanel21.setImage(mainBufferedImage);

                            polygonVectorizer = new PolygonVectorizer(mainBufferedImage);
                            polygonVectorizer.initialize();
                            polygonVectorizer.setDestImagePanel(imagePanel22);

                            triangleVectorizer = new TriangleVectorizer(mainBufferedImage);
                            triangleVectorizer.initialize();
                            triangleVectorizer.setDestImagePanel(imagePanel24);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        slider1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int x = slider1.getValue();
                if(x!=lastSliderValue){
                    //System.out.println("Changed value to "+x);
                    lastSliderValue = x;

                    if(squareVectorizer!=null){
                        squareVectorizer.setCanceled(true);
                    }

                    squareVectorizer = new SquareVectorizer(x);
                    squareVectorizer.start();

                    triangleVectorizer.threshold = x;
                    triangleVectorizer.startJob();

                    polygonVectorizer.threshold = x;
                    polygonVectorizer.startJob();
                }
            }
        });

        chooseFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Choose image file");
                int returnedValue = fc.showOpenDialog(chooseFileButton);
                if(returnedValue == JFileChooser.APPROVE_OPTION){
                    mainImageFile = fc.getSelectedFile();
                }
            }
        });
    }




    public class SquareVectorizer extends Thread{
        boolean isCanceled = false;
        private int threshold;
        private ArrayList<SquareFragment> fragList = new ArrayList<>();
        long startTime,endTime;
        private ExecutorService executorService = Executors.newFixedThreadPool(4);


        public SquareVectorizer(int threshold){
            this.threshold = threshold;

        }


        @Override
        public void run() {
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(mainBufferedImage ==null || isCanceled)return ;
            int w = mainBufferedImage.getWidth();
            int h = mainBufferedImage.getHeight();
            SquareFragment squareFragment = new SquareFragment(0,w-1,0,h-1,-1);
            startTime = System.currentTimeMillis();
            //recFragCheck(squareFragment);
            splitRecFragCheck(squareFragment);
            executorService.shutdown();
            endTime = System.currentTimeMillis();
            if(isCanceled)return;
            //System.out.format("Finished vectorizing: %d shapes %.3f s\n", fragList.size(),(endTime-startTime)/1000.f);
            startTime = System.currentTimeMillis();
            BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = image.createGraphics();
            for(SquareFragment s : fragList){
                if(isCanceled)return;
                g.setColor(new Color(s.color));
                g.fillRect(s.l, s.t, s.r - s.l + 1, s.d - s.t + 1);
                //g.setColor(Color.GRAY);
                //g.drawRect(s.l, s.t, s.r - s.l + 1, s.d - s.t + 1);
            }
            endTime = System.currentTimeMillis();
            if(isCanceled)return;
            imagePanel23.setImage(image);
            //System.out.format("Finished drawing: %d shapes %.3f s\n", fragList.size(),(endTime-startTime)/1000.f);
            //System.out.format("Approx mainImageFile size: %d KB\n",fragList.size()*12/1024);
        }

        private void recFragCheck(SquareFragment s){
            int rTotal=0,gTotal=0,bTotal=0,count=0,color;
            int r,g,b;
            int t,min = 9999, max = -9999;

            boolean fail = false;

            for(int y=s.t;y<=s.d && !fail;y++)
                for(int x=s.l;x<=s.r && !fail;x++){
                    if(isCanceled)return;
                    count++;
                    color = mainBufferedImage.getRGB(x,y);
                    b = color & 0xff;
                    color >>= 8;
                    g = color & 0xff;
                    color >>= 8;
                    r = color & 0xff;

                    t = r + g + b;
                    bTotal += b;
                    gTotal += g;
                    rTotal += r;

                    if(t < min)min = t;
                    if(t > max)max = t;

                    if(max-min > threshold<<1) {
                        fail = true;
                    }
                }
            rTotal /= count;
            gTotal /= count;
            bTotal /= count;
            int avgColor = 0xff000000 | (rTotal<<16) | (gTotal<<8) | bTotal;
            for(int y=s.t;y<=s.d && !fail;y++)
                for(int x=s.l;x<=s.r && !fail;x++){
                    if(isCanceled)return;
                    color = mainBufferedImage.getRGB(x,y);
                    if(Utility.manhattanDistance(color, avgColor)>threshold)
                        fail = true;
                }
            if(!fail){
                s.color = avgColor;
                synchronized (fragList) {
                    fragList.add(s);
                }
            }else{
                int midX = (int) (s.l + (random.nextFloat()/2 + 0.25f) * (s.r-s.l));
                int midY = (int) (s.t + (random.nextFloat()/2 + 0.25f) * (s.d-s.t));

                SquareFragment s1 = new SquareFragment(s.l,     midX,   s.t,    midY,-1);
                SquareFragment s2 = new SquareFragment(midX+1,  s.r,    s.t,    midY,-1);
                SquareFragment s3 = new SquareFragment(s.l,     midX,   midY+1, s.d,-1);
                SquareFragment s4 = new SquareFragment(midX+1,  s.r,    midY+1, s.d,-1);

                if(s1.isValid()) recFragCheck(s1);
                if(s2.isValid()) recFragCheck(s2);
                if(s3.isValid()) recFragCheck(s3);
                if(s4.isValid()) recFragCheck(s4);
            }
        }

        private void splitRecFragCheck(SquareFragment s){
            int midX = (int) (s.l + (random.nextFloat()/2 + 0.25f) * (s.r-s.l));
            int midY = (int) (s.t + (random.nextFloat()/2 + 0.25f) * (s.d-s.t));

            SquareFragment squareFragments[] = new SquareFragment[]{
                    new SquareFragment(s.l,     midX,   s.t,    midY,-1),
                    new SquareFragment(midX+1,  s.r,    s.t,    midY,-1),
                    new SquareFragment(s.l,     midX,   midY+1, s.d,-1),
                    new SquareFragment(midX+1,  s.r,    midY+1, s.d,-1)
            };
            ArrayList<Thread> threads = new ArrayList<>(4);
            for(SquareFragment squareFragment : squareFragments){
                final SquareFragment sf = squareFragment;
                if(sf.isValid()){
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            recFragCheck(sf);
                        }
                    });
                    threads.add(t);
                    t.start();
                }
            }
            for(Thread t : threads){
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setCanceled(boolean b){
            isCanceled = b;
        }
    }

    public class SquareFragment{
        public int l,r,t,d,color;

        public SquareFragment(int l, int r, int t, int d, int color) {
            this.l = l;
            this.r = r;
            this.t = t;
            this.d = d;
            this.color = color;
        }

        public boolean isValid(){
            return l<=r && t<=d;
        }
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("MainForm");
        frame.setContentPane(new MainForm().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
