package br.com.ntxdev.zup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import br.com.ntxdev.zup.domain.BuscaExplore;
import br.com.ntxdev.zup.domain.CategoriaInventario;
import br.com.ntxdev.zup.domain.CategoriaRelato;
import br.com.ntxdev.zup.service.CategoriaInventarioService;
import br.com.ntxdev.zup.service.CategoriaRelatoService;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.ImageUtils;

public class FiltroExploreActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

	private TextView botaoConcluido;
	private TextView status;
	private TextView todosStatus;
	private TextView resolvidos;
	private TextView emAndamento;
	private TextView naoResolvidos;
	private TextView emAberto;
	private LinearLayout opcoes;
	
	private List<CategoriaInventario> inventarios = new ArrayList<CategoriaInventario>();
	private List<CategoriaRelato> relatos = new ArrayList<CategoriaRelato>();

	private BuscaExplore busca = new BuscaExplore();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filtro_explore);
		
		busca = (BuscaExplore) getIntent().getSerializableExtra("busca");

		((TextView) findViewById(R.id.filtros)).setTypeface(FontUtils.getLight(this));
		((TextView) findViewById(R.id.instrucoes)).setTypeface(FontUtils.getBold(this));
		((TextView) findViewById(R.id.textView)).setTypeface(FontUtils.getBold(this));
		View seekBar = findViewById(R.id.seekBar);
		((SeekBar) seekBar.findViewById(R.id.seekbar)).setOnSeekBarChangeListener(this);
		
		// AllTextCaps não funciona antes do Android 4.0... então vamos fazer na mão mesmo!
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			((TextView) findViewById(R.id.instrucoes)).setText(((TextView) findViewById(R.id.instrucoes)).getText().toString().toUpperCase(Locale.US));
			((TextView) findViewById(R.id.textView)).setText(((TextView) findViewById(R.id.textView)).getText().toString().toUpperCase(Locale.US));
		}

		opcoes = (LinearLayout) findViewById(R.id.opcoes);

		botaoConcluido = (TextView) findViewById(R.id.botaoConcluido);
		botaoConcluido.setTypeface(FontUtils.getRegular(this));
		botaoConcluido.setOnClickListener(this);

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
		
		montarCategoriasRelatos();
		montarCategoriasInventario();
	}
	
	private void unselectCategoriasInventario() {
		LinearLayout container = (LinearLayout) findViewById(R.id.categorias_inventario);
		for (int i = 0; i < container.getChildCount(); i++) {
			TextView view = (TextView) container.getChildAt(i);
			CategoriaInventario categoria = (CategoriaInventario) view.getTag();
			view.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getStateListDrawable(this, categoria.getIcone()), null, null);
			view.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
			
			inventarios.clear();
		}
	}
	
	private void unselectCategoriasRelatos() {
		LinearLayout container = (LinearLayout) findViewById(R.id.seletor_tipo);
		for (int i = 0; i < container.getChildCount(); i++) {
			TextView view = (TextView) container.getChildAt(i);
			CategoriaRelato categoria = (CategoriaRelato) view.getTag();
			view.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getStateListDrawable(this, categoria.getIcone()), null, null);
			view.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
			
			relatos.clear();
		}
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
			prepararObjetoRetorno();
			Intent i = new Intent();
			i.putExtra("busca", busca);
			setResult(Activity.RESULT_OK, i);
			finish();
			return;
		}
		
		Object categoria = v.getTag();
		TextView view = (TextView) v;
		if (categoria instanceof CategoriaInventario) {
			unselectCategoriasRelatos();
			if (inventarios.contains(categoria)) {
				inventarios.remove(categoria);
				view.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
				view.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getStateListDrawable(this, ((CategoriaInventario) categoria).getIcone()), null, null);
			} else {
				inventarios.add((CategoriaInventario) categoria);
				view.setTextColor(Color.BLACK);
				view.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(getResources(), ImageUtils.getScaled(this, ((CategoriaInventario) categoria).getIcone())), null, null);
			}
		} else if (categoria instanceof CategoriaRelato) {
			unselectCategoriasInventario();
			if (relatos.contains(categoria)) {
				relatos.remove(categoria);
				view.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
				view.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getStateListDrawable(this, ((CategoriaRelato) categoria).getIcone()), null, null);
			} else {
				relatos.add((CategoriaRelato) categoria);
				view.setTextColor(Color.BLACK);
				view.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(getResources(), ImageUtils.getScaled(this, ((CategoriaRelato) categoria).getIcone())), null, null);
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
	
	private void montarCategoriasRelatos() {
		List<CategoriaRelato> categorias = new CategoriaRelatoService().getCategorias(this);
		LinearLayout container = (LinearLayout) findViewById(R.id.seletor_tipo);
		
		for (CategoriaRelato categoria : categorias) {
			TextView view = (TextView) getLayoutInflater().inflate(R.layout.categoria_filtro_item, container, false);
			view.setTag(categoria);
			view.setText(categoria.getNome());
			view.setTypeface(FontUtils.getRegular(this));
			view.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getStateListDrawable(this, categoria.getIcone()), null, null);
			view.setOnClickListener(this);
			container.addView(view);
		}
	}
	
	private void montarCategoriasInventario() {
		List<CategoriaInventario> categorias = new CategoriaInventarioService().getCategorias(this);
		LinearLayout container = (LinearLayout) findViewById(R.id.categorias_inventario);
		
		for (CategoriaInventario categoria : categorias) {
			TextView view = (TextView) getLayoutInflater().inflate(R.layout.categoria_filtro_item, container, false);
			view.setTag(categoria);
			view.setText(categoria.getNome());
			view.setTypeface(FontUtils.getRegular(this));
			view.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getStateListDrawable(this, categoria.getIcone()), null, null);
			view.setOnClickListener(this);
			container.addView(view);
		}
	}
	
	private void prepararObjetoRetorno() {
		for (CategoriaInventario ci : inventarios) {
			busca.getIdsCategoriaInventario().add(ci.getId());
		}
		
		for (CategoriaRelato cr : relatos) {
			busca.getIdsCategoriaRelato().add(cr.getId());
		}
	}
}
