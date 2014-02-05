package br.com.ntxdev.zup.fragment;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import br.com.ntxdev.zup.DetalheMapaActivity;
import br.com.ntxdev.zup.FiltroExploreActivity;
import br.com.ntxdev.zup.MainActivity;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SolicitacaoDetalheActivity;
import br.com.ntxdev.zup.core.Constantes;
import br.com.ntxdev.zup.domain.BuscaExplore;
import br.com.ntxdev.zup.domain.CategoriaRelato;
import br.com.ntxdev.zup.domain.ItemInventario;
import br.com.ntxdev.zup.domain.ItemRelato;
import br.com.ntxdev.zup.domain.SolicitacaoListItem;
import br.com.ntxdev.zup.service.CategoriaInventarioService;
import br.com.ntxdev.zup.service.CategoriaRelatoService;
import br.com.ntxdev.zup.service.LoginService;
import br.com.ntxdev.zup.util.DateUtils;
import br.com.ntxdev.zup.util.FileUtils;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.GeoUtils;
import br.com.ntxdev.zup.util.ImageUtils;
import br.com.ntxdev.zup.util.PreferenceUtils;
import br.com.ntxdev.zup.widget.AutoCompleteAdapter;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

@SuppressLint("NewApi")
public class ExploreFragment extends Fragment implements OnInfoWindowClickListener, AdapterView.OnItemClickListener {

	private static final int SLEEP_TIMER = 500;
	private static final int TIMEOUT_TIMER = 1000;
	
	private boolean running = true;
	private GoogleMap map;
	private static View view;
	private double latitude, longitude;
	private long visibleRadius = 100000;
	
	private Queue<ExploreFragment.Request> requests = new ConcurrentLinkedQueue<ExploreFragment.Request>();
	private long timestamp = 0;

	private TextView botaoFiltrar;
	private BuscaExplore busca;

	private Map<Marker, Object> marcadores = new HashMap<Marker, Object>();

	private Marker pontoBusca;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}

		try {
			view = inflater.inflate(R.layout.fragment_explore, container, false);
		} catch (InflateException e) {
			Log.w("ZUP", e.getMessage());
		}

		map = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		botaoFiltrar = (TextView) view.findViewById(R.id.botaoFiltrar);
		botaoFiltrar.setTypeface(FontUtils.getRegular(getActivity()));
		botaoFiltrar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), FiltroExploreActivity.class);
				intent.putExtra("busca", busca);
				getActivity().startActivityForResult(intent, MainActivity.FILTRO_CODE);
			}
		});

		AutoCompleteTextView autoCompView = (AutoCompleteTextView) view.findViewById(R.id.autocomplete);
		autoCompView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item));
		autoCompView.setTypeface(FontUtils.getRegular(getActivity()));
		autoCompView.setOnItemClickListener(this);

		busca = PreferenceUtils.obterBuscaExplore(getActivity());
		if (busca == null) {
			busca = new BuscaExplore();
		}

		if (map != null) {
			map.setMyLocationEnabled(true);
			map.getUiSettings().setMyLocationButtonEnabled(false);
			map.getUiSettings().setZoomControlsEnabled(false);
			map.setOnInfoWindowClickListener(this);
			map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

				@Override
				public void onMyLocationChange(Location location) {
					CameraPosition position = new CameraPosition.Builder()
							.target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15).build();
					CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
					map.animateCamera(update);
					map.setOnMyLocationChangeListener(null);
				}
			});
			map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

				@Override
				public void onCameraChange(CameraPosition position) {
					latitude = position.target.latitude;
					longitude = position.target.longitude;
					visibleRadius = GeoUtils.getVisibleRadius(map);
					timestamp = System.currentTimeMillis();
				}
			});

			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
		
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
			new Timer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] {});
			new Searcher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] {});
		} else {
			new Timer().execute();
			new Searcher().execute();
		}

		return view;
	}

	private void adicionarMarker(ItemInventario item) {
		marcadores.put(
				map.addMarker(new MarkerOptions().position(new LatLng(item.getLatitude(), item.getLongitude()))
						.icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getScaled(getActivity(), item.getCategoria().getMarcador())))
						.title(item.getCategoria().getNome())), item);
	}

	private void adicionarMarker(ItemRelato item) {
		marcadores.put(
				map.addMarker(new MarkerOptions().position(new LatLng(item.getLatitude(), item.getLongitude()))
						.icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getScaled(getActivity(), item.getCategoria().getMarcador())))
						.title(item.getCategoria().getNome())), item);
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		Intent intent = null;

		Object marcador = marcadores.get(marker);
		if (marcador instanceof ItemInventario) {
			intent = new Intent(getActivity(), DetalheMapaActivity.class);
			intent.putExtra("item", (ItemInventario) marcador);
		} else if (marcador instanceof ItemRelato) {
			ItemRelato ir = (ItemRelato) marcador;
			SolicitacaoListItem item = new SolicitacaoListItem();
			item.setTitulo(marker.getTitle());
			item.setComentario(ir.getDescricao());
			item.setData(ir.getData());
			item.setEndereco(ir.getEndereco());
			item.setFotos(ir.getFotos());
			item.setProtocolo(ir.getProtocolo());
			item.setStatus(new SolicitacaoListItem.Status());
			for (CategoriaRelato.Status status : ir.getCategoria().getStatus()) {
				if (status.getId() == ir.getIdStatus()) {
					item.setStatus(new SolicitacaoListItem.Status(status.getNome(), status.getCor()));
				}
			}
			intent = new Intent(getActivity(), SolicitacaoDetalheActivity.class);
			intent.putExtra("solicitacao", item);
		}

		startActivity(intent);
	}

	public void aplicarFiltro(BuscaExplore busca) {
		map.clear();
		this.busca = busca;
		requests.add(new Request(latitude, longitude, visibleRadius));
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		try {
			String str = (String) adapterView.getItemAtPosition(position);
			Address addr = new Geocoder(getActivity()).getFromLocationName(str, 1).get(0);

			if (pontoBusca != null) {
				pontoBusca.remove();
			}

			pontoBusca = map.addMarker(new MarkerOptions().position(new LatLng(addr.getLatitude(), addr.getLongitude())).icon(
					BitmapDescriptorFactory.defaultMarker()));

			CameraPosition p = new CameraPosition.Builder().target(new LatLng(addr.getLatitude(), addr.getLongitude())).zoom(15).build();
			CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
			map.animateCamera(update);
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage());
		}
	}

	public class Searcher extends AsyncTask<Void, Void, Void> {

		private List<ItemInventario> itensInventario = new CopyOnWriteArrayList<ItemInventario>();
		private List<ItemRelato> itensRelato = new CopyOnWriteArrayList<ItemRelato>();
		private AtomicBoolean next = new AtomicBoolean(true);

		@Override
		protected Void doInBackground(Void... params) {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet get;
				HttpResponse response;

				while (running) {
					while (!requests.isEmpty()) {
						Request request = requests.poll();
						next.set(false);
						Log.i("ZUP", "Request sendo tratada: " + new Date());
						
						for (Long id : busca.getIdsCategoriaInventario()) {
							get = new HttpGet(Constantes.REST_URL + "/inventory/items?position[latitude]=" + request.getLatitude() + "&position[longitude]="
									+ request.getLongitude() + "&position[distance]=" + request.getRadius() + "&max_items=100&inventory_category_id=" + id);
							get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));
							response = client.execute(get);
							if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
								extrairItensInventario(EntityUtils.toString(response.getEntity(), "UTF-8"));
							}
						}

						for (Long id : busca.getIdsCategoriaRelato()) {
							String query = Constantes.REST_URL + "/reports/items?position[latitude]=" + latitude + "&position[longitude]="
									+ longitude + "&position[distance]=" + visibleRadius + "&max_items=100&category_id=" + id + "&begin_date="
									+ busca.getPeriodo().getDateString();
							if (busca.getStatus() != null) {
								query += "&statuses=" + busca.getStatus().getId();
							}
							get = new HttpGet(query);
							get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));
							response = client.execute(get);
							if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
								extrairItensRelato(EntityUtils.toString(response.getEntity(), "UTF-8"));
							}
						}
						
						publishProgress();
						while (!next.get());			
					}
				}
			} catch (Exception e) {
				Log.e("ZUP", e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			for (ItemInventario item : itensInventario) {
				if (!marcadores.containsKey(item)) {
					adicionarMarker(item);
				}
			}
			itensInventario.clear();

			for (ItemRelato item : itensRelato) {
				if (!marcadores.containsKey(item)) {
					adicionarMarker(item);
				}
			}
			itensRelato.clear();
			next.set(true);
		}

		private void extrairItensInventario(String raw) throws JSONException {
			CategoriaInventarioService service = new CategoriaInventarioService();
			JSONArray array = new JSONObject(raw).getJSONArray("items");
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.getJSONObject(i);
				ItemInventario item = new ItemInventario();
				item.setCategoria(service.getById(getActivity(), json.getLong("inventory_category_id")));
				item.setId(json.getLong("id"));
				item.setLatitude(json.getJSONObject("position").getDouble("latitude"));
				item.setLongitude(json.getJSONObject("position").getDouble("longitude"));
				itensInventario.add(item);
			}
		}

		private void extrairItensRelato(String raw) throws Exception {
			CategoriaRelatoService service = new CategoriaRelatoService();
			JSONArray array = new JSONObject(raw).getJSONArray("reports");
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.getJSONObject(i);
				ItemRelato item = new ItemRelato();
				item.setId(json.getLong("id"));
				item.setDescricao(json.getString("description"));
				item.setProtocolo(json.getString("protocol"));
				item.setEndereco(json.getString("address"));
				item.setData(DateUtils.getIntervaloTempo(DateUtils.parseRFC3339Date(json.getString("created_at"))));
				item.setCategoria(service.getById(getActivity(), json.getLong("category_id")));
				item.setLatitude(json.getJSONObject("position").getDouble("latitude"));
				item.setLongitude(json.getJSONObject("position").getDouble("longitude"));
				item.setIdItemInventario(json.optLong("inventory_item_id", -1));
				item.setIdStatus(json.optLong("status_id", -1));

				JSONArray fotos = json.getJSONArray("images");
				for (int j = 0; j < fotos.length(); j++) {
					String url = fotos.getJSONObject(j).getString("url");
					FileUtils.downloadImage(url);
					String[] parts = url.split("/");
					item.getFotos().add(parts[parts.length - 1]);
				}

				itensRelato.add(item);
			}
		}
	}

	public void refresh() {
		if (map != null) {
			requests.add(new Request(latitude, longitude, visibleRadius));
		}
	}

	private class Timer extends AsyncTask<Void, String, Void> {

		private Request lastRequest = null;
		
		@Override
		protected Void doInBackground(Void... ignore) {

			while (running) {
				try {
					Thread.sleep(SLEEP_TIMER);
					if (System.currentTimeMillis() - timestamp > TIMEOUT_TIMER) {
						Request request = new Request(latitude, longitude, visibleRadius);
						if (!request.equals(lastRequest)) {
							Log.i("ZUP", "Request adicionada: " + new Date());
							requests.add(request);
							lastRequest = request;
						}
					}
				} catch (Exception e) {
					Log.w("ZUP", e.getMessage());
				}
			}
			return null;
		}
	}

	@Override
	public void onDestroy() {
		running = false;
		PreferenceUtils.salvarBusca(getActivity(), busca);
		super.onDestroy();
	}

	private class Request {
		private double latitude, longitude;
		private long radius;
		
		public Request(double latitude, double longitude, long radius) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.radius = radius;
		}

		public double getLatitude() {
			return latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public long getRadius() {
			return radius;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			long temp;
			temp = Double.doubleToLongBits(latitude);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(longitude);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + (int) (radius ^ (radius >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Request other = (Request) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
				return false;
			if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
				return false;
			if (radius != other.radius)
				return false;
			return true;
		}

		private ExploreFragment getOuterType() {
			return ExploreFragment.this;
		}
	}
}
