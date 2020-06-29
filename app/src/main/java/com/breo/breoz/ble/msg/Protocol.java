package com.breo.breoz.ble.msg;


import android.util.Log;


import com.breo.breoz.ble.msg.base.PackageMsg;

import java.util.LinkedList;
import java.util.List;

public class Protocol {
    public static final byte STX = 0x4E;
    public static final byte VER = 0x01;
    public static final byte EOI = 0x4E;

    /**
     * 进行转义byte[]
     *
     * @return
     */
    public static byte[] escapeMsg(byte[] values, int start, int length) {
        List<Byte> cacheBytes = new LinkedList<>();
        for (int i = start; i < start + length; i++) {
            byte v = values[i];
            if (v == PackageMsg.ESCAPE_CODE) {
                cacheBytes.add(PackageMsg.ESCAPED_CODE_1);
                cacheBytes.add(PackageMsg.ESCAPED_CODE_2);
            } else if (v == PackageMsg.ESCAPED_CODE_1) {
                cacheBytes.add(PackageMsg.ESCAPED_CODE_1);
                cacheBytes.add(PackageMsg.ESCAPED_CODE_1_ESCAPED_2);
            } else {
                cacheBytes.add(values[i]);
            }
        }
        return listToArrayByte(cacheBytes);
    }

    public static byte[] escapeMsg(byte[] values) {
        List<Byte> cacheBytes = new LinkedList<>();
        cacheBytes.add(values[0]);
        for (int i = 1; i < values.length - 2; i++) {
            byte v = values[i];
            if (v == PackageMsg.ESCAPE_CODE) {
                cacheBytes.add(PackageMsg.ESCAPED_CODE_1);
                cacheBytes.add(PackageMsg.ESCAPED_CODE_2);
            } else if (v == PackageMsg.ESCAPED_CODE_1) {
                cacheBytes.add(PackageMsg.ESCAPED_CODE_1);
                cacheBytes.add(PackageMsg.ESCAPED_CODE_1_ESCAPED_2);
            } else {
                cacheBytes.add(values[i]);
            }
        }
        cacheBytes.add(values[values.length - 2]);
        cacheBytes.add(values[values.length - 1]);

        return listToArrayByte(cacheBytes);
    }

    /**
     * 进行反转义byte[]
     *
     * @return
     */
    public static byte[] unescapeMsg(byte[] values, int start, int length) throws RuntimeException {
        List<Byte> cacheBytes = new LinkedList<>();
        for (int i = start; i < start + length; i++) {
            byte v = values[i];
            if (v == PackageMsg.ESCAPED_CODE_1) {
                if (i + 1 < start + length) {
                    byte v1 = values[i + 1];
                    if (v1 == PackageMsg.ESCAPED_CODE_2) {
                        cacheBytes.add(PackageMsg.ESCAPE_CODE);
                        i++;
                    } else if (v1 == PackageMsg.ESCAPED_CODE_1_ESCAPED_2) {
                        cacheBytes.add(PackageMsg.ESCAPED_CODE_1);
                        i++;
                    }
                } else {
                    throw new RuntimeException("出现非法字符 0x5E 在整个字符串末尾");
                }
            } else if (v == PackageMsg.ESCAPE_CODE) {
                throw new RuntimeException("出现非法字符 0x4E ");
            } else {
                cacheBytes.add(values[i]);
            }
        }
        return listToArrayByte(cacheBytes);
    }

    private static byte[] listToArrayByte(List<Byte> byteList) {
        byte[] newBytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            newBytes[i] = byteList.get(i);
        }
        return newBytes;
    }

    public static void checkHex(byte hex, byte checkHex) throws RuntimeException {
        Log.d("checkHex", "hex=" + hex + "|checkHex=" + checkHex);

        if (hex != checkHex) {
            throw new RuntimeException("msg check Error");
        }
    }

    private static void checkErrorCode(byte[] bytes) {
        for (byte aByte : bytes) {
            if (aByte == PackageMsg.ESCAPE_CODE) {
                throw new RuntimeException("msg has error code");
            }
        }
    }

    public static byte getChksum(byte[] pkg, int start, int length) {
        int checksum = 0x00000000;
        for (int i = start; i < start + length; i++) {
            checksum = checksum + (pkg[i] & 0x000000FF);
        }
        checksum = ~checksum;
        return (byte) checksum;
    }
}
