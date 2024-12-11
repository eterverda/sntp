package io.github.eterverda.sntp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @see SNTPClientBuilder
 */
public final class SNTPClient {
    // all time calculations in seconds
    private static final long DAYS = 24L * 60L * 60L;
    private static final long YEARS = 365L * DAYS;
    // there were 17 leap days between 1900 and 1970
    private static final long OFFSET_1900_TO_1970 = (70L * YEARS) + (17L * DAYS);

    private static final int RECEIVE_TIME_OFFSET = 32;
    private static final int TRANSMIT_TIME_OFFSET = 40;
    private static final int NTP_PACKET_SIZE = 48;

    private static final int NTP_MODE_CLIENT = 3;
    private static final int NTP_VERSION = 3;

    private static final int NTP_PORT = 123;

    private final SNTPHosts hosts;
    private final int timeout;
    private final MonotonicClock clock;

    SNTPClient(SNTPHosts hosts, int timeout, MonotonicClock clock) {
        this.hosts = hosts;
        this.timeout = timeout;
        this.clock = clock;
    }

    /**
     * Executes SNTP response to {@link SNTPHosts#nextHost()} and returns result.
     *
     * @return response from SNTP server
     * @throws IOException if IO Error occurs
     */
    public SNTPResponse execute() throws IOException {
        final String host = hosts.nextHost();
        final InetAddress address = InetAddress.getByName(host);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(timeout);

            socket.connect(address, NTP_PORT);

            final byte[] buffer = new byte[NTP_PACKET_SIZE];
            final DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
            writeMode(buffer);

            final long requestTime = System.currentTimeMillis();
            final long requestTicks = clock != null ? clock.ticks() : requestTime;

            writeTimestamp(requestTime, buffer, TRANSMIT_TIME_OFFSET);

            socket.send(packet);
            socket.receive(packet);

            final long responseTicks = clock != null ? clock.ticks() : System.currentTimeMillis();
            final long responseTime = clock != null ? requestTime + (responseTicks - requestTicks) : responseTicks;

            socket.disconnect();

            final long receiveTime = readTimestamp(buffer, RECEIVE_TIME_OFFSET);
            final long transmitTime = readTimestamp(buffer, TRANSMIT_TIME_OFFSET);

            return SNTPResponse.create(requestTime, receiveTime, transmitTime, responseTime);

        } catch (SecurityException sex) {
            // Some shitty devices throw SecurityException caused by GaiException
            // instead of throwing UnknownHostException.
            // Here's our workaround of this issue.

            final Throwable cause = sex.getCause();
            if (cause == null) {
                throw sex; // some other SecurityException, let it fall
            }
            final String causeName = cause.getClass().getName();
            if (!causeName.equals("libcore.io.GaiException") && !causeName.equals("android.system.GaiException")) {
                throw sex; // some other SecurityException, let it fall
            }
            throw new UnknownHostException();

        } catch (NullPointerException npe) {
            if (npe.getMessage().equals("null buffer || null address")) {
                throw new UnknownHostException();
            }
            throw npe;

        } catch (AssertionError ass) {
            // For reasons unknown AssertionError thrown on some Android 4 devices.
            // Just coerce it to IOException.
            throw new IOException(ass);
        }
    }

    private static void writeMode(byte[] buffer) {
        final int ntpVersion = NTP_VERSION;
        final int mode = NTP_MODE_CLIENT;
        buffer[0] = (ntpVersion << 3) | mode;
    }

    private static long readTimestamp(byte[] buffer, int offset) {
        final long ntpSeconds = readUint32(buffer, offset);
        final long ntpFraction = readUint32(buffer, offset + 4);

        final long seconds = ntpSeconds - OFFSET_1900_TO_1970;
        final long milliseconds = ntpFraction * 1000L / 0x100000000L;

        return seconds * 1000 + milliseconds;
    }

    private static long readUint32(byte[] buf, int off) {
        long result = 0;
        result |= (long) readUint8(buf, off) << 24;
        result |= (long) readUint8(buf, off + 1) << 16;
        result |= (long) readUint8(buf, off + 2) << 8;
        result |= (long) readUint8(buf, off + 3);

        return result;
    }

    private static int readUint8(byte[] buf, int off) {
        final byte b = buf[off];
        return (b & 0x80) == 0x80 ? (b & 0x7F) + 0x80 : b;
    }

    private static void writeTimestamp(long time, byte[] buf, int off) {
        final long seconds = time / 1000L;
        final long milliseconds = time - seconds * 1000L;

        final long ntpSeconds = seconds + OFFSET_1900_TO_1970;
        final long ntpFraction = milliseconds * 0x100000000L / 1000L;
        final long ntpFractionPlusRandom = ntpFraction | (byte) (Math.random() * 0x100);

        writeUint32(ntpSeconds, buf, off);
        writeUint32(ntpFractionPlusRandom, buf, off + 4);
    }

    private static void writeUint32(long val, byte[] buf, int off) {
        buf[off] = (byte) (val >> 24);
        buf[off + 1] = (byte) (val >> 16);
        buf[off + 2] = (byte) (val >> 8);
        buf[off + 3] = (byte) val;
    }

    public interface MonotonicClock {
        long ticks();
    }
}
