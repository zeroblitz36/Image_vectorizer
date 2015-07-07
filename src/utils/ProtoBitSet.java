package utils;

import java.util.BitSet;

/**
 * Created by Zero on 07.07.2015.
 */
public class ProtoBitSet extends BitSet {
    private int readCounter;
    public void push(long x,int q){
        int size = size();
        q = Math.min(64,q);
        switch (q){
            case 64: set(size++,(x&1)==1);x>>=1;
            case 63: set(size++,(x&1)==1);x>>=1;
            case 62: set(size++,(x&1)==1);x>>=1;
            case 61: set(size++,(x&1)==1);x>>=1;
            case 60: set(size++,(x&1)==1);x>>=1;
            case 59: set(size++,(x&1)==1);x>>=1;
            case 58: set(size++,(x&1)==1);x>>=1;
            case 57: set(size++,(x&1)==1);x>>=1;
            case 56: set(size++,(x&1)==1);x>>=1;
            case 55: set(size++,(x&1)==1);x>>=1;
            case 54: set(size++,(x&1)==1);x>>=1;
            case 53: set(size++,(x&1)==1);x>>=1;
            case 52: set(size++,(x&1)==1);x>>=1;
            case 51: set(size++,(x&1)==1);x>>=1;
            case 50: set(size++,(x&1)==1);x>>=1;
            case 49: set(size++,(x&1)==1);x>>=1;
            case 48: set(size++,(x&1)==1);x>>=1;
            case 47: set(size++,(x&1)==1);x>>=1;
            case 46: set(size++,(x&1)==1);x>>=1;
            case 45: set(size++,(x&1)==1);x>>=1;
            case 44: set(size++,(x&1)==1);x>>=1;
            case 43: set(size++,(x&1)==1);x>>=1;
            case 42: set(size++,(x&1)==1);x>>=1;
            case 41: set(size++,(x&1)==1);x>>=1;
            case 40: set(size++,(x&1)==1);x>>=1;
            case 39: set(size++,(x&1)==1);x>>=1;
            case 38: set(size++,(x&1)==1);x>>=1;
            case 37: set(size++,(x&1)==1);x>>=1;
            case 36: set(size++,(x&1)==1);x>>=1;
            case 35: set(size++,(x&1)==1);x>>=1;
            case 34: set(size++,(x&1)==1);x>>=1;
            case 33: set(size++,(x&1)==1);x>>=1;
            case 32: set(size++,(x&1)==1);x>>=1;
            case 31: set(size++,(x&1)==1);x>>=1;
            case 30: set(size++,(x&1)==1);x>>=1;
            case 29: set(size++,(x&1)==1);x>>=1;
            case 28: set(size++,(x&1)==1);x>>=1;
            case 27: set(size++,(x&1)==1);x>>=1;
            case 26: set(size++,(x&1)==1);x>>=1;
            case 25: set(size++,(x&1)==1);x>>=1;
            case 24: set(size++,(x&1)==1);x>>=1;
            case 23: set(size++,(x&1)==1);x>>=1;
            case 22: set(size++,(x&1)==1);x>>=1;
            case 21: set(size++,(x&1)==1);x>>=1;
            case 20: set(size++,(x&1)==1);x>>=1;
            case 19: set(size++,(x&1)==1);x>>=1;
            case 18: set(size++,(x&1)==1);x>>=1;
            case 17: set(size++,(x&1)==1);x>>=1;
            case 16: set(size++,(x&1)==1);x>>=1;
            case 15: set(size++,(x&1)==1);x>>=1;
            case 14: set(size++,(x&1)==1);x>>=1;
            case 13: set(size++,(x&1)==1);x>>=1;
            case 12: set(size++,(x&1)==1);x>>=1;
            case 11: set(size++,(x&1)==1);x>>=1;
            case 10: set(size++,(x&1)==1);x>>=1;
            case 9: set(size++,(x&1)==1);x>>=1;
            case 8: set(size++,(x&1)==1);x>>=1;
            case 7: set(size++,(x&1)==1);x>>=1;
            case 6: set(size++,(x&1)==1);x>>=1;
            case 5: set(size++,(x&1)==1);x>>=1;
            case 4: set(size++,(x&1)==1);x>>=1;
            case 3: set(size++,(x&1)==1);x>>=1;
            case 2: set(size++,(x&1)==1);x>>=1;
            case 1: set(size++,(x&1)==1);x>>=1;
                default:
                    break;
        }
    }
    public void push(int x,int q){
        int size = size();
        q = Math.min(32,q);
        switch (q){
            case 32: set(size++,(x&1)==1);x>>=1;
            case 31: set(size++,(x&1)==1);x>>=1;
            case 30: set(size++,(x&1)==1);x>>=1;
            case 29: set(size++,(x&1)==1);x>>=1;
            case 28: set(size++,(x&1)==1);x>>=1;
            case 27: set(size++,(x&1)==1);x>>=1;
            case 26: set(size++,(x&1)==1);x>>=1;
            case 25: set(size++,(x&1)==1);x>>=1;
            case 24: set(size++,(x&1)==1);x>>=1;
            case 23: set(size++,(x&1)==1);x>>=1;
            case 22: set(size++,(x&1)==1);x>>=1;
            case 21: set(size++,(x&1)==1);x>>=1;
            case 20: set(size++,(x&1)==1);x>>=1;
            case 19: set(size++,(x&1)==1);x>>=1;
            case 18: set(size++,(x&1)==1);x>>=1;
            case 17: set(size++,(x&1)==1);x>>=1;
            case 16: set(size++,(x&1)==1);x>>=1;
            case 15: set(size++,(x&1)==1);x>>=1;
            case 14: set(size++,(x&1)==1);x>>=1;
            case 13: set(size++,(x&1)==1);x>>=1;
            case 12: set(size++,(x&1)==1);x>>=1;
            case 11: set(size++,(x&1)==1);x>>=1;
            case 10: set(size++,(x&1)==1);x>>=1;
            case 9: set(size++,(x&1)==1);x>>=1;
            case 8: set(size++,(x&1)==1);x>>=1;
            case 7: set(size++,(x&1)==1);x>>=1;
            case 6: set(size++,(x&1)==1);x>>=1;
            case 5: set(size++,(x&1)==1);x>>=1;
            case 4: set(size++,(x&1)==1);x>>=1;
            case 3: set(size++,(x&1)==1);x>>=1;
            case 2: set(size++,(x&1)==1);x>>=1;
            case 1: set(size++,(x&1)==1);x>>=1;
            default:
                break;
        }
    }
    public void push(short x,int q){
        int size = size();
        q = Math.min(16,q);
        switch (q){
            case 16: set(size++,(x&1)==1);x>>=1;
            case 15: set(size++,(x&1)==1);x>>=1;
            case 14: set(size++,(x&1)==1);x>>=1;
            case 13: set(size++,(x&1)==1);x>>=1;
            case 12: set(size++,(x&1)==1);x>>=1;
            case 11: set(size++,(x&1)==1);x>>=1;
            case 10: set(size++,(x&1)==1);x>>=1;
            case 9: set(size++,(x&1)==1);x>>=1;
            case 8: set(size++,(x&1)==1);x>>=1;
            case 7: set(size++,(x&1)==1);x>>=1;
            case 6: set(size++,(x&1)==1);x>>=1;
            case 5: set(size++,(x&1)==1);x>>=1;
            case 4: set(size++,(x&1)==1);x>>=1;
            case 3: set(size++,(x&1)==1);x>>=1;
            case 2: set(size++,(x&1)==1);x>>=1;
            case 1: set(size++,(x&1)==1);x>>=1;
            default:
                break;
        }
    }
    public void push(byte x,int q){
        int size = size();
        q = Math.min(8,q);
        switch (q){
            case 8: set(size++,(x&1)==1);x>>=1;
            case 7: set(size++,(x&1)==1);x>>=1;
            case 6: set(size++,(x&1)==1);x>>=1;
            case 5: set(size++,(x&1)==1);x>>=1;
            case 4: set(size++,(x&1)==1);x>>=1;
            case 3: set(size++,(x&1)==1);x>>=1;
            case 2: set(size++,(x&1)==1);x>>=1;
            case 1: set(size++,(x&1)==1);x>>=1;
            default:
                break;
        }
    }
}
