package br.com.ntxdev.zup;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import br.com.ntxdev.zup.domain.BuscaEstatisticas;
import br.com.ntxdev.zup.domain.CategoriaRelato;
import br.com.ntxdev.zup.domain.Periodo;
import br.com.ntxdev.zup.service.CategoriaRelatoService;
import br.com.ntxdev.zup.util.FontUtils;

public class FiltroEstatisticasActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

	private TextView status;
	private LinearLayout opcoes;
	
	private BuscaEstatisticas busca = new BuscaEstatisticas();
	private TextView botaoConcluido;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filtro_estatisticas);
		
		View seekBar = findViewById(R.id.seekBar);
		((SeekBar) seekBar.findViewById(R.id.seekbar)).setOnSeekBarChangeListener(this);
		
		opcoes = (LinearLayout) findViewById(R.id.opcoes);
		
		((TextView) findViewById(R.id.filtros)).setTypeface(FontUtils.getLight(this));
		
		botaoConcluido = (TextView) findViewById(R.id.botaoConcluido);
		botaoConcluido.setTypeface(FontUtils.getRegular(this));
		botaoConcluido.setOnClickListener(this);
		
		status = (TextView) findViewById(R.id.status);
		status.setTypeface(FontUtils.getLight(this));
		status.setOnClickListener(this);
		
		popularListCategorias();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		switch (progress) {
		case 0:
			busca.setPeriodo(Periodo.ULTIMOS_6_MESES);
			break;
		case 1:
			busca.setPeriodo(Periodo.ULTIMOS_3_MESES);
			break;
		case 2:
			busca.setPeriodo(Periodo.ULTIMO_MES);
			break;
		case 3:
			busca.setPeriodo(Periodo.ULTIMA_SEMANA);
			break;
		}
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
			for (int i = 0; i < opcoes.getChildCount(); i++) {
				((TextView) opcoes.getChildAt(i)).setTextColor(getResources().getColorStateList(R.color.text_option_color));
			}

			TextView selecionado = (TextView) v;
			status.setText(selecionado.getText());
			selecionado.setTextColor(Color.rgb(0x2a, 0xb4, 0xdc));
			opcoes.setVisibility(View.GONE);
			status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.seta_expandir, 0);

			CategoriaRelato categoria = (CategoriaRelato) v.getTag();
			busca.setCategoria(categoria.getId());
		}
	};
	
	private void popularListCategorias() {		
		List<CategoriaRelato> categorias = new CategoriaRelatoService().getCategorias(this);		
		
		LayoutInflater inflater = LayoutInflater.from(this);
		
		TextView tv = (TextView) inflater.inflate(R.layout.status_textview, opcoes, false);
		tv.setTypeface(FontUtils.getLight(this));
		tv.setText("Todas as categorias");
		tv.setOnClickListener(cliqueOpcao);
		opcoes.addView(tv);		
		
		for (CategoriaRelato categoria : categorias) {

			tv = (TextView) inflater.inflate(R.layout.status_textview, opcoes, false);
			tv.setTypeface(FontUtils.getLight(this));
			tv.setTag(categoria);
			tv.setText(categoria.getNome());
			tv.setOnClickListener(cliqueOpcao);
			if (categoria.equals(busca.getCategoria())) {
				tv.setTextColor(Color.parseColor("#2ab4dc"));
			}
			opcoes.addView(tv);

		}
		
		if (busca.getCategoria() == null) {
			((TextView) opcoes.getChildAt(0)).setTextColor(Color.parseColor("#2ab4dc"));
		}
	}
}
