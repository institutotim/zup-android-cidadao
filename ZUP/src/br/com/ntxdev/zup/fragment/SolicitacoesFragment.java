package br.com.ntxdev.zup.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SolicitacaoDetalheActivity;
import br.com.ntxdev.zup.domain.SolicitacaoListItem;
import br.com.ntxdev.zup.util.FontUtils;

public class SolicitacoesFragment extends ListFragment implements AdapterView.OnItemClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_solicitacoes, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		List<SolicitacaoListItem> items = new ArrayList<SolicitacaoListItem>();

		SolicitacaoListItem item = new SolicitacaoListItem();
		item.setData("há 2 dias atrás");
		item.setProtocolo("1746352803");
		item.setTitulo("Limpeza de boca de lobo");
		//item.setStatus(SolicitacaoListItem.Status.EM_ABERTO);
		//item.setFotos(Arrays.asList(R.drawable.bocalobo2));
		item.setEndereco("Av. 9 de julho, 1522, Bela Vista, São Paulo");
		item.setComentario("Hoje choveu e com a boca de lobo entupida, a água não pode escoar, causando alagamento e transtornos para pedestres como eu.");
		items.add(item);	
		
		item = new SolicitacaoListItem();
		item.setData("há 8 dias atrás");
		item.setProtocolo("1746352824");
		item.setTitulo("Limpeza de boca de lobo");
		//item.setStatus(SolicitacaoListItem.Status.EM_ANDAMENTO);
		//item.setFotos(Arrays.asList(R.drawable.bocalobo3));
		item.setEndereco("Rua Paim, 133, Bela Vista, São Paulo");
		item.setComentario("Manutenção urgente nessa boca de lobo!");
		items.add(item);
		
		item = new SolicitacaoListItem();
		item.setData("há 15 dias atrás");
		item.setProtocolo("1444352824");
		item.setTitulo("Limpeza de boca de lobo");
		//item.setStatus(SolicitacaoListItem.Status.RESOLVIDO);
		//item.setFotos(Arrays.asList(R.drawable.bocalobo5));
		item.setEndereco("Rua dos Estudantes, 31, Liberdade, São Paulo");
		item.setComentario("Há varios dias passo por aqui e a situação não muda. Mandem alguém limpar essa boca de lobo por favor.");
		items.add(item);
		
		getListView().setAdapter(new SolicitacaoAdapter(getActivity(), items));
		getListView().setOnItemClickListener(this);
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

			row.findViewById(R.id.bg).setBackgroundColor(item.getStatus().getCor());
			TextView indicadorStatus = (TextView) row.findViewById(R.id.indicadorStatus);
			indicadorStatus.setTypeface(FontUtils.getBold(getContext()));
//			switch (item.getStatus()) {
//			case EM_ABERTO:
//				indicadorStatus.setText(R.string.em_aberto);
//				indicadorStatus.setBackgroundResource(R.drawable.status_red_bg);
//				break;
//			case EM_ANDAMENTO:
//				indicadorStatus.setText(R.string.em_andamento);
//				indicadorStatus.setBackgroundResource(R.drawable.status_orange_bg);
//				break;
//			case RESOLVIDO:
//				indicadorStatus.setText(R.string.resolvido);
//				indicadorStatus.setBackgroundResource(R.drawable.status_green_bg);
//				break;
//			case NAO_RESOLVIDO:
//				indicadorStatus.setText(R.string.nao_resolvido);
//				indicadorStatus.setBackgroundResource(R.drawable.status_gray_bg);
//				break;
//			}

			return row;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SolicitacaoListItem item = (SolicitacaoListItem) parent.getItemAtPosition(position);
		Intent intent = new Intent(getActivity(), SolicitacaoDetalheActivity.class);
		intent.putExtra("solicitacao", item);
		intent.putExtra("alterar_botao", true);
		startActivity(intent);
	}
}
