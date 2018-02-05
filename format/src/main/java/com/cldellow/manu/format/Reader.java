package com.cldellow.manu.format;

import org.joda.time.DateTime;

public interface Reader {
    int getNullValue();
    Interval getInterval();
    String getFieldName(int i);
    FieldType getFieldType(int i);

    int getRecordOffset();
    int getNumRecords();
    int getNumFields();
    RecordIterator getRecords();

    DateTime getFrom();
    DateTime getTo();

    int getNumDatapoints();

    String getFileName();

    Record get(int id) throws Exception;
}
