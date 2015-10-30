package utils;

import java.io.Serializable;

public class StaticPointArray implements Serializable{
    private short[] pointArrayX;
    private short[] pointArrayY;
    private int count;
    private int size;

    public StaticPointArray(int size){
        this.size = size;
        pointArrayX = new short[size];
        pointArrayY = new short[size];
    }

    public void push(short x,short y){
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

    public short getX(int i){
        return pointArrayX[i];
    }
    public short getY(int i){
        return pointArrayY[i];
    }

    public short getLastX(){
        return pointArrayX[count-1];
    }
    public short getLastY(){
        return pointArrayY[count-1];
    }

    public void setX(short i,short x){pointArrayX[i]=x;}
    public void setY(short i,short y){pointArrayY[i]=y;}
    public void setXY(int i,short x,short y){
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

    public void delete(int i){
        if(i<0 || i>= count)throw new RuntimeException("There is nothing to delete at "+i);
        if(count == 0)throw new RuntimeException("The static point array is already empty");

        for(int j=i;j<count-1;j++){
            pointArrayX[j]=pointArrayX[j+1];
            pointArrayY[j]=pointArrayY[j+1];
        }
        count--;
    }


    public void copyFrom(StaticPointArray spa){
        count = spa.count;
        if(count >= size)throw new RuntimeException("There is not enough space to copy "+count+" into "+size);
        for(int i=0;i<count;i++){
            pointArrayX[i] = spa.pointArrayX[i];
            pointArrayY[i] = spa.pointArrayY[i];
        }
    }
}
