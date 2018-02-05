package com.cldellow.manu.common;

/**
 * Like {@link java.util.Vector}, but avoids the overhead of boxing.
 */
public class IntVector {
    // like Vector<Integer>, but memory efficient
    private int[] arr;
    private int size = 0;

    /**
     * Constructs an empty IntVector with initial capacity of 1024.
     */
    public IntVector() {
        this(1024);
    }

    /**
     * Constructs an empty IntVector with the specified initial capacity.
     * @param initialCapacity The number of elements for which to pre-allocate memory.
     */
    public IntVector(int initialCapacity) {
        if(initialCapacity < 1)
            throw new IllegalArgumentException("initialCapacity " + initialCapacity + " is out of bounds, must be > 0");
        arr = new int[initialCapacity];
    }

    /**
     * Appends the given value to the end of the vector.
     * @param value The value to append.
     */
    public void add(int value) {
        if(size == arr.length)
            grow();

        arr[size++] = value;
    }

    /**
     * Returns the value at the given index.
     * @param index The index of the value sought.
     * @return The previously stored value.
     */
    public int get(int index) {
        checkIndex(index);
        return arr[index];
    }

    /**
     * Sets the element at the given index to the given value.
     *
     * @param index The index of the value to update.
     * @param value The new value for the index.
     */
    public void set(int index, int value) {
        checkIndex(index);
        arr[index] = value;
    }

    /**
     * Returns the number of elements in the vector.
     * @return The number of elements in the vector.
     */
    public int getSize() { return size; }

    /**
     * Returns the underlying array that backs this vector.
     *
     * Note that the array's length may be greater than the size of the
     * vector due to pre-allocation of space.
     * @return The backing array.
     */
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
