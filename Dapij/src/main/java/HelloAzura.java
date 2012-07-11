/*
 * TODO: enter meaningful info
 */

import dapij.InstanceCreationTracker;
import java.util.Stack;

/**
 *
 * @author emszy
 */
public class HelloAzura {

    /*
    private int i = 7;

    public HelloAzura(int i) {
        this.i = i;
    }

    public HelloAzura(char c) {
        this(5);
        System.out.println(c);
    }

    public int square() {
        int sq = this.i * this.i;
        //System.out.println(sq);
        return sq;
    }
*/
    public int azura(int x) {
        return x;
        //System.out.println("Hello Azura");  /* Azura is the name of my dog */
    }

    public static void main(Object args[]) {
        HelloAzura ha = new HelloAzura();  
        ha.azura(2);
        
        /*
        System.out.println("BEGIN MAIN");
        HelloAzura ha = new HelloAzura('a');  
        ha.azura();
        ha.i = 1;
        System.out.println(ha.i);
        
        Stack stack = new Stack();
        stack.push(7);
        stack.push(new Object());
        System.out.println(stack.pop());
        System.out.println(stack.pop());

        System.out.println("Concurrent Map Size: " +
                String.valueOf(InstanceCreationTracker.INSTANCE.getSize()));
                * 
                */
    }
}
