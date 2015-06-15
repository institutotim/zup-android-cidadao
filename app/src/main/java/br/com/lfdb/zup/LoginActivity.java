package br.com.lfdb.zup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
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
import java.util.List;

import br.com.lfdb.zup.base.BaseActivity;
import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.service.LoginService;
import br.com.lfdb.zup.util.FontUtils;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

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

        TextView botaoCancelar = (TextView) findViewById(R.id.botaoCancelar);
        botaoCancelar.setTypeface(FontUtils.getRegular(this));
        botaoCancelar.setOnClickListener(v -> finish());
        botaoEntrar = (TextView) findViewById(R.id.botaoEntrar);
        botaoEntrar.setTypeface(FontUtils.getRegular(this));
        botaoEntrar.setOnClickListener(this);

        campoSenha = (EditText) findViewById(R.id.campoSenha);
        campoSenha.setTypeface(FontUtils.getLight(this));
        campoSenha.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_GO) {
                new Tasker().execute();
                handled = true;
            }
            return handled;
        });

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

    @Override
    protected String getScreenName() {
        return "Login";
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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(LoginActivity.this);
                prefs.edit().putString("gcm", gcm.register(LoginActivity.this.getString(R.string.gcm_project))).apply();

                HttpClient client = new OkApacheClient();
                HttpPost post = new HttpPost(Constantes.REST_URL + "/authenticate");
                List<NameValuePair> nameValuePairs = new ArrayList<>(2);
                nameValuePairs.add(new BasicNameValuePair("email", campoEmail.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("password", campoSenha.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("device_type", "android"));
                nameValuePairs.add(new BasicNameValuePair("device_token", PreferenceManager.getDefaultSharedPreferences(LoginActivity.this)
                        .getString("gcm", "")));
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = client.execute(post);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
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
                    JSONObject json = new JSONObject(result);
                    new LoginService().registrarLogin(LoginActivity.this,
                            json.getJSONObject("user"),
                            json.getString("token"));
                } catch (JSONException e) {
                    Log.e("ZUP", e.getMessage(), e);
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
