package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BERUtils {
    public static void writeLength(ByteArrayOutputStream baos, int length) {
        if (length < 128) {
            baos.write(length);
        } else {
            int numBytes = 1;
            if (length > 0xFF)
                numBytes++;
            if (length > 0xFFFF)
                numBytes++;
            if (length > 0xFFFFFF)
                numBytes++;

            baos.write(0x80 | numBytes);
            for (int i = numBytes - 1; i >= 0; i--) {
                baos.write((length >> (8 * i)) & 0xFF);
            }
        }
    }

    public static int getLength(byte[] data, int pos) {
        int firstByte = data[pos] & 0xFF;
        if ((firstByte & 0x80) == 0) {
            return firstByte;
        } else {
            int numBytes = firstByte & 0x7F;
            int length = 0;
            for (int i = 1; i <= numBytes; i++) {
                length = (length << 8) | (data[pos + i] & 0xFF);
            }
            return length;
        }
    }

    public static int getLengthLength(byte firstLengthByte) {
        return ((firstLengthByte & 0x80) == 0) ? 1 : (1 + (firstLengthByte & 0x7F));
    }
}