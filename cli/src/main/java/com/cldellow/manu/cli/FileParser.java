package com.cldellow.manu.cli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import com.cldellow.manu.cli.FileParserState;
import static com.cldellow.manu.cli.FileParserState.*;

public class FileParser {
    private final RandomAccessFile raf;
    private final ByteBuffer buffer;
    private final String fileName;
    private final int nullValue;
    private int numFields;

    public FileParser(String fileName, int nullValue) throws FileNotFoundException, IOException {
        this.fileName = fileName;
        this.nullValue = nullValue;
        this.raf = new RandomAccessFile(fileName, "r");
        FileChannel channel = raf.getChannel();
        buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, raf.length());

        // Read the first line to determine how many fields there are
        boolean done = false;
        byte b = 0;
        while (!done && buffer.hasRemaining()) {
            b = buffer.get();
            if (b == '\t')
                numFields++;
            if (b == '\n') {
                numFields++;
                done = true;
            }
        }

        if (b != '\n')
            throw new IllegalArgumentException(String.format(
                    "%s: first line lacks newline", fileName));

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
        private int[] ints = new int[getNumFields() - 1];
        private byte[] bytes = new byte[1024];
        private int currentRow = 0;

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

            public Row() {
                int keyLength = 0;
                byte next = -1;
                int currentField = 0;
                int scale = 1;
                int value = 0;
                FileParserState state = IN_KEY;
                while(state != ROW_START) {
                    if(!buffer.hasRemaining()) {
                        throw new IllegalArgumentException(String.format(
                                "%s: row %d ends unexpectedly",
                                fileName, currentRow));
                    }
                    switch(state) {
                        case IN_KEY:
                            next = buffer.get();
                            if(next == '\n' || next == '\t') {
                                key = new String(bytes, 0, keyLength, Charset.forName("UTF-8"));
                            } else {
                                bytes[keyLength++] = next;
                            }

                            if(next == '\n')
                                state = ROW_START;
                            if(next == '\t')
                                state = FIELD_START;
                            break;
                        case FIELD_START:
                            if(currentField >= ints.length)
                                throw new IllegalArgumentException(String.format(
                                        "%s: row %d has too many columns",
                                        fileName, currentRow));
                            scale = 1;
                            value = 0;
                            next = buffer.get();
                            if(next == '\n' || next == '\t')
                                ints[currentField++] = nullValue;
                            else if(next == '-') {
                                scale = -1;
                                state = INTEGER_REQUIRED;
                            } else {
                                buffer.position(buffer.position() - 1);
                                state = INTEGER_REQUIRED;
                            }

                            if(next == '\n')
                                state = ROW_START;

                            break;
                        case INTEGER_REQUIRED:
                            next = buffer.get();
                            if(next >= '0' && next <= '9') {
                                value = value * 10 + next - '0';
                                state = INTEGER_OPTIONAL;
                            } else {
                                throw new IllegalArgumentException(String.format(
                                        "%s: row %d column %d invalid",
                                        fileName, currentRow, currentField
                                ));
                            }
                            break;
                        case INTEGER_OPTIONAL:
                            next = buffer.get();
                            if(next >= '0' && next <= '9') {
                                value = value * 10 + next - '0';
                                state = INTEGER_OPTIONAL;
                            } else if(next == '\n' || next == '\t') {
                                ints[currentField++] = value * scale;
                            }
                            else {
                                throw new IllegalArgumentException(String.format(
                                        "%s: row %d column %d invalid",
                                        fileName, currentRow, currentField
                                ));
                            }

                            if(next == '\t')
                                state = FIELD_START;
                            if(next == '\n')
                                state = ROW_START;
                            break;
                        default:
                            throw new RuntimeException("impossible to get here");
                    }
                }

                if(currentField != ints.length)
                    throw new IllegalArgumentException(String.format(
                            "%s: row %d did not have enough fields",
                            fileName, currentRow));
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
