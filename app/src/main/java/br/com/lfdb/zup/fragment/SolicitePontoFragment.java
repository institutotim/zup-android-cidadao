package br.com.lfdb.zup.fragment;

import android.location.Address;
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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.okhttp.apache.OkApacheClient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import br.com.lfdb.zup.R;
import br.com.lfdb.zup.SoliciteActivity;
import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.domain.CategoriaInventario;
import br.com.lfdb.zup.domain.CategoriaRelato;
import br.com.lfdb.zup.domain.ItemInventario;
import br.com.lfdb.zup.domain.Place;
import br.com.lfdb.zup.service.CategoriaInventarioService;
import br.com.lfdb.zup.service.LoginService;
import br.com.lfdb.zup.util.BitmapUtils;
import br.com.lfdb.zup.util.FontUtils;
import br.com.lfdb.zup.util.GeoUtils;
import br.com.lfdb.zup.util.ImageUtils;
import br.com.lfdb.zup.util.ViewUtils;
import br.com.lfdb.zup.widget.PlacesAutoCompleteAdapter;

public class SolicitePontoFragment extends Fragment implements GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMarkerClickListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener, AdapterView.OnItemClickListener {

    // Local inicial: São Paulo
    private static final double INITIAL_LATITUDE = -23.5501283;
    private static final double INITIAL_LONGITUDE = -46.6338553;

    private static final int MAX_ITEMS_PER_REQUEST = 30;

    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private GoogleMap map;
    private static View view;
    private AutoCompleteTextView autoCompView;
    private Timer timer;

    private LocationClient mLocationClient;
    private boolean wasLocalized = false;
    private Request request;
    private CategoriaRelato categoria;
    public static double latitude = 0.0, longitude = 0.0;
    private Long id = null;
    private String endereco = "";

    private double latitudePonto = 0.0, longitudePonto = 0.0;

    private Set<Object> itens = new HashSet<>();
    private Map<Marker, Object> marcadores = new HashMap<>();

    private long raio = 0l;

    private ProgressBar progressBar;

    private Marker marker;

    private MarkerRetriever markerRetriever = null;
    private AddressTask addressTask = null;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((SoliciteActivity) getActivity()).setInfo(R.string.toque_no_ponto_exato_solicitacao);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }

        ((SoliciteActivity) getActivity()).enableNextButton(false);
        ((SoliciteActivity) getActivity()).exibirBarraInferior(true);
        ((SoliciteActivity) getActivity()).setInfo(R.string.toque_no_ponto_exato_solicitacao);

        try {
            view = inflater.inflate(R.layout.fragment_solicite_ponto, container, false);
        } catch (InflateException e) {
            Log.w("ZUP", e.getMessage());
        }

        map = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapaPonto)).getMap();
        progressBar = (ProgressBar) view.findViewById(R.id.loading);

        if (map != null) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.setOnCameraChangeListener(this);
            map.setOnMarkerClickListener(this);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            CameraPosition p = new CameraPosition.Builder().target(new LatLng(INITIAL_LATITUDE,
                    INITIAL_LONGITUDE)).zoom(16).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
            map.moveCamera(update);
        }

        autoCompView = (AutoCompleteTextView) view.findViewById(R.id.autocomplete);
        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item, ExploreFragment.class));
        autoCompView.setTypeface(FontUtils.getRegular(getActivity()));
        autoCompView.setOnItemClickListener(this);
        autoCompView.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                realizarBuscaAutocomplete(v.getText().toString());
                ViewUtils.hideKeyboard(getActivity(), v);
                handled = true;
            }
            return handled;
        });
        view.findViewById(R.id.clean).setOnClickListener(v -> autoCompView.setText(""));

        return view;
    }

    public Long getCategoriaId() {
        return id;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpLocationClientIfNeeded();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        categoria = ((SoliciteActivity) getActivity()).getCategoria();

        timer = new Timer();
        timer.schedule(new Requester(), 0, 500);
    }

    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(
                    getActivity(),
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (!wasLocalized) {
            mLocationClient.requestLocationUpdates(REQUEST, this);
        }
    }

    @Override
    public void onDisconnected() {

    }

    private void adicionarMarker(ItemInventario item) {
        try {
            itens.add(item);
            marcadores.put(map.addMarker(new MarkerOptions()
                            .position(new LatLng(item.getLatitude(), item.getLongitude()))
                            .icon(BitmapDescriptorFactory.fromBitmap(BitmapUtils.getInventoryMarker(getActivity(), item.getCategoria().getMarcador())))
            ), item);
        } catch (Exception e) {
            Log.w("ZUP", e.getMessage() == null ? "null" : e.getMessage(), e);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(),
                location.getLongitude())).zoom(18.5f).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        map.animateCamera(update);
        mLocationClient.removeLocationUpdates(this);
        wasLocalized = true;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        latitude = cameraPosition.target.latitude;
        longitude = cameraPosition.target.longitude;
        raio = GeoUtils.getVisibleRadius(map);
        removerItensMapa();
        exibirElementos();
        refresh();
    }

    private void exibirElementos() {
        for (Object pontoMapa : itens) {
            if (pontoMapa instanceof ItemInventario) {
               adicionarMarker((ItemInventario) pontoMapa);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }

    private void removerItensMapa() {
        Iterator<Marker> it = marcadores.keySet().iterator();
        while (it.hasNext()) {
            Marker marker = it.next();
            if (!GeoUtils.isVisible(map.getProjection().getVisibleRegion(), marker.getPosition())) {
                marker.remove();
                it.remove();
                marcadores.remove(marker);
            }
        }
    }

    public void refresh() {
        Request request = new Request();
        request.latitude = latitude;
        request.longitude = longitude;
        request.raio = raio;
        this.request = request;
    }

    public double getLatitude() {
        return latitudePonto;
    }

    public double getLongitude() {
        return longitudePonto;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        try {
            ItemInventario item = (ItemInventario) marcadores.get(marker);
            id = item.getId();
            if (this.marker != null) {
                this.marker.setVisible(false);
            }

            this.marker = map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getScaled(getActivity(),
                            "reports", categoria.getMarcador())))
                    .position(new LatLng(item.getLatitude(), item.getLongitude())));
            latitudePonto = marker.getPosition().latitude;
            longitudePonto = marker.getPosition().longitude;
            atualizarEndereco(marker.getPosition());
            ((SoliciteActivity) getActivity()).enableNextButton(true);
            return true;
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
            return false;
        }
    }

    public void setCategoria(CategoriaRelato categoria) {
        this.categoria = categoria;
        if (map != null) {
            map.clear();
        }
    }

    public String getEndereco() {
        return endereco;
    }

    private class MarkerRetriever extends AsyncTask<Void, ItemInventario, Void> {

        private List<ItemInventario> itensInventario = new CopyOnWriteArrayList<>();

        private Request request;

        private HttpGet get;

        public MarkerRetriever(Request request) {
            this.request = request;
        }

        @Override
        protected void onPreExecute() {
            progressBar.post(() -> progressBar.setVisibility(View.VISIBLE));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.post(() -> progressBar.setVisibility(View.GONE));
        }

        @Override
        protected void onCancelled() {
            if (get != null) get.abort();
            progressBar.post(() -> progressBar.setVisibility(View.GONE));
            Log.d("ZUP", "Request cancelled");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i("ZUP", "Request started");
            try {
                HttpClient client = new OkApacheClient();

                HttpResponse response;

                for (CategoriaInventario c : categoria.getCategoriasInventario()) {
                    get = new HttpGet(Constantes.REST_URL + "/inventory/items?position[latitude]=" + request.latitude + "&position[longitude]="
                            + request.longitude + "&position[distance]=" + request.raio + "&max_items=" + MAX_ITEMS_PER_REQUEST + "&inventory_category_id=" + c.getId());
                    get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));

                    if (isCancelled()) return null;

                    response = client.execute(get);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        if (isCancelled()) return null;
                        extrairItensInventario(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    }
                }
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage() != null ? e.getMessage() : "null", e);
                return null;
            }

            for (ItemInventario item : itensInventario) {
                if (!marcadores.containsValue(item)) {
                    publishProgress(item);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(ItemInventario... values) {
            adicionarMarker(values[0]);
        }

        private void extrairItensInventario(String raw) throws JSONException {
            CategoriaInventarioService service = new CategoriaInventarioService();
            JSONArray array = new JSONObject(raw).getJSONArray("items");
            for (int i = 0; i < array.length(); i++) {
                if (isCancelled()) return;
                try {
                    JSONObject json = array.getJSONObject(i);
                    ItemInventario item = new ItemInventario();
                    item.setCategoria(service.getById(getActivity(), json.getLong("inventory_category_id")));
                    item.setId(json.getLong("id"));
                    item.setLatitude(json.getJSONObject("position").getDouble("latitude"));
                    item.setLongitude(json.getJSONObject("position").getDouble("longitude"));
                    itensInventario.add(item);
                } catch (Exception e) {
                    Log.e("ZUP", e.getMessage(), e);
                }
            }
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
                        addr.getLongitude())).zoom(18.5f).build();
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
                map.animateCamera(update);
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
                        addr.getLongitude())).zoom(18.5f).build();
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
                map.animateCamera(update);
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        realizarBuscaAutocomplete((Place) parent.getItemAtPosition(position));
        ViewUtils.hideKeyboard(getActivity(), autoCompView);
    }

    private void atualizarEndereco(LatLng posicao) {
        if (addressTask != null) addressTask.cancel(true);

        addressTask = new AddressTask(posicao);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            addressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            addressTask.execute();
        }
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

    private class Request {
        double latitude, longitude;
        long raio;
    }

    private class Requester extends TimerTask {

        @Override
        public void run() {
            if (request != null) {
                if (markerRetriever != null) {
                    markerRetriever.cancel(true);
                }

                markerRetriever = new MarkerRetriever(request);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                    markerRetriever.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    markerRetriever.execute();
                }
                request = null;
            }
        }
    }

    private class AddressTask extends AsyncTask<Void, Void, Address> {

        private final LatLng posicao;

        public AddressTask(LatLng posicao) {
            this.posicao = posicao;
        }

        @Override
        protected Address doInBackground(Void... params) {
            try {
                return GeoUtils.getFromLocation(posicao.latitude, posicao.longitude, 1).get(0);
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Address addr) {
            if (addr != null) {
                endereco = getFormattedAddress(addr);
                autoCompView.setText(endereco);
                //autoCompView.setAdapter(null);
                if (getActivity() != null) {
                    autoCompView.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item, SolicitePontoFragment.class));
                }
            }
        }
    }

    private String getFormattedAddress(Address address) {
        StringBuilder builder = new StringBuilder();
        builder.append(address.getThoroughfare());

        if (address.getFeatureName() != null) {
            builder.append(", ").append(address.getFeatureName());
        } else {
            builder.append(", s/nº");
        }


        if (address.getSubLocality() != null) {
            builder.append(" - ").append(address.getSubLocality());
        }

        if (getCity(address) != null) {
            builder.append(", ").append(getCity(address));
        }

        if (address.getPostalCode() != null) {
            builder.append(", ").append(address.getPostalCode());
        }

        return builder.toString();
    }

    private String getCity(Address address) {
        return address.getSubAdminArea() != null ? address.getSubAdminArea() : address.getLocality();
    }
}
