Simple Network Time Protocol client for Java and Android.

Main methods
------------

- `SNTP.setClient()` initializes global static `SNTPClient`
- `SNTP.setCache()` initializes global static `SNTPCache`
- `SNTP.currentTimeMillis()` returns current global time. It may use netorking and can throw `IOException`. Do not call it on main event thread.
- `SNTP.safeCurrentTimeMillis()` same as above but will not throw anything and will use `System.currentTimeMillis()` as fallback.

Using on Android
----------------

In your `build.gradle`:

    repositories {
        maven { url 'https://raw.githubusercontent.com/eterverda/sntp/m2/' }
    }
    dependencies {
        compile 'io.github.eterverda.sntp:sntp-android:0.1.5'
    }

In `onCreate` of your application:

    SNTP.setClient(AndroidSNTPClientFactory.create());
    SNTP.setCache(AndroidSNTPCacheFactory.create(this));

... or you can extend `SNTPApplication`.

In your `AndroidManifest.xml` add:

    <receiver android:name="io.github.eterverda.sntp.android.SNTPResetCacheReceiver">
        <intent-filter>
            <action android:name="android.intent.action.TIME_SET"/>
        </intent-filter>
    </receiver>

... to reset SNTP cache when user changes time in phone settings.

Using in Java project
---------------------


In `build.gradle` of your Java project:

    repositories {
        maven { url 'https://raw.githubusercontent.com/eterverda/sntp/m2/' }
    }

    dependencies {
        compile 'io.github.eterverda.sntp:sntp:0.1.5'
    }

Somehere on startup of application:

    SNTP.init()

... howewer this will not create persistent SNTP cache. If you need one replace `SNTP.init()` with:

    SNTP.setClient(SNTPClientBuilder.create());
    SNTP.setCache(SNTPCacheBuilder.custom().setFile(sntpCacheFile).build());
