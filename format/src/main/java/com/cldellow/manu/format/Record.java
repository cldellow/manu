package com.cldellow.manu.format;

public interface Record {
    public int[] getValues(int field);
    public FieldEncoder getEncoder(int field);
}
