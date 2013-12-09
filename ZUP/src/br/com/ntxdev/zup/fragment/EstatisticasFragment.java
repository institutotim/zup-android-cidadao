package br.com.ntxdev.zup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import br.com.ntxdev.zup.FiltroEstatisticasActivity;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.util.FontUtils;

import com.todddavies.components.progressbar.ProgressWheel;

public class EstatisticasFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_estatisticas, container, false);
		
		((TextView) view.findViewById(R.id.titulo)).setTypeface(FontUtils.getLight(getActivity()));
		
		((TextView) view.findViewById(R.id.txtQtdAndamento)).setTypeface(FontUtils.getExtraBold(getActivity()));
		((TextView) view.findViewById(R.id.txtQtdEmAberto)).setTypeface(FontUtils.getExtraBold(getActivity()));
		((TextView) view.findViewById(R.id.txtQtdNaoResolvido)).setTypeface(FontUtils.getExtraBold(getActivity()));
		((TextView) view.findViewById(R.id.txtQtdResolvido)).setTypeface(FontUtils.getExtraBold(getActivity()));
		
		((TextView) view.findViewById(R.id.txtAndamento)).setTypeface(FontUtils.getRegular(getActivity()));
		((TextView) view.findViewById(R.id.txtEmAberto)).setTypeface(FontUtils.getRegular(getActivity()));
		((TextView) view.findViewById(R.id.txtNaoResolvido)).setTypeface(FontUtils.getRegular(getActivity()));
		((TextView) view.findViewById(R.id.txtResolvido)).setTypeface(FontUtils.getRegular(getActivity()));
		
		TextView botaoFiltrar = (TextView) view.findViewById(R.id.botaoFiltrar);
		botaoFiltrar.setTypeface(FontUtils.getRegular(getActivity()));
		botaoFiltrar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), FiltroEstatisticasActivity.class));				
			}
		});
		
		ProgressWheel pResolvido = (ProgressWheel) view.findViewById(R.id.spinnerResolvido);
		pResolvido.setProgress((int) (360.0 / 100.0 * 28));
		
		ProgressWheel pAndamento = (ProgressWheel) view.findViewById(R.id.spinnerAndamento);
		pAndamento.setProgress((int) (360.0 / 100.0 * 61));
		
		ProgressWheel pEmAberto = (ProgressWheel) view.findViewById(R.id.spinnerEmAberto);
		pEmAberto.setProgress((int) (360.0 / 100.0 * 11));
		
		ProgressWheel pNaoResolvido = (ProgressWheel) view.findViewById(R.id.spinnerNaoResolvido);
		pNaoResolvido.setProgress((int) (360.0 / 100.0 * 4));
		return view;
	}
}
