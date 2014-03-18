package br.com.ntxdev.zup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import br.com.ntxdev.zup.domain.BuscaExplore;
import br.com.ntxdev.zup.domain.CategoriaInventario;
import br.com.ntxdev.zup.domain.CategoriaRelato;
import br.com.ntxdev.zup.domain.Periodo;
import br.com.ntxdev.zup.service.CategoriaInventarioService;
import br.com.ntxdev.zup.service.CategoriaRelatoService;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.ImageUtils;

public class FiltroExploreActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

	private TextView botaoConcluido;
	private TextView status;
	private LinearLayout opcoes;
	private SeekBar seekBar;
	
	private List<CategoriaInventario> inventarios = new ArrayList<CategoriaInventario>();
	private List<CategoriaRelato> relatos = new ArrayList<CategoriaRelato>();

	private BuscaExplore busca;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filtro_explore);
		
		busca = (BuscaExplore) getIntent().getSerializableExtra("busca");
        if (busca == null) busca = new BuscaExplore();

		((TextView) findViewById(R.id.filtros)).setTypeface(FontUtils.getLight(this));
		((TextView) findViewById(R.id.instrucoes)).setTypeface(FontUtils.getBold(this));
		((TextView) findViewById(R.id.textView)).setTypeface(FontUtils.getBold(this));
		seekBar = (SeekBar) findViewById(R.id.seekBar).findViewById(R.id.seekbar);
		seekBar.setOnSeekBarChangeListener(this);
		
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
		
		montarCategoriasRelatos();
		montarCategoriasInventario();
        aplicarFiltroInicial();
		popularListStatus();
        if (busca.getStatus() != null) selecionarListStatus();
	}

    private void selecionarListStatus() {
        for (int i = 0; i < opcoes.getChildCount(); i++) {
            TextView tv = (TextView) opcoes.getChildAt(i);
            if (tv.getText().toString().equals(busca.getStatus().getNome())) {
                tv.setTextColor(Color.rgb(0x2a, 0xb4, 0xdc));
                status.setText(busca.getStatus().getNome());
            } else {
                tv.setTextColor(getResources().getColorStateList(R.color.text_option_color));
            }
        }
    }

    private void unselectCategoriasInventario() {
		LinearLayout container = (LinearLayout) findViewById(R.id.categorias_inventario);
		for (int i = 0; i < container.getChildCount(); i++) {
			TextView view = (TextView) container.getChildAt(i);
			CategoriaInventario categoria = (CategoriaInventario) view.getTag();
			view.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getInventoryStateListDrawable(this, categoria.getIconeAtivo(), categoria.getIconeInativo()), null, null);
			view.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
			
			inventarios.clear();
		}
	}
	
	private void unselectCategoriasRelatos() {
		LinearLayout container = (LinearLayout) findViewById(R.id.seletor_tipo);
		for (int i = 0; i < container.getChildCount(); i++) {
			TextView view = (TextView) container.getChildAt(i);
			CategoriaRelato categoria = (CategoriaRelato) view.getTag();
			view.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getReportStateListDrawable(this, categoria.getIconeAtivo(), categoria.getIconeInativo()), null, null);
			view.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
			
			relatos.clear();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == status.getId() && !relatos.isEmpty()) {
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
            unselectCategoriasInventario();
			unselectCategoriasRelatos();
			seekBar.setEnabled(false);
			if (inventarios.contains(categoria)) {
				inventarios.remove(categoria);
				view.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
				view.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getInventoryStateListDrawable(this, ((CategoriaInventario) categoria).getIconeAtivo(), ((CategoriaInventario) categoria).getIconeInativo()), null, null);
			} else {
				inventarios.add((CategoriaInventario) categoria);
				view.setTextColor(Color.BLACK);
				view.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(getResources(), ImageUtils.getScaled(this, "inventory", ((CategoriaInventario) categoria).getIconeAtivo())), null, null);
			}
		} else if (categoria instanceof CategoriaRelato) {
			unselectCategoriasInventario();
			seekBar.setEnabled(true);
			if (relatos.contains(categoria)) {
				relatos.remove(categoria);
				view.setTextColor(getResources().getColorStateList(R.color.icon_text_color));
				view.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getReportStateListDrawable(this, ((CategoriaRelato) categoria).getIconeAtivo(), ((CategoriaRelato) categoria).getIconeInativo()), null, null);
			} else {
				relatos.add((CategoriaRelato) categoria);
				view.setTextColor(Color.BLACK);
				view.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(getResources(), ImageUtils.getScaled(this, "reports", ((CategoriaRelato) categoria).getIconeAtivo())), null, null);
			}
			popularListStatus();
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

			CategoriaRelato.Status status = (CategoriaRelato.Status) v.getTag();
			busca.setStatus(status);
		}
	};

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
	
	private void montarCategoriasRelatos() {
		List<CategoriaRelato> categorias = new CategoriaRelatoService().getCategorias(this);
		LinearLayout container = (LinearLayout) findViewById(R.id.seletor_tipo);
		
		if (categorias.isEmpty()) {
			container.setVisibility(View.GONE);
			findViewById(R.id.seekBar).setVisibility(View.GONE);
			findViewById(R.id.status).setVisibility(View.GONE);
			findViewById(R.id.instrucoes).setVisibility(View.GONE);
			return;
		}
		
		for (CategoriaRelato categoria : categorias) {
			TextView view = (TextView) getLayoutInflater().inflate(R.layout.categoria_filtro_item, container, false);
			view.setTag(categoria);
			view.setText(categoria.getNome());
			view.setTypeface(FontUtils.getRegular(this));
			view.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getReportStateListDrawable(this, categoria.getIconeAtivo(), categoria.getIconeInativo()), null, null);
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
			view.setCompoundDrawablesWithIntrinsicBounds(null, ImageUtils.getInventoryStateListDrawable(this, categoria.getIconeAtivo(), categoria.getIconeInativo()), null, null);
			view.setOnClickListener(this);
			container.addView(view);
		}
	}
	
	private void prepararObjetoRetorno() {
		busca.getIdsCategoriaInventario().clear();
		for (CategoriaInventario ci : inventarios) {
			busca.getIdsCategoriaInventario().add(ci.getId());
		}
		
		busca.getIdsCategoriaRelato().clear();
		for (CategoriaRelato cr : relatos) {
			busca.getIdsCategoriaRelato().add(cr.getId());
		}
	}
	
	private void aplicarFiltroInicial() {
		LinearLayout container = (LinearLayout) findViewById(R.id.categorias_inventario);
		for (Long id : busca.getIdsCategoriaInventario()) {			
			for (int i = 0; i < container.getChildCount(); i++) {
				View v = container.getChildAt(i);
				if (((CategoriaInventario) v.getTag()).getId() == id) {
					inventarios.add((CategoriaInventario) v.getTag());
					((TextView) v).setTextColor(Color.BLACK);
					((TextView) v).setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(getResources(), ImageUtils.getScaled(this, "inventory", ((CategoriaInventario) v.getTag()).getIconeAtivo())), null, null);
				}
			}
		}
		
		container = (LinearLayout) findViewById(R.id.seletor_tipo);
		for (Long id : busca.getIdsCategoriaRelato()) {
			for (int i = 0; i < container.getChildCount(); i++) {
				View v = container.getChildAt(i);
				if (((CategoriaRelato) v.getTag()).getId() == id) {
					relatos.add((CategoriaRelato) v.getTag());
					((TextView) v).setTextColor(Color.BLACK);
					((TextView) v).setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(getResources(), ImageUtils.getScaled(this, "reports", ((CategoriaRelato) v.getTag()).getIconeAtivo())), null, null);
				}
			}
		}
		
		switch (busca.getPeriodo()) {
		case ULTIMOS_6_MESES:
			seekBar.setProgress(0);
			break;
		case ULTIMOS_3_MESES:
			seekBar.setProgress(1);
			break;
		case ULTIMO_MES:
			seekBar.setProgress(2);
			break;
		case ULTIMA_SEMANA:
			seekBar.setProgress(3);
			break;
		}
	}
	
	private void popularListStatus() {
		CategoriaRelatoService service = new CategoriaRelatoService();
		List<CategoriaRelato.Status> status = new ArrayList<CategoriaRelato.Status>();
		for (CategoriaRelato categoria : relatos) {
			status.addAll(service.getStatus(this, categoria.getId()));
		}
		
		LayoutInflater inflater = LayoutInflater.from(this);
		opcoes.removeAllViews();
		
		TextView tv = (TextView) inflater.inflate(R.layout.status_textview, opcoes, false);
		tv.setTypeface(FontUtils.getLight(this));
		tv.setText("Todos os status");
		tv.setOnClickListener(cliqueOpcao);
		opcoes.addView(tv);
		
		// Verificando se o status atualmente selecionado não foi removido
		if (busca.getStatus() != null && !status.contains(busca.getStatus())) {
			this.status.setText("Todos os status");
			this.status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.seta_expandir, 0);
			opcoes.setVisibility(View.GONE);
			busca.setStatus(null);
		}
		
		Set<Long> ids = new HashSet<Long>();
		for (CategoriaRelato.Status s : status) {
			if (!ids.contains(s.getId())) {
				tv = (TextView) inflater.inflate(R.layout.status_textview, opcoes, false);
				tv.setTypeface(FontUtils.getLight(this));
				tv.setTag(s);
				tv.setText(s.getNome());
				tv.setOnClickListener(cliqueOpcao);
				if (s.equals(busca.getStatus())) {
					tv.setTextColor(Color.parseColor("#2ab4dc"));
				}
				opcoes.addView(tv);
				ids.add(s.getId());
			}
		}
		
		if (busca.getStatus() == null) {
			((TextView) opcoes.getChildAt(0)).setTextColor(Color.parseColor("#2ab4dc"));
		}
	}
}
