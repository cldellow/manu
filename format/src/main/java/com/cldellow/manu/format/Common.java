package com.cldellow.manu.format;

import me.lemire.integercompression.differential.IntegratedBinaryPacking;
import me.lemire.integercompression.differential.IntegratedComposition;
import me.lemire.integercompression.differential.IntegratedIntegerCODEC;
import me.lemire.integercompression.differential.IntegratedVariableByte;

class Common {
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
            default:
                throw new IllegalArgumentException(String.format("unknown encoder: %d", id));
        }
    }
}
