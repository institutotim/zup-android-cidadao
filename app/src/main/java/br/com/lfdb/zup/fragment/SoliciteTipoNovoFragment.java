package br.com.lfdb.zup.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import br.com.lfdb.zup.R;
import br.com.lfdb.zup.SoliciteActivity;
import br.com.lfdb.zup.domain.CategoriaRelato;
import br.com.lfdb.zup.domain.Solicitacao;
import br.com.lfdb.zup.service.CategoriaRelatoService;
import br.com.lfdb.zup.util.ImageUtils;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class SoliciteTipoNovoFragment extends Fragment {

    @InjectView(R.id.categoriasContainer)
    LinearLayout categoriasContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_solicite_tipo_novo, container, false);
        ButterKnife.inject(this, view);

        ((SoliciteActivity) getActivity()).exibirBarraInferior(false);
        ((SoliciteActivity) getActivity()).setInfo(R.string.selecione_a_categoria_subcategoria);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        CategoriaRelato selecionada = null;
        if (getArguments() != null) {
            Solicitacao solicitacao = (Solicitacao) getArguments().getSerializable("solicitacao");
            if (solicitacao != null) {
                selecionada = solicitacao.getCategoria();
            }
        }

        if (selecionada == null) {
            selecionada = ((SoliciteActivity) getActivity()).getCategoria();
        }

        montarCategoriasRelatos(selecionada);
    }

    private void montarCategoriasRelatos(CategoriaRelato selecionada) {
        List<CategoriaRelato> categorias = new CategoriaRelatoService().getCategorias(getActivity());
        for (final CategoriaRelato categoria : categorias) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.item_expandable_category, categoriasContainer, false);
            view.setTag(categoria);
            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);

            ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);

            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);

            if (!categoria.isSubcategoria(selecionada)) {
                imagem.setImageBitmap(ImageUtils.getScaledCustom(getActivity(), "reports", categoria.getIconeInativo(), 0.75f));
                nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                imagem.setImageBitmap(ImageUtils.getScaledCustom(getActivity(), "reports", categoria.getIconeAtivo(), 0.75f));
            }

            nomeCategoria.setText(categoria.getNome());
            nomeCategoria.setOnClickListener(v -> {

                if (categoria.getSubcategorias().isEmpty()) {
                    desmarcarTudo();

                    nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);
                    imagem.setImageBitmap(ImageUtils.getScaledCustom(getActivity(), "reports", categoria.getIconeAtivo(), 0.75f));

                    ((SoliciteActivity) getActivity()).setCategoria(categoria);
                } else {
                    if (nomeCategoria.getTag() == null) {
                        nomeCategoria.setTag(new Object());
                        subcategorias.setVisibility(View.VISIBLE);
                    } else {
                        nomeCategoria.setTag(null);
                        subcategorias.setVisibility(View.GONE);
                    }
                }
            });

            ButterKnife.findById(view, R.id.expander).setVisibility(View.GONE);

            for (CategoriaRelato sub : categoria.getSubcategorias()) {
                View subView = getActivity().getLayoutInflater().inflate(R.layout.item_subcategoria, subcategorias, false);
                final TextView nome = ButterKnife.findById(subView, R.id.nome);

                if (selecionada == null || selecionada.getId() != sub.getId()) {
                    nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }

                nome.setText(sub.getNome());
                subcategorias.addView(subView);
                subView.setOnClickListener(v -> {

                    desmarcarTudo();
                    subcategorias.setVisibility(View.VISIBLE);
                    imagem.setImageBitmap(ImageUtils.getScaledCustom(getActivity(), "reports", categoria.getIconeAtivo(), 0.75f));
                    nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.filtros_check_categoria, 0);

                    ((SoliciteActivity) getActivity()).setCategoria(sub);
                });
            }

            categoriasContainer.addView(view);
        }
    }

    private void desmarcarTudo() {
        for (int i = 0; i < categoriasContainer.getChildCount(); i++) {
            View view = categoriasContainer.getChildAt(i);
            TextView nomeCategoria = ButterKnife.findById(view, R.id.nomeCategoria);
            nomeCategoria.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            ImageView imagem = ButterKnife.findById(view, R.id.imagemCategoria);
            imagem.setImageBitmap(ImageUtils.getScaledCustom(getActivity(), "reports", ((CategoriaRelato) view.getTag()).getIconeInativo(), 0.75f));

            ViewGroup subcategorias = ButterKnife.findById(view, R.id.subcategorias);
            subcategorias.setVisibility(View.GONE);
            for (int j = 0; j < subcategorias.getChildCount(); j++) {
                View v = subcategorias.getChildAt(j);
                TextView nome = ButterKnife.findById(v, R.id.nome);
                nome.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((SoliciteActivity) getActivity()).setInfo(R.string.selecione_a_categoria_subcategoria);
        }
    }
}
