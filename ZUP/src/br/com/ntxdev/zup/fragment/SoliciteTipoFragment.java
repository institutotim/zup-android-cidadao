package br.com.ntxdev.zup.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import br.com.ntxdev.zup.domain.Solicitacao;

public class SoliciteTipoFragment extends Fragment implements View.OnClickListener {

	private TextView opcaoColetaEntulho;
	private TextView opcaoBocaDeLobo;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_solicite_tipo, container, false);
		
		((SoliciteActivity) getActivity()).exibirBarraInferior(false);
		((SoliciteActivity) getActivity()).setInfo(R.string.selecione_a_categoria);
		
		opcaoBocaDeLobo = (TextView) view.findViewById(R.id.opcaoBocaDeLobo);
		opcaoBocaDeLobo.setOnClickListener(this);
		opcaoColetaEntulho = (TextView) view.findViewById(R.id.opcaoColetaEntulho);
		opcaoColetaEntulho.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View v) {		
		select(v.getId());
		
		if (v.getId() == R.id.opcaoBocaDeLobo) {
			((SoliciteActivity) getActivity()).setTipo(Solicitacao.Tipo.BOCA_LOBO);
		} else if (v.getId() == R.id.opcaoColetaEntulho) {
			((SoliciteActivity) getActivity()).setTipo(Solicitacao.Tipo.COLETA_ENTULHO);
		}
	}
	
	private void select(int id) {
		if (id == R.id.opcaoBocaDeLobo) {
			opcaoBocaDeLobo.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.icon_boca_lobo_normal), null, null);
			opcaoBocaDeLobo.setTextColor(Color.BLACK);
			opcaoColetaEntulho.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.icon_coleta_entulho_disabled), null, null);
			opcaoColetaEntulho.setTextColor(Color.rgb(0x99, 0x99, 0x99));
		} else if (id == R.id.opcaoColetaEntulho) {
			opcaoBocaDeLobo.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.icon_boca_lobo_disabled), null, null);
			opcaoBocaDeLobo.setTextColor(Color.rgb(0x99, 0x99, 0x99));
			opcaoColetaEntulho.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.icon_coleta_entulho_normal), null, null);
			opcaoColetaEntulho.setTextColor(Color.BLACK);
		}
	}
}
