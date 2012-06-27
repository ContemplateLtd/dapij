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

    private int i = 1;
    
    public static int square(int input) {
        return input * input;
    }

    public static void azura() {
        System.out.println("Hello Azura");  /* Azura is the name of my dog */
    }
    
    public static void main(String args[]) {
        HelloAzura ha = new HelloAzura();   /* create object to test agent */
        // System.out.println("The square of 5 is " + square(5));
        azura();
    }
}
