package io.github.eterverda.sntp.android;

import android.app.Application;

import io.github.eterverda.sntp.SNTP;

public class SNTPApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SNTP.setClient(AndroidSNTPClientFactory.create());
        SNTP.setCache(AndroidSNTPCacheFactory.create(this));
    }
}
