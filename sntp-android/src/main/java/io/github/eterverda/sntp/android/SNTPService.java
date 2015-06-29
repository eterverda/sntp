package io.github.eterverda.sntp.android;

import android.app.IntentService;
import android.content.Intent;

import java.io.IOException;

import io.github.eterverda.sntp.SNTP;

public final class SNTPService extends IntentService {

    public SNTPService() {
        super("SNTP");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            SNTP.currentTimeMillis();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
