/*
 * TODO: enter sensible info
 */
// package xxx;

import dapij.ObjectCreationStats;

/**
 *
 * @author emszy
 */
public class HelloAzura {

    ObjectCreationStats s = null;
    
    public static int square(int input) {
        return input * input;
    }

    public HelloAzura(Class c, String m, int off, long id) {
       // this.s = new ObjectCreationStats(c, m, off, id);
 
    }

    public static void azura() {
        System.out.println("Hello Azura"); /* Azura is the name of my dog */
    }
    
    public static void main(String args[]) {
        Class c = HelloAzura.class;
        String m = "main";
        int off = 5;
        long id = 30;
        HelloAzura ha = new HelloAzura(c, m, off, id);
        System.out.println("The square of 5 is " + square(5));
        azura();
    }
}
