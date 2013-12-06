package br.com.ntxdev.zup;

import br.com.ntxdev.zup.domain.BuscaExplore;
import br.com.ntxdev.zup.util.FontUtils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class FiltroEstatisticasActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

	private TextView status;
	private TextView todosStatus;
	private TextView resolvidos;
	private TextView emAndamento;
	private TextView emAberto;
	private TextView naoResolvidos;
	private LinearLayout opcoes;
	
	private BuscaExplore busca = new BuscaExplore();
	private Button botaoConcluido;
	private TextView bairros;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filtro_estatisticas);
		
		View seekBar = findViewById(R.id.seekBar);
		((SeekBar) seekBar.findViewById(R.id.seekbar)).setOnSeekBarChangeListener(this);
		
		opcoes = (LinearLayout) findViewById(R.id.opcoes);
		
		((TextView) findViewById(R.id.filtros)).setTypeface(FontUtils.getLight(this));
		
		botaoConcluido = (Button) findViewById(R.id.botaoConcluido);
		botaoConcluido.setTypeface(FontUtils.getRegular(this));
		botaoConcluido.setOnClickListener(this);
		
		status = (TextView) findViewById(R.id.status);
		status.setTypeface(FontUtils.getLight(this));
		status.setOnClickListener(this);
		
		bairros = (TextView) findViewById(R.id.bairros);
		bairros.setTypeface(FontUtils.getLight(this));

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
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
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
}
