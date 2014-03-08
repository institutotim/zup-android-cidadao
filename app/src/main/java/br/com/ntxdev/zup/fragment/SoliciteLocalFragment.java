package br.com.ntxdev.zup.fragment;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import br.com.ntxdev.zup.domain.Place;
import br.com.ntxdev.zup.domain.Solicitacao;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.GeoUtils;
import br.com.ntxdev.zup.util.ImageUtils;
import br.com.ntxdev.zup.util.ViewUtils;
import br.com.ntxdev.zup.widget.PlacesAutoCompleteAdapter;

public class SoliciteLocalFragment extends Fragment implements AdapterView.OnItemClickListener,
        GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    // Local inicial: São Paulo
    private static final double INITIAL_LATITUDE = -23.5501283;
    private static final double INITIAL_LONGITUDE = -46.6338553;

    private GoogleMap map;
    private static View view;
    public static double latitude, longitude;
    private String file;
    private String endereco = "";
    private TimerEndereco task;
    private AutoCompleteTextView autoCompView;

    private LocationClient mLocationClient;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    @SuppressLint("NewApi")
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
            Log.w("ZUP", e.getMessage());
        }

        map = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapaLocal)).getMap();
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
                    INITIAL_LONGITUDE)).zoom(12).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
            map.moveCamera(update);
        }

        autoCompView = (AutoCompleteTextView) view.findViewById(R.id.autocomplete);
        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item, SoliciteLocalFragment.class));
        autoCompView.setTypeface(FontUtils.getRegular(getActivity()));
        autoCompView.setOnItemClickListener(this);
        autoCompView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    realizarBuscaAutocomplete(v.getText().toString());
                    ViewUtils.hideKeyboard(getActivity(), v);
                    handled = true;
                }
                return handled;
            }
        });
        view.findViewById(R.id.clean).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCompView.setText("");
            }
        });

        task = new TimerEndereco();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (map != null) {
            if (getArguments() != null) {
                Solicitacao solicitacao = (Solicitacao) getArguments().getSerializable("solicitacao");
                if (solicitacao != null) {
                    file = solicitacao.getCategoria().getMarcador();
                    CameraPosition p = new CameraPosition.Builder().target(new LatLng(solicitacao.getLatitude(),
                            solicitacao.getLongitude())).zoom(15).build();
                    CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
                    map.moveCamera(update);
                }
            }
        }

        if (file != null && !file.isEmpty()) {
            ((ImageView) view.findViewById(R.id.marcador)).setImageBitmap(ImageUtils.getScaled(getActivity(), file));
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((SoliciteActivity) getActivity()).setInfo(R.string.selecione_o_local);
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
        realizarBuscaAutocomplete((Place) adapterView.getItemAtPosition(position));
        ViewUtils.hideKeyboard(getActivity(), autoCompView);
    }

    private void realizarBuscaAutocomplete(Place place) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            new GeocoderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, place);
        } else {
            new GeocoderTask().execute(place);
        }
    }

    private void realizarBuscaAutocomplete(String query) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            new SearchTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
        } else {
            new SearchTask().execute(query);
        }
    }

    private void atualizarEndereco() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            new AddressTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new AddressTask().execute();
        }
    }

    private String getEndereco() {
        return endereco;
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
                    Log.e("ZUP", e.getMessage(), e);
                }

                if (lat != latitude && lon != longitude) {
                    atualizarEndereco();
                    lat = latitude;
                    lon = longitude;
                    publishProgress(getEndereco());
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            endereco = values[0];
            autoCompView.setText(values[0]);
            //autoCompView.setAdapter(null);
            autoCompView.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item, SoliciteLocalFragment.class));
        }
    }

    private class GeocoderTask extends AsyncTask<Place, Void, Address> {

        @Override
        protected Address doInBackground(Place... params) {
            try {
                return GeoUtils.getFromPlace(params[0]);
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Address addr) {
            if (addr != null) {
                CameraPosition p = new CameraPosition.Builder().target(new LatLng(addr.getLatitude(),
                        addr.getLongitude())).zoom(15).build();
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
                map.animateCamera(update);
            }
        }
    }

    private class AddressTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                return GeoUtils.getFromLocation(latitude, longitude, 1).get(0).getAddressLine(0);
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String addr) {
            if (addr != null) {
                endereco = addr;
                autoCompView.setText(endereco);
                //autoCompView.setAdapter(null);
                if (getActivity() != null) {
                    autoCompView.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item, SoliciteLocalFragment.class));
                }
            }
        }
    }

    private class SearchTask extends AsyncTask<String, Void, Address> {

        @Override
        protected Address doInBackground(String... params) {
            try {
                return GeoUtils.search(params[0], latitude, longitude);
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Address addr) {
            if (addr != null) {
                CameraPosition p = new CameraPosition.Builder().target(new LatLng(addr.getLatitude(),
                        addr.getLongitude())).zoom(15).build();
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
                map.animateCamera(update);
            }
        }
    }

    private boolean wasLocalized = false;

    @Override
    public void onConnected(Bundle bundle) {
        if (!wasLocalized) {
            mLocationClient.requestLocationUpdates(REQUEST, this);
        }
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(),
                location.getLongitude())).zoom(15).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        map.moveCamera(update);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        atualizarEndereco();
        wasLocalized = true;
        mLocationClient.removeLocationUpdates(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpLocationClientIfNeeded();
        mLocationClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
    }

    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(
                    getActivity(),
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }
}
