package vectorizer;

import java.awt.*;
import java.awt.geom.Path2D;
import java.io.DataOutputStream;
import java.io.Serializable;

public class Triangle implements Serializable{
    public float x0,y0;
    public float x1,y1;
    public float x2,y2;
    public int color;
    public transient float yMin,yMax;
    public transient float xMin,xMax;
    public transient float area;
    private transient Path2D.Float path;
    public Triangle(float x0, float y0, float x1, float y1, float x2, float y2) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        yMin = y0;
        if(yMin > y1) yMin = y1;
        if(yMin > y2) yMin = y2;

        xMin = x0;
        if(xMin > x1) xMin = x1;
        if(xMin > x2) xMin = x2;


        yMax = y0;
        if(yMax < y1) yMax = y1;
        if(yMax < y2) yMax = y2;

        xMax = x0;
        if(xMax < x1) xMax = x1;
        if(xMax < x2) xMax = x2;

        area = Math.abs(x0*(y1-y2)+x1*(y2-y0)+x2*(y0-y1))/2.f;
    }

    public Path2D.Float getPath(){
        if(path==null){
            path = new Path2D.Float();
            path.moveTo(x0,y0);
            path.lineTo(x1,y1);
            path.lineTo(x2,y2);
            path.lineTo(x0,y0);
        }
        return path;
    }

    public Path2D.Float getClonePath(){
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x0,y0);
        p.lineTo(x1,y1);
        p.lineTo(x2,y2);
        p.lineTo(x0,y0);
        return p;
    }
}
