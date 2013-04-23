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

    static private int makeUnsignedMedium(byte b2, byte b1, byte b0) {
        return ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff));
    }

    static private int makeInt(byte b3, byte b2, byte b1, byte b0) {
        return (((b3) << 24) | //
                ((b2 & 0xff) << 16) | //
                ((b1 & 0xff) << 8) | //
        ((b0 & 0xff)));
    }
}
