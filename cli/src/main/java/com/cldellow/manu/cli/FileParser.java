package com.cldellow.manu.cli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FileParser {
    private RandomAccessFile raf;
    private int numFields = 0;
    private ByteBuffer buffer;
    private String fileName;

    public FileParser(String fileName) throws FileNotFoundException, IOException {
        this.fileName = fileName;
        this.raf = new RandomAccessFile(fileName, "r");
        FileChannel channel = raf.getChannel();
        buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, raf.length());

        // Read the first line to determine how many fields there are
        boolean done = false;
        while (!done && buffer.hasRemaining()) {
            byte b = buffer.get();
            if (b == '\t')
                numFields++;
            if (b == '\n') {
                numFields++;
                done = true;
            }
        }

        buffer.position(0);
    }

    public int getNumFields() {
        return numFields;
    }

    public void close() throws IOException {
        raf.close();
    }

    public Iterator<RowIterator.Row> getIterator() {
        return new RowIterator();
    }

    class RowIterator implements Iterator<RowIterator.Row> {
        byte[] bytes = new byte[1024];
        int[] ints = new int[getNumFields() - 1];
        int currentRow = 0;
        RowIterator() {
            buffer.position(0);
        }

        public boolean hasNext() {
            return buffer.hasRemaining();
        }

        public Row next() {
            return new Row();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        class Row {
            String key = null;
            byte next = -1;

            public Row() {
                boolean done = false;
                int howMany = 0;

                while(!done && buffer.hasRemaining()) {
                    next = buffer.get();
                    done = next == '\t' || next == '\n';
                    if(!done) {
                        bytes[howMany] = next;
                        howMany++;
                    }
                }

                key = new String(bytes, 0, howMany, Charset.forName("UTF-8"));
                if(next == '\t') next = buffer.get();

                int i = 0;
                int numInts = getNumFields() - 1;
                while(i < numInts) {
                    int value = 0;
                    if(next >= '0' && next <= '9') {
                        while(next >= '0' && next <= '9') {
                            value *= 10;
                            value += next - '0';
                            next = buffer.get();
                        }
                    } else {
                        throw new IllegalArgumentException(String.format(
                                "%s: row %d malformed: only %d columns, expected %d",
                                fileName, currentRow, i, numInts));
                    }
                    ints[i] = value;
                    i++;
                    if(next == '\t' && i < numInts) {
                        next = buffer.get();
                    }
                }
                if(next != '\n')
                    throw new IllegalArgumentException(String.format(
                            "%s: row %d malformed: at least %d columns, expected %d",
                            fileName, currentRow, i+1, numInts));
                currentRow++;


            }

            public String getKey() {
                return key;
            }

            public int[] getInts() {
                return ints;
            }
        }
    }
}
