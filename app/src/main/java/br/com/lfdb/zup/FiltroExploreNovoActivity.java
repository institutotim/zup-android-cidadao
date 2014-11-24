package br.com.lfdb.zup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.lfdb.zup.domain.BuscaExplore;
import br.com.lfdb.zup.domain.CategoriaInventario;
import br.com.lfdb.zup.domain.CategoriaRelato;
import br.com.lfdb.zup.domain.Periodo;
import br.com.lfdb.zup.service.CategoriaInventarioService;
import br.com.lfdb.zup.service.CategoriaRelatoService;
import br.com.lfdb.zup.util.FontUtils;
import br.com.lfdb.zup.util.ImageUtils;
import br.com.lfdb.zup.widget.SeekbarWithIntervals;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FiltroExploreNovoActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    @InjectView(R.id.formCategorias)
    View formCategorias;
    @InjectView(R.id.formInventario)
    View formInventario;
    @InjectView(R.id.formPeriodo)
    View formPeriodo;

    @InjectView(R.id.seletorInventario)
    TextView seletorInventario;
    @InjectView(R.id.seletorPeriodo)
    TextView seletorPeriodo;
    @InjectView(R.id.seletorCategorias)
    TextView seletorCategorias;

    @InjectView(R.id.categoriasContainer)
    LinearLayout categoriasContainer;
    @InjectView(R.id.inventarioContainer)
    LinearLayout inventarioContainer;

    @InjectView(R.id.iconeTodos)
    ImageView iconeTodos;
    @InjectView(R.id.textoTodos)
    TextView textoTodos;

    @InjectView(R.id.opcoes)
    LinearLayout opcoes;
    @InjectView(R.id.status)
    TextView status;

    @InjectView(R.id.seekBar)
    SeekbarWithIntervals seekBar;

    private BuscaExplore busca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro_explore_novo);
        ButterKnife.inject(this);

        busca = (BuscaExplore) getIntent().getSerializableExtra("busca");
        if (busca == null) busca = new BuscaExplore();

        seekBar.setOnSeekBarChangeListener(this);

        verificarTodasRemovidas();

        preencherCategorias();
        preencherInventario();
        aplicarFiltroInicial();
        popularListStatus();
    }

    private void preencherCategorias() {
        List<CategoriaRelato> categorias = new CategoriaRelatoService().getCategorias(this);
        for (final CategoriaRelato categoria : categorias) {
            View view = getLayoutInflater().inflate(R.layout.item_expandable_category, categoriasContainer, false);
            view.setTag(categoria);
            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);

            ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);

            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);

            if (!busca.getIdsCategoriaRelato().contains(categoria.getId())) {
                imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", categoria.getIconeInativo(), 0.75f));
                nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", categoria.getIconeAtivo(), 0.75f));
            }


            nomeCategoria.setText(categoria.getNome());
            nomeCategoria.setOnClickListener(v -> {
                desmarcarCategoriasInventario();

                if (busca.getIdsCategoriaRelato().contains(categoria.getId())) {
                    checkSubCategories(view, categoria, false);
                    busca.getIdsCategoriaRelato().remove(categoria.getId());
                    nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", categoria.getIconeInativo(), 0.75f));

                    verificarTodasRemovidas();
                } else {
                    desativarLabel();
                    checkSubCategories(view, categoria, true);
                    busca.getIdsCategoriaRelato().add(categoria.getId());
                    nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
                    imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", categoria.getIconeAtivo(), 0.75f));
                }

                popularListStatus();
            });

            final TextView expander = ButterKnife.findById(view, R.id.expander);
            expander.setOnClickListener(v -> {
                if (expander.getTag() == null) {
                    expander.setTag(new Object());
                    subcategorias.setVisibility(View.VISIBLE);
                    expander.setText("Ocultar subcategorias");
                } else {
                    expander.setTag(null);
                    subcategorias.setVisibility(View.GONE);
                    expander.setText("Ver subcategorias");
                }
            });

            for (CategoriaRelato sub : categoria.getSubcategorias()) {
                View subView = getLayoutInflater().inflate(R.layout.item_subcategoria, subcategorias, false);
                final TextView nome = ButterKnife.findById(subView, R.id.nome);

                if (!busca.getIdsCategoriaRelato().contains(sub.getId()))
                    nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                nome.setText(sub.getNome());
                subcategorias.addView(subView);
                subView.setOnClickListener(v -> {
                    desmarcarCategoriasInventario();

                    if (busca.getIdsCategoriaRelato().contains(sub.getId())) {
                        busca.getIdsCategoriaRelato().remove(sub.getId());
                        nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                        boolean todasSubCategoriasRemovidas = true;
                        for (CategoriaRelato sc : categoria.getSubcategorias()) {
                            if (busca.getIdsCategoriaRelato().contains(sc.getId())) {
                                todasSubCategoriasRemovidas = false;
                                break;
                            }
                        }

                        if (todasSubCategoriasRemovidas) {
                            imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", categoria.getIconeInativo(), 0.75f));

                            nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            if (busca.getIdsCategoriaRelato().contains(sub.getCategoriaMae().getId()))
                                busca.getIdsCategoriaRelato().remove(sub.getCategoriaMae().getId());
                        }

                        verificarTodasRemovidas();
                    } else {
                        busca.getIdsCategoriaRelato().add(sub.getId());
                        if (!busca.getIdsCategoriaRelato().contains(sub.getCategoriaMae().getId()))
                            busca.getIdsCategoriaRelato().add(sub.getCategoriaMae().getId());
                        nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
                        nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);

                        imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", categoria.getIconeAtivo(), 0.75f));

                        desativarLabel();
                    }

                    popularListStatus();
                });
            }

            categoriasContainer.addView(view);
        }
    }

    private void preencherInventario() {
        List<CategoriaInventario> categorias = new CategoriaInventarioService().getCategorias(this);
        for (final CategoriaInventario categoria : categorias) {
            View view = getLayoutInflater().inflate(R.layout.item_expandable_category, categoriasContainer, false);
            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);
            view.setTag(categoria);

            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);

            if (!busca.getIdsCategoriaInventario().contains(categoria.getId())) {
                imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "inventory", categoria.getIconeInativo(), 0.75f));
                nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "inventory", categoria.getIconeAtivo(), 0.75f));
            }

            nomeCategoria.setText(categoria.getNome());
            nomeCategoria.setOnClickListener(v -> {
                desmarcarCategoriasInventario();
                if (busca.getIdsCategoriaInventario().contains(categoria.getId())) {
                    busca.getIdsCategoriaInventario().remove(categoria.getId());
                    nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "inventory", categoria.getIconeInativo(), 0.75f));
                } else {
                    busca.getIdsCategoriaInventario().add(categoria.getId());
                    nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
                    imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "inventory", categoria.getIconeAtivo(), 0.75f));
                }

                desmarcarCategoriasRelato();
            });

            ButterKnife.findById(view, R.id.expander).setVisibility(View.GONE);
            ButterKnife.findById(view, R.id.subcategorias).setVisibility(View.GONE);

            inventarioContainer.addView(view);
        }
    }

    private void desmarcarCategoriasRelato() {
        busca.getIdsCategoriaRelato().clear();

        iconeTodos.setImageResource(R.drawable.filtros_check_todascategorias_ativar);
        textoTodos.setText("Ativar todas as categorias");

        for (int i = 0; i < categoriasContainer.getChildCount(); i++) {
            View view = categoriasContainer.getChildAt(i);
            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);
            nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);
            imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", ((CategoriaRelato) view.getTag()).getIconeInativo(), 0.75f));

            ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);
            for (int j = 0; j < subcategorias.getChildCount(); j++) {
                View v = subcategorias.getChildAt(j);
                TextView nome = ButterKnife.findById(v, R.id.nome);
                nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }

        popularListStatus();
    }

    private void desmarcarCategoriasInventario() {
        busca.getIdsCategoriaInventario().clear();

        for (int i = 0; i < inventarioContainer.getChildCount(); i++) {
            View view = inventarioContainer.getChildAt(i);
            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);
            nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);
            imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "inventory", ((CategoriaInventario) view.getTag()).getIconeInativo(), 0.75f));

            ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);
            for (int j = 0; j < subcategorias.getChildCount(); j++) {
                View v = subcategorias.getChildAt(j);
                TextView nome = ButterKnife.findById(v, R.id.nome);
                nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
    }

    private void verificarTodasRemovidas() {
        if (busca.getIdsCategoriaRelato().isEmpty()) {
            iconeTodos.setImageResource(R.drawable.filtros_check_todascategorias_ativar);
            textoTodos.setText("Ativar todas as categorias");
        } else {
            iconeTodos.setImageResource(R.drawable.filtros_check_todascategorias_desativar);
            textoTodos.setText("Desativar todas as categorias");
        }
    }

    private void desativarLabel() {
        iconeTodos.setImageResource(R.drawable.filtros_check_todascategorias_desativar);
        textoTodos.setText("Desativar todas as categorias");
    }

    private void popularListStatus() {
        CategoriaRelatoService service = new CategoriaRelatoService();
        List<CategoriaRelato.Status> status = new ArrayList<>();
        for (long id : busca.getIdsCategoriaRelato()) {
            status.addAll(service.getStatus(this, id));
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

        Set<Long> ids = new HashSet<>();
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

    @OnClick(R.id.toggleAll)
    public void selecionarOuRemoverTodos() {
        desmarcarCategoriasInventario();
        if (busca.getIdsCategoriaRelato().isEmpty()) {
            adicionarTodasCategoriasRelato();

            iconeTodos.setImageResource(R.drawable.filtros_check_todascategorias_desativar);
            textoTodos.setText("Desativar todas as categorias");

            for (int i = 0; i < categoriasContainer.getChildCount(); i++) {
                View view = categoriasContainer.getChildAt(i);
                TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);
                ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);
                imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", ((CategoriaRelato) view.getTag()).getIconeAtivo(), 0.75f));

                nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);

                ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);
                for (int j = 0; j < subcategorias.getChildCount(); j++) {
                    View v = subcategorias.getChildAt(j);
                    TextView nome = ButterKnife.findById(v, R.id.nome);
                    nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
                }
            }
        } else {
            busca.getIdsCategoriaRelato().clear();

            iconeTodos.setImageResource(R.drawable.filtros_check_todascategorias_ativar);
            textoTodos.setText("Ativar todas as categorias");

            for (int i = 0; i < categoriasContainer.getChildCount(); i++) {
                View view = categoriasContainer.getChildAt(i);
                TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);
                nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);
                imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", ((CategoriaRelato) view.getTag()).getIconeInativo(), 0.75f));

                ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);
                for (int j = 0; j < subcategorias.getChildCount(); j++) {
                    View v = subcategorias.getChildAt(j);
                    TextView nome = ButterKnife.findById(v, R.id.nome);
                    nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        }
    }

    private void adicionarTodasCategoriasRelato() {
        List<CategoriaRelato> categorias = new CategoriaRelatoService().getCategorias(this);
        for (CategoriaRelato categoria : categorias) {
            busca.getIdsCategoriaRelato().add(categoria.getId());
            for (CategoriaRelato sub : categoria.getSubcategorias()) {
                busca.getIdsCategoriaRelato().add(sub.getId());
            }
        }
    }

    private void checkSubCategories(View view, CategoriaRelato categoria, boolean check) {
        for (CategoriaRelato sub : categoria.getSubcategorias()) {
            if (check) busca.getIdsCategoriaRelato().add(sub.getId());
            else busca.getIdsCategoriaRelato().remove(sub.getId());
        }

        ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);
        for (int i = 0; i < subcategorias.getChildCount(); i++) {
            View v = subcategorias.getChildAt(i);
            TextView nome = ButterKnife.findById(v, R.id.nome);
            if (check)
                nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
            else nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    @OnClick(R.id.seletorCategorias)
    public void selecinarCategorias() {
        formCategorias.setVisibility(View.VISIBLE);
        formInventario.setVisibility(View.GONE);
        formPeriodo.setVisibility(View.GONE);

        seletorInventario.setTextColor(Color.parseColor("#808080"));
        seletorPeriodo.setTextColor(Color.parseColor("#808080"));
        seletorCategorias.setTextColor(Color.WHITE);
    }

    @OnClick(R.id.seletorPeriodo)
    public void selecionarPeriodo() {
        formCategorias.setVisibility(View.GONE);
        formInventario.setVisibility(View.GONE);
        formPeriodo.setVisibility(View.VISIBLE);

        seletorInventario.setTextColor(Color.parseColor("#808080"));
        seletorPeriodo.setTextColor(Color.WHITE);
        seletorCategorias.setTextColor(Color.parseColor("#808080"));
    }

    @OnClick(R.id.seletorInventario)
    public void selecionarInventario() {
        formCategorias.setVisibility(View.GONE);
        formInventario.setVisibility(View.VISIBLE);
        formPeriodo.setVisibility(View.GONE);

        seletorInventario.setTextColor(Color.WHITE);
        seletorPeriodo.setTextColor(Color.parseColor("#808080"));
        seletorCategorias.setTextColor(Color.parseColor("#808080"));
    }

    @OnClick(R.id.status)
    public void toggleStatus() {
        if (!busca.getIdsCategoriaRelato().isEmpty()) {
            if (opcoes.getVisibility() == View.GONE) {
                opcoes.setVisibility(View.VISIBLE);
                status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.seta_contrair, 0);
            } else {
                opcoes.setVisibility(View.GONE);
                status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.seta_expandir, 0);
            }
        }
    }

    private View.OnClickListener cliqueOpcao = v -> {

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
    };

    private void aplicarFiltroInicial() {

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

    @OnClick(R.id.botaoConcluido)
    public void concluir() {
        Intent i = new Intent();
        i.putExtra("busca", busca);
        setResult(Activity.RESULT_OK, i);
        finish();
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
}
