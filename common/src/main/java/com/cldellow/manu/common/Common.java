package com.cldellow.manu.common;

public class Common {
    public String getFile(String path) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource(path).getFile();
    }
}
