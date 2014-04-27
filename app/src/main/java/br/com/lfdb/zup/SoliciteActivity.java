package br.com.lfdb.zup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.apache.OkApacheClient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.domain.CategoriaRelato;
import br.com.lfdb.zup.domain.Solicitacao;
import br.com.lfdb.zup.domain.SolicitacaoListItem;
import br.com.lfdb.zup.fragment.SoliciteDetalhesFragment;
import br.com.lfdb.zup.fragment.SoliciteFotosFragment;
import br.com.lfdb.zup.fragment.SoliciteLocalFragment;
import br.com.lfdb.zup.fragment.SolicitePontoFragment;
import br.com.lfdb.zup.fragment.SoliciteTipoFragment;
import br.com.lfdb.zup.service.LoginService;
import br.com.lfdb.zup.service.UsuarioService;
import br.com.lfdb.zup.social.util.SocialUtils;
import br.com.lfdb.zup.util.DateUtils;
import br.com.lfdb.zup.util.FontUtils;
import br.com.lfdb.zup.util.NetworkUtils;
import br.com.lfdb.zup.util.ViewUtils;

public class SoliciteActivity extends FragmentActivity implements View.OnClickListener {

    public static final int LOGIN_REQUEST = 1578;

    private Passo atual = Passo.TIPO;
    private Solicitacao solicitacao = new Solicitacao();

    private TextView botaoAvancar;

    private SoliciteTipoFragment tipoFragment;
    private SoliciteFotosFragment fotosFragment;
    private SoliciteLocalFragment localFragment;
    private SolicitePontoFragment pontoFragment;
    private SoliciteDetalhesFragment detalhesFragment;

    private enum Passo {
        TIPO, LOCAL, FOTOS, COMENTARIOS
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicite);

        ((TextView) findViewById(R.id.titulo)).setTypeface(FontUtils.getLight(this));

        botaoAvancar = (TextView) findViewById(R.id.botaoAvancar);
        botaoAvancar.setOnClickListener(this);
        botaoAvancar.setTypeface(FontUtils.getRegular(this));
        TextView botaoVoltar = (TextView) findViewById(R.id.botaoVoltar);
        botaoVoltar.setOnClickListener(this);
        botaoVoltar.setTypeface(FontUtils.getRegular(this));

        TextView botaoCancelar = (TextView) findViewById(R.id.botaoCancelar);
        botaoCancelar.setTypeface(FontUtils.getRegular(this));
        botaoCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        if (savedInstanceState != null) {
            solicitacao = new Gson().fromJson(savedInstanceState.getString("solicitacao"), Solicitacao.class);
            atual = new Gson().fromJson(savedInstanceState.getString("passo"), Passo.class);
            restoreFragmentsStates(savedInstanceState);
        } else {
            tipoFragment = new SoliciteTipoFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, tipoFragment).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("solicitacao", new Gson().toJson(solicitacao));
        outState.putString("passo", new Gson().toJson(atual));
        if (fotosFragment != null) {
            outState.putString("imagemTemporaria", fotosFragment.getImagemTemporaria());
        }

        outState.putBoolean("tipo", tipoFragment != null);
        outState.putBoolean("fotos", fotosFragment != null);
        outState.putBoolean("local", localFragment != null);
        outState.putBoolean("ponto", pontoFragment != null);
        outState.putBoolean("detalhes", detalhesFragment != null);
    }

    public void exibirBarraInferior(boolean exibir) {
        findViewById(R.id.barra_navegacao).setVisibility(exibir ? View.VISIBLE : View.GONE);
    }

    public void setReferencia(String referencia) {
        solicitacao.setReferencia(referencia);
    }

    public ArrayList<String> getFotos() {
        return solicitacao.getFotos();
    }

    public void setCategoria(CategoriaRelato categoria) {
        solicitacao.setCategoria(categoria);
        if (new UsuarioService().getUsuarioAtivo(this) == null) {
            startActivityForResult(new Intent(this, WarningActivity.class), LOGIN_REQUEST);
            return;
        }

        if (!categoria.getCategoriasInventario().isEmpty()) {
            if (pontoFragment == null) {
                pontoFragment = new SolicitePontoFragment();
                pontoFragment.setCategoria(categoria);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                if (localFragment != null) {
                    ft.remove(localFragment).commit();
                    ft = getSupportFragmentManager().beginTransaction();
                    localFragment = null;
                }
                ft.add(R.id.fragments_place, pontoFragment).hide(tipoFragment).commitAllowingStateLoss();
            } else {
                getSupportFragmentManager().beginTransaction().hide(tipoFragment).show(pontoFragment).commitAllowingStateLoss();
                pontoFragment.setCategoria(categoria);
            }
        } else {
            if (localFragment == null) {
                localFragment = new SoliciteLocalFragment();
                localFragment.setMarcador(categoria.getMarcador());
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                if (pontoFragment != null) {
                    ft.remove(pontoFragment).commit();
                    ft = getSupportFragmentManager().beginTransaction();
                    pontoFragment = null;
                }
                ft.add(R.id.fragments_place, localFragment).hide(tipoFragment).commitAllowingStateLoss();
            } else {
                getSupportFragmentManager().beginTransaction().hide(tipoFragment).show(localFragment).commitAllowingStateLoss();
                localFragment.setMarcador(categoria.getMarcador());
            }
        }

        atual = Passo.LOCAL;
        exibirBarraInferior(true);
    }

    public void enableNextButton(boolean enabled) {
        botaoAvancar.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    public CategoriaRelato getCategoria() {
        return solicitacao.getCategoria();
    }

    public void setInfo(int string) {
        TextView info = (TextView) findViewById(R.id.instrucoes);
        info.setText(string);
        info.setTypeface(FontUtils.getBold(this));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            info.setText(info.getText().toString().toUpperCase(Locale.US));
        }
    }

    public void adicionarFoto(String foto) {
        solicitacao.adicionarFoto(foto);
    }

    public void removerFoto(String foto) {
        solicitacao.removerFoto(foto);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.botaoAvancar) {
            if (atual.equals(Passo.LOCAL)) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                if (!getCategoria().getCategoriasInventario().isEmpty()) {
                    ft.hide(pontoFragment);
                    solicitacao.setIdItemInventario(pontoFragment.getCategoriaId());
                    solicitacao.setLatitudeLongitude(pontoFragment.getLatitude(), pontoFragment.getLongitude());
                } else {
                    if (!localFragment.validarEndereco()) return;
                    ft.hide(localFragment);
                }

                if (fotosFragment == null) {
                    fotosFragment = new SoliciteFotosFragment();
                    ft.add(R.id.fragments_place, fotosFragment).commit();
                } else {
                    ft.show(fotosFragment).commit();
                }
                atual = Passo.FOTOS;
            } else if (atual.equals(Passo.FOTOS)) {
                if (detalhesFragment == null) {
                    detalhesFragment = new SoliciteDetalhesFragment();
                    getSupportFragmentManager().beginTransaction().hide(fotosFragment).add(R.id.fragments_place, detalhesFragment).commit();
                } else {
                    getSupportFragmentManager().beginTransaction().hide(fotosFragment).show(detalhesFragment).commit();
                }
                botaoAvancar.setText(R.string.publicar);
                atual = Passo.COMENTARIOS;
            } else if (atual.equals(Passo.COMENTARIOS)){
                solicitar();
            }
        } else if (v.getId() == R.id.botaoVoltar) {
            botaoAvancar.setText(R.string.proximo);
            if (atual.equals(Passo.COMENTARIOS)) {
                getSupportFragmentManager().beginTransaction().hide(detalhesFragment).show(fotosFragment).commit();
                atual = Passo.FOTOS;
            } else if (atual.equals(Passo.FOTOS)) {
                if (!getCategoria().getCategoriasInventario().isEmpty()) {
                    getSupportFragmentManager().beginTransaction().hide(fotosFragment).show(pontoFragment).commit();
                    atual = Passo.LOCAL;
                } else {
                    getSupportFragmentManager().beginTransaction().hide(fotosFragment).show(localFragment).commit();
                    atual = Passo.LOCAL;
                }
            } else if (atual.equals(Passo.LOCAL)) {
                if (!getCategoria().getCategoriasInventario().isEmpty()) {
                    getSupportFragmentManager().beginTransaction().hide(pontoFragment).show(tipoFragment).commit();
                } else {
                    getSupportFragmentManager().beginTransaction().hide(localFragment).show(tipoFragment).commit();
                }
                exibirBarraInferior(false);
                atual = Passo.TIPO;
            }
        }
    }

    public void solicitar() {
        solicitacao.setComentario(detalhesFragment.getComentario());
        if (solicitacao.getComentario().length() > 800) {
            alertarTamanhoComentario();
            return;
        }
        if (solicitacao.getCategoria().getCategoriasInventario().isEmpty()) {
            solicitacao.setLatitudeLongitude(localFragment.getLatitudeAtual(), localFragment.getLongitudeAtual());
        } else {
            if (solicitacao.getIdItemInventario() == null) {
                alertarItemInventario();
                return;
            }

            solicitacao.setIdItemInventario(pontoFragment.getCategoriaId());
        }
        enviarSolicitacao();
    }

    private void alertarItemInventario() {
        new AlertDialog.Builder(this)
                .setMessage("O local do relato não foi selecionado corretamente")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void alertarTamanhoComentario() {
        new AlertDialog.Builder(this)
                .setMessage("O comentário deve ter menos de 800 caracteres")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @SuppressLint("NewApi")
    private void enviarSolicitacao() {
        if (!NetworkUtils.isInternetPresent(this)) {
            new AlertDialog.Builder(this).setMessage("Sua conexão com a Internet encontra-se indisponível. Verifique a conexão e tente novamente")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            return;
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            new Tasker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new Tasker().execute();
        }
    }

    public String getReferencia() {
        return solicitacao.getReferencia() != null ? solicitacao.getReferencia() : "";
    }

    public class Tasker extends AsyncTask<Void, Void, SolicitacaoListItem> implements DialogInterface.OnCancelListener{

        private ProgressDialog dialog;
        private HttpPost post;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(SoliciteActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setIndeterminate(true);
            dialog.setMessage("Enviando solicitação...");
            dialog.show();
            dialog.setOnCancelListener(this);
        }

        @Override
        protected SolicitacaoListItem doInBackground(Void... params) {
            try {
                HttpClient client = new OkApacheClient();
                post = new HttpPost(Constantes.REST_URL + "/reports/" + solicitacao.getCategoria().getId() + "/items");

                MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
                multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                multipartEntity.setCharset(Charset.forName("UTF-8"));

                multipartEntity.addTextBody("description", solicitacao.getComentario().trim(), ContentType.APPLICATION_JSON);
                if (solicitacao.getCategoria().getCategoriasInventario().isEmpty()) {
                    multipartEntity.addTextBody("latitude", String.valueOf(solicitacao.getLatitude()));
                    multipartEntity.addTextBody("longitude", String.valueOf(solicitacao.getLongitude()));
                    if (solicitacao.getReferencia() != null && !solicitacao.getReferencia().trim().isEmpty()) {
                        multipartEntity.addTextBody("reference", solicitacao.getReferencia(), ContentType.APPLICATION_JSON);
                    }
                    solicitacao.setEndereco(localFragment.getEnderecoAtual());
                } else {
                    multipartEntity.addTextBody("inventory_item_id", String.valueOf(solicitacao.getIdItemInventario()));
                    solicitacao.setEndereco(pontoFragment.getEndereco());
                    solicitacao.setLatitudeLongitude(pontoFragment.getLatitude(), pontoFragment.getLongitude());
                }
                multipartEntity.addTextBody("address", solicitacao.getEndereco(), ContentType.APPLICATION_JSON);
                multipartEntity.addTextBody("category_id", String.valueOf(solicitacao.getCategoria().getId()));

                for (String foto : solicitacao.getFotos()) {
                    multipartEntity.addPart("images[]", new FileBody(new File(foto)));
                }

                post.setEntity(multipartEntity.build());
                post.setHeader("X-App-Token", new LoginService().getToken(SoliciteActivity.this));

                if (!isCancelled() && !post.isAborted()) {
                    HttpResponse response = client.execute(post);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                        if (detalhesFragment.getPublicar()) {
                            SocialUtils.post(SoliciteActivity.this, "Acabei de publicar uma solicitação com o #ZUP!\n" + solicitacao.getComentario());
                        }
                        return getSolicitacao(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    } else {
                        Log.i("ZUP", new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8")).toString(2));
                    }
                }
            } catch (HttpHostConnectException e) {
                Log.w("ZUP", e.getMessage());
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(SolicitacaoListItem result) {
            dialog.dismiss();
            if (result != null) {
                Toast.makeText(SoliciteActivity.this, "Solicitação enviada com sucesso!", Toast.LENGTH_LONG).show();

                Intent i = new Intent(SoliciteActivity.this, SolicitacaoDetalheActivity.class);
                i.putExtra("solicitacao", result);
                startActivity(i);

                setResult(Activity.RESULT_OK);
                finish();
            } else {
                Toast.makeText(SoliciteActivity.this, "Falha no envio da solicitação", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            if (post != null) post.abort();
            dialog.dismiss();
            Toast.makeText(SoliciteActivity.this, "Envio de solicitação cancelado", Toast.LENGTH_SHORT).show();
            cancel(true);
        }
    }

    public void assertFragmentVisibility() {
        getSupportFragmentManager().beginTransaction().hide(localFragment != null ? localFragment : pontoFragment).show(fotosFragment).commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_REQUEST && resultCode == Activity.RESULT_OK) {
            setCategoria(solicitacao.getCategoria());
        } else if (resultCode == Activity.RESULT_OK) {
            fotosFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private SolicitacaoListItem getSolicitacao(String retorno) throws Exception {
        SolicitacaoListItem item = new SolicitacaoListItem();
        JSONObject json = new JSONObject(retorno).getJSONObject("report");

        item.setComentario(solicitacao.getComentario());
        item.setData(DateUtils.getIntervaloTempo(new Date()));
        item.setFotos(new ArrayList<String>());
        JSONArray fotos = json.getJSONArray("images");
        for (int j = 0; j < fotos.length(); j++) {
            item.getFotos().add(ViewUtils.isMdpiOrLdpi(this) ? fotos.getJSONObject(j).getString("low") : fotos.getJSONObject(j).getString("high"));
        }
        item.setProtocolo(json.getString("protocol"));
        item.setStatus(new SolicitacaoListItem.Status(json.getJSONObject("status").getString("title"), json.getJSONObject("status").getString("color")));
        item.setTitulo(json.getJSONObject("category").getString("title"));
        item.setCategoria(solicitacao.getCategoria());
        item.setLatitude(solicitacao.getLatitude());
        item.setLongitude(solicitacao.getLongitude());
        item.setEndereco(solicitacao.getEndereco());
        item.setReferencia(solicitacao.getReferencia());
        return item;
    }

    private void restoreFragmentsStates(Bundle bundle) {
        Bundle params = new Bundle();
        params.putSerializable("solicitacao", solicitacao);
        params.putString("imagemTemporaria", bundle.getString("imagemTemporaria"));

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (bundle.getBoolean("tipo")) {
            tipoFragment = new SoliciteTipoFragment();
            tipoFragment.setArguments(params);
            ft.add(R.id.fragments_place, tipoFragment);

            if (atual != Passo.TIPO) {
                ft.hide(tipoFragment);
            }
        }

        if (bundle.getBoolean("fotos")) {
            fotosFragment = new SoliciteFotosFragment();
            fotosFragment.setArguments(params);
            ft.add(R.id.fragments_place, fotosFragment);

            if (atual != Passo.FOTOS) {
                ft.hide(fotosFragment);
            }
        }

        if (bundle.getBoolean("local")) {
            localFragment = new SoliciteLocalFragment();
            localFragment.setArguments(params);
            ft.add(R.id.fragments_place, localFragment);

            if (atual != Passo.LOCAL) {
                ft.hide(localFragment);
            }
        }

        if (bundle.getBoolean("detalhes")) {
            detalhesFragment = new SoliciteDetalhesFragment();
            detalhesFragment.setArguments(params);
            ft.add(R.id.fragments_place, detalhesFragment);

            if (atual != Passo.COMENTARIOS) {
                ft.hide(detalhesFragment);
            }
        }

        if (bundle.getBoolean("ponto")) {
            pontoFragment = new SolicitePontoFragment();
            pontoFragment.setArguments(params);
            pontoFragment.setCategoria(solicitacao.getCategoria());
            ft.add(R.id.fragments_place, pontoFragment);

            if (atual != Passo.LOCAL) {
                ft.hide(pontoFragment);
            }
        }

        ft.commitAllowingStateLoss();
    }
}
