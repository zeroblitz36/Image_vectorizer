package vectorizer;

import java.io.Serializable;

/**
 * Created by Zero on 07.07.2015.
 */
public class SquareFragment implements Serializable {
    public short l, r, t, d;
    public int color;

    public SquareFragment(short l, short r, short t, short d, int color) {
        this.l = l;
        this.r = r;
        this.t = t;
        this.d = d;
        this.color = color;
    }

    public boolean isValid() {
        return l <= r && t <= d;
    }
}
