package br.com.ntxdev.zup;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import br.com.ntxdev.zup.util.SentrySender;

@ReportsCrashes(formKey = "")
public class ZupApplication extends Application {

	static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);
        SentrySender sentry = new SentrySender("https://70310cf77e7a458d853f077510ac44ad:d0bbcb5db6994c3db636aea2a02379c2@app.getsentry.com/17177");
        ACRA.getErrorReporter().setReportSender(sentry);
        ACRA.getErrorReporter().checkReportsOnApplicationStart();

        context = this.getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
