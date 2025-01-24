package eu.infolead.jtk.fp.tailrec;

public class Factorial {
    public static int factorialRec(final int number){
        if(number==1){
            return 1;
        }else{
            return number*factorialRec(number-1);
        }
    }
    public static void main(final String[] args){
        System.out.println(factorialRec(5));
    }
}
