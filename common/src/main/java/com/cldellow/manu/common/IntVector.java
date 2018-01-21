package com.cldellow.manu.common;

public class IntVector {
    // like Vector<Integer>, but memory efficient
    private int[] arr;
    private int size = 0;

    public IntVector() {
        this(1024);
    }

    public IntVector(int initialSize) {
        if(initialSize < 1)
            throw new IllegalArgumentException("initialSize " + initialSize + " is out of bounds, must be > 0");
        arr = new int[initialSize];
    }

    public void add(int i) {
        if(size == arr.length)
            grow();

        arr[size++] = i;
    }

    public int get(int i) {
        checkIndex(i);
        return arr[i];
    }
    public void set(int i, int v) {
        checkIndex(i);
        arr[i] = v;
    }
    public int getSize() { return size; }
    public int[] getArray() { return arr; }

    private void checkIndex(int i) {
        if(i >= size)
            throw new ArrayIndexOutOfBoundsException("index " + i + " is beyond size of " + size);
    }

    private void grow() {
        int[] newArr = new int[arr.length * 2];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        arr = newArr;
    }
}
