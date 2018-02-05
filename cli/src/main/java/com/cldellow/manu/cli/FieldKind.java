package com.cldellow.manu.cli;

import com.cldellow.manu.format.FieldType;

enum FieldKind {
    INT(0, FieldType.INT),
    FIXED1(1, FieldType.FIXED1),
    FIXED2(2, FieldType.FIXED2),
    LOSSY(3, FieldType.INT);

    private final int value;
    private final FieldType fieldType;
    private FieldKind(int value, FieldType fieldType) {
        this.value = value;
        this.fieldType = fieldType;
    }

    public FieldType getFieldType() { return this.fieldType; }
}
