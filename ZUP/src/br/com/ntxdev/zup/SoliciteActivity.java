package br.com.ntxdev.zup;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import br.com.ntxdev.zup.core.Constantes;
import br.com.ntxdev.zup.domain.CategoriaRelato;
import br.com.ntxdev.zup.domain.Solicitacao;
import br.com.ntxdev.zup.fragment.SoliciteDetalhesFragment;
import br.com.ntxdev.zup.fragment.SoliciteFotosFragment;
import br.com.ntxdev.zup.fragment.SoliciteLocalFragment;
import br.com.ntxdev.zup.fragment.SoliciteTipoFragment;
import br.com.ntxdev.zup.service.LoginService;
import br.com.ntxdev.zup.util.FontUtils;

public class SoliciteActivity extends FragmentActivity implements View.OnClickListener {

	private TextView botaoCancelar;
	private Passo atual = Passo.TIPO;
	private Solicitacao solicitacao = new Solicitacao();

	private TextView botaoAvancar;
	private TextView botaoVoltar;
	
	private SoliciteTipoFragment tipoFragment;
	private SoliciteFotosFragment fotosFragment;
	private SoliciteLocalFragment localFragment;
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
		botaoVoltar = (TextView) findViewById(R.id.botaoVoltar);
		botaoVoltar.setOnClickListener(this);
		botaoVoltar.setTypeface(FontUtils.getRegular(this));

		botaoCancelar = (TextView) findViewById(R.id.botaoCancelar);
		botaoCancelar.setTypeface(FontUtils.getRegular(this));
		botaoCancelar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		tipoFragment = new SoliciteTipoFragment();		
		
		getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, tipoFragment).commit();
	}

	public void exibirBarraInferior(boolean exibir) {
		findViewById(R.id.barra_navegacao).setVisibility(exibir ? View.VISIBLE : View.GONE);
	}
	
	public void setCategoria(CategoriaRelato categoria) {
		solicitacao.setCategoria(categoria);
		if (localFragment == null) {
			localFragment = new SoliciteLocalFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, localFragment).commit();
		} else {
			getSupportFragmentManager().beginTransaction().hide(tipoFragment).show(localFragment).commit();
		}		
		atual = Passo.LOCAL;
		exibirBarraInferior(true);
	}

	public CategoriaRelato getCategoria() {
		return solicitacao.getCategoria();
	}

	public void setInfo(int string) {
		TextView info = (TextView) findViewById(R.id.instrucoes);
		info.setText(string);
		info.setTypeface(FontUtils.getBold(this));
	}

	public void setComentario(String comentario) {
		solicitacao.setComentario(comentario.trim());
	}

	public void setRedeSocial(boolean publicar) {
		solicitacao.setRedeSocial(publicar);
	}

	public void adicionarFoto(String foto) {
		solicitacao.adicionarFoto(foto);
	}

	public void removerFoto(String foto) {
		solicitacao.removerFoto(foto);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == botaoAvancar.getId()) {
			if (atual.equals(Passo.LOCAL)) {
				if (fotosFragment == null) {
					fotosFragment = new SoliciteFotosFragment();
					getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, fotosFragment).commit();					
				} else {
					getSupportFragmentManager().beginTransaction().hide(localFragment).show(fotosFragment).commit();
				}
				atual = Passo.FOTOS;
			} else if (atual.equals(Passo.FOTOS)) {
				if (detalhesFragment == null) {
					detalhesFragment = new SoliciteDetalhesFragment();
					getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, detalhesFragment).commit();					
				} else {
					getSupportFragmentManager().beginTransaction().hide(fotosFragment).show(detalhesFragment).commit();
				}
				botaoAvancar.setText(R.string.publicar);
				atual = Passo.COMENTARIOS;
			} else if (atual.equals(Passo.COMENTARIOS)){
				solicitacao.setComentario(detalhesFragment.getComentario());
				solicitacao.setLatitudeLongitude(localFragment.getLatitudeAtual(), localFragment.getLongitudeAtual());
				enviarSolicitacao();
			}
		} else if (v.getId() == botaoVoltar.getId()) {
			botaoAvancar.setText(R.string.proximo);
			if (atual.equals(Passo.COMENTARIOS)) {
				getSupportFragmentManager().beginTransaction().hide(detalhesFragment).show(fotosFragment).commit();
				atual = Passo.FOTOS;
			} else if (atual.equals(Passo.FOTOS)) {
				getSupportFragmentManager().beginTransaction().hide(fotosFragment).show(localFragment).commit();
				atual = Passo.LOCAL;
			} else if (atual.equals(Passo.LOCAL)) {
				getSupportFragmentManager().beginTransaction().hide(localFragment).show(tipoFragment).commit();
				exibirBarraInferior(false);
				atual = Passo.TIPO;
			}
		}
	}
	
	private void enviarSolicitacao() {
		new Tasker().execute();
	}
	
	public class Tasker extends AsyncTask<Void, Void, Boolean> {
		
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(SoliciteActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
			dialog.setMessage("Enviando solicitação...");
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(Constantes.REST_URL + "/reports/" + solicitacao.getCategoria().getId() + "/items");
				
				MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
				multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				multipartEntity.setCharset(Charset.forName("UTF-8"));
				
				multipartEntity.addTextBody("latitude", String.valueOf(solicitacao.getLatitude()));
				multipartEntity.addTextBody("longitude", String.valueOf(solicitacao.getLongitude()));
				multipartEntity.addTextBody("description", solicitacao.getComentario(), ContentType.APPLICATION_JSON);
				multipartEntity.addTextBody("address", localFragment.getEnderecoAtual(), ContentType.APPLICATION_JSON);
				multipartEntity.addTextBody("category_id", String.valueOf(solicitacao.getCategoria().getId()));
				
				for (String foto : solicitacao.getFotos()) {
					multipartEntity.addPart("images[]", new FileBody(new File(foto)));
				}
				
				post.setEntity(multipartEntity.build());
				post.setHeader("X-App-Token", new LoginService().getToken(SoliciteActivity.this));
				HttpResponse response = client.execute(post);
				System.out.println(EntityUtils.toString(response.getEntity(), "UTF-8"));
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
					return Boolean.TRUE;
				}
			} catch (Exception e) {
				Log.e("ZUP", e.getMessage());
			}
			return Boolean.FALSE;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if (result) {
				Toast.makeText(SoliciteActivity.this, "Solicitação enviada com sucesso!", Toast.LENGTH_LONG).show();
				finish();
			} else {
				Toast.makeText(SoliciteActivity.this, "Falha no envio da solicitação", Toast.LENGTH_LONG).show();
			}
		}
	}
}
