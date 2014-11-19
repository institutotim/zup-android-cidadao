package br.com.lfdb.zup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import br.com.lfdb.zup.task.Updater;
import br.com.lfdb.zup.util.NetworkUtils;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class SplashActivity extends Activity {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!BuildConfig.DEBUG) Fabric.with(this, new Crashlytics());

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
		
		setContentView(R.layout.activity_splash);

        new CategoriaUpdater().execute();
	}

    @Override
	public void onBackPressed() {
	}

	private class CategoriaUpdater extends AsyncTask<Void, Void, Boolean> {

		long start, finish;
        boolean error = false;

		@Override
		protected void onPreExecute() {
			start = System.currentTimeMillis();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!NetworkUtils.isInternetPresent(SplashActivity.this))
				return Boolean.FALSE;

            try {
			    new Updater().update(SplashActivity.this);
            } catch (Exception e) {
                error = true;
                return Boolean.FALSE;
            }
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
                finish();
			} else {
                if (error) {
                    new AlertDialog.Builder(SplashActivity.this)
                            .setTitle("Falha na sincronização")
                            .setMessage("Não foi possível realizar o sincronismo de dados. Deseja tentar novamente?")
                            .setPositiveButton("Sim", (dialog, which) -> {
                                dialog.dismiss();
                                new CategoriaUpdater().execute();
                            })
                            .setNegativeButton("Não", (dialog, which) -> {
                                dialog.dismiss();
                                finish();
                            })
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "Conexão com a Internet indisponível", Toast.LENGTH_SHORT).show();
                    finish();
                }
			}
		}
	}
}
