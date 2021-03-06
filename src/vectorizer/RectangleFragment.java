package vectorizer;

import java.io.Serializable;

/**
 * Created by Zero on 07.07.2015.
 */
public class RectangleFragment implements Serializable {
    public short l, r, t, d;
    public int color;

    public RectangleFragment(short l, short r, short t, short d, int color) {
        this.l = l;
        this.r = r;
        this.t = t;
        this.d = d;
        this.color = color;
    }

    public RectangleFragment() {

    }

    public void set(short l, short r, short t, short d){
        this.l = l;
        this.r = r;
        this.t = t;
        this.d = d;
    }
    public boolean isValid() {
        return l <= r && t <= d;
    }

    public int area(){
        return (r-l+1)*(d-t+1);
    }

}
