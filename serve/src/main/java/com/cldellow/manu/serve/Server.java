package com.cldellow.manu.serve;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.joda.time.format.ISODateTimeFormat;
import spark.Request;
import spark.Response;

import javax.servlet.ServletOutputStream;
import java.util.Iterator;
import java.util.Map;

import static spark.Spark.*;

public class Server {
    private final MetricRegistry metrics = new MetricRegistry();
    final JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
    private final Meter requests = metrics.meter("requests");
    private final Map<String, Collection> collections;
    private JsonFactory factory = new JsonFactory();

    public Server(int port, Map<String, Collection> collections) throws Exception {
        port(port);
        this.collections = collections;
    }

    public void run() {
        reporter.start();

        Iterator<Map.Entry<String, Collection>> it = collections.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Collection> entry = it.next();

            post("/api/" + entry.getKey(),
                    (request, response) -> dispatchQuery(request, response, entry.getValue()));

            get("/api/" + entry.getKey() + "/meta",
                    (request, response) -> dispatchMeta(request, response, entry.getKey(), entry.getValue()));
        }

        awaitInitialization();
    }

    private Object dispatchMeta(Request request, Response response, String name, Collection collection) throws Exception {
        response.status(200);
        ServletOutputStream sos = response.raw().getOutputStream();
        //response.raw().setContentLength(1);

//        {
//            "name": "wikipedia-hourly-pageviews",
//                "fields": ["pageviews"],
//            "interval": "hour",
//                "from": "2007-12-09T18:00Z",
//                "to": "2018-01-14T00:00Z"
//        }

        JsonGenerator gen = factory.createGenerator(sos, JsonEncoding.UTF8);
        gen.writeStartObject();
        gen.writeStringField("name", name);
        gen.writeFieldName("fields");
        gen.writeStartArray();
        String[] fields = collection.readers[0].fieldNames;
        for(int i =0; i < fields.length; i++)
            gen.writeString(fields[i]);
        gen.writeEndArray();
        gen.writeStringField("interval", collection.readers[0].interval.name().toLowerCase());
        gen.writeStringField(
                "from",
                ISODateTimeFormat.dateTime().print(collection.readers[0].from));
        gen.writeStringField(
                "to",
                ISODateTimeFormat.dateTime().print(collection.readers[collection.readers.length-1].to));
        gen.writeEndObject();
        gen.flush();
        gen.close();
        sos.flush();
        sos.close();
        return response.raw();
    }

    private Object dispatchQuery(Request request, Response response, Collection collection) throws Exception {
        System.out.println(request.params("bar"));
        System.out.println(request.queryParams("bar"));

        String bar[] = request.queryParamsValues("bar");
        for (int i = 0; i < bar.length; i++)
            System.out.println(bar[i]);
        requests.mark();
        return 1;
    }

    public void stop() throws Exception {
        reporter.stop();
        Iterator<Collection> it = collections.values().iterator();
        while (it.hasNext())
            it.next().close();
    }
}
