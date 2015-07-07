package vectorizer;

import utils.StaticPointArray;

import java.awt.geom.Path2D;

/**
 * Created by GeorgeRoscaneanu on 28.04.2015.
 */
public class ColoredPolygon {
    private Path2D.Float path;
    public int color;
    public StaticPointArray pointArray;



    public Path2D.Float getPath(){
        if(path==null){
            path = new Path2D.Float();
            if(pointArray.size()>0) {
                path.moveTo(pointArray.getX(0), pointArray.getY(0));
                for (int i = 1; i < pointArray.size(); i++) {
                    path.lineTo(pointArray.getX(i), pointArray.getY(i));
                }
                path.lineTo(pointArray.getX(0), pointArray.getY(0));
            }
        }
        return path;
    }
}
