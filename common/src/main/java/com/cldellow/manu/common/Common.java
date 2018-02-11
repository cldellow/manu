package com.cldellow.manu.common;

/**
 * Miscellaneous utilities.
 */
public class Common {
    /**
     * Returns whether the given string is in the array.
     *
     * @param haystack The array of strings to search.
     * @param needle   The string whose presence is to be tested.
     * @return If the string is in the collection.
     */
    public static boolean contains(String[] haystack, String needle) {
        for (int i = 0; i < haystack.length; i++)
            if (haystack[i].equals(needle))
                return true;
        return false;
    }

    /**
     * Get the path to a resource on the classpath.
     *
     * @param path The resource.
     * @return The path to the resource.
     */
    public String getFile(String path) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource(path).getFile();
    }
}
