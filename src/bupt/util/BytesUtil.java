package bupt.util;

public class BytesUtil {

    public static byte[] getBytes(int num) {
        return new byte[] {
                (byte) ((num >>> 24) & 0xFF),
                (byte) ((num >>> 16) & 0xFF),
                (byte) ((num >>>  8) & 0xFF),
                (byte) ((num >>>  0) & 0xFF)
        };
    }
}
