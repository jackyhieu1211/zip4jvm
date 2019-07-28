package com.cop.zip4j.crypto.pkware;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@SuppressWarnings({ "MethodCanBeVariableArityMethod", "NewMethodNamingConvention" })
public class StandardEngine {

    private static final int[] CRC_TABLE = new int[256];

    private final int[] keys;

    static {
        for (int i = 0, r = i; i < CRC_TABLE.length; i++, r = i) {
            for (int j = 0; j < 8; j++)
                r = (r & 1) == 1 ? (r >>> 1) ^ 0xedb88320 : (r >>> 1);

            CRC_TABLE[i] = r;
        }
    }

    public StandardEngine(char[] password) {
        keys = createKeys(password);
    }

    /** see 6.1.5 */
    private static int[] createKeys(char[] password) {
        int[] keys = { 0x12345678, 0x23456789, 0x34567890 };

        for (int i = 0; i < ArrayUtils.getLength(password); i++)
            updateKeys(keys, (byte)(password[i] & 0xFF));

        return keys;
    }

    private static void updateKeys(int[] keys, byte val) {
        keys[0] = crc32(keys[0], val);
        keys[1] = (keys[1] + (keys[0] & 0xFF)) * 0x8088405 + 1;
        keys[2] = crc32(keys[2], (byte)(keys[1] >> 24));
    }

    private static int crc32(int crc, byte val) {
        return (crc >>> 8) ^ CRC_TABLE[(crc ^ val) & 0xFF];
    }

    public void updateKeys(byte charAt) {
        updateKeys(keys, charAt);
    }

    public byte decryptByte() {
        int tmp = keys[2] | 2;
        return (byte)((tmp * (tmp ^ 1)) >>> 8);
    }

    private byte stream() {
        int tmp = keys[2] | 3;
        return (byte)((tmp * (tmp ^ 1)) >>> 8);
    }

    public byte encode(byte b) {
        byte cipher = (byte)(stream() ^ b & 0xFF);
        updateKeys(b);
        return cipher;
    }

}
