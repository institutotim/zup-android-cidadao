package br.com.ntxdev.zup.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import br.com.ntxdev.zup.EditarContaActivity;
import br.com.ntxdev.zup.OpeningActivity;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SolicitacaoDetalheActivity;
import br.com.ntxdev.zup.SolicitacaoListItemAdapter;
import br.com.ntxdev.zup.core.Constantes;
import br.com.ntxdev.zup.domain.SolicitacaoListItem;
import br.com.ntxdev.zup.domain.Usuario;
import br.com.ntxdev.zup.service.LoginService;
import br.com.ntxdev.zup.service.UsuarioService;
import br.com.ntxdev.zup.util.FileUtils;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.ImageUtils;

public class MinhaContaFragment extends Fragment implements AdapterView.OnItemClickListener {
	
	private static final int REQUEST_EDIT_USER = 1099;
	
	private TextView botaoSair;
	private TextView botaoEditar;

	private TextView nomeUsuario;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_minha_conta, container, false);
		
		botaoSair = (TextView) view.findViewById(R.id.botaoSair);
		botaoSair.setTypeface(FontUtils.getRegular(getActivity()));
		botaoSair.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(getActivity())
						.setMessage(R.string.deseja_realmente_sair_da_sua_conta)
						.setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								new LoginService().registrarLogout(getActivity());
								startActivity(new Intent(getActivity(), OpeningActivity.class));
								getActivity().finish();								
							}
						})
						.setNegativeButton(R.string.nao, new DialogInterface.OnClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();								
							}
						})
						.show();
				
			}
		});
		
		botaoEditar = (TextView) view.findViewById(R.id.botaoEditar);
		botaoEditar.setTypeface(FontUtils.getRegular(getActivity()));
		botaoEditar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getActivity(), EditarContaActivity.class), REQUEST_EDIT_USER);				
			}
		});
		
		((TextView) view.findViewById(R.id.instrucoes)).setTypeface(FontUtils.getBold(getActivity()));

		List<SolicitacaoListItem> items = new ArrayList<SolicitacaoListItem>();
		
		((TextView) view.findViewById(R.id.minhaConta)).setTypeface(FontUtils.getLight(getActivity()));
		nomeUsuario = (TextView) view.findViewById(R.id.nomeUsuario);
		nomeUsuario.setTypeface(FontUtils.getLight(getActivity()));
		
		Usuario usuario = new UsuarioService().getUsuarioAtivo(getActivity());
		if (usuario != null) {
			nomeUsuario.setText(usuario.getNome() != null ? usuario.getNome() : usuario.getEmail());
		}
		
		TextView solicitacoes = (TextView) view.findViewById(R.id.solicitacoes);
		solicitacoes.setTypeface(FontUtils.getBold(getActivity()));
		solicitacoes.setText(items.size() + " " + 
				(items.size() == 1 ? getString(R.string.solicitacao) : getString(R.string.solicitacoes)));

		return view;
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			new Tasker().execute();
		}
	}
	
	private void preencherLista(List<SolicitacaoListItem> itens) {
		ListView list = (ListView) getView().findViewById(R.id.listaSolicitacoes);
		list.setOnItemClickListener(this);
		list.setAdapter(new SolicitacaoAdapter(getActivity(), itens));
		
		TextView solicitacoes = (TextView) getView().findViewById(R.id.solicitacoes);
		solicitacoes.setTypeface(FontUtils.getBold(getActivity()));
		solicitacoes.setText(itens.size() + " " + 
				(itens.size() == 1 ? getString(R.string.solicitacao) : getString(R.string.solicitacoes)));
	}

	public class SolicitacaoAdapter extends ArrayAdapter<SolicitacaoListItem> {

		private List<SolicitacaoListItem> items;

		public SolicitacaoAdapter(Context context, List<SolicitacaoListItem> objects) {
			super(context, R.layout.solicitacao_row, objects);
			items = objects;
		}

		@SuppressLint("NewApi")
		@SuppressWarnings("deprecation")
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
			int fiveDp = (int) ImageUtils.dpToPx(getActivity(), 5);
			int tenDp = (int) ImageUtils.dpToPx(getActivity(), 10);
			indicadorStatus.setPadding(tenDp, fiveDp, tenDp, fiveDp);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				indicadorStatus.setBackgroundDrawable(ImageUtils.getStatusBackground(getActivity(), item.getStatus().getCor()));
			} else {
				indicadorStatus.setBackground(ImageUtils.getStatusBackground(getActivity(), item.getStatus().getCor()));
			}
			indicadorStatus.setText(item.getStatus().getNome());

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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_EDIT_USER && resultCode == Activity.RESULT_OK) {
			Usuario usuario = new UsuarioService().getUsuarioAtivo(getActivity());
			if (usuario != null) {
				nomeUsuario.setText(usuario.getNome() != null ? usuario.getNome() : usuario.getEmail());
			}
		}
	}
	
	public class Tasker extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(getActivity());
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
			dialog.setMessage("Por favor, aguarde...");
			dialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(Constantes.REST_URL + "/reports/users/me/items");
				get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));
				HttpResponse response = client.execute(get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), "UTF-8");
					baixarFotos(json);
					return json;
				}
			} catch (Exception e) {
				Log.e("ZUP", e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			if (result != null) {
				try {
					JSONArray array = new JSONObject(result).getJSONArray("reports");
					List<SolicitacaoListItem> itens = new ArrayList<SolicitacaoListItem>();
					for (int i = 0; i < array.length(); i++) {
						itens.add(SolicitacaoListItemAdapter.adapt(array.getJSONObject(i)));
					}
					preencherLista(itens);
				} catch (Exception e) {
					Log.e("ZUP", e.getMessage());
					Toast.makeText(getActivity(), "Não foi possível obter sua lista de relatos", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getActivity(), "Não foi possível obter sua lista de relatos", Toast.LENGTH_LONG).show();
			}
		}
		
		private void baixarFotos(String json) throws Exception {
			JSONArray array = new JSONObject(json).getJSONArray("reports");
			for (int i = 0; i < array.length(); i++) {
				JSONArray fotos = array.getJSONObject(i).getJSONArray("images");
				for (int j = 0; j < fotos.length(); j++) {
					FileUtils.downloadImage(fotos.getJSONObject(j).getString("url"));
				}				
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		new Tasker().execute();
	}
}
