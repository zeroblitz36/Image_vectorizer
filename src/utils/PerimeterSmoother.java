package utils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zero on 28.07.2015.
 */
public class PerimeterSmoother {
    public static List<Point2D.Float> convertToBSplineArray(List<Point2D.Float> input,int p,int h){
        ArrayList<Point2D.Float> list = new ArrayList<Point2D.Float>();
        int n = input.size()-1;
        double P[][] = new double [h-1][2];
        double Q[][] = new double [h-1][2];
        double N[][] = new double [h-1][h-1];

        double D[][] = new double[n+1][2];
        for(int i=0;i<=n;i++){
            Point2D.Float point = input.get(i);
            D[i][0] = point.getX();
            D[i][1] = point.getY();
        }
        double t[] = new double[n+1];
        for(int i=0;i<=n;i++){
            t[i] = centripetalParameter(D[i],D[(i+1)%n],0.5);
        }


        return list;
    }

    public static double centripetalParameter(double p0[],double p1[],double a){
        return Math.pow(Math.sqrt((p0[0]-p1[0])*(p0[0]-p1[0]) + (p0[1]-p1[1])*(p0[1]-p1[1])) , a);
    }
}
