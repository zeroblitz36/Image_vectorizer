package mainpackage;

/**
 * Created by GeorgeRoscaneanu on 16.04.2015.
 */
public class Filters {
    public static float[][] generateGaussian(int size,float ro){
        if(size<=0 || size%2==0)throw new RuntimeException("The size must be a positive odd integer");
        float m[][] = new float[size][size];

        float a = 2*ro*ro;
        float b = (float)(a * Math.PI);
        int k = (size-1)/2;

        for(int i=0;i<size;i++)
            for(int j=0;j<size;j++){
                m[i][j] = (float) Math.exp(-((i-k)*(i-k)+(j-k)*(j-k))/a) / b;
            }

        //normalize
        float sum=0;
        for(int i=0;i<size;i++)
            for(int j=0;j<size;j++)
                sum += m[i][j];

        for(int i=0;i<size;i++)
            for(int j=0;j<size;j++)
                m[i][j] /= sum;
        return m;
    }
}
