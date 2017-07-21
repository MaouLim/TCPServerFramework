package bupt.util;

import java.util.Arrays;

public class ArrayUtil {

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(
                first, first.length + second.length
        );

        System.arraycopy(
                second, 0, result, first.length, second.length
        );
        return result;
    }

    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(
                first, first.length + second.length
        );

        System.arraycopy(
                second, 0, result, first.length, second.length
        );
        return result;
    }
}
