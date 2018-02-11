package com.cldellow.manu.format;

import org.junit.Ignore;
import org.junit.Test;

/* Dumping ground / harness for exploring performance of operations. */
public class ManuReaderBenchmarkTest {
    @Test
    @Ignore
    public void testGetPerf() throws Exception {
        int[] numReads = new int[]{10, 100, 1000, 10000, 100000, 300000000};

        for (int i = 0; i < numReads.length; i++)
            doReads(numReads[i]);
    }

    void doReads(int howMany) throws Exception {
        long now = System.currentTimeMillis();
        ManuReader mr = new ManuReader("/mnt/storage/pvs/2007/12/pagecounts-20071212-180000.manu");
        for (int i = 0; i < howMany; i++)
            mr.get(0);
        System.out.println(howMany + " reads took " + (System.currentTimeMillis() - now) + "ms");
        mr.close();
    }
}
