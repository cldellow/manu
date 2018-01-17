package com.cldellow.manu.serve;

import com.cldellow.manu.common.Common;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ServerTest {
    static Server s;

    @AfterClass
    public static void afterClass() throws Exception {
        s.stop();
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        Map<String, Collection> collections = new HashMap<String, Collection>();
        Collection c = new Collection(new Common().getFile("./datadir/daily"));
        collections.put("daily", c);
        s = new Server(6268, collections);
        s.run();
    }

    @Test
    public void testMetadataUnknown() throws Exception {
        HttpResponse f = Http.get("http://localhost:6268/api/foo/meta");
        assertEquals(404, f.status);
    }

    @Test
    public void testMetadata() throws Exception {
        HttpResponse f = Http.get("http://localhost:6268/api/daily/meta");
        assertEquals(200, f.status);
        assertEquals("{\"name\":\"daily\",\"fields\":[\"field1\",\"field2\"],\"interval\":\"day\",\"from\":\"2008-01-01T00:00:00.000Z\",\"to\":\"2008-03-01T00:00:00.000Z\"}",
                f.body.trim());
    }

    @Test
    public void testQuery() throws Exception {
        HttpResponse f = Http.post("http://localhost:6268/api/daily", "from=2008-01-01&to=2008-01-05&key=a");
        assertEquals(200, f.status);
        assertEquals("{\"meta\":{\"interval\":\"day\",\"from\":\"2008-01-01T00:00:00.000Z\",\"to\":\"2008-01-05T00:00:00.000Z\"},\"values\":{\"a\":{\"field1\":[1,2,3,4],\"field2\":[101,102,103,104]}}}",
                f.body.trim());
    }


    @Test
    public void testQueryFilterField() throws Exception {
        HttpResponse f = Http.post("http://localhost:6268/api/daily", "from=2008-01-01&to=2008-01-05&key=a&field=field1");
        assertEquals(200, f.status);
        assertEquals("{\"meta\":{\"interval\":\"day\",\"from\":\"2008-01-01T00:00:00.000Z\",\"to\":\"2008-01-05T00:00:00.000Z\"},\"values\":{\"a\":{\"field1\":[1,2,3,4]}}}",
                f.body.trim());
    }

    @Test
    public void testQueryMissingKey() throws Exception {
        HttpResponse f = Http.post("http://localhost:6268/api/daily", "from=2008-01-01&to=2008-01-05&key=adflkj");
        assertEquals(200, f.status);
        assertEquals("{\"meta\":{\"interval\":\"day\",\"from\":\"2008-01-01T00:00:00.000Z\",\"to\":\"2008-01-05T00:00:00.000Z\"},\"values\":{}}",
                f.body.trim());
    }

    @Test
    public void testQuerySpanFiles() throws Exception {
        HttpResponse f = Http.post("http://localhost:6268/api/daily", "from=2008-01-31&to=2008-02-02&key=a&field=field1");
        assertEquals(200, f.status);
        assertEquals("{\"meta\":{\"interval\":\"day\",\"from\":\"2008-01-31T00:00:00.000Z\",\"to\":\"2008-02-02T00:00:00.000Z\"},\"values\":{\"a\":{\"field1\":[31,1]}}}",
                f.body.trim());
    }


    @Test
    public void testQueryMoreThanSet() throws Exception {
        HttpResponse f = Http.post("http://localhost:6268/api/daily", "from=2007-01-31&to=2010-02-02&key=a&field=field1");
        assertEquals(200, f.status);
        assertEquals("{\"meta\":{\"interval\":\"day\",\"from\":\"2008-01-01T00:00:00.000Z\",\"to\":\"2008-03-01T00:00:00.000Z\"},\"values\":{\"a\":{\"field1\":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,0]}}}",
                f.body.trim());
    }


    @Test
    public void testBadArg() throws Exception {
        HttpResponse f = Http.post("http://localhost:6268/api/daily", "from=2017-01-31&to=2010-02-02&key=a&field=field1");
        assertEquals(500, f.status);
        assertEquals("`to` should be after `from`",
                f.body.trim());
    }
}