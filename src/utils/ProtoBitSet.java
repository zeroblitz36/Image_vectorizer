package utils;

import java.util.BitSet;

/**
 * Created by Zero on 07.07.2015.
 */
public class ProtoBitSet extends BitSet {
    private int readCounter;
    public int currentLength=0;
    public void push(long x,int q){
        q = Math.min(64, q);
        switch (q){
            case 64: set(currentLength++,(x&1)==1);x>>=1;
            case 63: set(currentLength++,(x&1)==1);x>>=1;
            case 62: set(currentLength++,(x&1)==1);x>>=1;
            case 61: set(currentLength++,(x&1)==1);x>>=1;
            case 60: set(currentLength++,(x&1)==1);x>>=1;
            case 59: set(currentLength++,(x&1)==1);x>>=1;
            case 58: set(currentLength++,(x&1)==1);x>>=1;
            case 57: set(currentLength++,(x&1)==1);x>>=1;
            case 56: set(currentLength++,(x&1)==1);x>>=1;
            case 55: set(currentLength++,(x&1)==1);x>>=1;
            case 54: set(currentLength++,(x&1)==1);x>>=1;
            case 53: set(currentLength++,(x&1)==1);x>>=1;
            case 52: set(currentLength++,(x&1)==1);x>>=1;
            case 51: set(currentLength++,(x&1)==1);x>>=1;
            case 50: set(currentLength++,(x&1)==1);x>>=1;
            case 49: set(currentLength++,(x&1)==1);x>>=1;
            case 48: set(currentLength++,(x&1)==1);x>>=1;
            case 47: set(currentLength++,(x&1)==1);x>>=1;
            case 46: set(currentLength++,(x&1)==1);x>>=1;
            case 45: set(currentLength++,(x&1)==1);x>>=1;
            case 44: set(currentLength++,(x&1)==1);x>>=1;
            case 43: set(currentLength++,(x&1)==1);x>>=1;
            case 42: set(currentLength++,(x&1)==1);x>>=1;
            case 41: set(currentLength++,(x&1)==1);x>>=1;
            case 40: set(currentLength++,(x&1)==1);x>>=1;
            case 39: set(currentLength++,(x&1)==1);x>>=1;
            case 38: set(currentLength++,(x&1)==1);x>>=1;
            case 37: set(currentLength++,(x&1)==1);x>>=1;
            case 36: set(currentLength++,(x&1)==1);x>>=1;
            case 35: set(currentLength++,(x&1)==1);x>>=1;
            case 34: set(currentLength++,(x&1)==1);x>>=1;
            case 33: set(currentLength++,(x&1)==1);x>>=1;
            case 32: set(currentLength++,(x&1)==1);x>>=1;
            case 31: set(currentLength++,(x&1)==1);x>>=1;
            case 30: set(currentLength++,(x&1)==1);x>>=1;
            case 29: set(currentLength++,(x&1)==1);x>>=1;
            case 28: set(currentLength++,(x&1)==1);x>>=1;
            case 27: set(currentLength++,(x&1)==1);x>>=1;
            case 26: set(currentLength++,(x&1)==1);x>>=1;
            case 25: set(currentLength++,(x&1)==1);x>>=1;
            case 24: set(currentLength++,(x&1)==1);x>>=1;
            case 23: set(currentLength++,(x&1)==1);x>>=1;
            case 22: set(currentLength++,(x&1)==1);x>>=1;
            case 21: set(currentLength++,(x&1)==1);x>>=1;
            case 20: set(currentLength++,(x&1)==1);x>>=1;
            case 19: set(currentLength++,(x&1)==1);x>>=1;
            case 18: set(currentLength++,(x&1)==1);x>>=1;
            case 17: set(currentLength++,(x&1)==1);x>>=1;
            case 16: set(currentLength++,(x&1)==1);x>>=1;
            case 15: set(currentLength++,(x&1)==1);x>>=1;
            case 14: set(currentLength++,(x&1)==1);x>>=1;
            case 13: set(currentLength++,(x&1)==1);x>>=1;
            case 12: set(currentLength++,(x&1)==1);x>>=1;
            case 11: set(currentLength++,(x&1)==1);x>>=1;
            case 10: set(currentLength++,(x&1)==1);x>>=1;
            case 9: set(currentLength++,(x&1)==1);x>>=1;
            case 8: set(currentLength++,(x&1)==1);x>>=1;
            case 7: set(currentLength++,(x&1)==1);x>>=1;
            case 6: set(currentLength++,(x&1)==1);x>>=1;
            case 5: set(currentLength++,(x&1)==1);x>>=1;
            case 4: set(currentLength++,(x&1)==1);x>>=1;
            case 3: set(currentLength++,(x&1)==1);x>>=1;
            case 2: set(currentLength++,(x&1)==1);x>>=1;
            case 1: set(currentLength++,(x&1)==1);x>>=1;
                default:
                    break;
        }
    }
    public void push(int x,int q){
        q = Math.min(32,q);
        switch (q){
            case 32: set(currentLength++,(x&1)==1);x>>=1;
            case 31: set(currentLength++,(x&1)==1);x>>=1;
            case 30: set(currentLength++,(x&1)==1);x>>=1;
            case 29: set(currentLength++,(x&1)==1);x>>=1;
            case 28: set(currentLength++,(x&1)==1);x>>=1;
            case 27: set(currentLength++,(x&1)==1);x>>=1;
            case 26: set(currentLength++,(x&1)==1);x>>=1;
            case 25: set(currentLength++,(x&1)==1);x>>=1;
            case 24: set(currentLength++,(x&1)==1);x>>=1;
            case 23: set(currentLength++,(x&1)==1);x>>=1;
            case 22: set(currentLength++,(x&1)==1);x>>=1;
            case 21: set(currentLength++,(x&1)==1);x>>=1;
            case 20: set(currentLength++,(x&1)==1);x>>=1;
            case 19: set(currentLength++,(x&1)==1);x>>=1;
            case 18: set(currentLength++,(x&1)==1);x>>=1;
            case 17: set(currentLength++,(x&1)==1);x>>=1;
            case 16: set(currentLength++,(x&1)==1);x>>=1;
            case 15: set(currentLength++,(x&1)==1);x>>=1;
            case 14: set(currentLength++,(x&1)==1);x>>=1;
            case 13: set(currentLength++,(x&1)==1);x>>=1;
            case 12: set(currentLength++,(x&1)==1);x>>=1;
            case 11: set(currentLength++,(x&1)==1);x>>=1;
            case 10: set(currentLength++,(x&1)==1);x>>=1;
            case 9: set(currentLength++,(x&1)==1);x>>=1;
            case 8: set(currentLength++,(x&1)==1);x>>=1;
            case 7: set(currentLength++,(x&1)==1);x>>=1;
            case 6: set(currentLength++,(x&1)==1);x>>=1;
            case 5: set(currentLength++,(x&1)==1);x>>=1;
            case 4: set(currentLength++,(x&1)==1);x>>=1;
            case 3: set(currentLength++,(x&1)==1);x>>=1;
            case 2: set(currentLength++,(x&1)==1);x>>=1;
            case 1: set(currentLength++,(x&1)==1);x>>=1;
            default:
                break;
        }
    }
    public void push(short x,int q){
        q = Math.min(16,q);
        switch (q){
            case 16: set(currentLength++,(x&1)==1);x>>=1;
            case 15: set(currentLength++,(x&1)==1);x>>=1;
            case 14: set(currentLength++,(x&1)==1);x>>=1;
            case 13: set(currentLength++,(x&1)==1);x>>=1;
            case 12: set(currentLength++,(x&1)==1);x>>=1;
            case 11: set(currentLength++,(x&1)==1);x>>=1;
            case 10: set(currentLength++,(x&1)==1);x>>=1;
            case 9: set(currentLength++,(x&1)==1);x>>=1;
            case 8: set(currentLength++,(x&1)==1);x>>=1;
            case 7: set(currentLength++,(x&1)==1);x>>=1;
            case 6: set(currentLength++,(x&1)==1);x>>=1;
            case 5: set(currentLength++,(x&1)==1);x>>=1;
            case 4: set(currentLength++,(x&1)==1);x>>=1;
            case 3: set(currentLength++,(x&1)==1);x>>=1;
            case 2: set(currentLength++,(x&1)==1);x>>=1;
            case 1: set(currentLength++,(x&1)==1);x>>=1;
            default:
                break;
        }
    }
    public void push(byte x,int q){
        q = Math.min(8,q);
        switch (q){
            case 8: set(currentLength++,(x&1)==1);x>>=1;
            case 7: set(currentLength++,(x&1)==1);x>>=1;
            case 6: set(currentLength++,(x&1)==1);x>>=1;
            case 5: set(currentLength++,(x&1)==1);x>>=1;
            case 4: set(currentLength++,(x&1)==1);x>>=1;
            case 3: set(currentLength++,(x&1)==1);x>>=1;
            case 2: set(currentLength++,(x&1)==1);x>>=1;
            case 1: set(currentLength++,(x&1)==1);x>>=1;
            default:
                break;
        }
    }
}
