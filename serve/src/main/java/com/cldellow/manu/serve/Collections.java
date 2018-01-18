package com.cldellow.manu.serve;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Collections {
    public static Map<String, Collection> discover(String datadir) throws Exception {
        Map<String, Collection> rv = new HashMap<>();
        File f = new File(datadir);
        File[] files = f.listFiles();
        if(files == null)
            files = new File[0];

        for(int i = 0; i < files.length; i++) {
            if(Collection.isCollection(files[i].getAbsolutePath())) {
                Collection collection = new Collection(files[i].getAbsolutePath());
                rv.put(files[i].getName(), collection);
            }
        }

        return rv;
    }
}
