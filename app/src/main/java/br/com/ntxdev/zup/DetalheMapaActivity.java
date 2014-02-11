package br.com.ntxdev.zup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import br.com.ntxdev.zup.core.Constantes;
import br.com.ntxdev.zup.domain.ItemInventario;
import br.com.ntxdev.zup.domain.SolicitacaoListItem;
import br.com.ntxdev.zup.fragment.ComentariosFragment;
import br.com.ntxdev.zup.fragment.InformacoesFragment;
import br.com.ntxdev.zup.fragment.SolicitacoesFragment;
import br.com.ntxdev.zup.service.LoginService;
import br.com.ntxdev.zup.util.FontUtils;

public class DetalheMapaActivity extends FragmentActivity implements View.OnClickListener {

	private InformacoesFragment infoFragment;
	private ComentariosFragment comFragment;
	private SolicitacoesFragment solFragment;
	
	private ArrayList<SolicitacaoListItem> relatos;
	private HashMap<String, String> camposDinamicos = new HashMap<String, String>();
	
	private ItemInventario item;
	private TextView comentarios;
	private TextView solicitacoes;
	private TextView informacoes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detalhe_mapa);
		
		item = (ItemInventario) getIntent().getSerializableExtra("item");
		
		TextView titulo = (TextView) findViewById(R.id.titulo);
		titulo.setText(item.getCategoria().getNome());
		titulo.setTypeface(FontUtils.getLight(this));
		
		TextView voltar = (TextView) findViewById(R.id.botaoVoltar);
		voltar.setTypeface(FontUtils.getRegular(this));
		voltar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();				
			}
		});		
		
		informacoes = (TextView) findViewById(R.id.botaoInformacoes);
		informacoes.setTypeface(FontUtils.getLight(this));
		informacoes.setOnClickListener(this);
		
		solicitacoes = (TextView) findViewById(R.id.botaoSolicitacoes);
		solicitacoes.setTypeface(FontUtils.getLight(this));
		solicitacoes.setOnClickListener(this);
		
		comentarios = (TextView) findViewById(R.id.botaoComentarios);
		comentarios.setTypeface(FontUtils.getLight(this));
		comentarios.setOnClickListener(this);

		infoFragment = new InformacoesFragment();
		solFragment = new SolicitacoesFragment();
		comFragment = new ComentariosFragment();
		
		getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, solFragment).add(R.id.fragments_place, infoFragment).add(R.id.fragments_place, comFragment).commit();
		
		new Tasker().execute();
	}

	@Override
	public void onClick(View v) {
		((TextView) findViewById(R.id.botaoInformacoes)).setTextColor(getResources().getColorStateList(R.color.text_previous_color));
		((TextView) findViewById(R.id.botaoSolicitacoes)).setTextColor(getResources().getColorStateList(R.color.text_previous_color));
		((TextView) findViewById(R.id.botaoComentarios)).setTextColor(getResources().getColorStateList(R.color.text_previous_color));
		
		switch (v.getId()) {
		case R.id.botaoInformacoes:
			getSupportFragmentManager().beginTransaction().show(infoFragment).hide(comFragment).hide(solFragment).commit();
			break;
		case R.id.botaoComentarios:
			getSupportFragmentManager().beginTransaction().hide(infoFragment).show(comFragment).hide(solFragment).commit();
			break;
		case R.id.botaoSolicitacoes:
			getSupportFragmentManager().beginTransaction().hide(infoFragment).hide(comFragment).show(solFragment).commit();
			break;
		default:
			return;
		}
		
		((TextView) findViewById(v.getId())).setTextColor(getResources().getColorStateList(R.color.text_next_color));				
	}
	
	private class Tasker extends AsyncTask<Void, Void, Boolean> {
		
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(DetalheMapaActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
			dialog.setMessage("Por favor, aguarde...");
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(Constantes.REST_URL + "/reports/inventory/" + item.getId() + "/items");
				get.setHeader("X-App-Token", new LoginService().getToken(DetalheMapaActivity.this));
				HttpResponse response = client.execute(get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					setRelatos(EntityUtils.toString(response.getEntity(), "UTF-8"));
				}
				
				// Dados (campos dinâmicos)
				get = new HttpGet(Constantes.REST_URL + "/inventory/categories/" + item.getCategoria().getId() + "?display_type=full");
				get.setHeader("X-App-Token", new LoginService().getToken(DetalheMapaActivity.this));
				response = client.execute(get);
				String categoria = null;
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					categoria = EntityUtils.toString(response.getEntity(), "UTF-8");
				}
				
				get = new HttpGet(Constantes.REST_URL + "/inventory/categories/" + item.getCategoria().getId() + "/items/" + item.getId());
				get.setHeader("X-App-Token", new LoginService().getToken(DetalheMapaActivity.this));
				response = client.execute(get);
				String itemInventario = null;
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					itemInventario = EntityUtils.toString(response.getEntity(), "UTF-8");
				}
				
				montarHashMap(categoria, itemInventario);
				return Boolean.TRUE;
			} catch (Exception e) {
				Log.e("ZUP", e.getMessage());
				return Boolean.FALSE;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if (result) {
				if (relatos.isEmpty()) {
					ocultarAbasAdicionais();					
				} else {
					prepararAbas();
					solFragment.setRelatos(relatos);					
				}
				infoFragment.setDados(camposDinamicos);
			} else {
				Toast.makeText(DetalheMapaActivity.this, "Não foi possível obter os dados do item", Toast.LENGTH_LONG).show();
				finish();
			}
		}
		
		private void setRelatos(String json) throws Exception {
			JSONArray array = new JSONObject(json).getJSONArray("reports");
			relatos = new ArrayList<SolicitacaoListItem>();
			for (int i = 0; i < array.length(); i++) {
				relatos.add(SolicitacaoListItemAdapter.adapt(DetalheMapaActivity.this, array.getJSONObject(i)));
			}
		}
		
		private void montarHashMap(String categoria, String itemInventario) throws Exception {
			List<JSONObject> campos = new ArrayList<JSONObject>();
			List<JSONObject> dados = new ArrayList<JSONObject>();
			
			JSONArray sections = new JSONObject(categoria).getJSONObject("category").getJSONArray("sections");
			for (int i = 0; i < sections.length(); i++) {
				JSONArray fields = sections.getJSONObject(i).getJSONArray("fields");
				for (int j = 0; j < fields.length(); j++) {
					campos.add(fields.getJSONObject(j));
				}
			}
			
			JSONArray data = new JSONObject(itemInventario).getJSONObject("item").getJSONArray("data");
			for (int i = 0; i < data.length(); i++) {
				dados.add(data.getJSONObject(i));
			}
			
			for (JSONObject dado : dados) {
				for (JSONObject campo : campos) {
					if (dado.getLong("inventory_field_id") == campo.getLong("id")) {
						camposDinamicos.put(campo.getString("title"), dado.getString("content"));
					}
				}
			}
		}
	}
	
	private void ocultarAbasAdicionais() {
		comentarios.setVisibility(View.GONE);
		solicitacoes.setVisibility(View.GONE);
		
		((TextView) findViewById(R.id.botaoInformacoes)).setTextColor(getResources().getColorStateList(R.color.text_next_color));
		((TextView) findViewById(R.id.botaoSolicitacoes)).setTextColor(getResources().getColorStateList(R.color.text_previous_color));
		((TextView) findViewById(R.id.botaoComentarios)).setTextColor(getResources().getColorStateList(R.color.text_previous_color));
		getSupportFragmentManager().beginTransaction().hide(solFragment).hide(comFragment).show(infoFragment).commit();
	}
	
	private void prepararAbas() {
		getSupportFragmentManager().beginTransaction().show(solFragment).hide(comFragment).hide(infoFragment).commit();
	}
}
