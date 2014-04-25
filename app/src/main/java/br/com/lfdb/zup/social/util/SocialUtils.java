package br.com.lfdb.zup.social.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.plus.PlusShare;

import br.com.lfdb.zup.social.SocialConstants;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class SocialUtils {

    public static void post(Activity context, String message) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String logged = prefs.getString(SocialConstants.PREF_LOGGED_SOCIAL, "");
        if (logged.equalsIgnoreCase("twitter")) {
            postToTwitter(context, message);
        } else if (logged.equalsIgnoreCase("google+")) {
            postToGooglePlus(context, message);
        }
    }

    private static void postToGooglePlus(Context context, String message) {
        Intent shareIntent = new PlusShare.Builder(context)
                .setType("text/plain")
                .setText(message)
                //.setContentUrl(Uri.parse("https://developers.google.com/+/"))
                .getIntent();

        context.startActivity(shareIntent);
    }

    public static void logout(Activity context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String logged = prefs.getString(SocialConstants.PREF_LOGGED_SOCIAL, "");
        if (logged.equalsIgnoreCase("twitter")) {
            logoutTwitter(context);
        } else if (logged.equalsIgnoreCase("google+")) {
            logoutGooglePlus(context);
        }
    }

    private static void logoutGooglePlus(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(SocialConstants.PREF_LOGGED_SOCIAL).commit();

    }

    private static void logoutTwitter(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(SocialConstants.PREF_TWITTER_AUTH_TOKEN).remove(SocialConstants.PREF_TWITTER_AUTH_TOKEN_SECRET)
                .remove(SocialConstants.PREF_LOGGED_SOCIAL).commit();

    }

    private static void postToTwitter(final Activity context, final String message) {
        TwitterFactory twitterFactory = new TwitterFactory();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        AccessToken accessToken = new AccessToken(prefs.getString(SocialConstants.PREF_TWITTER_AUTH_TOKEN, ""),
                prefs.getString(SocialConstants.PREF_TWITTER_AUTH_TOKEN_SECRET, ""));
        final Twitter twitter = twitterFactory.getInstance();
        twitter.setOAuthConsumer(SocialConstants.TWITTER_CONSUMER_KEY, SocialConstants.TWITTER_CONSUMER_SECRET);
        twitter.setOAuthAccessToken(accessToken);
        new Thread() {
            @Override
            public void run() {
                try {
                    twitter.updateStatus(message);
                } catch (TwitterException e) {
                    Log.e("Social", e.getMessage(), e);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Falha ao publicar tweet", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }.start();
    }
}
