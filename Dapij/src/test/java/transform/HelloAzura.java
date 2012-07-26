package transform;

/*
 * TODO: This class is no longer needed, remove when a substitute is written
 * as a test
 */

/**
 *
 * @author emszy
 */
public class HelloAzura {

    private int i = 7;
    private char c;

    public HelloAzura(int i) {
        this.i = i;
    }

    public HelloAzura(char c) {
        this(5);
        this.c = c;
    }

    public int square() {
        int sq = this.i * this.i;
        System.out.println("Hello Azura::square(): " + sq);
        return sq;
    }

    public void azura() {
        /* Azura is the name of my dog */
        System.out.println("Hello Azura::azura(): " + String.valueOf(c));
    }

    public static void main(String args[]) {
        System.out.println("Hello Azura::main(): " +
                new String(String.valueOf(new Integer(5))));
        HelloAzura ha = new HelloAzura('a');  /* create object to test agent */
        ha.azura();
        ha.square();
        ha.i = 1;
    }
}