package br.com.lfdb.zup.base;

import android.support.v4.app.FragmentActivity;

import br.com.lfdb.zup.track.GoogleAnalyticsTracker;

public abstract class BaseActivity extends FragmentActivity {

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalyticsTracker.getInstance().track(getScreenName());
    }

    protected abstract String getScreenName();
}
