package utils;

import java.util.Arrays;
import java.util.Random;

/**
 * A class containing some useful utility methods.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class Helpers {

    private static Random random = new Random();

    private Helpers() {}

    /**
     * A generic method for gluing lists of objects.
     *
     * @param first
     *            the first array.
     * @param rest
     *            the rest of the arrays.
     * @return
     */
    public static <T> T[] arrCat(T[] first, T[]... rest) {
        int ttlLen = first.length;
        for (T[] arr : rest) {
            ttlLen += arr.length;
        }
        T[] result = Arrays.copyOf(first, ttlLen);
        int currOfst = first.length;
        for (T[] arr : rest) {
            System.arraycopy(arr, 0, result, currOfst, arr.length);
            currOfst += arr.length;
        }

        return result;
    }

    /**
     * A method for gluing byte lists.
     *
     * @param first
     *            the first array.
     * @param rest
     *            the rest of the arrays.
     * @return
     */
    public static byte[] arrCat(byte[] first, byte[]... rest) {
        int ttlLen = first.length;
        for (byte[] arr : rest) {
            ttlLen += arr.length;
        }
        byte[] result = Arrays.copyOf(first, ttlLen);
        int currOfst = first.length;
        for (byte[] arr : rest) {
            System.arraycopy(arr, 0, result, currOfst, arr.length);
            currOfst += arr.length;
        }

        return result;
    }

    /**
     * Returns a random number within the specified interval.
     *
     * @param min lower bound of interval.
     * @param max upper bound if interval.
     * @return the random number as an {@link Integer}.
     */
    public static int randomInt(int min, int max) {
        return min + random.nextInt(max - min);
    }

    public static long randomLong() {
        return random.nextLong();
    }
}


