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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SolicitacaoDetalheActivity;
import br.com.ntxdev.zup.domain.SolicitacaoListItem;
import br.com.ntxdev.zup.util.FontUtils;

public class MinhaContaFragment extends Fragment implements AdapterView.OnItemClickListener {
	
	private Button botaoSair;
	private Button botaoEditar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_minha_conta, container, false);
		
		botaoSair = (Button) view.findViewById(R.id.botaoSair);
		botaoSair.setTypeface(FontUtils.getRegular(getActivity()));
		botaoEditar = (Button) view.findViewById(R.id.botaoEditar);
		botaoEditar.setTypeface(FontUtils.getRegular(getActivity()));
		
		((TextView) view.findViewById(R.id.instrucoes)).setTypeface(FontUtils.getBold(getActivity()));

		List<SolicitacaoListItem> items = new ArrayList<SolicitacaoListItem>();
		
		((TextView) view.findViewById(R.id.minhaConta)).setTypeface(FontUtils.getLight(getActivity()));
		TextView nomeUsuario = (TextView) view.findViewById(R.id.nomeUsuario);
		nomeUsuario.setTypeface(FontUtils.getLight(getActivity()));
		
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
		
		TextView solicitacoes = (TextView) view.findViewById(R.id.solicitacoes);
		solicitacoes.setTypeface(FontUtils.getBold(getActivity()));
		solicitacoes.setText(items.size() + " " + 
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

			TextView titulo = (TextView) row.findViewById(R.id.titulo);
			titulo.setText(item.getTitulo());
			titulo.setTypeface(FontUtils.getLight(getContext()));
			
			TextView data = (TextView) row.findViewById(R.id.data);
			data.setText(item.getData());
			data.setTypeface(FontUtils.getBold(getContext()));
			
			TextView protocolo = (TextView) row.findViewById(R.id.protocolo);
			protocolo.setText(getString(R.string.protocolo) + " " + item.getProtocolo());
			protocolo.setTypeface(FontUtils.getRegular(getContext()));
			
			row.findViewById(R.id.bg).setBackgroundColor(item.getStatus().getColor());
			TextView indicadorStatus = (TextView) row.findViewById(R.id.indicadorStatus);
			indicadorStatus.setTypeface(FontUtils.getBold(getContext()));
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
