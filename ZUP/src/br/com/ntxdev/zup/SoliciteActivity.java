package br.com.ntxdev.zup;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import br.com.ntxdev.zup.domain.Solicitacao;
import br.com.ntxdev.zup.fragment.SoliciteDetalhesFragment;
import br.com.ntxdev.zup.fragment.SoliciteFotosFragment;
import br.com.ntxdev.zup.fragment.SoliciteLocalFragment;
import br.com.ntxdev.zup.fragment.SoliciteTipoFragment;
import br.com.ntxdev.zup.util.FontUtils;

public class SoliciteActivity extends FragmentActivity implements View.OnClickListener {
	
	private Button botaoCancelar;
	private Passo atual = Passo.TIPO;
	private Solicitacao solicitacao = new Solicitacao();
	
	private TextView botaoAvancar;
	private TextView botaoVoltar;
	
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
		
		botaoCancelar = (Button) findViewById(R.id.botaoCancelar);
		botaoCancelar.setTypeface(FontUtils.getRegular(this));
		botaoCancelar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();				
			}
		});
		
		getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, new SoliciteTipoFragment()).commit();
	}
	
	public void exibirBarraInferior(boolean exibir) {
		findViewById(R.id.barra_navegacao).setVisibility(exibir ? View.VISIBLE : View.GONE);
	}
	
	public void setTipo(Solicitacao.Tipo tipo) {
		solicitacao.setTipo(tipo);
		getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, new SoliciteLocalFragment()).commit();
		atual = Passo.LOCAL;
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
				getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, new SoliciteFotosFragment()).commit();
				atual = Passo.FOTOS;
			} else if (atual.equals(Passo.FOTOS)) {
				getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, new SoliciteDetalhesFragment()).commit();
				botaoAvancar.setText(R.string.publicar);
				atual = Passo.COMENTARIOS;
			}
		} else if (v.getId() == botaoVoltar.getId()) {
			botaoAvancar.setText(R.string.proximo);
			if (atual.equals(Passo.COMENTARIOS)) {
				getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, new SoliciteFotosFragment()).commit();
				atual = Passo.FOTOS;
			} else if (atual.equals(Passo.FOTOS)) {
				getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, new SoliciteLocalFragment()).commit();
				atual = Passo.LOCAL;
			} else if (atual.equals(Passo.LOCAL)) {
				getSupportFragmentManager().beginTransaction().add(R.id.fragments_place, new SoliciteTipoFragment()).commit();
				atual = Passo.TIPO;
			}
		}
	}
}
