package utils;

/**
 * A singleton managing client settings.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class Message {

    private static boolean verbose = false;


    public static void turnOnNetworkMessages() {
        verbose = true;
    }

    /* TODO: Use logger & remove this. */
    public static void println(String msg) {


        if (verbose) {
            System.out.println(Thread.currentThread().getName() + ": " + msg);
        }
    }
}