package br.com.ntxdev.zup.fragment;

import java.util.List;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;

import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import br.com.ntxdev.zup.domain.Solicitacao;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.ImageUtils;
import br.com.ntxdev.zup.widget.AutoCompleteAdapter;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class SoliciteLocalFragment extends Fragment implements AdapterView.OnItemClickListener, GoogleMap.OnMyLocationChangeListener {

    // Local inicial: São Paulo
    private static final double INITIAL_LATITUDE = -23.6824124;
    private static final double INITIAL_LONGITUDE = -46.5952992;

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private static View view;
    private double latitude, longitude;
    private String file;
    private String endereco = "";
    private TimerEndereco task;
    private AutoCompleteTextView autoCompView;

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("ZUP", "onCreateView called");
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
            Log.w("ZUP", e.getMessage());
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

            map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    latitude = cameraPosition.target.latitude;
                    longitude = cameraPosition.target.longitude;
                }
            });

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            CameraPosition p = new CameraPosition.Builder().target(new LatLng(INITIAL_LATITUDE,
                    INITIAL_LONGITUDE)).zoom(15).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
            map.moveCamera(update);
        }

        autoCompView = (AutoCompleteTextView) view.findViewById(R.id.autocomplete);
        autoCompView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item));
        autoCompView.setTypeface(FontUtils.getRegular(getActivity()));
        autoCompView.setOnItemClickListener(this);

        task = new TimerEndereco();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[]{});
        } else {
            task.execute();
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            Log.i("ZUP", "arguments is not null");
            Solicitacao solicitacao = (Solicitacao) getArguments().getSerializable("solicitacao");
            if (solicitacao != null) {
                file = solicitacao.getCategoria().getMarcador();
                CameraPosition p = new CameraPosition.Builder().target(new LatLng(solicitacao.getLatitude(),
                        solicitacao.getLongitude())).zoom(15).build();
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
                map.moveCamera(update);
            }
        } else {
            Log.i("ZUP", "arguments is null");
            map.setOnMyLocationChangeListener(this);
        }

        if (file != null && !file.isEmpty()) {
            ((ImageView) view.findViewById(R.id.marcador)).setImageBitmap(ImageUtils.getScaled(getActivity(), file));
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (file != null && !file.isEmpty()) {
                ((ImageView) view.findViewById(R.id.marcador)).setImageBitmap(ImageUtils.getScaled(getActivity(), file));
            }
        }
    }

    @Override
    public void onDestroy() {
        task.cancel(true);
        super.onDestroy();
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
        return endereco;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        try {
            String str = (String) adapterView.getItemAtPosition(position);
            Address addr = new Geocoder(getActivity()).getFromLocationName(str, 1).get(0);

            CameraPosition p = new CameraPosition.Builder().target(new LatLng(addr.getLatitude(),
                    addr.getLongitude())).zoom(15).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
            map.animateCamera(update);
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage());
        }
    }

    private void atualizarEndereco() {
        try {
            Geocoder geocoder = new Geocoder(getActivity());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                endereco = addresses.get(0).getAddressLine(0);
                autoCompView.setText(endereco);
            }
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
        }
    }

    private String getEndereco() {
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

    @Override
    public void onMyLocationChange(Location location) {
        CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(),
                location.getLongitude())).zoom(15).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        map.moveCamera(update);
        map.setOnMyLocationChangeListener(null);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        atualizarEndereco();
    }

    private class TimerEndereco extends AsyncTask<Void, String, Void> {

        private double lat, lon;
        private boolean run = true;

        public TimerEndereco() {
            lat = latitude;
            lon = longitude;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            while (run) {
                try {
                    Thread.sleep(250);
                } catch (Exception e) {
                    Log.e("ZUP", e.getMessage());
                }

                if (lat != latitude && lon != longitude) {
                    lat = latitude;
                    lon = longitude;
                    publishProgress(getEndereco());
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            autoCompView.setText(values[0]);
        }
    }
}
