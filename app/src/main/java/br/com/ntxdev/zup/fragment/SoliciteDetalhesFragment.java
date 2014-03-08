package br.com.ntxdev.zup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import br.com.ntxdev.zup.TermosDeUsoActivity;
import br.com.ntxdev.zup.domain.Solicitacao;
import br.com.ntxdev.zup.util.FontUtils;

public class SoliciteDetalhesFragment extends Fragment implements View.OnClickListener {

	private boolean publicar = false;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((SoliciteActivity) getActivity()).setInfo(R.string.concluir_solicitacao);
        }
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		((SoliciteActivity) getActivity()).setInfo(R.string.concluir_solicitacao);

		View view = inflater.inflate(R.layout.fragment_solicite_detalhes, container, false);
		view.findViewById(R.id.seletor_postagem).setOnClickListener(this);

		EditText comentario = (EditText) view.findViewById(R.id.comentario);
		comentario.setTypeface(FontUtils.getRegular(getActivity()));
        comentario.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    ((SoliciteActivity) getActivity()).solicitar();
                    handled = true;
                }
                return handled;
            }
        });

		TextView redeSocial = (TextView) view.findViewById(R.id.redeSocial);
		redeSocial.setText(getString(R.string.compartilhar_rede_social, "Facebook"));
		redeSocial.setTypeface(FontUtils.getLight(getActivity()));

		TextView termos = (TextView) view.findViewById(R.id.termos);
		termos.setText(Html.fromHtml(getString(R.string.termos_de_uso_relato)));
		termos.setTypeface(FontUtils.getLight(getActivity()));
		termos.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), TermosDeUsoActivity.class));
			}
		});

		return view;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            Solicitacao solicitacao = (Solicitacao) getArguments().getSerializable("solicitacao");
            TextView comentario = (TextView) getView().findViewById(R.id.comentario);
            comentario.setText(solicitacao.getComentario());
        }
    }

    @Override
	public void onClick(View v) {
		publicar = !publicar;

		if (publicar) {
			((ImageView) v).setImageResource(R.drawable.switch_on);
		} else {
			((ImageView) v).setImageResource(R.drawable.switch_off);
		}
	}

	public String getComentario() {
		return ((TextView) getView().findViewById(R.id.comentario)).getText().toString();
	}
}
