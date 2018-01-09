package com.cldellow.manu.format;

import java.util.HashMap;
import java.util.Map;

public enum FieldType {
    INT(0),
    FLOAT(1);

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