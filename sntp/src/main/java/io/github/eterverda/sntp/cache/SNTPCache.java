package io.github.eterverda.sntp.cache;

import io.github.eterverda.sntp.SNTPResponse;

/**
 * {@see SNTPCacheBuilder}
 */
public interface SNTPCache {
    SNTPResponse get();

    void put(SNTPResponse response);
}
