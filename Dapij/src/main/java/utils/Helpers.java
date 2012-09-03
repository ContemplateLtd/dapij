package utils;

import java.util.Arrays;

/**
 * A class containing some useful utility methods.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class Helpers {

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
}
