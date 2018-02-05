package com.cldellow.manu.cli;

class FieldDef {
    private KeyKind keyKind = KeyKind.KEY;
    private FieldKind fieldKind = FieldKind.INT;
    private String name = null;
    private String file = null;

    public KeyKind getKeyKind() { return this.keyKind; }
    public FieldKind getFieldKind() { return this.fieldKind; }
    public String getName() { return this.name; }
    public String getFile() { return this.file; }

    public void setKeyKind(KeyKind keyKind) { this.keyKind = keyKind; }
    public void setFieldKind(FieldKind fieldKind) { this.fieldKind = fieldKind; }
    public void setName(String name) { this.name = name; }
    public void setFile(String file) { this.file = file; }
}
