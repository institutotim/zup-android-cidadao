package br.com.lfdb.vcsbc.base;

import android.support.v4.app.FragmentActivity;

import br.com.lfdb.vcsbc.track.GoogleAnalyticsTracker;

public abstract class BaseActivity extends FragmentActivity {

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalyticsTracker.getInstance().track(getScreenName());
    }

    protected abstract String getScreenName();
}
