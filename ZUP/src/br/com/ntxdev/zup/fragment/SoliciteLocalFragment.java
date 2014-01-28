package br.com.ntxdev.zup.fragment;

import java.util.List;

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
import android.widget.ImageView;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import br.com.ntxdev.zup.util.ImageUtils;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class SoliciteLocalFragment extends Fragment {

	private static final String TAG = "ExploreFragment";
	private static View view;
	private SupportMapFragment mapFragment;
	private GoogleMap map;
	
	private double latitude, longitude;
	private String file;

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
			map.getUiSettings().setZoomControlsEnabled(false);
		}

		if (map != null) {
			map.setMyLocationEnabled(true);
			map.getUiSettings().setMyLocationButtonEnabled(false);
			map.getUiSettings().setZoomControlsEnabled(false);
			map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
				
				@Override
				public void onMyLocationChange(Location location) {
					CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(),
							location.getLongitude())).zoom(15).build();
					CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
					map.moveCamera(update);
					map.setOnMyLocationChangeListener(null);
					latitude = location.getLatitude();
					longitude = location.getLongitude();
				}
			});
			map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
				
				@Override
				public void onCameraChange(CameraPosition cameraPosition) {
					latitude = cameraPosition.target.latitude;
					longitude = cameraPosition.target.longitude;
				}
			});
			
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}

		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		((ImageView) getView().findViewById(R.id.marcador)).setImageBitmap(ImageUtils.getScaled(getActivity(), file));
	}
	
	public double getLatitudeAtual() {
		return latitude;
	}
	
	public double getLongitudeAtual() {
		return longitude;
	}
	
	public void setMarcador(String file) {
		this.file = file;		
	}
	
	public String getEnderecoAtual() {
		Geocoder geocoder = new Geocoder(getActivity());
		try {
			List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
			if (!addresses.isEmpty()) {
				return addresses.get(0).getAddressLine(0);
			}
		} catch (Exception e) {
			Log.e("ZUP", e.getMessage(), e);
		}
		return "";
	}
}
