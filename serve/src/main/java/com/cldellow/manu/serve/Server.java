package com.cldellow.manu.serve;

import static spark.Spark.*;

import com.cldellow.manu.format.Index;
import com.cldellow.manu.format.Reader;
import com.cldellow.manu.format.Record;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

public class Server {
    Index i;
    Reader r;
    private final MetricRegistry metrics = new MetricRegistry();
    private final Meter requests = metrics.meter("requests");
    final JmxReporter reporter = JmxReporter.forRegistry(metrics).build();

    public Server() throws Exception {
        i = new Index("/home/cldellow/src/manu/keys", true);
        r = new Reader("/home/cldellow/src/manu/pvs.out");
    }

    public void run() {
        reporter.start();

        get("/hello", (request, response) ->
        {
            requests.mark();
            Record rec = r.get(4329);
            int[] vals = rec.getValues(0);
            int sum = 0;
            for(int i = 0; i < vals.length; i++)
                sum+= vals[i];
            return "Foo: " + sum;
        });
    }
}
