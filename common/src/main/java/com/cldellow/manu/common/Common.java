package com.cldellow.manu.common;

public class Common {
    public String getFile(String path) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource(path).getFile();
    }

    public static boolean contains(String[] haystack, String needle) {
        for(int i = 0; i < haystack.length; i++)
            if(haystack[i].equals(needle))
                return true;
        return false;
    }
}
