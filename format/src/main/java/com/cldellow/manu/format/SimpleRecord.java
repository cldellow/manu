package com.cldellow.manu.format;

public class SimpleRecord implements Record {
    private final int id;
    private final FieldEncoder[] encoders;
    private final int[][] values;

    public SimpleRecord(int id, FieldEncoder[] encoders, int[][] values) {
        this.id = id;
        this.encoders = encoders;
        this.values = values;
    }

    public int getId() {
        return id;
    }

    public int[] getValues(int field) {
        return values[field];
    }

    public FieldEncoder getEncoder(int field) {
        return encoders[field];
    }
}
