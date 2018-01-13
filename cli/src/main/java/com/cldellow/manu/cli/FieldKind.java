package com.cldellow.manu.cli;

public enum FieldKind {
    INT(0),
    FIXED1(1),
    FIXED2(2),
    LOSSY(3);

    private int value;
    private FieldKind(int value) {
        this.value = value;
    }
}
