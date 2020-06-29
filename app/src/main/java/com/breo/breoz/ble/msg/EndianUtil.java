package com.breo.breoz.ble.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2015/12/4.
 */
public class EndianUtil {

    public static final String CHARSET = "UTF-8";

    public static boolean readBoolean(DataInputStream is) throws IOException {
        return is.readBoolean();
    }

    public static byte readByte(DataInputStream is) throws IOException {
        return is.readByte();
    }

    public static byte[] readBytes(DataInputStream is, int i) throws IOException {
        byte[] data = new byte[i];
        is.readFully(data);
        return data;
    }

    public static char readChar(DataInputStream is) throws IOException {
        return Character.reverseBytes(is.readChar());
    }

    public static char[] readChars(DataInputStream is, int length) throws IOException {
        char[] msgs = new char[length];
        for (int i = 0; i < length; i++) {
            msgs[i] = readChar(is);
        }
        return msgs;
    }

    public static double readDouble(DataInputStream is) throws IOException {
        return Double.longBitsToDouble(readLong(is));
    }

    public static float readFloat(DataInputStream is) throws IOException {
        return Float.intBitsToFloat(readInt(is));
    }

    public static int readInt(DataInputStream is) throws IOException {
        return Integer.reverseBytes(is.readInt());
    }

    public static int[] readInts(DataInputStream is, int length) throws IOException {
        int[] msgs = new int[length];
        for (int i = 0; i < length; i++) {
            msgs[i] = readInt(is);
        }
        return msgs;
    }

    public static long readLong(DataInputStream is) throws IOException {
        return Long.reverseBytes(is.readLong());
    }

    public static short readShort(DataInputStream is) throws IOException {
        return Short.reverseBytes(is.readShort());
    }

    public static String readUTF(DataInputStream is) throws IOException {
        short s = readShort(is);
        byte[] str = new byte[s];
        is.readFully(str);
        return new String(str, CHARSET);
    }

    public static void writeBoolean(DataOutputStream os, boolean b) throws IOException {
        os.writeBoolean(b);
    }

    public static void writeByte(DataOutputStream os, int b) throws IOException {
        os.writeByte(b);
    }

    public static void writeBytes(DataOutputStream os, byte[] data, int start, int length) throws IOException {
        os.write(data, start, length);
    }

    public static void writeChar(DataOutputStream os, char b) throws IOException {
        writeShort(os, (short) b);
    }

    public static void writeChars(DataOutputStream os, char[] b, int start, int length) throws IOException {
        for (int i = start; i < length + start; i++) {
            writeShort(os, (short) b[i]);
        }
    }

    public static void writeDouble(DataOutputStream os, double d) throws IOException {
        writeLong(os, Double.doubleToLongBits(d));
    }

    public static void writeFloat(DataOutputStream os, float f) throws IOException {
        writeInt(os, Float.floatToIntBits(f));
    }

    public static void writeInt(DataOutputStream os, int i) throws IOException {
        os.writeInt(Integer.reverseBytes(i));
    }

    public static void writeInts(DataOutputStream os, int[] msgs, int start, int length) throws IOException {
        for (int i = start; i < length + start; i++) {
            writeInt(os, msgs[i]);
        }
    }

    public static void writeLong(DataOutputStream os, long l) throws IOException {
        os.writeLong(Long.reverseBytes(l));
    }

    public static void writeShort(DataOutputStream os, short s) throws IOException {
        os.writeShort(Short.reverseBytes(s));
    }

    public static void writeUTF(DataOutputStream os, String str) throws IOException {
        byte[] data = str.getBytes(CHARSET);
        writeShort(os, (short) data.length);
        os.write(data);
    }

    public static byte[] int2Bytes(int i) {
        return ByteBuffer.allocate(4).putInt(Integer.reverseBytes(i)).array();
    }

    public static byte[] shorts2Bytes(short[] ss) {
        byte[] bytes = new byte[ss.length * 2];
        for (int i = 0; i < ss.length; i++) {
            byte[] innerBytes = short2Bytes(ss[i]);
            bytes[i * 2] = innerBytes[0];
            bytes[i * 2 + 1] = innerBytes[1];
        }
        return bytes;
    }

    public static byte[] short2Bytes(short s) {
        return ByteBuffer.allocate(2).putShort(Short.reverseBytes(s)).array();
    }

    public static byte[] float2Bytes(float f) {
        return ByteBuffer.allocate(4).putInt(Integer.reverseBytes(Float.floatToIntBits(f))).array();
    }
}

