package br.com.ntxdev.zup.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import br.com.ntxdev.zup.domain.Usuario;
import br.com.ntxdev.zup.social.twitter.TwitterApp;
import br.com.ntxdev.zup.social.twitter.TwitterApp.TwDialogListener;

public class TwitterAuth extends Activity {

	private TwitterApp mTwitter;
	Button mBtnTwitter;
	public static final String CONSUMER_KEY = "KYdPXY0D2YJBQu54LXRnAg";
	public static final String CONSUMER_SECRET = "4WbLHvYdFwgSd9tBPRebXpBbwG5cl8Uc0Dev5TUhD8A";

	private enum FROM {
		TWITTER_POST, TWITTER_LOGIN
	};

	private enum MESSAGE {
		SUCCESS, DUPLICATE, FAILED, CANCELLED
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		mTwitter = new TwitterApp(this, CONSUMER_KEY, CONSUMER_SECRET);

		mTwitter.setListener(mTwLoginDialogListener);
		mTwitter.resetAccessToken();
		if (mTwitter.hasAccessToken()) {
			try {
				returnUser();
			} catch (Exception e) {
				if (e.getMessage().toString().contains("duplicate")) {
					postAsToast(FROM.TWITTER_POST, MESSAGE.DUPLICATE);
				}
				Log.e("ZUP", e.getMessage(), e);
				mTwitter.resetAccessToken();
			}			
		} else {
			mTwitter.authorize();
		}
	}

	private void postAsToast(FROM twitterPost, MESSAGE success) {
		switch (twitterPost) {
		case TWITTER_LOGIN:
			switch (success) {			
			case FAILED:
				setResult(Activity.RESULT_CANCELED);
				finish();
			default:
				break;
			}
			break;
		case TWITTER_POST:
			switch (success) {
			case SUCCESS:
				Toast.makeText(this, "Posted Successfully", Toast.LENGTH_LONG).show();
				break;
			case FAILED:
				Toast.makeText(this, "Posting Failed", Toast.LENGTH_LONG).show();
				break;
			case DUPLICATE:
				Toast.makeText(this, "Posting Failed because of duplicate message...", Toast.LENGTH_LONG).show();
			default:
				break;
			}
			break;
		}
	}

	private TwDialogListener mTwLoginDialogListener = new TwDialogListener() {

		public void onError(String value) {
			postAsToast(FROM.TWITTER_LOGIN, MESSAGE.FAILED);
			Log.e("TWITTER", value);
			mTwitter.resetAccessToken();
		}

		public void onComplete(String value) {
			try {				
				returnUser();
				return;
			} catch (Exception e) {
				if (e.getMessage().toString().contains("duplicate")) {
					postAsToast(FROM.TWITTER_POST, MESSAGE.DUPLICATE);
				}
				e.printStackTrace();
			}
			mTwitter.resetAccessToken();
		}
	};
	
	public void returnUser() {
		Usuario usuario = new Usuario();
		usuario.setNome(mTwitter.getUsername());
		Intent i = new Intent();
		i.putExtra("usuario", usuario);
		setResult(Activity.RESULT_OK, i);
		
		SocialUtil.saveSigned(this, SocialUtil.TWITTER);
		
		finish();
	}
}
