package utils;

/**
 * Created by GeorgeRoscaneanu on 17.04.2015.
 */
public class Utility {
    public static int manhattanDistance(int c1,int c2){
        char b1 = (char)(c1 & 255);
        c1 >>= 8;
        char g1 = (char)(c1 & 255);
        c1 >>= 8;
        char r1 = (char)(c1 & 255);

        char b2 = (char)(c2 & 255);
        c2 >>= 8;
        char g2 = (char)(c2 & 255);
        c2 >>= 8;
        char r2 = (char)(c2 & 255);

        return abs(r1,r2)+abs(g1,g2)+abs(b1,b2);
    }

    public static int rainbow(float y){
        int x = (int) (1280.f*y);
        if(x<0)return 0xff000000;
        if(x<256) return 0xff000000 | (x<<16);
        x -= 256;
        if(x<256) return 0xffff0000 | (x<<8);
        x -= 256;
        if(x<256) return 0xff00ff00 | ((255-x)<<16);
        x -= 256;
        if(x<256) return 0xff00ff0 | (x);
        x -= 256;
        if(x<256) return 0xff0000ff | ((255-x)<<8);
        return 0xff000000;
    }
    public static int greyScale(float y){
        int x = (int) (255*y);
        return 0xff000000 | (x<<16) | (x<<8) | x;
    }

    public static int abs(char a,char b){
        if(a<b) return b-a;
        return a-b;
    }


}
