package com.example.time;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;

import io.github.eterverda.sntp.SNTP;
import io.github.eterverda.sntp.SNTPResponse;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_go) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        SNTP.currentTimeMillis();
                        final SNTPResponse response = SNTP.getCache().get();

                        final String cur = SNTPResponse.flattenTimestampToString(System.currentTimeMillis());
                        final String resp = response.flattenToString();

                        Log.d("111", String.format("cur %s %s", cur, resp));
                    } catch (IOException e) {
                        Log.e("111", "Something went wrong", e);
                    }
                }
            }.start();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
