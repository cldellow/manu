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

    public Server(int port) throws Exception {
        port(port);
        i = new Index("/home/cldellow/src/manu/keys", true);
        r = new Reader("/home/cldellow/src/manu/pvs.out");
    }

    public void run() {
        reporter.start();

        post("/api", (request, response) ->
        {
//            System.out.println(request.body());
            System.out.println(request.params("bar"));
            System.out.println(request.queryParams("bar"));

            String bar[] = request.queryParamsValues("bar");
            for(int i = 0; i < bar.length; i++)
                System.out.println(bar[i]);
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
