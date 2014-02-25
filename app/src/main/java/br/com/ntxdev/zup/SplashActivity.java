package br.com.ntxdev.zup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import br.com.ntxdev.zup.task.Updater;
import br.com.ntxdev.zup.util.NetworkUtils;

public class SplashActivity extends Activity {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
		
		setContentView(R.layout.activity_splash);

        if (checkPlayServices()) {
            new CategoriaUpdater().execute();
        }
	}

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
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
			} else {
                if (error) {
                    new AlertDialog.Builder(SplashActivity.this)
                            .setTitle("Falha na sincronização")
                            .setMessage("Não foi possível realizar o sincronismo de dados. Deseja tentar novamente?")
                            .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    new CategoriaUpdater().execute();
                                }
                            })
                            .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "Conexão com a Internet indisponível", Toast.LENGTH_SHORT).show();
                }
			}
			finish();
		}
	}

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000).show();
            } else {
                Log.i("ZUP", "Dispositivo não suportado.");
                finish();
            }
            return false;
        }
        return true;
    }
}
