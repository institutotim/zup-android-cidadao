package br.com.ntxdev.zup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import br.com.ntxdev.zup.domain.Solicitacao;
import br.com.ntxdev.zup.fragment.SoliciteDetalhesFragment;
import br.com.ntxdev.zup.fragment.SoliciteFotosFragment;
import br.com.ntxdev.zup.fragment.SoliciteLocalFragment;
import br.com.ntxdev.zup.fragment.SoliciteTipoFragment;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.SessionSocialNetwork;

import com.google.gson.Gson;

public class SoliciteActivity extends FragmentActivity implements View.OnClickListener {

	private TextView botaoCancelar;
	private Passo atual = Passo.TIPO;
	private Solicitacao solicitacao = new Solicitacao();

	private TextView botaoAvancar;
	private TextView botaoVoltar;
	
	private SessionSocialNetwork sessionSocialNetwork;
	private SharedPreferences sharedPref;
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
		
		Context context = getApplicationContext();
		SharedPreferences sharedPref = context.getSharedPreferences(
		        getString(R.string.pref_shared_session_key), Context.MODE_PRIVATE);

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

	public void setTipo(Solicitacao.Tipo tipo) {
		solicitacao.setTipo(tipo);
		if (localFragment == null) {
			localFragment = new SoliciteLocalFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, localFragment).commit();
		} else {
			getSupportFragmentManager().beginTransaction().hide(tipoFragment).show(localFragment).commit();
		}		
		atual = Passo.LOCAL;
		exibirBarraInferior(true);
	}

	public Solicitacao.Tipo getTipo() {
		return solicitacao.getTipo();
	}

	public void setInfo(int string) {
		TextView info = (TextView) findViewById(R.id.instrucoes);
		info.setText(string);
		info.setTypeface(FontUtils.getBold(this));
	}

	public void setComentario(String comentario) {
		solicitacao.setComentario(comentario);
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
				//Pega a sessão na Shared Preferences
				Gson gson = new Gson();
				sessionSocialNetwork = gson.fromJson(sharedPref.getString("sessionNetwork", ""), SessionSocialNetwork.class);
				//Publica na rede social
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
}
