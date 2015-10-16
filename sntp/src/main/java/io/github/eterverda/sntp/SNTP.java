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

    /**
     * @param client client for future use
     * @throws IllegalStateException if client already set
     */
    public static void setClient(SNTPClient client) {
        if (client == null) {
            throw new NullPointerException("client == null");
        }
        if (!CLIENT.compareAndSet(null, client)) {
            throw new IllegalStateException("client already set");
        }
    }

    /**
     * @param cache cache for future use
     * @throws IllegalStateException if cache already set
     */
    public static void setCache(SNTPCache cache) {
        if (cache == null) {
            throw new NullPointerException("cache == null");
        }
        if (!CACHE.compareAndSet(null, cache)) {
            throw new IllegalStateException("cache already set");
        }
    }

    /**
     * @return client provided to {@link #setClient(SNTPClient)} or {@code null} if there is none.
     */
    public static SNTPClient getClient() {
        return CLIENT.get();
    }

    /**
     * @return client provided to {@link #setCache(SNTPCache)} or {@code null} if there is none.
     */
    public static SNTPCache getCache() {
        return CACHE.get();
    }

    /**
     * This method will always perform SNTP request using client provided to {@link #setClient(SNTPClient)}}.
     * If there was {@link SNTPCache} provided to {@link #setCache(SNTPCache)} received response will be cached.
     * <p/>
     * Do not call this method on your main event thread!
     *
     * @return current time on world clock
     * @throws IOException           if IO error occurs
     * @throws IllegalStateException if {@link #setClient(SNTPClient)} was not called
     * @see #setClient(SNTPClient)
     * @see #currentTimeMillis()
     */
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

    /**
     * This method will calculate time on world clock using {@link SNTPCache} provided to {@link #setCache(SNTPCache)}.
     * <p/>
     * You can call this method on your main event thread if you don't mind some file IO with persistent cache.
     *
     * @return current time on world clock
     * @throws IllegalStateException if {@link #setCache(SNTPCache)}} was not called or cache is empty or expired
     * @see #setCache(SNTPCache)
     * @see #currentTimeMillis()
     */
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

    /**
     * Same as {@link #currentTimeMillisFromCache()} but never throws. Will fallback to
     * {@link System#currentTimeMillis()}.
     *
     * @return current time on world (or maybe local) clock
     */
    public static long safeCurrentTimeMillisFromCache() {
        final SNTPCache cache = CACHE.get();
        if (cache != null) {
            final SNTPResponse response = cache.get();
            if (response != null) {
                return response.currentGlobalTimeMillis();
            }
        }
        return System.currentTimeMillis();
    }

    /**
     * This method will try to calculate current time on global clock using {@link SNTPCache} provided
     * to {@link #setCache(SNTPCache)}. If there is no cache or cache empty or expired network request will be
     * performed. Response from network is subject for caching.
     * <p/>
     * May perform networking. Do not call this method on your main event thread!
     *
     * @return current time on world clock
     * @throws IOException           if IO error occurs
     * @throws IllegalStateException if {@link #setClient(SNTPClient)} was not called and cache is empty or expired
     * @see #setClient(SNTPClient)
     * @see #setCache(SNTPCache)
     */
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

    /**
     * Same as {@link #currentTimeMillis()} but never throws. Will fallback to {@link System#currentTimeMillis()}.
     * <p/>
     * May perform networking. Do not call this method on your main event thread!
     *
     * @return current time on world (or maybe local) clock
     */
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
                if (cache != null) {
                    cache.put(response);
                }
                return response.currentGlobalTimeMillis();
            } catch (IOException ignore) {
            }
        }
        return System.currentTimeMillis();
    }
}
