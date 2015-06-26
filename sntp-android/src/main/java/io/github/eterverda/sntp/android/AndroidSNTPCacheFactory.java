package io.github.eterverda.sntp.android;

import android.content.Context;

import java.io.File;

import io.github.eterverda.sntp.cache.SNTPCache;
import io.github.eterverda.sntp.cache.SNTPCacheBuilder;

public class AndroidSNTPCacheFactory {
    public static SNTPCache create(Context context) {
        return custom(context)
                .build();
    }

    public static SNTPCacheBuilder custom(Context context) {
        return SNTPCacheBuilder.custom()
                .setFile(new File(context.getCacheDir(), "sntp/cache"));
    }
}
