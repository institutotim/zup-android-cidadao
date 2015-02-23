package br.com.lfdb.zup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.apache.OkApacheClient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
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
import br.com.lfdb.zup.service.FeatureService;
import br.com.lfdb.zup.service.LoginService;
import br.com.lfdb.zup.service.UsuarioService;
import br.com.lfdb.zup.util.FontUtils;
import br.com.lfdb.zup.validador.CpfValidador;

public class CadastroActivity extends Activity implements OnClickListener {

    private static final int REQUEST_SOCIAL = 9876;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        ((TextView) findViewById(R.id.novaConta)).setTypeface(FontUtils.getLight(this));

        TextView botaoCancelar = (TextView) findViewById(R.id.botaoCancelar);
        botaoCancelar.setTypeface(FontUtils.getRegular(this));
        botaoCancelar.setOnClickListener(v -> finish());
        TextView botaoCriar = (TextView) findViewById(R.id.botaoCriar);
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
        campoBairro.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_GO) {
                validarECadastrar();
                handled = true;
            }
            return handled;
        });

        TextView termos = (TextView) findViewById(R.id.termos);
        termos.setText(Html.fromHtml(getString(R.string.termos_de_uso_cadastro)));
        termos.setTypeface(FontUtils.getLight(this));
        termos.setOnClickListener(v -> startActivity(new Intent(CadastroActivity.this, TermosDeUsoActivity.class)));
    }

    private void validarECadastrar() {
        limparFundoCampos();
        List<Integer> validadores = validar();
        if (validadores.isEmpty()) {
            if (FeatureService.getInstance(this).isAnySocialEnabled()) {
                startActivityForResult(new Intent(this, RedesSociaisCadastroActivity.class), REQUEST_SOCIAL);
            } else {
                cadastrar();
            }
        } else {
            destacarCampos(validadores);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.botaoCriar:
                validarECadastrar();
                break;
        }
    }

    private List<Integer> validar() {
        List<Integer> campos = new ArrayList<>();
        if (campoSenha.getText().toString().trim().isEmpty() || campoConfirmarSenha.getText().toString().trim().isEmpty() ||
                !campoSenha.getText().toString().equals(campoConfirmarSenha.getText().toString())) {
            campos.add(campoSenha.getId());
            campos.add(campoConfirmarSenha.getId());
        }

        for (Integer id : Arrays.asList(R.id.campoNome, R.id.campoEmail, R.id.campoCPF, R.id.campoTelefone,
                R.id.campoEndereco, R.id.campoCEP, R.id.campoBairro)) {
            if (((TextView) findViewById(id)).getText().toString().trim().isEmpty()) {
                campos.add(id);
            }
        }

        if (!CpfValidador.isValid(campoCPF.getText().toString().trim().replace("-", "").replace(".", ""))) {
            campos.add(campoCPF.getId());
        }

        return campos;
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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CadastroActivity.this);
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(CadastroActivity.this);
                prefs.edit().putString("gcm", gcm.register(CadastroActivity.this.getString(R.string.gcm_project))).apply();

                HttpClient client = new OkApacheClient();
                HttpPost post = new HttpPost(Constantes.REST_URL + "/users");
                JSONObject json = new UsuarioService().converterParaJSON(params[0]);
                List<NameValuePair> nameValuePairs = new ArrayList<>(json.length());
                Iterator<String> iterator = json.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    nameValuePairs.add(new BasicNameValuePair(key, json.getString(key)));
                }
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = client.execute(post);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                    post = new HttpPost(Constantes.REST_URL + "/authenticate");
                    nameValuePairs = new ArrayList<>(2);
                    nameValuePairs.add(new BasicNameValuePair("email", params[0].getEmail()));
                    nameValuePairs.add(new BasicNameValuePair("password", params[0].getSenha()));
                    nameValuePairs.add(new BasicNameValuePair("device_type", "android"));
                    nameValuePairs.add(new BasicNameValuePair("device_token", PreferenceManager.getDefaultSharedPreferences(CadastroActivity.this)
                            .getString("gcm", "")));
                    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    response = client.execute(post);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                        return EntityUtils.toString(response.getEntity(), "UTF-8");
                    }
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
                    JSONObject json = new JSONObject(result);
                    new LoginService().registrarLogin(CadastroActivity.this,
                            json.getJSONObject("user"),
                            json.getString("token"));
                } catch (JSONException e) {
                    Log.e("ZUP", e.getMessage(), e);
                }
                Toast.makeText(CadastroActivity.this, "Login realizado com sucesso", Toast.LENGTH_LONG).show();
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                Toast.makeText(CadastroActivity.this, "Falha no cadastro", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SOCIAL && resultCode == Activity.RESULT_OK) {
            cadastrar();
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
}
