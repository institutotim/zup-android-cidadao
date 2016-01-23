package br.com.lfdb.zup;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import br.com.lfdb.zup.track.GoogleAnalyticsTracker;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import io.fabric.sdk.android.Fabric;
import net.danlew.android.joda.JodaTimeAndroid;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ZupApplication extends MultiDexApplication {

  static Context context;

  @Override public void onCreate() {
    super.onCreate();

    JodaTimeAndroid.init(this);
    new Thread(() -> {
      CalligraphyConfig.initDefault(
          new CalligraphyConfig.Builder().setDefaultFontPath("fonts/OpenSans-Regular.ttf")
              .setFontAttrId(R.attr.fontPath)
              .build());

      CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
      Fabric.with(this, new Crashlytics.Builder().core(core).build(), new Crashlytics());
    }).start();

    GoogleAnalyticsTracker.init(this);

    context = this.getApplicationContext();
  }

  public static Context getContext() {
    return context;
  }
}
