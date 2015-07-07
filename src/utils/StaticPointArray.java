package utils;

public class StaticPointArray {
    private int[] pointArrayX;
    private int[] pointArrayY;
    private int count;
    private int size;

    public StaticPointArray(int size){
        this.size = size;
        pointArrayX = new int[size];
        pointArrayY = new int[size];
    }

    public void push(int x,int y){
        if(count<size) {
            pointArrayX[count] = x;
            pointArrayY[count] = y;
            count++;
        }else {
            throw new RuntimeException("Exceeded size");
        }
    }

    public void deleteLast(){
        count--;
        if(count<0) {
            count = 0;
            System.err.print("Attempting element from empty array");
        }
    }

    public void clearAll(){
        count=0;
    }

    public boolean isEmpty(){
        return count==0;
    }

    public int size(){
        return count;
    }

    public int getX(int i){
        return pointArrayX[i];
    }
    public int getY(int i){
        return pointArrayY[i];
    }

    public int getLastX(){
        return pointArrayX[count-1];
    }
    public int getLastY(){
        return pointArrayY[count-1];
    }

    public void setX(int i,int x){pointArrayX[i]=x;}
    public void setY(int i,int y){pointArrayY[i]=y;}
    public void setXY(int i,int x,int y){
        pointArrayX[i] = x;
        pointArrayY[i] = y;
    }

    public StaticPointArray cloneUpTo(int s){
        StaticPointArray spa = new StaticPointArray(s);
        for(int i=0;i<s && i<count;i++){
            spa.push(pointArrayX[i],pointArrayY[i]);
        }
        return spa;
    }
}
