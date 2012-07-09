/*
 * TODO: enter meaningful info
 */

import dapij.InstanceCreationTracker;

/**
 *
 * @author emszy
 */
public class HelloAzura {

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
        System.out.println(sq);
        return sq;
    }

    public void azura() {
        System.out.println("Hello Azura");  /* Azura is the name of my dog */
    }

    public static void main(String args[]) {
        System.out.println("BEGIN MAIN");
        HelloAzura ha = new HelloAzura('a');  /* create object to test agent */
        ha.azura();
        ha.i = 1;
        System.out.println(ha.i);

        System.out.println("Concurrent Map Size: " +
                String.valueOf(InstanceCreationTracker.INSTANCE.getSize()));
    }
}
