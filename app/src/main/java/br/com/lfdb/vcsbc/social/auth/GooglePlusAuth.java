package br.com.lfdb.vcsbc.social.auth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import br.com.lfdb.vcsbc.social.SocialConstants;

public class GooglePlusAuth extends Activity {

    // Como o login não foi necessário, apenas guardamos a opção pelo G+
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(SocialConstants.PREF_LOGGED_SOCIAL, "google").apply();
        setResult(Activity.RESULT_OK);
        finish();
    }
}
