package mainpackage;

import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Created by GeorgeRoscaneanu on 20.04.2015.
 */
public class Triangle {
    public float x0,y0;
    public float x1,y1;
    public float x2,y2;
    public float yMin,yMax;
    public float xMin,xMax;
    public int color;
    public float area;
    //public float xArray[];
    //public float yArray[];
    //public Polygon polygon;
    public Path2D.Float path;
    public Triangle(float x0, float y0, float x1, float y1, float x2, float y2) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        //xArray = new float[]{x0,x1,x2};
        //yArray = new float[]{y0,y1,y2};
        //polygon = new Polygon(xArray,yArray,3);

        path = new Path2D.Float();
        path.moveTo(x0,y0);
        path.lineTo(x1,y1);
        path.lineTo(x2,y2);
        path.lineTo(x0,y0);

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

    public void expand(int t,int d,int l,int r){
        float midX = (x0+x1+x2)/3;
        float midY = (y0+y1+y2)/3;
        x0 += (midX<x0)?1:-1;
        x1 += (midX<x1)?1:-1;
        x2 += (midX<x2)?1:-1;

        y0 += (midY<y0)?1:-1;
        y1 += (midY<y1)?1:-1;
        y2 += (midY<y2)?1:-1;

        if(x0 < l)x0 = l;
        if(x0 > r)x0 = r;
        if(x1 < l)x1 = l;
        if(x1 > r)x1 = r;
        if(x2 < l)x2 = l;
        if(x2 > r)x2 = r;

        if(y0 < t)y0 = t;
        if(y0 > d)y0 = d;
        if(y1 < t)y1 = t;
        if(y1 > d)y1 = d;
        if(y2 < t)y2 = t;
        if(y2 > d)y2 = d;

        path.reset();
        path.moveTo(x0, y0);
        path.lineTo(x1,y1);
        path.lineTo(x2,y2);
        path.lineTo(x0, y0);

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

    public void iterateAllPointsInside(){
        float i0=0,i1=0,man;
        int flag;
        float a;
        for(float y = yMin;y <= yMax;y++){
            flag = 0;
            a = (y-y0)/(y1-y0);
            flag += ((a >= 0) && (a <= 1)) ? 1 : 0;

            if(flag == 1){
                i0 = (int) (x0 + (x1-x0)*a);
            }

            a = (y-y1)/(y2-y1);
            flag += ((a >= 0) && (a <= 1)) ? 1 : 0;

            if(flag == 1){
                i0 = (int) (x1 + (x2-x1)*a);
            }else if(flag == 2){
                i1 = (int) (x1 + (x2-x1)*a);
            }

            if(flag == 1){
                a = (y-y2)/(y0-y2);
                i1 = (int) (x2 + (x0-x2)*a);
            }

            if(i0 > i1){
                man = i0;
                i0 = i1;
                i1 = man;
            }
            i0 = (int) Math.ceil(i0);
            i1 = (int) Math.floor(i1);
            System.out.format("Points on line %d from %d to %d: ",(int)y,(int)i0,(int)i1);
            for(int x = (int) i0;x<= i1;x++){
                System.out.format("(%d,%d) ",x,(int)y);
            }
            System.out.println();
        }
    }


}
