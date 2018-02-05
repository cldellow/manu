package com.cldellow.manu.cli;

import com.cldellow.manu.format.Interval;
import com.cldellow.manu.common.ArgHolder;
import com.cldellow.manu.common.NotEnoughArgsException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Collection;
import java.util.Vector;

class Parsers {
    public static Interval interval(String name) {
        if (name.equals("minute"))
            return Interval.MINUTE;
        if (name.equals("hour"))
            return Interval.HOUR;
        if (name.equals("day"))
            return Interval.DAY;
        if (name.equals("week"))
            return Interval.WEEK;
        if (name.equals("month"))
            return Interval.MONTH;
        if (name.equals("quarter"))
            return Interval.QUARTER;
        if (name.equals("year"))
            return Interval.YEAR;

        throw new IllegalArgumentException(
                String.format("unknown interval: %s", name));
    }

    public static KeyKind keyKind(String flag) {
        if (flag.equals("--key"))
            return KeyKind.KEY;
        if (flag.equals("--id"))
            return KeyKind.ID;

        throw new IllegalArgumentException(
                String.format("unknown key kind: %s", flag));
    }

    public static FieldKind fieldKind(String flag) {
        if (flag.equals("--int"))
            return FieldKind.INT;
        if (flag.equals("--fixed1"))
            return FieldKind.FIXED1;
        if (flag.equals("--fixed2"))
            return FieldKind.FIXED2;
        if (flag.equals("--lossy"))
            return FieldKind.LOSSY;

        throw new IllegalArgumentException(
                String.format("unknown field kind: %s", flag));
    }

    public static long epochMs(String string) {
        DateTimeFormatter iso8601 = ISODateTimeFormat.dateTimeParser().withZoneUTC();
        DateTime dt = iso8601.parseDateTime(string);
        return dt.toDateTime(DateTimeZone.UTC).getMillis();
    }

    public static Vector<FieldDef> fieldDefs(ArgHolder arg) throws NotEnoughArgsException {
        Vector<FieldDef> rv = new Vector<FieldDef>();

        FieldDef def = null;
        String consumed = "";
        while (arg.hasNext()) {
            String next = arg.next();
            consumed = consumed + " " + next;

            if(def == null)
                def = new FieldDef();

            if(next.startsWith("--")) {
                if(def.getName() != null)
                    throw new IllegalArgumentException(String.format(
                            "cannot set flags after providing name: %s", consumed));

                try {
                    def.setKeyKind(Parsers.keyKind(next));
                } catch(IllegalArgumentException iae) {
                    def.setFieldKind(Parsers.fieldKind(next));
                }
                continue;
            }

            if(def.getName() == null)
                def.setName(next);
            else {
                def.setFile(next);
                rv.add(def);
                def = null;
            }
        }

        if (def != null)
            throw new IllegalArgumentException(String.format(
                    "not a valid field def: %s", consumed));

        return rv;
    }
}
