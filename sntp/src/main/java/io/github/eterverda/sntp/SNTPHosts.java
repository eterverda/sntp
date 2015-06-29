package io.github.eterverda.sntp;

import java.util.Arrays;

public final class SNTPHosts {
    public static final SNTPHosts THE = new SNTPHosts(true, "pool.ntp.org");

    public static final SNTPHosts GLOBAL = new SNTPHosts(true, "0.pool.ntp.org", "1.pool.ntp.org", "2.pool.ntp.org", "3.pool.ntp.org");

    public static final SNTPHosts EUROPE = new SNTPHosts(true, "0.europe.pool.ntp.org", "1.europe.pool.ntp.org", "2.europe.pool.ntp.org", "3.europe.pool.ntp.org");
    public static final SNTPHosts ASIA = new SNTPHosts(true, "0.asia.pool.ntp.org", "1.asia.pool.ntp.org", "2.asia.pool.ntp.org", "3.asia.pool.ntp.org");

    public static final SNTPHosts RU = new SNTPHosts(true, "0.ru.pool.ntp.org", "1.ru.pool.ntp.org", "2.ru.pool.ntp.org", "3.ru.pool.ntp.org");

    private final String[] hosts;
    private transient int index;

    public SNTPHosts(String host) {
        if (host == null) {
            throw new NullPointerException("host == null");
        }
        hosts = new String[]{host};
    }

    public SNTPHosts(String... hosts) {
        if (hosts == null) {
            throw new NullPointerException("hosts == null");
        }
        final int length = hosts.length;
        final String[] h = Arrays.copyOf(hosts, length);
        if (h.length == 0) {
            throw new IllegalArgumentException("hosts.length == 0");
        }
        for (int i = 0; i < length; i++) {
            if (h[i] == null) {
                throw new NullPointerException("hosts[" + i + "] == null");
            }
        }
        this.hosts = h;
    }

    private SNTPHosts(@SuppressWarnings("UnusedParameters") boolean safe, String... hosts) {
        this.hosts = hosts;
        index = (int) (Math.random() * hosts.length);
    }

    public String nextHost() {
        if (index == hosts.length) {
            index = 0;
        }
        return hosts[index++];
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SNTPHosts && equals((SNTPHosts) other);
    }

    public boolean equals(SNTPHosts other) {
        return other == this || Arrays.equals(hosts, other.hosts);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hosts);
    }
}
