package com.cldellow.manu.serve;

import com.cldellow.manu.format.FieldType;
import com.cldellow.manu.format.Interval;
import com.cldellow.manu.format.ManuReader;
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

class Server {
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
        for (int i = 0; i < collection.readers[0].getNumFields(); i++)
            gen.writeString(collection.readers[0].getFieldName(i));
        gen.writeEndArray();
        gen.writeStringField("interval", collection.readers[0].getInterval().name().toLowerCase());
        gen.writeStringField(
                "from",
                fmt.print(collection.readers[0].getFrom()));
        gen.writeStringField(
                "to",
                fmt.print(collection.readers[collection.readers.length - 1].getTo()));
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
            ManuReader[] readers = collection.readers;
            Interval interval = readers[0].getInterval();

            errMsg = "error parsing `from` parameter";
            DateTime from = interval.truncate(iso8601.parseDateTime(request.queryParams("from")));
            errMsg = "error parsing `to` parameter";
            DateTime to = interval.truncate(iso8601.parseDateTime(request.queryParams("to")));

            errMsg = "`to` should be after `from`";
            if (to.isBefore(from))
                throw new Exception(errMsg);

            if (from.isBefore(readers[0].getFrom()))
                from = readers[0].getFrom();

            if (to.isAfter(readers[readers.length - 1].getTo()))
                to = readers[readers.length - 1].getTo();
            errMsg = "error parsing `field` parameters";
            String field[] = request.queryParamsValues("field");
            Vector<Integer> fieldIds = new Vector<>();
            if (field == null) {
                for (int i = 0; i < readers[0].getNumFields(); i++)
                    fieldIds.add(i);
            } else {
                for (int i = 0; i < readers[0].getNumFields(); i++)
                    for (int j = 0; j < field.length; j++)
                        if (field[j].equals(readers[0].getFieldName(i)))
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
            int startIndex = interval.difference(readers[0].getFrom(), from);
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
                    gen.writeFieldName(readers[0].getFieldName(fieldId));
                    FieldType fieldType = readers[0].getFieldType(fieldId);

                    System.arraycopy(zeroes, 0, rv, 0, rv.length);
                    for (int k = 0; k < readers.length; k++) {
                        int readerStart = interval.difference(readers[0].getFrom(), readers[k].getFrom());
                        int readerEnd = interval.difference(readers[0].getFrom(), readers[k].getTo());

                        if (readerStart <= endIndex && startIndex <= readerEnd) {
                            int collectionStart = startIndex - readerStart;
                            if (collectionStart < 0)
                                collectionStart = 0;

                            int collectionLength = Math.min(readers[k].getNumDatapoints(),
                                    endIndex - (readerStart + collectionStart));

                            if (collectionLength + collectionStart > readers[k].getNumDatapoints())
                                collectionLength--;

                            int[] values = null;
                            if (readers[k].getRecordOffset() <= id &&
                                    (readers[k].getRecordOffset() + readers[k].getNumRecords()) > id) {
                                Record r = readers[k].get(id);
                                if (r != null)
                                    values = r.getValues(fieldId);
                            }

                            if (values != null)
                                System.arraycopy(
                                        values,
                                        collectionStart,
                                        rv,
                                        readerStart + collectionStart - startIndex,
                                        collectionLength
                                        //endIndex - readerEnd

                                );
                            else {
                                for (int l = 0; l < collectionLength; l++)
                                    rv[l + readerStart + collectionStart - startIndex] = readers[0].getNullValue();
                            }
                        }
                    }
                    gen.writeStartArray();
                    for (int k = 0; k < rv.length; k++) {
                        int intVal = rv[k];
                        if (intVal == readers[0].getNullValue())
                            gen.writeNull();
                        else {
                            if (fieldType == FieldType.INT)
                                gen.writeNumber(intVal);
                            else if (fieldType == FieldType.FIXED1) {
                                double fixed = (double) intVal / 10;
                                gen.writeNumber(fixed);
                            } else { // if (fieldType == FieldType.FIXED2) {
                                double fixed = (double) intVal / 100;
                                gen.writeNumber(fixed);
                            }
                        }
                    }

                    gen.writeEndArray();
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
