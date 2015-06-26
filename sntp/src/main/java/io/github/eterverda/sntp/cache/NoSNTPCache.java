package io.github.eterverda.sntp.cache;

import io.github.eterverda.sntp.SNTPResponse;

final class NoSNTPCache implements SNTPCache {
    public static final SNTPCache INSTANCE = new NoSNTPCache();

    private NoSNTPCache() {
    }

    @Override
    public SNTPResponse get() {
        return null;
    }

    @Override
    public void put(SNTPResponse response) {
    }
}
