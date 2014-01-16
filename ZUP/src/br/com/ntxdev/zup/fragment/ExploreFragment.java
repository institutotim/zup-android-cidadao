package br.com.ntxdev.zup.fragment;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SolicitacaoDetalheActivity;
import br.com.ntxdev.zup.domain.BuscaExplore;
import br.com.ntxdev.zup.domain.SolicitacaoListItem;
import br.com.ntxdev.zup.util.FontUtils;
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

	private static final String TAG = "ExploreFragment";
	private static int FILTRO_CODE = 1507;
	private static View view;
	private SupportMapFragment mapFragment;
	private GoogleMap map;
	
	private TextView botaoFiltrar;
	private Marker pontoBocaLobo;
	private Marker relatoEntulho;
	private BuscaExplore busca;
	
	private Marker pontoBusca;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null) {
				parent.removeView(view);
			}
		}

		try {
			view = inflater.inflate(R.layout.fragment_explore, container, false);
		} catch (InflateException e) {
			Log.w(TAG, e.getMessage());
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
				startActivityForResult(intent, FILTRO_CODE);
			}
		});

		busca = new BuscaExplore();
		busca.setColetaDeEntulho(true);
		busca.setLimpezaBocaDeLobo(true);

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
				}
			});
			
			addMarkers(busca);			
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
		
		AutoCompleteTextView autoCompView = (AutoCompleteTextView) view.findViewById(R.id.autocomplete);
        autoCompView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item));
        autoCompView.setTypeface(FontUtils.getRegular(getActivity()));
        autoCompView.setOnItemClickListener(this);

		return view;
	}

	private void addMarkers(BuscaExplore busca) {
		if (busca.isExibirBocasLobo())		
			pontoBocaLobo = map.addMarker(new MarkerOptions()
					.position(new LatLng(-23.551102912885938, -46.635670783080855))
					.title("Boca de lobo 4758268")
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ponto_bocalobo)));
		if (busca.isLimpezaBocaDeLobo())
			map.addMarker(new MarkerOptions()
					.position(new LatLng(-23.551102912885938, -46.635670783080855))
					.title("Boca de lobo 4758268")
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_boca_lobo)));
		if (busca.isColetaDeEntulho())
			relatoEntulho = map.addMarker(new MarkerOptions()
					.position(new LatLng(-23.549126029339284, -46.63785946563701))
					.title("Coleta de entulho")
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_entulho)));	
		if (busca.isExibirFlorestaUrbana())
			map.addMarker(new MarkerOptions()
					.position(new LatLng(-23.5512602754729, -46.63378250793437))
					.title("Árvore 07828-00034-r-1-01")
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ponto_floresta_urbana)));
		if (busca.isExibirPracasWifi())
			map.addMarker(new MarkerOptions()
					.position(new LatLng(-23.552843726018978, -46.63550985053996))
					.title("Praça Dom José Gaspar")
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ponto_praca_wifi)));		
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		Intent intent = null;		
		
		if (marker.equals(relatoEntulho)) {
			SolicitacaoListItem item = new SolicitacaoListItem();
			item.setTitulo(marker.getTitle());
			item.setComentario("Apesar da placa, o pessoal vive enchendo a calçada de entulho, cansei de ter que desviar pela rua. Pior quando chove e esse entulho começa a se espalhar.");
			item.setData("há 4 dias");
			item.setEndereco("Rua Hermílio Lemos, 498, Cambuci, São Paulo");
			item.setFotos(Arrays.asList(R.drawable.entulho1, R.drawable.entulho2));
			item.setProtocolo("1844356633");
			item.setStatus(SolicitacaoListItem.Status.EM_ANDAMENTO);
			intent = new Intent(getActivity(), SolicitacaoDetalheActivity.class);
			intent.putExtra("solicitacao", item);
		} else {
			intent = new Intent(getActivity(), DetalheMapaActivity.class);
			intent.putExtra("title", marker.getTitle());
		}
		
		if (marker.equals(pontoBocaLobo)) {
			intent.putExtra("info_page", true);
		}
		
		startActivity(intent);		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FILTRO_CODE && resultCode == Activity.RESULT_OK) {
			map.clear();
			LatLng latLng = new LatLng(-23.55056197755646, -46.633128048934736);
			map.addMarker(new MarkerOptions().anchor(0.0f, 1.0f).position(latLng));
			busca = (BuscaExplore) data.getSerializableExtra("busca");
			addMarkers(busca);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		try {
			String str = (String) adapterView.getItemAtPosition(position);
			Address addr = new Geocoder(getActivity()).getFromLocationName(str, 1).get(0);
			
			map.setOnMyLocationChangeListener(null);
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
}
