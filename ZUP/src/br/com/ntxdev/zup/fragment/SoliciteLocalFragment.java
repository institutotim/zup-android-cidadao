package br.com.ntxdev.zup.fragment;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class SoliciteLocalFragment extends Fragment implements LocationListener {

	private static final String TAG = "ExploreFragment";
	private static View view;
	private SupportMapFragment mapFragment;
	private GoogleMap map;
	//protected CustomAndroidLocationSource locationSource;
	
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
		
		mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapaLocal);
		map = mapFragment.getMap();
		
		if (map != null) {
			LatLng latLng = new LatLng(-23.5505233, -46.6342982);
			
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
}
