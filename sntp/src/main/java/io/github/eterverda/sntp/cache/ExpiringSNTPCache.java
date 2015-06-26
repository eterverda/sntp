package io.github.eterverda.sntp.cache;

import io.github.eterverda.sntp.SNTPResponse;

final class ExpiringSNTPCache implements SNTPCache {
    public static final long DEFAULT_EXPIRING_INTERVAL = 60 * 60 * 1000;

    private final SNTPCache delegate;
    private final long expirationInterval;

    public ExpiringSNTPCache(SNTPCache delegate, long expirationInterval) {
        this.delegate = delegate;
        this.expirationInterval = expirationInterval;
    }

    @Override
    public SNTPResponse get() {
        final SNTPResponse response = delegate.get();
        if (response == null) {
            return null;
        } else if (isExpired(response)) {
            return null;
        }
        return response;
    }

    @Override
    public void put(SNTPResponse response) {
        delegate.put(response);
    }

    private boolean isExpired(SNTPResponse response) {
        return response.getResponseTimeMillis() + expirationInterval < System.currentTimeMillis();
    }
}
