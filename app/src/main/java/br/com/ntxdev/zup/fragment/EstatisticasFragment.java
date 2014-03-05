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

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import br.com.ntxdev.zup.FiltroEstatisticasActivity;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.core.Constantes;
import br.com.ntxdev.zup.domain.BuscaEstatisticas;
import br.com.ntxdev.zup.domain.Estatistica;
import br.com.ntxdev.zup.service.LoginService;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.ImageUtils;

import com.todddavies.components.progressbar.ProgressWheel;

public class EstatisticasFragment extends Fragment {

	public static final int REQUEST_FILTRO = 1478;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_estatisticas, container, false);
		
		((TextView) view.findViewById(R.id.titulo)).setTypeface(FontUtils.getLight(getActivity()));
		
		TextView botaoFiltrar = (TextView) view.findViewById(R.id.botaoFiltrar);
		botaoFiltrar.setTypeface(FontUtils.getRegular(getActivity()));
		botaoFiltrar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getActivity(), FiltroEstatisticasActivity.class), REQUEST_FILTRO);				
			}
		});

        new Tasker().execute();
		
		return view;
	}

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            new Tasker().execute();
        }
    }

    public void aplicarFiltro(BuscaEstatisticas busca) {
		new Tasker(busca).execute();
	}
	
	public class Tasker extends AsyncTask<Void, Void, String> {
		
		private ProgressDialog dialog;
		private String data = null;
		private Long id = null;
		
		public Tasker() {
		}
		
		public Tasker(BuscaEstatisticas busca) {
			this.id = busca.getCategoria();
			this.data = busca.getPeriodo().getDateString();
		}

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
				String request = Constantes.REST_URL + "/reports/stats";
				if (id != null) {
					request += "?category_id=" + id;
				}
				if (data != null) {
					if (id != null) {
						request += "&begin_date=" + data;
					} else {
						request += "?begin_date=" + data;
					}
				}
				HttpGet get = new HttpGet(request);
				get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));
				HttpResponse response = client.execute(get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					return EntityUtils.toString(response.getEntity(), "UTF-8");
				}
			} catch (Exception e) {
				Log.e("ZUP", e.getMessage(), e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			if (result != null) {
				try {
					JSONArray array = new JSONObject(result).getJSONArray("stats");
					List<Estatistica> estatisticas = new ArrayList<Estatistica>();
					for (int i = 0; i < array.length(); i++) {
						JSONArray statuses = array.getJSONObject(i).getJSONArray("statuses");
						for (int j = 0; j < statuses.length(); j++) {
							JSONObject status = statuses.getJSONObject(j);
							Estatistica estatistica = new Estatistica(status.getInt("status_id"), Color.parseColor(status.getString("color")), 
									status.getInt("count"), status.getString("title"));
							if (estatisticas.contains(estatistica)) {
								for (Estatistica e : estatisticas) {
									if (e.equals(estatistica)) {
										e.setQuantidade(e.getQuantidade() + estatistica.getQuantidade());
										break;
									}
								}
							} else {
								estatisticas.add(estatistica);
							}
						}
					}
					
					int total = 0;
					for (Estatistica estatistica : estatisticas) {
						total += estatistica.getQuantidade();
					}
					
					for (Estatistica estatistica : estatisticas) {
						estatistica.setPorcentagem((int) (estatistica.getQuantidade() * 100.0 / total));
					}
					
					montarGraficos(estatisticas);
				} catch (Exception e) {
					Log.e("ZUP", e.getMessage(), e);
					Toast.makeText(getActivity(), "Não foi possível obter as estatísticas", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getActivity(), "Não foi possível obter as estatísticas", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void montarGraficos(List<Estatistica> estatisticas) {
		for (int i = 1; i <= 5; i++) {
			((LinearLayout) getView().findViewById(getActivity().getResources().getIdentifier("line_" + i, "id", getActivity().getPackageName()))).removeAllViews();
		}
		
		int linha = 0;
		boolean margin = false;
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		for (int i = 0; i < estatisticas.size(); i++) {
			if (i % 2 == 0) linha++;
			LinearLayout container = (LinearLayout) getView().findViewById(getActivity().getResources().getIdentifier("line_" + linha, "id", getActivity().getPackageName()));
			View view = inflater.inflate(R.layout.estatistica_item, container, false);
			ProgressWheel wheel = (ProgressWheel) view.findViewById(R.id.spinner);
			wheel.setText(estatisticas.get(i).getPorcentagem() + "%");
			wheel.setBarColor(estatisticas.get(i).getCor());
			wheel.setProgress((int) (360.0 / 100.0 * estatisticas.get(i).getPorcentagem()));
			TextView qtd = (TextView) view.findViewById(R.id.txtQtd);
			qtd.setText(String.valueOf(estatisticas.get(i).getQuantidade()));
			qtd.setTypeface(FontUtils.getExtraBold(getActivity()));
			qtd.setTextColor(estatisticas.get(i).getCor());
			TextView label = (TextView) view.findViewById(R.id.txtLabel);
			label.setText(estatisticas.get(i).getTexto());
			label.setTypeface(FontUtils.getRegular(getActivity()));
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
			if (margin) {
				params.setMargins(0, 0, (int) ImageUtils.dpToPx(getActivity(), 35), 0);
			} else {
				params.setMargins((int) ImageUtils.dpToPx(getActivity(), 35), 0, 0, 0);
			}
			view.setLayoutParams(params);
			margin = !margin;
			
			container.addView(view);
		}
	}
}
