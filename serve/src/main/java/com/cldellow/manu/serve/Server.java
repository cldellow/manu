package com.cldellow.manu.serve;

import com.cldellow.manu.format.Interval;
import com.cldellow.manu.format.Reader;
import com.cldellow.manu.format.Record;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import spark.Request;
import spark.Response;

import javax.servlet.ServletOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import static spark.Spark.*;

public class Server {
    private final MetricRegistry metrics = new MetricRegistry();
    final JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
    private final Meter requests = metrics.meter("requests");
    private final Map<String, Collection> collections;
    private final DateTimeFormatter iso8601 = ISODateTimeFormat.dateTimeParser().withZoneUTC();
    private final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
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

        JsonGenerator gen = factory.createGenerator(sos, JsonEncoding.UTF8);
        gen.writeStartObject();
        gen.writeStringField("name", name);
        gen.writeFieldName("fields");
        gen.writeStartArray();
        String[] fields = collection.readers[0].fieldNames;
        for (int i = 0; i < fields.length; i++)
            gen.writeString(fields[i]);
        gen.writeEndArray();
        gen.writeStringField("interval", collection.readers[0].interval.name().toLowerCase());
        gen.writeStringField(
                "from",
                fmt.print(collection.readers[0].from));
        gen.writeStringField(
                "to",
                fmt.print(collection.readers[collection.readers.length - 1].to));
        gen.writeEndObject();
        gen.flush();
        gen.close();
        sos.flush();
        sos.close();
        return response.raw();
    }

    private Object dispatchQuery(Request request, Response response, Collection collection) throws Exception {
        requests.mark();

        String errMsg = "";

        try {
            Reader[] readers = collection.readers;
            Interval interval = readers[0].interval;

            errMsg = "error parsing `from` parameter";
            DateTime from = interval.truncate(iso8601.parseDateTime(request.queryParams("from")));
            errMsg = "error parsing `to` parameter";
            DateTime to = interval.truncate(iso8601.parseDateTime(request.queryParams("to")));

            errMsg = "`to` should be after `from`";
            if (to.isBefore(from))
                throw new Exception(errMsg);

            if (from.isBefore(readers[0].from))
                from = readers[0].from;

            if (to.isAfter(readers[readers.length - 1].to))
                to = readers[readers.length - 1].to;
            errMsg = "error parsing `field` parameters";
            String field[] = request.queryParamsValues("field");
            Vector<Integer> fieldIds = new Vector<>();
            if (field == null) {
                for (int i = 0; i < readers[0].numFields; i++)
                    fieldIds.add(i);
            } else {
                for (int i = 0; i < readers[0].numFields; i++)
                    for (int j = 0; j < field.length; j++)
                        if (field[j].equals(readers[0].fieldNames[i]))
                            fieldIds.add(i);
            }

            errMsg = "error parsing `key` parameters";
            String requestedKeys[] = request.queryParamsValues("key");
            Vector<Integer> ids = new Vector<>();
            Vector<String> keys = new Vector<>();
            for (int i = 0; i < requestedKeys.length; i++) {
                int id = collection.index.get(requestedKeys[i]);
                if (id != -1) {
                    keys.add(requestedKeys[i]);
                    ids.add(id);
                }
            }

            response.status(200);
            ServletOutputStream sos = response.raw().getOutputStream();

            JsonGenerator gen = factory.createGenerator(sos, JsonEncoding.UTF8);
            gen.writeStartObject();

            gen.writeFieldName("meta");
            gen.writeStartObject();
            gen.writeStringField("interval", interval.name().toLowerCase());
            gen.writeStringField("from", fmt.print(from));
            gen.writeStringField("to", fmt.print(to));
            gen.writeEndObject();
            gen.writeFieldName("values");
            gen.writeStartObject();


            int numMergedDatapoints = interval.difference(from, to);
            int startIndex = interval.difference(readers[0].from, from);
            int endIndex = startIndex + numMergedDatapoints;
            int[] rv = new int[numMergedDatapoints];
            int[] zeroes = new int[numMergedDatapoints];
            double[] rvDbl = null;
            for (int i = 0; i < keys.size(); i++) {
                int id = ids.get(i);
                gen.writeFieldName(keys.get(i));
                gen.writeStartObject();
                for (int j = 0; j < fieldIds.size(); j++) {
                    int fieldId = fieldIds.get(j);
                    gen.writeFieldName(readers[0].fieldNames[fieldId]);

                    System.arraycopy(zeroes, 0, rv, 0, rv.length);
                    for (int k = 0; k < readers.length; k++) {
                        int readerStart = interval.difference(readers[0].from, readers[k].from);
                        int readerEnd = interval.difference(readers[0].from, readers[k].to);

                        if(readerStart <= endIndex && startIndex <= readerEnd) {
                            Record r = readers[k].get(id);
                            int[] values = r.getValues(fieldId);
                            int collectionStart = startIndex - readerStart;
                            if(collectionStart < 0)
                                collectionStart = 0;

                            int collectionLength = Math.min(readers[k].numDatapoints,
                                    endIndex - (readerStart + collectionStart));

                            if(collectionLength + collectionStart >= readers[k].numDatapoints)
                                collectionLength--;

                            System.arraycopy(
                                    values,
                                    collectionStart,
                                    rv,
                                    readerStart + collectionStart- startIndex,
                                    collectionLength
                                    //endIndex - readerEnd

                                    );
                        }
                    }
                    gen.writeArray(rv, 0, rv.length);
                }
                gen.writeEndObject();
            }
            gen.writeEndObject();
            gen.writeEndObject();
            gen.flush();
            gen.close();
            sos.flush();
            sos.close();

            return response.raw();
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500);
            return errMsg;
        }
    }

    public void stop() throws Exception {
        reporter.stop();
        Iterator<Collection> it = collections.values().iterator();
        while (it.hasNext())
            it.next().close();
    }
}
