package io.github.eterverda.sntp.cache;

import io.github.eterverda.sntp.SNTPResponse;

public interface SNTPCache {
    SNTPResponse get();

    void put(SNTPResponse response);
}
