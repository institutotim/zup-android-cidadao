package br.com.lfdb.zup.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.lfdb.zup.R;
import br.com.lfdb.zup.SoliciteActivity;
import br.com.lfdb.zup.adapter.ReportCategoryAdapter;
import br.com.lfdb.zup.domain.BuscaExplore;
import br.com.lfdb.zup.domain.CategoriaRelato;
import br.com.lfdb.zup.domain.Solicitacao;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class SoliciteTipoNovoFragment extends Fragment {

    @InjectView(R.id.lista)
    RecyclerView lista;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_solicite_tipo_novo, container, false);
        ButterKnife.inject(this, view);

        lista.setLayoutManager(new LinearLayoutManager(getActivity()));
        lista.setHasFixedSize(true);

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
        lista.setAdapter(new ReportCategoryAdapter(getActivity(), new BuscaExplore()));
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((SoliciteActivity) getActivity()).setInfo(R.string.selecione_a_categoria_subcategoria);
        }
    }
}
