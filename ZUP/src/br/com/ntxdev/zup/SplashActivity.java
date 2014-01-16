package br.com.ntxdev.zup;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import br.com.ntxdev.zup.task.Updater;
import br.com.ntxdev.zup.util.NetworkUtils;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageView image = new ImageView(this);
		image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		image.setImageResource(R.drawable.ic_splash);
		setContentView(image);

		new CategoriaUpdater().execute();
	}

	@Override
	public void onBackPressed() {
	}

	private class CategoriaUpdater extends AsyncTask<Void, Void, Boolean> {

		long start, finish;

		@Override
		protected void onPreExecute() {
			start = System.currentTimeMillis();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!NetworkUtils.isInternetPresent(SplashActivity.this))
				return Boolean.FALSE;

			new Updater().update(SplashActivity.this);
			return Boolean.TRUE;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				finish = System.currentTimeMillis();
				if (finish - start < 3500)
					try {
						Thread.sleep(finish - start);
					} catch (Exception e) {
						Log.w("ZUP", e.getMessage());
					}
				Intent intent = new Intent(SplashActivity.this, OpeningActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(getApplicationContext(), "Conexão com a Internet indisponível", Toast.LENGTH_SHORT).show();
			}
			finish();
		}
	}
}
