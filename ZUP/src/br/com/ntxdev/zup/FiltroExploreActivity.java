package br.com.ntxdev.zup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import br.com.ntxdev.zup.domain.BuscaExplore;
import br.com.ntxdev.zup.util.FontUtils;

public class FiltroExploreActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

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

	private BuscaExplore busca = new BuscaExplore();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filtro_explore);

		((TextView) findViewById(R.id.filtros)).setTypeface(FontUtils.getLight(this));
		((TextView) findViewById(R.id.instrucoes)).setTypeface(FontUtils.getBold(this));
		((TextView) findViewById(R.id.textView)).setTypeface(FontUtils.getBold(this));
		View seekBar = findViewById(R.id.seekBar);
		((SeekBar) seekBar.findViewById(R.id.seekbar)).setOnSeekBarChangeListener(this);

		opcoes = (LinearLayout) findViewById(R.id.opcoes);

		botaoConcluido = (Button) findViewById(R.id.botaoConcluido);
		botaoConcluido.setTypeface(FontUtils.getRegular(this));
		botaoConcluido.setOnClickListener(this);

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
			return;
		}

		if (v.getId() == botaoConcluido.getId()) {
			Intent i = new Intent();
			i.putExtra("busca", busca);
			setResult(Activity.RESULT_OK, i);
			finish();
			return;
		}

		switch (v.getId()) {
		case R.id.opcaoBocaDeLobo:
			if (busca.isLimpezaBocaDeLobo()) {
				busca.setLimpezaBocaDeLobo(false);
				opcaoBocaDeLobo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_boca_lobo, 0, 0);
				opcaoBocaDeLobo.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
			} else {
				busca.setLimpezaBocaDeLobo(true);
				opcaoBocaDeLobo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_boca_lobo_normal, 0, 0);
				opcaoBocaDeLobo.setTextColor(Color.BLACK);
			}
			break;
		case R.id.opcaoColetaEntulho:
			if (busca.isColetaDeEntulho()) {
				busca.setColetaDeEntulho(false);
				opcaoColetaEntulho.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_coleta_entulho, 0, 0);
				opcaoColetaEntulho.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
			} else {
				busca.setColetaDeEntulho(true);
				opcaoColetaEntulho.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_coleta_entulho_normal, 0, 0);
				opcaoColetaEntulho.setTextColor(Color.BLACK);
			}
			break;
		case R.id.opcaoBocasDeLoboLocal:
			if (busca.isExibirBocasLobo()) {
				busca.setExibirBocasLobo(false);
				opcaoBocasDeLoboLocal.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_boca_lobo, 0, 0);
				opcaoBocasDeLoboLocal.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
			} else {
				busca.setExibirBocasLobo(true);
				opcaoBocasDeLoboLocal.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_boca_lobo_normal, 0, 0);
				opcaoBocasDeLoboLocal.setTextColor(Color.BLACK);
			}
			break;
		case R.id.opcaoPracasWifi:
			if (busca.isExibirPracasWifi()) {
				busca.setExibirPracasWifi(false);
				opcaoPracasWifi.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_praca_wifi, 0, 0);
				opcaoPracasWifi.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
			} else {
				busca.setExibirPracasWifi(true);
				opcaoPracasWifi.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_praca_wifi_normal, 0, 0);
				opcaoPracasWifi.setTextColor(Color.BLACK);
			}
			break;
		case R.id.opcaoFlorestaUrbana:
			if (busca.isExibirFlorestaUrbana()) {
				busca.setExibirFlorestaUrbana(false);
				opcaoFlorestaUrbana.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_floresta_urbana, 0, 0);
				opcaoFlorestaUrbana.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
			} else {
				busca.setExibirFlorestaUrbana(true);
				opcaoFlorestaUrbana.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_floresta_urbana_normal, 0, 0);
				opcaoFlorestaUrbana.setTextColor(Color.BLACK);
			}
			break;
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

			switch (v.getId()) {
			case R.id.todosStatus:
				busca.setStatus(BuscaExplore.Status.TODOS);
				break;
			case R.id.resolvidos:
				busca.setStatus(BuscaExplore.Status.RESOLVIDOS);
				break;
			case R.id.emAndamento:
				busca.setStatus(BuscaExplore.Status.EM_ANDAMENTO);
				break;
			case R.id.emAberto:
				busca.setStatus(BuscaExplore.Status.EM_ABERTO);
				break;
			case R.id.naoResolvidos:
				busca.setStatus(BuscaExplore.Status.NAO_RESOLVIDOS);
				break;
			}
		}
	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		System.out.println(progress);
		switch (progress) {
		case 0:
			busca.setPeriodo(BuscaExplore.Periodo.ULTIMOS_6_MESES);
			break;
		case 1:
			busca.setPeriodo(BuscaExplore.Periodo.ULTIMOS_3_MESES);
			break;
		case 2:
			busca.setPeriodo(BuscaExplore.Periodo.ULTIMO_MES);
			break;
		case 3:
			busca.setPeriodo(BuscaExplore.Periodo.ULTIMA_SEMANA);
			break;
		}	
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {		
	}
}
