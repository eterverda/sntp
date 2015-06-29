package io.github.eterverda.sntp;

public final class SNTPClientBuilder {
    public static final int DEFAULT_SOCKET_TIMEOUT = 4_000;

    private int timeout = DEFAULT_SOCKET_TIMEOUT;
    private SNTPHosts hosts = SNTPHosts.GLOBAL;
    private SNTPClient.MonotonicClock clock;

    private SNTPClientBuilder() {
    }

    public static SNTPClient create() {
        return custom().build();
    }

    public static SNTPClientBuilder custom() {
        return new SNTPClientBuilder();
    }

    public SNTPClientBuilder setMonotonicClock(SNTPClient.MonotonicClock clock) {
        this.clock = clock;
        return this;
    }

    public SNTPClientBuilder setHosts(SNTPHosts hosts) {
        if (hosts == null) {
            throw new IllegalArgumentException("hosts == null");
        }
        this.hosts = hosts;
        return this;
    }

    public SNTPClientBuilder setSoTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0");
        }
        this.timeout = timeout;
        return this;
    }

    public SNTPClient build() {
        return new SNTPClient(hosts, timeout, clock);
    }
}
