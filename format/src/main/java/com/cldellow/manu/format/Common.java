package com.cldellow.manu.format;

import me.lemire.integercompression.differential.IntegratedBinaryPacking;
import me.lemire.integercompression.differential.IntegratedComposition;
import me.lemire.integercompression.differential.IntegratedIntegerCODEC;
import me.lemire.integercompression.differential.IntegratedVariableByte;

import java.util.Arrays;
import java.util.Iterator;

class Common {
    public static final int INT_ARRAY_SIZE = 131072;
    public static short getVersion() { return 1; }
    public static IntegratedIntegerCODEC getRowListCodec() {
        return new
                IntegratedComposition(
                new IntegratedBinaryPacking(),
                new IntegratedVariableByte());
    }

    public static FieldEncoder getEncoder(int id) {
        switch(id) {
            case 0:
                return new CopyEncoder();
            case 1:
                return new PFOREncoder();
            case 2:
                return new AverageEncoder();
            case 3:
                return new SingleValueEncoder();
            default:
                throw new IllegalArgumentException(String.format("unknown encoder: %d", id));
        }
    }

    public static FieldEncoder[] getEncoders() {
        FieldEncoder[] encoders = new FieldEncoder[4];
        for(int i = 0; i < encoders.length; i++)
            encoders[i] = getEncoder(i);

        return encoders;
    }

    public static<T> Iterator<T> nonNullIterator(T[] items) {
        int cnt = 0;
        for(int i = 0; i < items.length; i++)
            if(items[i] != null)
                cnt++;

        T[] rv = Arrays.copyOf(items, cnt);
        cnt = 0;
        for(int i = 0; i < items.length; i++) {
            if(items[i] != null) {
                rv[cnt] = items[i];
                cnt++;
            }
        }

        return Arrays.asList(rv).iterator();
    }
}
