package vectorizer;

/**
 * Created by Zero on 27.10.2015.
 */
public class BenchmarkTool {
    private boolean isCanceled = false;
    public void test(BaseVectorizer vectorizer){
        for(int i=0;i<=512;i++){

        }
    }

    public synchronized boolean isCanceled() {
        return isCanceled;
    }

    public synchronized void setIsCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }
}
