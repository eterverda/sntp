package io.github.eterverda.sntp.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import io.github.eterverda.sntp.SNTP;
import io.github.eterverda.sntp.cache.SNTPCache;

public final class SNTPSyncReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_TIME_CHANGED.equals(action)) {
            final SNTPCache cache = SNTP.getCache();
            if (cache != null) {
                cache.put(null);
            }
        }

        context.startService(new Intent(context, SNTPService.class));
    }
}
