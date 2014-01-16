package br.com.ntxdev.zup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import br.com.ntxdev.zup.TermosDeUsoActivity;
import br.com.ntxdev.zup.util.FontUtils;

public class SoliciteDetalhesFragment extends Fragment implements View.OnClickListener {

	private boolean publicar = false;
	private boolean termos = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		((SoliciteActivity) getActivity())
				.setInfo(R.string.concluir_solicitacao);

		View view = inflater.inflate(R.layout.fragment_solicite_detalhes,
				container, false);
		view.findViewById(R.id.seletor_postagem).setOnClickListener(this);
		view.findViewById(R.id.seletor_termos).setOnClickListener(this);

		TextView comentario = (TextView) view.findViewById(R.id.comentario);
		comentario.setTypeface(FontUtils.getRegular(getActivity()));

		TextView redeSocial = (TextView) view.findViewById(R.id.redeSocial);
		redeSocial.setText(getString(R.string.compartilhar_rede_social, "Facebook"));
		redeSocial.setTypeface(FontUtils.getLight(getActivity()));
		
		TextView termos = (TextView) view.findViewById(R.id.termos);
		termos.setText(Html.fromHtml(getString(R.string.concordo_com_os_termos_de_uso)));
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
	public void onClick(View v) {
		if (v.getId() == R.id.seletor_postagem) {
			publicar = !publicar;
	
			if (publicar) {
				((ImageView) v).setImageResource(R.drawable.switch_on);
			} else {
				((ImageView) v).setImageResource(R.drawable.switch_off);
			}
		} else if (v.getId() == R.id.seletor_termos) {
			termos = !termos;
			
			if (termos) {
				((ImageView) v).setImageResource(R.drawable.switch_on);
			} else {
				((ImageView) v).setImageResource(R.drawable.switch_off);
			}
		}
	}
	
	public String getComentario() {
		return ((TextView) getView().findViewById(R.id.comentario)).getText().toString();
	}
}
