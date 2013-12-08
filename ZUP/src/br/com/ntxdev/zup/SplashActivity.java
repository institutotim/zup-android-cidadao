package br.com.ntxdev.zup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageView image = new ImageView(this);
		image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		image.setImageResource(R.drawable.ic_splash);
		setContentView(image);
		
		IntentLauncher launcher = new IntentLauncher();
		launcher.start();
	}

	@Override
	public void onBackPressed() {
	}

	private class IntentLauncher extends Thread {

		@Override
		public void run() {
			try {
				Thread.sleep(3500);
			} catch (Exception e) {
				Log.w(getClass().getSimpleName(), e.getMessage(), e);
			}

			Intent intent = new Intent(SplashActivity.this, OpeningActivity.class);
			SplashActivity.this.startActivity(intent);
			SplashActivity.this.finish();
		}
	}
}
