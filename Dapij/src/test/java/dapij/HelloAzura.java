/*
 * TODO: enter meaningful info
 */
package dapij;

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
        System.out.println(sq);
        return sq;
    }

    public void azura() {
        System.out.println("Hello Azura::azura(): "+String.valueOf(c));  /* Azura is the name of my dog */
    }

    public static void main(String args[]) {
        HelloAzura ha = new HelloAzura('a');  /* create object to test agent */;
        ha.azura();
        ha.i = 1;
    }
}