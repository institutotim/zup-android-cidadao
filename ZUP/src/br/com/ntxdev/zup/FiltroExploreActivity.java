package br.com.ntxdev.zup;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.com.ntxdev.zup.util.FontUtils;

public class FiltroExploreActivity extends Activity implements View.OnClickListener {

	private Button botaoConcluido;
	private TextView opcaoBocaDeLobo;
	private TextView opcaoColetaEntulho;
	private TextView status;
	private TextView todosStatus;
	private TextView resolvidos;
	private TextView emAndamento;
	private TextView naoResolvidos;
	private TextView emAberto;
	private LinearLayout opcoes;
	private TextView opcaoBocasDeLoboLocal;
	private TextView opcaoPracasWifi;
	private TextView opcaoFlorestaUrbana;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filtro_explore);

		((TextView) findViewById(R.id.filtros)).setTypeface(FontUtils.getLight(this));
		((TextView) findViewById(R.id.instrucoes)).setTypeface(FontUtils.getBold(this));
		((TextView) findViewById(R.id.textView1)).setTypeface(FontUtils.getBold(this));
		
		opcoes = (LinearLayout) findViewById(R.id.opcoes);

		botaoConcluido = (Button) findViewById(R.id.botaoConcluido);
		botaoConcluido.setTypeface(FontUtils.getRegular(this));

		opcaoBocaDeLobo = (TextView) findViewById(R.id.opcaoBocaDeLobo);
		opcaoBocaDeLobo.setOnClickListener(this);
		opcaoBocaDeLobo.setTypeface(FontUtils.getRegular(this));
		opcaoColetaEntulho = (TextView) findViewById(R.id.opcaoColetaEntulho);
		opcaoColetaEntulho.setOnClickListener(this);
		opcaoColetaEntulho.setTypeface(FontUtils.getRegular(this));
		
		opcaoBocasDeLoboLocal = (TextView) findViewById(R.id.opcaoBocasDeLoboLocal);
		opcaoBocasDeLoboLocal.setOnClickListener(this);
		opcaoBocasDeLoboLocal.setTypeface(FontUtils.getRegular(this));
		opcaoPracasWifi = (TextView) findViewById(R.id.opcaoPracasWifi);
		opcaoPracasWifi.setOnClickListener(this);
		opcaoPracasWifi.setTypeface(FontUtils.getRegular(this));
		opcaoFlorestaUrbana = (TextView) findViewById(R.id.opcaoFlorestaUrbana);
		opcaoFlorestaUrbana.setOnClickListener(this);
		opcaoFlorestaUrbana.setTypeface(FontUtils.getRegular(this));

		status = (TextView) findViewById(R.id.status);
		status.setTypeface(FontUtils.getLight(this));
		status.setOnClickListener(this);
		
		todosStatus = (TextView) findViewById(R.id.todosStatus);
		todosStatus.setTypeface(FontUtils.getLight(this));
		todosStatus.setOnClickListener(cliqueOpcao);
		
		resolvidos = (TextView) findViewById(R.id.resolvidos);
		resolvidos.setTypeface(FontUtils.getLight(this));
		resolvidos.setOnClickListener(cliqueOpcao);
		
		emAndamento = (TextView) findViewById(R.id.emAndamento);
		emAndamento.setTypeface(FontUtils.getLight(this));
		emAndamento.setOnClickListener(cliqueOpcao);
		
		emAberto = (TextView) findViewById(R.id.emAberto);
		emAberto.setTypeface(FontUtils.getLight(this));
		emAberto.setOnClickListener(cliqueOpcao);
		
		naoResolvidos = (TextView) findViewById(R.id.naoResolvidos);
		naoResolvidos.setTypeface(FontUtils.getLight(this));
		naoResolvidos.setOnClickListener(cliqueOpcao);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == status.getId()) {
			if (opcoes.getVisibility() == View.GONE) {
				opcoes.setVisibility(View.VISIBLE);
				status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.seta_contrair, 0);
			} else {
				opcoes.setVisibility(View.GONE);
				status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.seta_expandir, 0);
			}
		}
	}
	
	private View.OnClickListener cliqueOpcao = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			todosStatus.setTextColor(getResources().getColorStateList(R.color.text_option_color));
			resolvidos.setTextColor(getResources().getColorStateList(R.color.text_option_color));
			emAndamento.setTextColor(getResources().getColorStateList(R.color.text_option_color));
			emAberto.setTextColor(getResources().getColorStateList(R.color.text_option_color));
			naoResolvidos.setTextColor(getResources().getColorStateList(R.color.text_option_color));
			
			TextView selecionado = (TextView) v;
			status.setText(selecionado.getText());
			selecionado.setTextColor(Color.rgb(0x2a, 0xb4, 0xdc));
			opcoes.setVisibility(View.GONE);
			status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.seta_expandir, 0);
		}
	};
}
