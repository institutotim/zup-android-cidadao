package br.com.ntxdev.zup.social;

import java.util.Arrays;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import br.com.ntxdev.zup.social.twitter.TwitterSession;

import com.facebook.AccessToken;
import com.facebook.AccessTokenSource;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.google.android.gms.plus.PlusShare;

public class SocialUtil {

	public static final int FACEBOOK = 1;
	public static final int TWITTER = 2;
	public static final int GOOGLE_PLUS = 3;

	public static void postar(Context context, String mensagem) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int social = prefs.getInt("social", -1);

		switch (social) {
		case FACEBOOK:
			postarFacebook(context, mensagem);
			break;
		case TWITTER:
			postarTwitter(context, mensagem);
			break;
		case GOOGLE_PLUS:
			postarGooglePlus(context, mensagem);
			break;
		default:
			// Not signed
			break;
		}
	}

	public static void saveSigned(Context context, int social) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putInt("social", social).commit();
	}

	private static void postarTwitter(Context context, String mensagem) {
		try {
			TwitterFactory factory = new TwitterFactory();
			twitter4j.auth.AccessToken accessToken = new TwitterSession(context).getAccessToken();
			Twitter twitter = factory.getInstance();
			twitter.setOAuthConsumer(TwitterAuth.CONSUMER_KEY, TwitterAuth.CONSUMER_SECRET);
			twitter.setOAuthAccessToken(accessToken);
			Status status = twitter.updateStatus(mensagem);
			Toast.makeText(context, "Successfully updated the status to [" + status.getText() + "].", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private static void postarFacebook(final Context context, final String mensagem) {
		SharedPreferences prefs = context.getSharedPreferences("facebook-session", Context.MODE_PRIVATE);
		AccessToken token = AccessToken.createFromExistingAccessToken(prefs.getString("access_token", ""),
				new Date(prefs.getLong("expires_in", 0)), null, AccessTokenSource.CLIENT_TOKEN,
				Arrays.asList("basic_info", "email", "publish_actions"));
		Session.openActiveSessionWithAccessToken(context, token, new Session.StatusCallback() {

			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if (session.isOpened()) {

					Bundle postParams = new Bundle();
					postParams.putString("message", mensagem);
					// postParams.putString("name", "Facebook SDK for Android");
					// postParams.putString("caption",
					// "Build great social apps and get more installs.");
					// postParams.putString("description",
					// "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
					// postParams.putString("link",
					// "https://developers.facebook.com/android");
					// postParams.putString("picture",
					// "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");

					Request.Callback callback = new Request.Callback() {
						public void onCompleted(Response response) {
							JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
							String postId = null;
							try {
								postId = graphResponse.getString("id");
							} catch (JSONException e) {
								Log.e("Social Me", "JSON error " + e.getMessage());
							}
							FacebookRequestError error = response.getError();
							if (error != null) {
								Toast.makeText(context, error.getErrorMessage(), Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(context, postId, Toast.LENGTH_LONG).show();
							}
						}
					};

					Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);

					RequestAsyncTask task = new RequestAsyncTask(request);
					task.execute();
				}
			}
		});
	}

	private static void postarGooglePlus(Context context, String mensagem) {
		Intent shareIntent = new PlusShare.Builder(context).setType("text/plain").setText(mensagem)
				.getIntent();

		((Activity) context).startActivityForResult(shareIntent, 0);
	}
}
