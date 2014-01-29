package br.com.ntxdev.zup.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
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
import android.widget.Toast;
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
import br.com.ntxdev.zup.util.ImageUtils;
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

public class ExploreFragment extends Fragment implements OnInfoWindowClickListener, AdapterView.OnItemClickListener {

	private SupportMapFragment mapFragment;
	private GoogleMap map;
	private static View view;
	private double latitude, longitude;
	
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

		mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
		map = mapFragment.getMap();

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

		busca = new BuscaExplore();

		if (map != null) {
			map.setMyLocationEnabled(true);
			map.getUiSettings().setMyLocationButtonEnabled(false);
			map.getUiSettings().setZoomControlsEnabled(false);
			map.setOnInfoWindowClickListener(this);
			map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
				
				@Override
				public void onMyLocationChange(Location location) {
					CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(),
							location.getLongitude())).zoom(15).build();
					CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
					map.animateCamera(update);
					map.setOnMyLocationChangeListener(null);
					latitude = location.getLatitude();
					longitude = location.getLongitude();
					new Tasker(location.getLatitude(), location.getLongitude()).execute();
				}
			});
					
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
		
		AutoCompleteTextView autoCompView = (AutoCompleteTextView) view.findViewById(R.id.autocomplete);
        autoCompView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item));
        autoCompView.setTypeface(FontUtils.getRegular(getActivity()));
        autoCompView.setOnItemClickListener(this);

		return view;
	}
	
	private void adicionarMarker(ItemInventario item) {
		marcadores.put(map.addMarker(new MarkerOptions()
				.position(new LatLng(item.getLatitude(), item.getLongitude()))
				.icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getScaled(getActivity(), item.getCategoria().getMarcador())))
				.title(item.getCategoria().getNome())), item);
	}
	
	private void adicionarMarker(ItemRelato item) {
		marcadores.put(map.addMarker(new MarkerOptions()
				.position(new LatLng(item.getLatitude(), item.getLongitude()))
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
		new Searcher(busca.getIdsCategoriaRelato(), busca.getIdsCategoriaInventario()).execute();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		try {
			String str = (String) adapterView.getItemAtPosition(position);
			Address addr = new Geocoder(getActivity()).getFromLocationName(str, 1).get(0);
			
			if (pontoBusca != null) {
				pontoBusca.remove();
			}
			
			pontoBusca = map.addMarker(new MarkerOptions()
				.position(new LatLng(addr.getLatitude(), addr.getLongitude()))
				.icon(BitmapDescriptorFactory.defaultMarker()));
			
			CameraPosition p = new CameraPosition.Builder().target(new LatLng(addr.getLatitude(),
					addr.getLongitude())).zoom(15).build();
			CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
			map.animateCamera(update);
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage());
		}
	}
	
	public class Searcher extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog dialog;
		private List<Long> relatos;
		private List<Long> inventarios;
		private List<ItemInventario> itensInventario = new ArrayList<ItemInventario>();
		private List<ItemRelato> itensRelato = new ArrayList<ItemRelato>();

		public Searcher(List<Long> relatos, List<Long> inventarios) {
			this.relatos = relatos;
			this.inventarios = inventarios;			
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
		protected Boolean doInBackground(Void... params) {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet get;
				HttpResponse response;
				
				for (Long id : inventarios) {
					get = new HttpGet(Constantes.REST_URL + "/inventory/items?position[latitude]=" + 
							latitude + "&position[longitude]=" + longitude + "&position[distance]=15000&max_items=100&inventory_category_id=" + id);
					get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));
					response = client.execute(get);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						extrairItensInventario(EntityUtils.toString(response.getEntity(), "UTF-8"));					
					}
				}
				
				for (Long id : relatos) {
					get = new HttpGet(Constantes.REST_URL + "/reports/items?position[latitude]=" + 
							latitude + "&position[longitude]=" + longitude + "&position[distance]=15000&max_items=100&category_id=" + id);
					get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));
					response = client.execute(get);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						extrairItensRelato(EntityUtils.toString(response.getEntity(), "UTF-8"));					
					}
				}
				return Boolean.TRUE;
			} catch (Exception e) {
				Log.e("ZUP", e.getMessage());
			}
			return Boolean.FALSE;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {			
			if (result) {
				try {
					for (ItemInventario item : itensInventario) {
						adicionarMarker(item);
					}
					
					for (ItemRelato item : itensRelato) {
						adicionarMarker(item);
					}
				} catch (Exception e) {
					Log.e("ZUP", e.getMessage());
					Toast.makeText(getActivity(), "Não foi possível obter sua lista de relatos", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getActivity(), "Não foi possível obter sua lista de relatos", Toast.LENGTH_LONG).show();
			}
			dialog.dismiss();
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
	
	public class Tasker extends AsyncTask<Void, Void, Boolean> {
		
		private ProgressDialog dialog;
		private List<ItemInventario> itensInventario = new ArrayList<ItemInventario>();
		private List<ItemRelato> itensRelato = new ArrayList<ItemRelato>();
		private double latitude, longitude;
		
		public Tasker(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
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
		protected Boolean doInBackground(Void... params) {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(Constantes.REST_URL + "/inventory/items?position[latitude]=" + 
						latitude + "&position[longitude]=" + longitude + "&position[distance]=15000&max_items=100");
				get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));
				HttpResponse response = client.execute(get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					extrairItensInventario(EntityUtils.toString(response.getEntity(), "UTF-8"));					
				}
				
				get = new HttpGet(Constantes.REST_URL + "/reports/items?position[latitude]=" + 
						latitude + "&position[longitude]=" + longitude + "&position[distance]=15000&max_items=100");
				get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));
				response = client.execute(get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					extrairItensRelato(EntityUtils.toString(response.getEntity(), "UTF-8"));					
				}
				return Boolean.TRUE;
			} catch (Exception e) {
				Log.e("ZUP", e.getMessage());
			}
			return Boolean.FALSE;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {			
			if (result) {
				try {
					for (ItemInventario item : itensInventario) {
						adicionarMarker(item);
					}
					
					for (ItemRelato item : itensRelato) {
						adicionarMarker(item);
					}
				} catch (Exception e) {
					Log.e("ZUP", e.getMessage());
					Toast.makeText(getActivity(), "Não foi possível obter sua lista de relatos", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getActivity(), "Não foi possível obter sua lista de relatos", Toast.LENGTH_LONG).show();
			}
			dialog.dismiss();
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
			map.clear();
			if (busca.getIdsCategoriaInventario().isEmpty() && busca.getIdsCategoriaRelato().isEmpty()) {
				new Tasker(latitude, longitude).execute();
			} else {
				new Searcher(busca.getIdsCategoriaRelato(), busca.getIdsCategoriaInventario()).execute();
			}
		}
	}
}
