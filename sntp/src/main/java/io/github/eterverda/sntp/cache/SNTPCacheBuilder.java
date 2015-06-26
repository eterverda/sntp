package io.github.eterverda.sntp.cache;

import java.io.File;

import io.github.eterverda.sntp.SNTPResponse;

public class SNTPCacheBuilder {
    private File file;
    private long expirationInterval = ExpiringSNTPCache.DEFAULT_EXPIRING_INTERVAL;
    private SNTPResponse initialResponse;

    public SNTPCacheBuilder setFile(File file) {
        this.file = file;
        return this;
    }

    public SNTPCacheBuilder setExpirationInterval(long expirationInterval) {
        if (expirationInterval < 0) {
            throw new IllegalArgumentException("expirationInterval < 0");
        }
        this.expirationInterval = expirationInterval;
        return this;
    }

    public SNTPCacheBuilder setInitialResponse(SNTPResponse initialResponse) {
        this.initialResponse = initialResponse;
        return this;
    }

    public static SNTPCache create() {
        return custom()
                .build();
    }

    public static SNTPCacheBuilder custom() {
        return new SNTPCacheBuilder();
    }

    public SNTPCache build() {
        if (expirationInterval == 0) {
            return NoSNTPCache.INSTANCE;
        }
        if (expirationInterval == Long.MAX_VALUE) {
            return buildMemoryCache();
        }
        return new ExpiringSNTPCache(buildMemoryCache(), expirationInterval);
    }

    private MemorySNTPCache buildMemoryCache() {
        return new MemorySNTPCache(buildPersistentCache(), initialResponse);
    }

    private SNTPCache buildPersistentCache() {
        if (file == null) {
            return null;
        }
        return new SimpleFileSNTPCache(file);
    }
}
