package br.com.ntxdev.zup;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import br.com.ntxdev.zup.domain.Solicitacao;
import br.com.ntxdev.zup.fragment.SoliciteLocalFragment;
import br.com.ntxdev.zup.fragment.SoliciteTipoFragment;

public class SoliciteActivity extends FragmentActivity implements View.OnClickListener {
	
	private Button botaoCancelar;
	private Passo atual = Passo.TIPO;
	private Solicitacao solicitacao = new Solicitacao();
	
	private View botaoAvancar;
	private View botaoVoltar;
	
	private enum Passo {
		TIPO, LOCAL, FOTOS, COMENTARIOS
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solicite);
		
		botaoAvancar = findViewById(R.id.botaoAvancar);
		botaoAvancar.setOnClickListener(this);
		botaoVoltar = findViewById(R.id.botaoVoltar);
		botaoVoltar.setOnClickListener(this);
		
		botaoCancelar = (Button) findViewById(R.id.botaoCancelar);
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
	
	public void setInfo(int string) {
		((TextView) findViewById(R.id.instrucoes)).setText(string);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == botaoAvancar.getId()) {
			
		} else if (v.getId() == botaoVoltar.getId()) {
			
		}
	}
}
