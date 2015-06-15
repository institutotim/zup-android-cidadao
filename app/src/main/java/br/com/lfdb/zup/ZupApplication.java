package br.com.lfdb.zup;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;

import net.danlew.android.joda.JodaTimeAndroid;

import br.com.lfdb.zup.track.GoogleAnalyticsTracker;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ZupApplication extends MultiDexApplication {

    static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);
        new Thread(() -> {
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build());

            if (!BuildConfig.DEBUG) {
                Fabric.with(this, new Crashlytics());
            }
        }).start();

        GoogleAnalyticsTracker.init(this);

        context = this.getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

}
