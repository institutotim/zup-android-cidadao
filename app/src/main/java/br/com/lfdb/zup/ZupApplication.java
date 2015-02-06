package br.com.lfdb.zup;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import net.danlew.android.joda.JodaTimeAndroid;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import br.com.lfdb.zup.util.SentrySender;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

@ReportsCrashes(formKey = "")
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
                ACRA.init(this);
                SentrySender sentry = new SentrySender("https://70310cf77e7a458d853f077510ac44ad:d0bbcb5db6994c3db636aea2a02379c2@app.getsentry.com/17177");
                ACRA.getErrorReporter().setReportSender(sentry);
                ACRA.getErrorReporter().checkReportsOnApplicationStart();
            }
        }).start();

        context = this.getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

}
