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

    /**
     * @return response time on local clock useful for caching purposes
     */
    public long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    /**
     * Difference between local clock and world clock. Positive values mean local clock is behind.
     * Negative values mean local clock is ahead world clock.
     * <p/>
     * To get global (world) time you need to call {@code System.currentTimeMillis() + clockOffset}
     *
     * @return world clock offset
     * @see #globalTimeMillis(long)
     * @see #currentGlobalTimeMillis()
     */
    public long getClockOffset() {
        return clockOffset;
    }

    /**
     * @return {@code localTimeMillis + clockOffset}
     * @see #getClockOffset()
     */
    public long globalTimeMillis(long localTimeMillis) {
        return localTimeMillis + clockOffset;
    }

    /**
     * @return current time on word clock
     * @see #getClockOffset()
     */
    public long currentGlobalTimeMillis() {
        return globalTimeMillis(System.currentTimeMillis());
    }

    /**
     * Serializes response for {@link #unflattenFromString(String)}
     */
    public String flattenToString() {
        final long sys = getResponseTimeMillis();
        final long ntp = globalTimeMillis(sys);
        final long off = getClockOffset();

        final String sysStr = flattenTimestampToString(sys);
        final String ntpStr = flattenTimestampToString(ntp);

        return String.format(Locale.US, FORMAT, sysStr, ntpStr, off);
    }

    /**
     * Serializes timestamp for {@link #unflattenTimestampFromString(String)}
     */
    public static String flattenTimestampToString(long timeMillis) {
        return DATE_FORMAT.format(new Date(timeMillis));
    }

    /**
     * Parses response serialized by {@link #flattenToString()}
     *
     * @throws ParseException when gievn string is malformed
     */
    public static SNTPResponse unflattenFromString(String string) throws ParseException {
        final Matcher matcher = PATTERN.matcher(string);

        if (!matcher.matches()) {
            throw new ParseException("malformed input " + string, 0);
        }

        final String sysStr = matcher.group(1);
        final String ntpStr = matcher.group(2);

        final long sys = unflattenTimestampFromString(sysStr);
        final long ntp = unflattenTimestampFromString(ntpStr);

        return create(sys, ntp);
    }

    /**
     * Parses timestamp rerialized by {@link #flattenTimestampToString(long)}
     *
     * @throws ParseException when gievn string is malformed
     */
    public static long unflattenTimestampFromString(String string) throws ParseException {
        return DATE_FORMAT.parse(string).getTime();
    }

    static SNTPResponse create(long localTimeMillis, long globalTimeMillis) {
        final long clockOffset = globalTimeMillis - localTimeMillis;

        return new SNTPResponse(localTimeMillis, clockOffset);
    }

    static SNTPResponse create(long originateTimeMillis, long receiveTimeMillis, long transmitTimeMillis, long responseTimeMillis) {
        final long clockOffset = ((receiveTimeMillis - originateTimeMillis) + (transmitTimeMillis - responseTimeMillis)) / 2;

        return new SNTPResponse(responseTimeMillis, clockOffset);
    }
}
