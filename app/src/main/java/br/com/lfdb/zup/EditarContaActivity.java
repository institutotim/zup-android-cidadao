package br.com.lfdb.zup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.apache.OkApacheClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.domain.Usuario;
import br.com.lfdb.zup.service.LoginService;
import br.com.lfdb.zup.service.UsuarioService;
import br.com.lfdb.zup.social.SocialConstants;
import br.com.lfdb.zup.social.auth.FacebookAuth;
import br.com.lfdb.zup.social.auth.GooglePlusAuth;
import br.com.lfdb.zup.social.auth.TwitterAuth;
import br.com.lfdb.zup.social.util.SocialUtils;
import br.com.lfdb.zup.util.FontUtils;
import br.com.lfdb.zup.util.ViewUtils;

public class EditarContaActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_CODE = 9999;

    private EditText campoNome;
	private EditText campoSenha;
	private EditText campoConfirmarSenha;
	private EditText campoEmail;
	private EditText campoCPF;
	private EditText campoTelefone;
	private EditText campoEndereco;
	private EditText campoComplemento;
	private EditText campoCEP;
	private EditText campoBairro;
	private Usuario usuario;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editar_conta);
		
		((TextView) findViewById(R.id.editarConta)).setTypeface(FontUtils.getLight(this));
		((TextView) findViewById(R.id.instrucoes)).setTypeface(FontUtils.getBold(this));
		((TextView) findViewById(R.id.instrucoes_dados)).setTypeface(FontUtils.getBold(this));
		((TextView) findViewById(R.id.textView1)).setTypeface(FontUtils.getLight(this));

        TextView botaoCancelar = (TextView) findViewById(R.id.botaoCancelar);
		botaoCancelar.setTypeface(FontUtils.getRegular(this));
		botaoCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.hideKeyboard(EditarContaActivity.this, v.getWindowToken());
                finish();
            }
        });

        TextView botaoCriar = (TextView) findViewById(R.id.botaoSalvar);
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
		
		preencherTela();
	}

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ImageButton botaoFacebook = (ImageButton) findViewById(R.id.botao_logar_facebook);
        botaoFacebook.setOnClickListener(this);
        if (!prefs.getString(SocialConstants.PREF_LOGGED_SOCIAL, "").equals("facebook")) {
            botaoFacebook.setImageResource(R.drawable.btn_logar_facebook_logoff);
        } else {
            botaoFacebook.setImageResource(R.drawable.btn_logar_facebook);
        }

        ImageButton botaoTwitter = (ImageButton) findViewById(R.id.botao_logar_twitter);
        botaoTwitter.setOnClickListener(this);
        if (!prefs.getString(SocialConstants.PREF_LOGGED_SOCIAL, "").equals("twitter")) {
            botaoTwitter.setImageResource(R.drawable.btn_logar_twitter_logoff);
        } else {
            botaoTwitter.setImageResource(R.drawable.btn_logar_twitter);
        }

        ImageButton botaoGoogle = (ImageButton) findViewById(R.id.botao_logar_google);
        botaoGoogle.setOnClickListener(this);
        if (!prefs.getString(SocialConstants.PREF_LOGGED_SOCIAL, "").equals("google")) {
            botaoGoogle.setImageResource(R.drawable.btn_logar_google_logoff);
        } else {
            botaoGoogle.setImageResource(R.drawable.btn_logar_google);
        }
    }

    @Override
	public void onClick(View v) {
        if (v.getId() == R.id.botaoSalvar) {
            limparFundoCampos();
            List<Integer> validadores = validar();
            if (validadores.isEmpty()) {
                montarUsuario();
                new Tasker().execute();
            } else {
                destacarCampos(validadores);
            }
        } else if (v.getId() == R.id.botao_logar_twitter) {
            if (isLogged("twitter")) {
                logout("twitter", (ImageButton) v);
            } else {
                startActivityForResult(new Intent(this, TwitterAuth.class), REQUEST_CODE);
            }
        } else if (v.getId() == R.id.botao_logar_google) {
            if (isLogged("google")) {
                logout("google+", (ImageButton) v);
            } else {
                startActivityForResult(new Intent(this, GooglePlusAuth.class), REQUEST_CODE);
            }
        } else if (v.getId() == R.id.botao_logar_facebook) {
            if (isLogged("facebook")) {
                logout("facebook", (ImageButton) v);
            } else {
                startActivityForResult(new Intent(this, FacebookAuth.class), REQUEST_CODE);
            }
        }
	}

    private void logout(final String social, final ImageButton imgButton) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout_social))
                .setMessage(getString(R.string.logout_social_message, StringUtils.capitalize(social)))
                .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Activity context = EditarContaActivity.this;
                        dialog.dismiss();
                        SocialUtils.logout(context);
                        int resource = context.getResources().getIdentifier(String.format("btn_logar_%s_logoff", social.replace("+", "")), "drawable", context.getPackageName());
                        imgButton.setImageResource(resource);
                    }
                })
                .setNegativeButton(R.string.nao, null)
                .show();
    }

    private boolean isLogged(String social) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString(SocialConstants.PREF_LOGGED_SOCIAL, "").equals(social);
    }
	
	private Usuario montarUsuario() {
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
		return usuario;
	}
	
	private void preencherTela() {
		usuario = new UsuarioService().getUsuarioAtivo(this);
		if (usuario != null) {
			if (usuario.getNome() != null) campoNome.setText(usuario.getNome());
			if (usuario.getEmail() != null) campoEmail.setText(usuario.getEmail());
			if (usuario.getCpf() != null) campoCPF.setText(usuario.getCpf());
			if (usuario.getTelefone() != null) campoTelefone.setText(usuario.getTelefone());
			if (usuario.getEndereco() != null) campoEndereco.setText(usuario.getEndereco());
			if (usuario.getComplemento() != null) campoComplemento.setText(usuario.getComplemento());
			if (usuario.getCep() != null) campoCEP.setText(usuario.getCep());
			if (usuario.getBairro() != null) campoBairro.setText(usuario.getBairro());
		}
	}
	
	public class Tasker extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(EditarContaActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
			dialog.setMessage("Por favor, aguarde...");
			dialog.show();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected String doInBackground(Void... params) {
			try {
				HttpClient client = new OkApacheClient();
				HttpPut put = new HttpPut(Constantes.REST_URL + "/users/" + usuario.getId());
				JSONObject json = new UsuarioService().converterParaJSON(usuario);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(json.length());
				Iterator<String> iterator = json.keys();
				while (iterator.hasNext()) {
					String key = iterator.next();
					if (!json.isNull(key) && !json.getString(key).isEmpty()) {
						nameValuePairs.add(new BasicNameValuePair(key, json.getString(key)));
					}
				}
				put.setHeader("X-App-Token", new LoginService().getToken(EditarContaActivity.this));
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
				try {
					new LoginService().atualizarUsuario(EditarContaActivity.this, new UsuarioService().converterParaJSON(usuario));
				} catch (JSONException e) {
					Log.e("ZUP", e.getMessage(), e);
				}
				Toast.makeText(EditarContaActivity.this, "Dados atualizados com sucesso", Toast.LENGTH_LONG).show();
				setResult(Activity.RESULT_OK);
				finish();
			} else {
				Toast.makeText(EditarContaActivity.this, "Falha no atualização dos dados", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void destacarCampos(List<Integer> campos) {
		for (Integer id : campos) {
			findViewById(id).setBackgroundResource(R.drawable.textbox_red);
		}
		Toast.makeText(this, "Complete ou corrija os campos indicados", Toast.LENGTH_LONG).show();
	}
	
	private void limparFundoCampos() {
		for (Integer id : Arrays.asList(R.id.campoNome, R.id.campoEmail, R.id.campoCPF, R.id.campoTelefone,
				R.id.campoEndereco, R.id.campoCEP, R.id.campoBairro, R.id.campoSenha, R.id.campoConfirmarSenha)) {
			findViewById(id).setBackgroundResource(R.drawable.textbox_bg);
		}
	}
	
	private List<Integer> validar() {
		List<Integer> campos = new ArrayList<Integer>();
		if (!campoSenha.getText().toString().trim().isEmpty() && !campoConfirmarSenha.getText().toString().trim().isEmpty() 
				&& !campoSenha.getText().toString().equals(campoConfirmarSenha.getText().toString())) {
			campos.add(campoSenha.getId());
			campos.add(campoConfirmarSenha.getId());
		}		
		
		for (Integer id : Arrays.asList(R.id.campoNome, R.id.campoEmail, R.id.campoCPF, R.id.campoTelefone,
				R.id.campoEndereco, R.id.campoCEP, R.id.campoBairro)) {
			if (((TextView) findViewById(id)).getText().toString().trim().isEmpty()) {
				campos.add(id);
			}
		}
		
		return campos;
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, getString(R.string.failed_social_auth), Toast.LENGTH_LONG).show();
            }
        }
    }
}
