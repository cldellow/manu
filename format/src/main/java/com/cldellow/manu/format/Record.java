package com.cldellow.manu.format;

public interface Record {
    public int getId();
    public int[] getValues(int field);

    public FieldEncoder getEncoder(int field);
}
