package br.com.ntxdev.zup;

import java.util.ArrayList;
import java.util.Iterator;
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
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import br.com.ntxdev.zup.core.Constantes;
import br.com.ntxdev.zup.domain.Usuario;
import br.com.ntxdev.zup.service.LoginService;
import br.com.ntxdev.zup.service.UsuarioService;
import br.com.ntxdev.zup.social.FacebookAuth;
import br.com.ntxdev.zup.social.GooglePlusAuth;
import br.com.ntxdev.zup.social.TwitterAuth;
import br.com.ntxdev.zup.util.FontUtils;

public class CadastroActivity extends Activity implements OnClickListener {

	private static final int REQUEST_CODE = 9999;
	
	private TextView botaoCancelar;
	private TextView botaoCriar;

	private EditText campoNome;
	private EditText campoEmail;
	private EditText campoSenha;
	private EditText campoConfirmarSenha;
	private EditText campoCPF;
	private EditText campoTelefone;
	private EditText campoEndereco;
	private EditText campoComplemento;
	private EditText campoCEP;
	private EditText campoBairro;
	
	private ImageButton botaoTwitter;
	private ImageButton botaoFacebook;
	private ImageButton botaoGooglePlus;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cadastro);

		((TextView) findViewById(R.id.novaConta)).setTypeface(FontUtils.getLight(this));
		((TextView) findViewById(R.id.textView1)).setTypeface(FontUtils.getLight(this));
		((TextView) findViewById(R.id.textView2)).setTypeface(FontUtils.getLight(this));

		botaoCancelar = (TextView) findViewById(R.id.botaoCancelar);
		botaoCancelar.setTypeface(FontUtils.getRegular(this));
		botaoCancelar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();				
			}
		});
		botaoCriar = (TextView) findViewById(R.id.botaoCriar);
		botaoCriar.setTypeface(FontUtils.getRegular(this));
		botaoCriar.setOnClickListener(this);

		campoNome = (EditText) findViewById(R.id.campoNome);
		campoNome.setTypeface(FontUtils.getLight(this));

		campoSenha = (EditText) findViewById(R.id.campoSenha);
		campoSenha.setTypeface(FontUtils.getLight(this));

		campoConfirmarSenha = (EditText) findViewById(R.id.campoConfirmarSenha);
		campoConfirmarSenha.setTypeface(FontUtils.getLight(this));

		campoEmail = (EditText) findViewById(R.id.campoEmail);
		campoEmail.setTypeface(FontUtils.getLight(this));
		
		campoCPF = (EditText) findViewById(R.id.campoCPF);
		campoCPF.setTypeface(FontUtils.getLight(this));
		
		campoTelefone = (EditText) findViewById(R.id.campoTelefone);
		campoTelefone.setTypeface(FontUtils.getLight(this));
		
		campoEndereco = (EditText) findViewById(R.id.campoEndereco);
		campoEndereco.setTypeface(FontUtils.getLight(this));
		
		campoComplemento = (EditText) findViewById(R.id.campoComplemento);
		campoComplemento.setTypeface(FontUtils.getLight(this));
		
		campoCEP = (EditText) findViewById(R.id.campoCEP);
		campoCEP.setTypeface(FontUtils.getLight(this));
		
		campoBairro = (EditText) findViewById(R.id.campoBairro);
		campoBairro.setTypeface(FontUtils.getLight(this));
		
		botaoFacebook = (ImageButton) findViewById(R.id.botao_logar_facebook);
		botaoFacebook.setOnClickListener(this);
		botaoTwitter = (ImageButton) findViewById(R.id.botao_logar_twitter);
		botaoTwitter.setOnClickListener(this);
		botaoGooglePlus = (ImageButton) findViewById(R.id.botao_logar_google);
		botaoGooglePlus.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.botao_logar_twitter:
			startActivityForResult(new Intent(this, TwitterAuth.class), REQUEST_CODE);
			break;
		case R.id.botao_logar_facebook:
			startActivityForResult(new Intent(this, FacebookAuth.class), REQUEST_CODE);
			break;
		case R.id.botao_logar_google:
			startActivityForResult(new Intent(this, GooglePlusAuth.class), REQUEST_CODE);
			break;
		case R.id.botaoCriar:
			if (validar()) cadastrar();
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Usuario usuario = (Usuario) data.getSerializableExtra("usuario");
				campoNome.setText(usuario.getNome());
				campoEmail.setText(usuario.getEmail());
				
				botaoTwitter.setVisibility(View.GONE);
				botaoFacebook.setVisibility(View.GONE);
				botaoGooglePlus.setVisibility(View.GONE);
				((TextView) findViewById(R.id.textView1)).setText(R.string.complete_seus_dados);
				((TextView) findViewById(R.id.textView2)).setVisibility(View.GONE);
			}
		}
	}
	
	private boolean validar() {
		if (!campoSenha.getText().toString().equals(campoConfirmarSenha.getText().toString())) {
			return false;
		}		
		return true;
	}
	
	private void cadastrar() {
		Usuario usuario = new Usuario();
		usuario.setBairro(campoBairro.getText().toString());
		usuario.setCep(campoCEP.getText().toString());
		usuario.setComplemento(campoComplemento.getText().toString());
		usuario.setCpf(campoCPF.getText().toString());
		usuario.setEmail(campoEmail.getText().toString());
		usuario.setEndereco(campoEndereco.getText().toString());
		usuario.setNome(campoNome.getText().toString());
		usuario.setTelefone(campoTelefone.getText().toString());
		usuario.setSenha(campoSenha.getText().toString());
		usuario.setConfirmacaoSenha(campoConfirmarSenha.getText().toString());
		new Tasker().execute(usuario);
	}
	
	public class Tasker extends AsyncTask<Usuario, Void, String> {
		
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(CadastroActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
			dialog.setMessage("Por favor, aguarde...");
			dialog.show();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected String doInBackground(Usuario... params) {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(Constantes.REST_URL + "/users");
				JSONObject json = new UsuarioService().converterParaJSON(params[0]);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(json.length());
				Iterator<String> iterator = json.keys();
				while (iterator.hasNext()) {
					String key = iterator.next();
					nameValuePairs.add(new BasicNameValuePair(key, json.getString(key)));
				}
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = client.execute(post);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
					post = new HttpPost(Constantes.REST_URL + "/authenticate");
					nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair("email", params[0].getEmail()));
					nameValuePairs.add(new BasicNameValuePair("password", params[0].getSenha()));
					post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					response = client.execute(post);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
						return EntityUtils.toString(response.getEntity());
					}
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
					new LoginService().registrarLogin(CadastroActivity.this, 
							json.getJSONObject("user"), 
							json.getString("token"));
				} catch (JSONException e) {
					Log.e("ZUP", e.getMessage());
				}
				Toast.makeText(CadastroActivity.this, "Login realizado com sucesso", Toast.LENGTH_LONG).show();
				setResult(Activity.RESULT_OK);
				finish();
			} else {
				Toast.makeText(CadastroActivity.this, "Falha no cadastro", Toast.LENGTH_LONG).show();
			}
		}
	}
}
