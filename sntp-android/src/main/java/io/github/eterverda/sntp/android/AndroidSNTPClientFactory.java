package io.github.eterverda.sntp.android;

import android.os.SystemClock;

import io.github.eterverda.sntp.SNTPClient;
import io.github.eterverda.sntp.SNTPClientBuilder;

public class AndroidSNTPClientFactory {
    public static final SNTPClient.MonotonicClock ANDROID_SYSTEM_CLOCK = new AndroidSystemClock();

    public static SNTPClient create() {
        return custom().build();
    }

    public static SNTPClientBuilder custom() {
        return SNTPClientBuilder.custom()
                .setMonotonicClock(ANDROID_SYSTEM_CLOCK);
    }

    static final class AndroidSystemClock implements SNTPClient.MonotonicClock {
        AndroidSystemClock() {
        }

        @Override
        public long ticks() {
            return SystemClock.elapsedRealtime();
        }
    }
}
