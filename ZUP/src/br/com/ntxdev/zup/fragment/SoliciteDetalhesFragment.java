package br.com.ntxdev.zup.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import br.com.ntxdev.zup.util.FontUtils;

public class SoliciteDetalhesFragment extends Fragment implements View.OnClickListener {

	private boolean publicar = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		((SoliciteActivity) getActivity()).setInfo(R.string.concluir_solicitacao);
		
		View view = inflater.inflate(R.layout.fragment_solicite_detalhes, container, false);		
		view.findViewById(R.id.seletor_postagem).setOnClickListener(this);
		
		TextView comentario = (TextView) view.findViewById(R.id.comentario);
		comentario.setTypeface(FontUtils.getRegular(getActivity()));
		comentario.setOnEditorActionListener(new TextView.OnEditorActionListener() {			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				((SoliciteActivity) getActivity()).setComentario(v.getText().toString());
				return false;
			}
		});
		
		TextView redeSocial = (TextView) view.findViewById(R.id.redeSocial);
		redeSocial.setText(getString(R.string.compartilhar_rede_social, "Facebook"));
		redeSocial.setTypeface(FontUtils.getLight(getActivity()));
		
		return view;
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
}
