package io.github.eterverda.sntp.cache;

import io.github.eterverda.sntp.SNTPResponse;

final class MemorySNTPCache implements SNTPCache {
    private final SNTPCache delegate;
    private SNTPResponse response;

    public MemorySNTPCache(SNTPCache delegate, SNTPResponse initialResponse) {
        this.delegate = delegate;
        this.response = initialResponse;
    }

    @Override
    public SNTPResponse get() {
        if (response != null) {
            return response;
        }
        if (delegate != null) {
            final SNTPResponse delegateResponse = delegate.get();
            response = delegateResponse;
            return delegateResponse;
        }
        return null;
    }

    @Override
    public void put(SNTPResponse response) {
        if (delegate != null) {
            delegate.put(response);
        }
        this.response = response;
    }
}
