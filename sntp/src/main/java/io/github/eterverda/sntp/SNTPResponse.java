package io.github.eterverda.sntp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SNTPResponse {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private static final Pattern PATTERN = Pattern.compile("sys ([^\\s]+) ntp ([^\\s]+) off [^\\s]+");
    private static final String FORMAT = "sys %s ntp %s off %d";

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private final long responseTimeMillis;
    private final long clockOffset;

    private SNTPResponse(long responseLocalTimeMillis, long clockOffset) {
        this.responseTimeMillis = responseLocalTimeMillis;
        this.clockOffset = clockOffset;
    }

    public long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    public long getClockOffset() {
        return clockOffset;
    }

    public long globalTimeMillis(long localTimeMillis) {
        return localTimeMillis + clockOffset;
    }

    public long currentGlobalTimeMillis() {
        return globalTimeMillis(System.currentTimeMillis());
    }

    public String flattenToString() {
        final long sys = getResponseTimeMillis();
        final long ntp = globalTimeMillis(sys);
        final long off = getClockOffset();

        final String sysStr = flattenTimestampToString(sys);
        final String ntpStr = flattenTimestampToString(ntp);

        return String.format(Locale.US, FORMAT, sysStr, ntpStr, off);
    }

    public static String flattenTimestampToString(long timeMillis) {
        return DATE_FORMAT.format(new Date(timeMillis));
    }

    public static SNTPResponse unflattenFromString(String line) throws ParseException {
        final Matcher matcher = PATTERN.matcher(line);

        if (!matcher.matches()) {
            throw new ParseException("malformed input " + line, 0);
        }

        final String sysStr = matcher.group(1);
        final String ntpStr = matcher.group(2);

        final long sys = unflattenTimestampFromString(sysStr);
        final long ntp = unflattenTimestampFromString(ntpStr);

        return create(sys, ntp);
    }

    public static long unflattenTimestampFromString(String sysStr) throws ParseException {
        return DATE_FORMAT.parse(sysStr).getTime();
    }

    public static SNTPResponse create(long localTimeMillis, long globalTimeMillis) {
        final long clockOffset = globalTimeMillis - localTimeMillis;

        return new SNTPResponse(localTimeMillis, clockOffset);
    }

    public static SNTPResponse create(long originateTimeMillis, long receiveTimeMillis, long transmitTimeMillis, long responseTimeMillis) {
        final long clockOffset = ((receiveTimeMillis - originateTimeMillis) + (transmitTimeMillis - responseTimeMillis)) / 2;

        return new SNTPResponse(responseTimeMillis, clockOffset);
    }
}
