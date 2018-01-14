package com.cldellow.manu.serve;

import static spark.Spark.*;

import com.cldellow.manu.format.Index;
import com.cldellow.manu.format.Reader;
import com.cldellow.manu.format.Record;

public class Server {
    Index i;
    Reader r;

    public Server() throws Exception {
        i = new Index("/home/cldellow/src/manu/keys", true);
        r = new Reader("/home/cldellow/src/manu/pvs.out");
    }

    public void run() {
        get("/hello", (request, response) ->
        {
            Record rec = r.get(4329);
            int[] vals = rec.getValues(0);
            int sum = 0;
            for(int i = 0; i < vals.length; i++)
                sum+= vals[i];
            return "Foo: " + sum;
        });
    }
}
