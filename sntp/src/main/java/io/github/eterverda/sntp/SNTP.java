package io.github.eterverda.sntp;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import io.github.eterverda.sntp.cache.SNTPCache;
import io.github.eterverda.sntp.cache.SNTPCacheBuilder;

public final class SNTP {
    private static final AtomicReference<SNTPClient> CLIENT = new AtomicReference<>();
    private static final AtomicReference<SNTPCache> CACHE = new AtomicReference<>();

    private SNTP() {
    }

    public static void init() {
        setClient(SNTPClientBuilder.create());
        setCache(SNTPCacheBuilder.create());
    }

    public static void setClient(SNTPClient client) {
        if (client == null) {
            throw new NullPointerException("client == null");
        }
        if (!CLIENT.compareAndSet(null, client)) {
            throw new IllegalStateException("client already set");
        }
    }

    public static void setCache(SNTPCache cache) {
        if (cache == null) {
            throw new NullPointerException("cache == null");
        }
        if (!CACHE.compareAndSet(null, cache)) {
            throw new IllegalStateException("cache already set");
        }
    }

    public static SNTPClient getClient() {
        return CLIENT.get();
    }

    public static SNTPCache getCache() {
        return CACHE.get();
    }

    public static long currentTimeMillisFromNetwork() throws IOException {
        final SNTPClient client = CLIENT.get();
        if (client == null) {
            throw new IllegalStateException("client not set");
        }

        final SNTPResponse response = client.execute();

        final SNTPCache cache = CACHE.get();
        if (cache != null) {
            cache.put(response);
        }
        return response.currentGlobalTimeMillis();
    }

    public static long currentTimeMillisFromCache() throws IllegalStateException {
        final SNTPCache cache = CACHE.get();
        if (cache == null) {
            throw new IllegalStateException("cache not set");
        }
        final SNTPResponse response = cache.get();
        if (response == null) {
            throw new IllegalStateException("cache is empty or expired");
        }
        return response.currentGlobalTimeMillis();
    }

    public static long currentTimeMillis() throws IOException {
        final SNTPCache cache = CACHE.get();
        if (cache != null) {
            final SNTPResponse response = cache.get();
            if (response != null) {
                return response.currentGlobalTimeMillis();
            }
        }
        return currentTimeMillisFromNetwork();
    }

    public static long safeCurrentTimeMillis() {
        final SNTPCache cache = CACHE.get();
        if (cache != null) {
            final SNTPResponse response = cache.get();
            if (response != null) {
                return response.currentGlobalTimeMillis();
            }
        }
        final SNTPClient client = CLIENT.get();
        if (client != null) {
            try {
                final SNTPResponse response = client.execute();
                return response.currentGlobalTimeMillis();
            } catch (IOException ignore) {
            }
        }
        return System.currentTimeMillis();
    }
}
