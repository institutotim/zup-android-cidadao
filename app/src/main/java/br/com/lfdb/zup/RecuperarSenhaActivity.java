package br.com.lfdb.zup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.apache.OkApacheClient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.util.FontUtils;

public class RecuperarSenhaActivity extends Activity implements View.OnClickListener {

    private EditText campoEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recuperar_senha);

		((TextView) findViewById(R.id.esqueceuSenha)).setTypeface(FontUtils.getLight(this));

        TextView botaoVoltar = (TextView) findViewById(R.id.botaoVoltar);
		botaoVoltar.setTypeface(FontUtils.getRegular(this));
		botaoVoltar.setOnClickListener(v -> finish());

        TextView botaoEnviar = (TextView) findViewById(R.id.botaoEnviar);
		botaoEnviar.setTypeface(FontUtils.getRegular(this));
		botaoEnviar.setOnClickListener(this);
		
		campoEmail = (EditText) findViewById(R.id.campoEmail);
		campoEmail.setTypeface(FontUtils.getLight(this));
	}

	@Override
	public void onClick(View v) {
		new Tasker().execute();
	}
	
	public class Tasker extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(RecuperarSenhaActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
			dialog.setMessage("Por favor, aguarde...");
			dialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				HttpClient client = new OkApacheClient();
				HttpPut put = new HttpPut(Constantes.REST_URL + "/recover_password");
				List<NameValuePair> nameValuePairs = new ArrayList<>(1);
				nameValuePairs.add(new BasicNameValuePair("email", campoEmail.getText().toString()));
				put.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = client.execute(put);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					return EntityUtils.toString(response.getEntity(), "UTF-8");
				}
			} catch (Exception e) {
				Log.e("ZUP", e.getMessage(), e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			if (result != null) {
				Toast.makeText(RecuperarSenhaActivity.this, "Solicitação enviada com sucesso. Verifique seu e-mail", Toast.LENGTH_LONG).show();
				setResult(Activity.RESULT_OK);
				finish();
			} else {
				Toast.makeText(RecuperarSenhaActivity.this, "Falha no envio da solicitação", Toast.LENGTH_LONG).show();
			}
		}
	}
}
