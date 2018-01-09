package com.cldellow.manu.format;

import java.util.HashMap;
import java.util.Map;

public enum FieldType {
    INT(0),     // an int
    FIXED1(1),  // an int, divided by 10 on output
    FIXED2(2);  // an int, divided by 100 on output

    private int value;
    private static Map map = new HashMap();

    private FieldType(int value) {
        this.value = value;
    }

    static {
        for (FieldType fieldType : FieldType.values()) {
            map.put(fieldType.value, fieldType);
        }
    }

    public static FieldType valueOf(int fieldType) {
        return (FieldType) map.get(fieldType);
    }

    public int getValue() {
        return value;
    }
}