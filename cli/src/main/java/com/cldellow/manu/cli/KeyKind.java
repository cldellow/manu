package com.cldellow.manu.cli;

enum KeyKind {
    KEY(0),
    ID(1);

    private int value;
    private KeyKind(int value) {
        this.value = value;
    }
}
