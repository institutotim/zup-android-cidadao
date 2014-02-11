package br.com.ntxdev.zup;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import br.com.ntxdev.zup.core.Constantes;
import br.com.ntxdev.zup.service.LoginService;
import br.com.ntxdev.zup.util.FontUtils;

public class LoginActivity extends Activity implements View.OnClickListener {

	private TextView botaoCancelar;
	private TextView botaoEntrar;
	private EditText campoSenha;
	private EditText campoEmail;
	private TextView linkEsqueciSenha;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		((TextView) findViewById(R.id.login)).setTypeface(FontUtils.getLight(this));
		
		linkEsqueciSenha = (TextView) findViewById(R.id.linkEsqueciSenha);
		linkEsqueciSenha.setTypeface(FontUtils.getBold(this));
		linkEsqueciSenha.setOnClickListener(this);
		
		botaoCancelar = (TextView) findViewById(R.id.botaoCancelar);
		botaoCancelar.setTypeface(FontUtils.getRegular(this));
		botaoCancelar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();				
			}
		});
		botaoEntrar = (TextView) findViewById(R.id.botaoEntrar);
		botaoEntrar.setTypeface(FontUtils.getRegular(this));
		botaoEntrar.setOnClickListener(this);
		
		campoSenha = (EditText) findViewById(R.id.campoSenha);
		campoSenha.setTypeface(FontUtils.getLight(this));
		
		campoEmail = (EditText) findViewById(R.id.campoEmail);
		campoEmail.setTypeface(FontUtils.getLight(this));
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == linkEsqueciSenha.getId()) {
			startActivity(new Intent(this, RecuperarSenhaActivity.class));
		} else if (v.getId() == botaoEntrar.getId()) {
			new Tasker().execute();
		}
	}
	
	public class Tasker extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(LoginActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
			dialog.setMessage("Por favor, aguarde...");
			dialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(Constantes.REST_URL + "/authenticate");
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("email", campoEmail.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("password", campoSenha.getText().toString()));
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = client.execute(post);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
					return EntityUtils.toString(response.getEntity(), "UTF-8");
				}
			} catch (Exception e) {
				Log.e("ZUP", e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			if (result != null) {
				try {
				JSONObject json = new JSONObject(result);
					new LoginService().registrarLogin(LoginActivity.this, 
							json.getJSONObject("user"), 
							json.getString("token"));
				} catch (JSONException e) {
					Log.e("ZUP", e.getMessage());
				}
				Toast.makeText(LoginActivity.this, "Login realizado com sucesso", Toast.LENGTH_LONG).show();
				setResult(Activity.RESULT_OK);
				finish();
			} else {
				Toast.makeText(LoginActivity.this, "Falha no login", Toast.LENGTH_LONG).show();
			}
		}
	}
}
