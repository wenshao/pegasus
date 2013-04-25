package com.alibaba.pegasus.net;

import java.nio.ByteBuffer;

public class Bits {

    public static int getIntL(ByteBuffer bb, int bi) {
        return makeInt(bb.get(bi + 3), bb.get(bi + 2), bb.get(bi + 1), bb.get(bi));
    }

    public static int getMediumL(ByteBuffer bb, int bi) {
        return makeMedium(bb.get(bi + 2), bb.get(bi + 1), bb.get(bi));
    }

    public static int getUnsignedMediumL(ByteBuffer bb, int bi) {
        return makeUnsignedMedium(bb.get(bi + 2), bb.get(bi + 1), bb.get(bi));
    }

    public static int getUnsignedShortL(ByteBuffer bb) {
        byte b0 = bb.get();
        byte b1 = bb.get();
        return makeUnsignedShort(b1, b0);
    }

    public static int getUnsignedMediumL(ByteBuffer bb) {
        byte b0 = bb.get();
        byte b1 = bb.get();
        byte b2 = bb.get();
        return makeUnsignedMedium(b2, b1, b0);
    }

    public static long getUnsignedIntL(ByteBuffer bb) {
        byte b0 = bb.get();
        byte b1 = bb.get();
        byte b2 = bb.get();
        byte b3 = bb.get();
        return makeUnsignedInt(b3, b2, b1, b0);
    }

    public static int getUnsignedMedium(ByteBuffer bb, int index) {
        return bb.get(index) & 0xff | //
               (bb.get(index + 1) & 0xff) << 8 | //
               (bb.get(index + 2) & 0xff) << 16;
    }

    public static int getUnsignedMediumB(ByteBuffer bb, int bi) {
        return makeUnsignedMedium(bb.get(bi), bb.get(bi + 1), bb.get(bi + 2));
    }

    // public int getUnsignedMedium(int index) {
    // return array[index] & 0xff |
    // (array[index + 1] & 0xff) << 8 |
    // (array[index + 2] & 0xff) << 16;
    // }

    public static short getShortL(ByteBuffer bb, int bi) {
        return makeShort(bb.get(bi + 1), bb.get(bi));
    }

    static private short makeShort(byte b1, byte b0) {
        return (short) ((b1 << 8) | (b0 & 0xff));
    }

    static private int makeMedium(byte b2, byte b1, byte b0) {
        return (b2 << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff));
    }

    static private int makeUnsignedShort(byte b1, byte b0) {
        return ((b1 & 0xff) << 8) | //
               ((b0 & 0xff));
    }

    static private int makeUnsignedMedium(byte b2, byte b1, byte b0) {
        return ((b2 & 0xff) << 16) | //
               ((b1 & 0xff) << 8) | //
               ((b0 & 0xff));
    }

    static private int makeInt(byte b3, byte b2, byte b1, byte b0) {
        return ((b3) << 24) | //
               ((b2 & 0xff) << 16) | //
               ((b1 & 0xff) << 8) | //
               ((b0 & 0xff));
    }

    static private long makeUnsignedInt(byte b3, byte b2, byte b1, byte b0) {
        return (long) ((b3) << 24 & 0xff) | //
               (long) ((b2 & 0xff) << 16) | //
               (long) ((b1 & 0xff) << 8) | //
               ((long) (b0 & 0xff));
    }
}
