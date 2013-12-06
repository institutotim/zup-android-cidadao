package br.com.ntxdev.zup.fragment;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import br.com.ntxdev.zup.domain.Solicitacao;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SoliciteLocalFragment extends Fragment implements LocationListener {

	private static final String TAG = "ExploreFragment";
	private static View view;
	private SupportMapFragment mapFragment;
	private GoogleMap map;
	
	private double latitudeAtual = -23.536726;
	private double longitudeAtual = -46.639841;
	private HashMap<String, String> enderecoAtual;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}

		((SoliciteActivity) getActivity()).exibirBarraInferior(true);
		((SoliciteActivity) getActivity()).setInfo(R.string.selecione_o_local);

		try {
			view = inflater.inflate(R.layout.fragment_solicite_local, container, false);
		} catch (InflateException e) {
			Log.w(TAG, e.getMessage());
		}

		enderecoAtual = new HashMap<String, String>();

		mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapaLocal);
		map = mapFragment.getMap();		

		if (map != null) {
			Location location = getLocalizacao();
			if(location != null){
				latitudeAtual = location.getLatitude();
				longitudeAtual = location.getLongitude();
			}
			atualizaEndereco();

			map.clear();
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			LatLng latLng = new LatLng(latitudeAtual, longitudeAtual);
			
			map.addMarker(new MarkerOptions().anchor(0.0f, 1.0f)
					// Anchors the marker on the bottom left
					.position(new LatLng(latitudeAtual, longitudeAtual))
					.icon(BitmapDescriptorFactory.fromResource(((SoliciteActivity) getActivity()).getTipo().equals(Solicitacao.Tipo.BOCA_LOBO) ? R.drawable.map_pin_boca_lobo : R.drawable.map_pin_entulho))
					.draggable(true));

			map.setOnMarkerDragListener(new OnMarkerDragListener() {
				@Override
				public void onMarkerDrag(Marker arg0) {
					Log.i("System out", "onMarkerDrag...");
				}

				@Override
				public void onMarkerDragEnd(Marker arg0) {
					latitudeAtual = arg0.getPosition().latitude;
					longitudeAtual = arg0.getPosition().longitude;
					atualizaEndereco();
					Log.d("System out", "onMarkerDragEnd..." + arg0.getPosition().latitude + "..." + arg0.getPosition().longitude);
					map.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
				}

				@Override
				public void onMarkerDragStart(Marker arg0) {
					Log.d("System out", "onMarkerDragStart..." + arg0.getPosition().latitude + "..." + arg0.getPosition().longitude);
				}
			});

			final CameraPosition position = new CameraPosition.Builder().target(latLng).zoom(17).build();
			CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
			map.animateCamera(update);
		}

		return view;
	}

	@Override
	public void onLocationChanged(Location location) {
		LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public Location getLocalizacao() {
		LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 100, this);
		} else {
			// Solicita ao usu‡rio para ligar o GPS
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			alertDialogBuilder.setMessage(R.string.gps_off).setCancelable(false)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(callGPSSettingIntent);
						}
					});
			alertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog alert = alertDialogBuilder.create();
			alert.show();
		}
		return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}

	public void atualizaEndereco() {
		Geocoder geocoder = new Geocoder(getActivity().getApplicationContext());
		List<Address> addresses;
		try {
			addresses = geocoder.getFromLocation(latitudeAtual, longitudeAtual, 1);
			enderecoAtual.put("endereco", addresses.get(0).getAddressLine(0));
			enderecoAtual.put("cidade", addresses.get(0).getAddressLine(1));
			enderecoAtual.put("cep", addresses.get(0).getAddressLine(2));
			enderecoAtual.put("pais", addresses.get(0).getAddressLine(3));
			enderecoAtual.put("siglaPais", addresses.get(0).getCountryCode());
			enderecoAtual.put("estado", addresses.get(0).getAdminArea());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
