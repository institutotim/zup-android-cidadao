package br.com.ntxdev.zup.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SolicitacaoDetalheActivity;
import br.com.ntxdev.zup.domain.SolicitacaoListItem;

public class MinhaContaFragment extends Fragment implements AdapterView.OnItemClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_minha_conta, container, false);

		List<SolicitacaoListItem> items = new ArrayList<SolicitacaoListItem>();

		SolicitacaoListItem item = new SolicitacaoListItem();
		item.setData("hoje");
		item.setProtocolo("uhuihhfgisdhguhd");
		item.setTitulo("Limpeza de boca de lobo");
		item.setStatus(SolicitacaoListItem.Status.EM_ABERTO);
		items.add(item);

		item = new SolicitacaoListItem();
		item.setData("há 12 dias atrás");
		item.setProtocolo("uhuihhfgisdhguhd");
		item.setTitulo("Coleta de entulho");
		item.setStatus(SolicitacaoListItem.Status.EM_ANDAMENTO);
		items.add(item);

		item = new SolicitacaoListItem();
		item.setData("há 2 dias atrás");
		item.setProtocolo("uhuihhfgisdhguhd");
		item.setTitulo("Coleta de entulho");
		item.setStatus(SolicitacaoListItem.Status.NAO_RESOLVIDO);
		items.add(item);

		item = new SolicitacaoListItem();
		item.setData("ontem");
		item.setProtocolo("uhuihhfgisdhguhd");
		item.setTitulo("Coleta de entulho");
		item.setStatus(SolicitacaoListItem.Status.RESOLVIDO);
		items.add(item);

		ListView list = (ListView) view.findViewById(R.id.listaSolicitacoes);
		list.setOnItemClickListener(this);
		list.setAdapter(new SolicitacaoAdapter(getActivity(), items));
		
		((TextView) view.findViewById(R.id.solicitacoes)).setText(items.size() + " " + 
				(items.size() == 1 ? getString(R.string.solicitacao) : getString(R.string.solicitacoes)));

		return view;
	}

	public class SolicitacaoAdapter extends ArrayAdapter<SolicitacaoListItem> {

		private List<SolicitacaoListItem> items;

		public SolicitacaoAdapter(Context context, List<SolicitacaoListItem> objects) {
			super(context, R.layout.solicitacao_row, objects);
			items = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = getActivity().getLayoutInflater().inflate(R.layout.solicitacao_row, parent, false);
			SolicitacaoListItem item = items.get(position);

			((TextView) row.findViewById(R.id.titulo)).setText(item.getTitulo());
			((TextView) row.findViewById(R.id.data)).setText(item.getData());
			((TextView) row.findViewById(R.id.protocolo)).setText(getString(R.string.protocolo) + " " + item.getProtocolo());
			
			row.findViewById(R.id.bg).setBackgroundColor(item.getStatus().getColor());
			TextView indicadorStatus = (TextView) row.findViewById(R.id.indicadorStatus);
			switch (item.getStatus()) {
			case EM_ABERTO:
				indicadorStatus.setText(R.string.em_aberto);
				indicadorStatus.setBackgroundResource(R.drawable.status_red_bg);
				break;
			case EM_ANDAMENTO:
				indicadorStatus.setText(R.string.em_andamento);
				indicadorStatus.setBackgroundResource(R.drawable.status_orange_bg);
				break;
			case RESOLVIDO:
				indicadorStatus.setText(R.string.resolvido);
				indicadorStatus.setBackgroundResource(R.drawable.status_green_bg);
				break;
			case NAO_RESOLVIDO:
				indicadorStatus.setText(R.string.nao_resolvido);
				indicadorStatus.setBackgroundResource(R.drawable.status_gray_bg);
				break;
			}

			return row;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SolicitacaoListItem item = (SolicitacaoListItem) parent.getItemAtPosition(position);
		Intent intent = new Intent(getActivity(), SolicitacaoDetalheActivity.class);
		intent.putExtra("solicitacao", item);
		startActivity(intent);
	}
}
