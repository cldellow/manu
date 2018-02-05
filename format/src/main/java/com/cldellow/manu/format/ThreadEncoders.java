package com.cldellow.manu.format;

class ThreadEncoders {
    private static final ThreadLocal<FieldEncoder[]> encoders =
            new ThreadLocal<FieldEncoder[]>() {
                @Override protected FieldEncoder[] initialValue() {
                    return Common.getEncoders();
                }
            };

    // Returns the current thread's unique ID, assigning it if necessary
    public static FieldEncoder[] get() {
        return encoders.get();
    }
}