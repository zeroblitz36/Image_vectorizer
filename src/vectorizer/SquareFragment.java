package vectorizer;

/**
 * Created by Zero on 07.07.2015.
 */
public class SquareFragment {
    public int l, r, t, d, color;

    public SquareFragment(int l, int r, int t, int d, int color) {
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
