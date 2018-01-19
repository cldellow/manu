package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

import java.nio.ByteBuffer;

public class AverageEncoder implements FieldEncoder {
    public static boolean eligible(int[] data) {
        int sum = 0;
        int numKnown = 0;
        int max = 0;
        for (int i = 0; i < data.length; i++) {
            if(data[i] < 0)
                return false;

            if (data[i] != 0) {
                numKnown++;
                sum += data[i];
                if (data[i] > max) max = data[i];
            }
        }
        return numKnown == 0 || (max < 100 && ((double) sum / numKnown) <= 10);
    }

    public int getId() {
        return 2;
    }

    public boolean isVariableLength() {
        return false;
    }

    public int getLength() {
        return 1;
    }

    public void encode(int[] data, int[] encoded, IntWrapper encodedLength) {
        byte[] rv = new byte[4];
        ByteBuffer buf = ByteBuffer.wrap(rv);

        int sum = 0;
        int numKnown = 0;
        int max = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] != 0) {
                numKnown++;
                sum += data[i];
                if (data[i] > max) max = data[i];
            }
        }

        buf.putShort((short) numKnown);
        buf.putShort((short) sum);
        buf.position(0);
        encoded[0] = buf.getInt();
        encodedLength.set(1);
    }

    public void decode(int[] encoded, int encodedLength, int[] data, IntWrapper dataLength) {
        byte[] rv = new byte[4];
        ByteBuffer buf = ByteBuffer.wrap(rv);
        buf.putInt(encoded[0]);
        buf.position(0);
        int numKnown = buf.getShort();
        int sum = buf.getShort();
        dataLength.set(data.length);

        int delta = 0;
        if(numKnown > 0)
            delta = sum / numKnown;

        int error = sum - (numKnown * delta);
        for(int i = 0; i < numKnown; i++) {
            data[i] = delta;
            if(error > 0) {
                data[i]++;
                error--;
            }
        }

        for(int i = numKnown; i < data.length; i++)
            data[i] = 0;
    }
}
