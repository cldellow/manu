package com.cldellow.manu.format;

public class SimpleRecord implements Record {
    public FieldEncoder[] encoders;
    public int[][] values;

    public SimpleRecord(FieldEncoder[] encoders, int[][] values) {
        this.encoders = encoders;
        this.values = values;
    }


    public int[] getValues(int field) {
        return values[field];
    }

    public FieldEncoder getEncoder(int field) {
        return encoders[field];
    }
}
