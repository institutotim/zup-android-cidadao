package br.com.ntxdev.zup;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import br.com.ntxdev.zup.domain.SolicitacaoListItem;

public class SolicitacaoDetalheActivity extends Activity {

	private SolicitacaoListItem solicitacao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solicitacao_detalhe);
		
		solicitacao = (SolicitacaoListItem) getIntent().getExtras().getSerializable("solicitacao");
		((TextView) findViewById(R.id.protocolo)).setText(getString(R.string.protocolo) + " " + solicitacao.getProtocolo());
		
		((Button) findViewById(R.id.botaoVoltar)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();				
			}
		});
		
		TextView indicadorStatus = (TextView) findViewById(R.id.indicadorStatus);
		switch (solicitacao.getStatus()) {
		case EM_ABERTO:
			indicadorStatus.setText(R.string.em_aberto);
			indicadorStatus.setBackgroundResource(R.drawable.status_red_bg);
			break;
		case EM_ANDAMENTO:
			indicadorStatus.setText(R.string.em_andamento);
			indicadorStatus.setBackgroundResource(R.drawable.status_orange_bg);
			break;
		case RESOLVIDO:
			indicadorStatus.setText(R.string.resolvido);
			indicadorStatus.setBackgroundResource(R.drawable.status_green_bg);
			break;
		case NAO_RESOLVIDO:
			indicadorStatus.setText(R.string.nao_resolvido);
			indicadorStatus.setBackgroundResource(R.drawable.status_gray_bg);
			break;
		}
	}
}
