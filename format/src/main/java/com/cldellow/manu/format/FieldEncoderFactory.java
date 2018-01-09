package com.cldellow.manu.format;

public interface FieldEncoderFactory {
    public FieldType getFieldType();

    public FieldEncoder getFieldEncoder(Object[] values, boolean[] nulls);
}
