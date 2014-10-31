package br.com.lfdb.zup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import br.com.lfdb.zup.domain.BuscaExplore;
import br.com.lfdb.zup.domain.CategoriaRelato;
import br.com.lfdb.zup.service.CategoriaRelatoService;
import br.com.lfdb.zup.util.ImageUtils;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FiltroExploreNovoActivity extends Activity {

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

    @InjectView(R.id.iconeTodos)
    ImageView iconeTodos;
    @InjectView(R.id.textoTodos)
    TextView textoTodos;

    private BuscaExplore busca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro_explore_novo);
        ButterKnife.inject(this);

        busca = (BuscaExplore) getIntent().getSerializableExtra("busca");
        if (busca == null) busca = new BuscaExplore();

        verificarTodasRemovidas();

        preencherCategorias();
    }

    private void preencherCategorias() {
        List<CategoriaRelato> categorias = new CategoriaRelatoService().getCategorias(this);
        for (CategoriaRelato categoria : categorias) {
            View view = getLayoutInflater().inflate(R.layout.item_expandable_category, categoriasContainer, false);
            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);
            imagem.setImageBitmap(ImageUtils.getScaledCustom(this, "reports", categoria.getIconeAtivo(), 0.75f));
            ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);

            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);

            if (!busca.getIdsCategoriaRelato().contains(categoria.getId())) nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            nomeCategoria.setText(categoria.getNome());
            nomeCategoria.setOnClickListener(v -> {
                if (busca.getIdsCategoriaRelato().contains(categoria.getId())) {
                    checkSubCategories(view, categoria, false);
                    busca.getIdsCategoriaRelato().remove(categoria.getId());
                    nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    verificarTodasRemovidas();
                } else {
                    desativarLabel();
                    checkSubCategories(view, categoria, true);
                    busca.getIdsCategoriaRelato().add(categoria.getId());
                    nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
                }
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

                if (!busca.getIdsCategoriaRelato().contains(sub.getId())) nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                nome.setText(sub.getNome());
                subcategorias.addView(subView);
                subView.setOnClickListener(v -> {
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
                            nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            if (busca.getIdsCategoriaRelato().contains(sub.getCategoriaMae().getId())) busca.getIdsCategoriaRelato().remove(sub.getCategoriaMae().getId());
                        }

                        verificarTodasRemovidas();
                    } else {
                        busca.getIdsCategoriaRelato().add(sub.getId());
                        if (!busca.getIdsCategoriaRelato().contains(sub.getCategoriaMae().getId())) busca.getIdsCategoriaRelato().add(sub.getCategoriaMae().getId());
                        nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
                        nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);

                        desativarLabel();
                    }
                });
            }

            categoriasContainer.addView(view);
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

    @OnClick(R.id.toggleAll)
    public void selecionarOuRemoverTodos() {
        if (busca.getIdsCategoriaRelato().isEmpty()) {
            adicionarTodasCategoriasRelato();

            iconeTodos.setImageResource(R.drawable.filtros_check_todascategorias_desativar);
            textoTodos.setText("Desativar todas as categorias");

            for (int i = 0; i < categoriasContainer.getChildCount(); i++) {
                View view = categoriasContainer.getChildAt(i);
                TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);
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
            if (check) nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
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
}
