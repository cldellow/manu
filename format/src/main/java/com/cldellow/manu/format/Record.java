package com.cldellow.manu.format;

public interface Record {
    int getId();
    int[] getValues(int field);

    FieldEncoder getEncoder(int field);
}
