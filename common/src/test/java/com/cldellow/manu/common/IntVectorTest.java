package com.cldellow.manu.common;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class IntVectorTest {
    @Test
    public void testVector() {
        IntVector i = new IntVector();
        assertEquals(0, i.getSize());

        i.add(123);
        assertEquals(1, i.getSize());
        assertEquals(123, i.get(0));
        int[] arr = i.getArray();
        assertEquals(123, arr[0]);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetOOBE() {
        IntVector i = new IntVector();
        i.get(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVectorBadInitialSize() {
        IntVector i = new IntVector(0);
    }

    @Test
    public void testVectorGrow() {
        IntVector i = new IntVector(1);
        assertEquals(0, i.getSize());

        i.add(123);
        assertEquals(1, i.getSize());
        assertEquals(123, i.get(0));

        i.add(245);
        assertEquals(2, i.getSize());
        assertEquals(123, i.get(0));
        assertEquals(245, i.get(1));
    }

    @Test
    public void testVectorSet() {
        IntVector i = new IntVector();
        i.add(123);
        i.set(0, 245);
        assertEquals(245, i.get(0));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testVectorSetOOBE() {
        IntVector i = new IntVector();
        i.set(0, 245);
    }

    @Property public void testLikeArray(int[] ints) {
        IntVector iv = new IntVector();
        for(int i = 0; i < ints.length; i++) {
            assertEquals(i, iv.getSize());
            iv.add(ints[i]);
            assertEquals(ints[i], iv.get(i));
            assertEquals(i + 1, iv.getSize());
        }
    }
}