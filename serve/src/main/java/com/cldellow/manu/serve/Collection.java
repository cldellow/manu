package com.cldellow.manu.serve;

import com.cldellow.manu.format.Index;
import com.cldellow.manu.format.Interval;
import com.cldellow.manu.format.NotManuException;
import com.cldellow.manu.format.Reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;

public class Collection {
    public final String dir;
    public final Index index;
    public final Reader[] readers;

    public Collection(String dir) throws SQLException, FileNotFoundException, IOException, NotManuException {
        this.dir = dir;

        File keyFile = new File(new File(dir), "keys");
        if (!keyFile.exists())
            throw new IllegalArgumentException(String.format(
                    "%s: doesn't have a keys file", dir));
        this.index = new Index(keyFile.getAbsolutePath(), true);

        int numFiles = 0;
        File[] files = new File(dir).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith((".manu")))
                numFiles++;
        }

        this.readers = new Reader[numFiles];
        numFiles = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith((".manu")))
                readers[numFiles++] = new Reader(files[i].getAbsolutePath());
        }

        validateReaders(dir, readers);
    }

    public static void validateReaders(String dir, Reader[] readers) {
        if (readers.length == 0)
            throw new IllegalArgumentException(String.format(
                    "%s doesn't have any .manu files", dir));

        Interval interval = readers[0].interval;
        for(int i = 0; i < readers.length; i++) {
            if(interval != readers[i].interval) {
                throw new IllegalArgumentException(String.format(
                        "%s has interval %s but %s has interval %s",
                        readers[0].fileName,
                        readers[0].interval,
                        readers[i].fileName,
                        readers[i].interval));
            }
        }

        // Sort them chronologically
        Arrays.sort(readers, new ReaderComparator());
        for(int i = 1; i < readers.length; i++) {
            if(!readers[i-1].to.equals(readers[i].from))
                throw new IllegalArgumentException(String.format(
                        "%s: %s is not immediately after %s",
                        new File(dir).getName(), readers[i-1].fileName, readers[i].fileName));
        }
    }

    public static boolean isCollection(String dir) {
        File f = new File(dir);

        if (!f.isDirectory())
            return false;

        File[] files = f.listFiles();
        boolean hasKeys = false;
        boolean hasManu = false;
        for (int i = 0; i < files.length; i++) {
            hasKeys = hasKeys || files[i].getName().equals("keys");
            hasManu = hasManu || files[i].getName().endsWith(".manu");
        }

        return hasKeys && hasManu;
    }

    public void close() throws Exception {
        index.close();
    }

    private static class ReaderComparator implements Comparator<Reader> {
        @Override
        public int compare(Reader reader, Reader t1) {
            if(reader.epochMs == t1.epochMs)
                return 0;
            if(reader.epochMs > t1.epochMs)
                return 1;
            return -1;
        }
    }
}