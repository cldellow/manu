package com.cldellow.manu.format;

import org.junit.Test;

import static org.junit.Assert.*;

public class CommonTest {

    @Test(expected=IllegalArgumentException.class)
    public void getEncoder() {
        Common.getEncoder(123);
    }
}