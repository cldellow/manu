package com.cldellow.manu.cli;

enum FileParserState {
    ROW_START,
    IN_KEY,
    FIELD_START,
    INTEGER_REQUIRED,
    INTEGER_OPTIONAL
}
