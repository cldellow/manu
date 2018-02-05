package com.cldellow.manu.format;

import java.util.HashMap;
import java.util.Map;

/**
 * The kind of number stored in a field.
 */
public enum FieldType {
    /**
     * An integer, for example, 42.
     */
    INT(0),
    /**
     * A number with one decimal point of precision, for example, 4.2.
     */
    FIXED1(1),
    /**
     * A number with two decimal points of precision, for example, 0.42.
     */
    FIXED2(2);

    private static Map map = new HashMap();

    static {
        for (FieldType fieldType : FieldType.values()) {
            map.put(fieldType.value, fieldType);
        }
    }

    private int value;

    private FieldType(int value) {
        this.value = value;
    }

    public static FieldType valueOf(int fieldType) {
        return (FieldType) map.get(fieldType);
    }

    public int getValue() {
        return value;
    }
}