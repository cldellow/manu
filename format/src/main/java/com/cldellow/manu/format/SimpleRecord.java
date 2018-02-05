package com.cldellow.manu.format;

/**
 * A mock implementation of {@link Record}, useful for testing.
 */
public class SimpleRecord implements Record {
    private final int id;
    private final FieldEncoder[] encoders;
    private final int[][] values;

    public SimpleRecord(int id, FieldEncoder[] encoders, int[][] values) {
        if(id < 0)
            throw new IllegalArgumentException(String.format(
                    "id %d cannot be negative",
                    id));
        if(encoders.length != values.length)
            throw new IllegalArgumentException(String.format(
                    "%d field encoders but %d sets of field values",
                    encoders.length,
                    values.length));

        if(encoders.length == 0)
            throw new IllegalArgumentException("a record with no fields makes no sense");

        int numDatapoints = values[0].length;
        for(int i = 0; i < values.length; i++)
            if(values[i].length != numDatapoints)
                throw new IllegalArgumentException(String.format(
                        "field %d has %d datapoints, expected %d",
                        i,
                        values[i].length,
                        numDatapoints));
        this.id = id;
        this.encoders = encoders;
        this.values = values;
    }

    public int getId() {
        return id;
    }

    public int[] getValues(int field) {
        return values[field];
    }

    public FieldEncoder getEncoder(int field) {
        return encoders[field];
    }
}
