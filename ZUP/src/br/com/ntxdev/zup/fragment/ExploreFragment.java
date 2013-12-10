package br.com.ntxdev.zup.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import br.com.ntxdev.zup.FiltroExploreActivity;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.util.FontUtils;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ExploreFragment extends Fragment implements android.location.LocationListener {

	private static final String TAG = "ExploreFragment";
	private static int FILTRO_CODE = 1507;
	private static View view;
	private SupportMapFragment mapFragment;
	private GoogleMap map;
	private double latitudeAtual = -23.536726;
	private double longitudeAtual = -46.639841;

	private TextView botaoFiltrar;

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
			Log.w(TAG, e.getMessage());
		}

		mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
		map = mapFragment.getMap();
		if (map != null) {
			map.getUiSettings().setZoomControlsEnabled(false);
		}

		botaoFiltrar = (TextView) view.findViewById(R.id.botaoFiltrar);
		botaoFiltrar.setTypeface(FontUtils.getRegular(getActivity()));
		botaoFiltrar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getActivity(), FiltroExploreActivity.class), FILTRO_CODE);
			}
		});

		if (map != null) {
			Location location = getLocalizacao();
			if(location != null){
				latitudeAtual = location.getLatitude();
				longitudeAtual = location.getLongitude();
			}

			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			LatLng latLng = new LatLng(latitudeAtual, longitudeAtual);

			map.addMarker(new MarkerOptions().anchor(0.0f, 1.0f) // Anchors the
																	// marker on
																	// the
																	// bottom
																	// left
					.position(new LatLng(latitudeAtual, longitudeAtual)));

			final CameraPosition position = new CameraPosition.Builder().target(latLng).zoom(17).build();
			CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
			map.animateCamera(update);

		}

		return view;
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

	@Override
	public void onLocationChanged(Location location) {
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
}
