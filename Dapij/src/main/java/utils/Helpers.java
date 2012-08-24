package utils;

import java.util.Arrays;

/**
 * A class containing useful utility methods.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class Helpers {

    private Helpers() {}

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
