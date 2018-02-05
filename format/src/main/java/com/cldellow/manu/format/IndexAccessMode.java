package com.cldellow.manu.format;

/**
 * The access mode to use for the SQLite index file.
 */
public enum IndexAccessMode {
    /**
     * Read only.
     */
    READ_ONLY,
    /**
     * Read-write, with a recovery journal in case of abnormal program termination.
     */
    READ_WRITE_SAFE,
    /**
     * Read-write, with no crash protection -- faster, but may corrupt the index on abnormal program termination.
     */
    READ_WRITE_UNSAFE
}
