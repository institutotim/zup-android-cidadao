package br.com.lfdb.zup.fragment;

import android.content.Intent;
import android.graphics.Color;
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
import org.json.JSONObject;

import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import br.com.lfdb.zup.DetalheMapaActivity;
import br.com.lfdb.zup.FiltroExploreNovoActivity;
import br.com.lfdb.zup.MainActivity;
import br.com.lfdb.zup.R;
import br.com.lfdb.zup.SolicitacaoDetalheActivity;
import br.com.lfdb.zup.api.model.Cluster;
import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.core.ConstantesBase;
import br.com.lfdb.zup.domain.BuscaExplore;
import br.com.lfdb.zup.domain.CategoriaInventario;
import br.com.lfdb.zup.domain.CategoriaRelato;
import br.com.lfdb.zup.domain.ItemInventario;
import br.com.lfdb.zup.domain.ItemRelato;
import br.com.lfdb.zup.domain.Place;
import br.com.lfdb.zup.domain.SolicitacaoListItem;
import br.com.lfdb.zup.service.CategoriaInventarioService;
import br.com.lfdb.zup.service.CategoriaRelatoService;
import br.com.lfdb.zup.service.LoginService;
import br.com.lfdb.zup.util.BitmapUtils;
import br.com.lfdb.zup.util.DateUtils;
import br.com.lfdb.zup.util.FontUtils;
import br.com.lfdb.zup.util.GeoUtils;
import br.com.lfdb.zup.util.MapUtils;
import br.com.lfdb.zup.util.PreferenceUtils;
import br.com.lfdb.zup.util.ViewUtils;
import br.com.lfdb.zup.widget.PlacesAutoCompleteAdapter;

public class ExploreFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnCameraChangeListener, AdapterView.OnItemClickListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener, View.OnClickListener, GoogleMap.OnMarkerClickListener {

    private double userLongitude;
    private double userLatitude;

    private boolean updateCameraUser = true;

    private static View view;

    private boolean wasLocalized = false;

    private float zoom = 12f;

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
        userLatitude = location.getLatitude();
        userLongitude = location.getLongitude();

        if (updateCameraUser) {
            CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(),
                    location.getLongitude())).zoom(15).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            map.animateCamera(update);
            updateCameraUser = false;
            wasLocalized = true;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.locationButton) {
            CameraPosition position = new CameraPosition.Builder().target(new LatLng(userLatitude,
                    userLongitude)).zoom(15).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            map.animateCamera(update);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object marcador = marcadores.get(marker);
        if (marcador instanceof Cluster) {
            Cluster cluster = (Cluster) marcador;
            increaseZoomTo(new LatLng(cluster.getLatitude(), cluster.getLongitude()));
        }
        return false;
    }

    private class Request {
        double latitude, longitude;
        long raio;
    }

    private LocationClient mLocationClient;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private static final int MAX_ITEMS_PER_REQUEST = 50;

    private Request request = null;

    private AutoCompleteTextView autoCompView;

    private GoogleMap map;
    //private static View view;
    private BuscaExplore busca;
    public static double latitude = 0.0, longitude = 0.0;

    private Set<Object> itens = new HashSet<>();
    private Map<Marker, Object> marcadores = new HashMap<>();

    private Marker pontoBusca;
    private long raio = 0l;

    private ProgressBar progressBar;

    private MarkerRetriever markerRetriever = null;

    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(
                    getActivity(),
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }

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

        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        map = fragment.getMap();

        boolean showLogo = getResources().getBoolean(R.bool.show_logo_header);
        if (showLogo) view.findViewById(R.id.logo_header).setVisibility(View.VISIBLE);

        TextView botaoFiltrar = (TextView) view.findViewById(R.id.botaoFiltrar);
        botaoFiltrar.setTypeface(FontUtils.getRegular(getActivity()));
        botaoFiltrar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FiltroExploreNovoActivity.class);
            intent.putExtra("busca", busca);
            getActivity().startActivityForResult(intent, MainActivity.FILTRO_CODE);
        });

        progressBar = (ProgressBar) view.findViewById(R.id.loading);

        busca = PreferenceUtils.obterBuscaExplore(getActivity());
        if (busca == null) {
            busca = new BuscaExplore();
            List<CategoriaRelato> categorias = new CategoriaRelatoService().getCategorias(getActivity());
            for (CategoriaRelato categoria : categorias) {
                busca.getIdsCategoriaRelato().add(categoria.getId());
                for (CategoriaRelato sub : categoria.getSubcategorias()) {
                    busca.getIdsCategoriaRelato().add(sub.getId());
                }
            }
        }

        if (map != null) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.setOnInfoWindowClickListener(this);
            map.setOnCameraChangeListener(this);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setOnMarkerClickListener(this);

            CameraPosition p = new CameraPosition.Builder().target(new LatLng(Constantes.INITIAL_LATITUDE,
                    Constantes.INITIAL_LONGITUDE)).zoom(12).build();
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

        view.findViewById(R.id.locationButton).setOnClickListener(this);

        return view;
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            new Timer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new Timer().execute();
        }
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
            item.setLatitude(ir.getLatitude());
            item.setLongitude(ir.getLongitude());
            item.setCategoria(ir.getCategoria());
            item.setReferencia(ir.getReferencia());
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

    private void increaseZoomTo(LatLng position) {
        CameraPosition p = new CameraPosition.Builder().target(position)
                .zoom(map.getCameraPosition().zoom + 0.5f).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
        map.moveCamera(update);
    }

    private void adicionarMarker(ItemInventario item) {
        itens.add(item);
        if (estaNoFiltro(item.getCategoria())) {
            marcadores.put(map.addMarker(new MarkerOptions()
                    .position(new LatLng(item.getLatitude(), item.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapUtils.getInventoryMarker(getActivity(), item.getCategoria().isShowMarker() ? item.getCategoria().getMarcador() : item.getCategoria().getPin())))
                    .title(item.getCategoria().getNome())), item);
        }
    }

    private void adicionarMarker(ItemRelato item) {
        itens.add(item);
        if (estaNoFiltro(item.getCategoria())) {
            marcadores.put(map.addMarker(new MarkerOptions()
                    .position(new LatLng(item.getLatitude(), item.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapUtils.getReportMarker(getActivity(), item.getCategoria().getMarcador())))
                    .title(item.getCategoria().getNome())), item);
        }
    }

    private void adicionarMarker(Cluster item) {
        validateCluster(item);
        itens.add(item);
        marcadores.put(map.addMarker(new MarkerOptions()
                        .position(new LatLng(item.getLatitude(), item.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromBitmap(MapUtils.createMarker(getActivity(), getClusterColor(item), item.getCount())))
        ), item);
    }

    private void validateCluster(Cluster item) {
        if (item.isReport()) {
            for (Long id : item.getCategoriesIds()) {
                removeReportMarkerIfContained(id);
            }
        } else {
            for (Long id : item.getCategoriesIds()) {
                removeInventoryMarkerIfContained(id);
            }
        }
    }

    private void removeInventoryMarkerIfContained(long id) {
        ItemInventario itemInventario = null;
        for (Object item : itens) {
            if (item instanceof ItemInventario) {
                if (((ItemInventario) item).getId() == id) {
                    itemInventario = (ItemInventario) item;
                    break;
                }
            }
        }

        if (itemInventario != null) {
            itens.remove(itemInventario);
            Marker marker = getKeyFromValue(marcadores, itemInventario);
            marcadores.remove(marker);
            marker.remove();
        }
    }

    private void removeReportMarkerIfContained(long id) {
        ItemRelato itemRelato = null;
        for (Object item : itens) {
            if (item instanceof ItemRelato) {
                if (((ItemRelato) item).getId() == id) {
                    itemRelato = (ItemRelato) item;
                    break;
                }
            }
        }

        if (itemRelato != null) {
            itens.remove(itemRelato);
            Marker marker = getKeyFromValue(marcadores, itemRelato);
            marcadores.remove(marker);
            marker.remove();
        }
    }

    private int getClusterColor(Cluster item) {
        if (!item.isSingleCategory()) return Color.parseColor("#2ab4db");

        return Color.parseColor(item.isReport() ? new CategoriaRelatoService().getById(getActivity(),
                item.getCategoryId()).getCor() : new CategoriaInventarioService().getById(getActivity(),
                item.getCategoryId()).getCor());
    }

    @Override
    public void onStop() {
        super.onStop();
        PreferenceUtils.salvarBusca(getActivity(), busca);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        realizarBuscaAutocomplete((Place) adapterView.getItemAtPosition(i));
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

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        latitude = cameraPosition.target.latitude;
        longitude = cameraPosition.target.longitude;
        raio = GeoUtils.getVisibleRadius(map);
        //if (Math.abs(zoom - cameraPosition.zoom) > 0.00001f) {
        //    removerTodosItensMapa();
        //} else {
            removerItensMapa();
            exibirElementos();
        //}
        zoom = cameraPosition.zoom;
        refresh();
    }

    private void exibirElementos() {
        for (Object pontoMapa : itens) {
            if (pontoMapa instanceof ItemRelato) {
                ItemRelato item = (ItemRelato) pontoMapa;
                if (busca.getIdsCategoriaRelato().contains(item.getId())) {
                    adicionarMarker(item);
                }
            } else if (pontoMapa instanceof ItemInventario) {
                ItemInventario item = (ItemInventario) pontoMapa;
                if (busca.getIdsCategoriaInventario().contains(item.getId())) {
                    adicionarMarker(item);
                }
            }
        }
    }

    private void removerItensMapa() {
        Iterator<Marker> it = marcadores.keySet().iterator();
        while (it.hasNext()) {
            Marker marker = it.next();
            if (!GeoUtils.isVisible(map.getProjection().getVisibleRegion(), marker.getPosition())
                    //|| marcadores.get(marker) instanceof Cluster
                    ) {
                marker.remove();
                it.remove();
                marcadores.remove(marker);
            }
        }
    }

    private void removerTodosItensMapa() {
        map.clear();
        marcadores.clear();
    }

    public void aplicarFiltro(BuscaExplore busca) {
        this.busca = busca;
        removerTodosItensMapa();
        exibirElementos();
        refresh();
    }

    public void refresh() {
        Request request = new Request();
        request.latitude = latitude;
        request.longitude = longitude;
        request.raio = raio;
        this.request = request;
    }

    private class Timer extends AsyncTask<Void, Void, Void> {

        private boolean run = true;

        @Override
        protected Void doInBackground(Void... params) {
            while (run) {
                try {
                    Thread.sleep(400);
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
                } catch (Exception e) {
                    Log.e("ZUP", e.getMessage(), e);
                }
            }
            return null;
        }
    }

    private boolean estaNoFiltro(CategoriaRelato categoria) {
        return busca != null && busca.getIdsCategoriaRelato() != null && busca.getIdsCategoriaRelato().contains(categoria.getId());
    }

    private boolean estaNoFiltro(CategoriaInventario categoria) {
        return busca != null && busca.getIdsCategoriaInventario() != null && busca.getIdsCategoriaInventario().contains(categoria.getId());
    }

    private class MarkerRetriever extends AsyncTask<Void, Object, Void> {

        private List<ItemInventario> itensInventario = new CopyOnWriteArrayList<>();
        private List<ItemRelato> itensRelato = new CopyOnWriteArrayList<>();
        private List<Cluster> clusters = new CopyOnWriteArrayList<>();

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
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpClient client = new OkApacheClient();
                HttpResponse response;

                if (!busca.getIdsCategoriaInventario().isEmpty()) {

                    String query;
                    if (busca.getIdsCategoriaInventario().size() == 1) {
                        query = "&inventory_category_id=" + busca.getIdsCategoriaInventario().get(0);
                    } else {
                        StringBuilder builder = new StringBuilder("&inventory_category_ids=");
                        for (Long id : busca.getIdsCategoriaInventario()) {
                            builder.append(id).append(",");
                        }
                        query = builder.toString();
                        query = query.substring(0, query.length() - 1);
                    }

                    get = new HttpGet(Constantes.REST_URL + "/search/inventory/items" + ConstantesBase.getItemInventarioQuery(getActivity()) + "&position[latitude]=" + request.latitude + "&position[longitude]="
                            + request.longitude + "&position[distance]=" + request.raio + "&max_items=" + MAX_ITEMS_PER_REQUEST + query + getClusterQuery());
                    get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));

                    if (isCancelled()) return null;

                    response = client.execute(get);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        if (isCancelled()) return null;
                        extrairItensInventario(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    }
                }

                if (!busca.getIdsCategoriaRelato().isEmpty()) {

                    String categories;
                    if (busca.getIdsCategoriaRelato().size() == 1) {
                        categories = "&category_id=" + busca.getIdsCategoriaRelato().get(0);
                    } else {
                        StringBuilder builder = new StringBuilder("&category_ids=");
                        for (Long id : busca.getIdsCategoriaRelato()) {
                            builder.append(id).append(",");
                        }
                        categories = builder.toString();
                        categories = categories.substring(0, categories.length() - 1);
                    }

                    String query = Constantes.REST_URL + "/search/reports/items" + ConstantesBase.getItemRelatoQuery(getActivity()) + "&position[latitude]=" + request.latitude + "&position[longitude]="
                            + request.longitude + "&position[distance]=" + request.raio + "&max_items=" + MAX_ITEMS_PER_REQUEST + "&begin_date="
                            + busca.getPeriodo().getDateString() + "&display_type=full" + categories + getClusterQuery();
                    if (busca.getStatus() != null) {
                        query += "&statuses=" + busca.getStatus().getId();
                    }
                    Log.d("REQUEST", query);
                    get = new HttpGet(query);
                    get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));

                    if (isCancelled()) return null;

                    response = client.execute(get);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        if (isCancelled()) return null;
                        extrairItensRelato(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    }
                }

            } catch (Exception e) {
                if (!(e instanceof InterruptedIOException))
                    Log.e("ZUP", e.getMessage() != null ? e.getMessage() : "null", e);
                return null;
            }

            for (ItemInventario item : itensInventario) {
                if (!marcadores.containsValue(item)) {
                    publishProgress(item);
                }
            }

            for (ItemRelato item : itensRelato) {
                if (!marcadores.containsValue(item)) {
                    publishProgress(item);
                }
            }

            for (Cluster item : clusters) {
                if (!marcadores.containsValue(item)) {
                    publishProgress(item);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            if (values[0] instanceof ItemInventario)
                adicionarMarker((ItemInventario) values[0]);
            else if (values[0] instanceof ItemRelato)
                adicionarMarker((ItemRelato) values[0]);
            else if (values[0] instanceof Cluster)
                adicionarMarker((Cluster) values[0]);
        }

        private void extrairItensInventario(String raw) throws Exception {
            CategoriaInventarioService service = new CategoriaInventarioService();
            JSONObject root = new JSONObject(raw);
            JSONArray array = root.getJSONArray("items");
            for (int i = 0; i < array.length(); i++) {
                if (isCancelled()) return;
                JSONObject json = array.getJSONObject(i);
                ItemInventario item = new ItemInventario();
                item.setCategoria(service.getById(getActivity(), json.getLong("inventory_category_id")));
                item.setId(json.getLong("id"));
                item.setLatitude(json.getJSONObject("position").getDouble("latitude"));
                item.setLongitude(json.getJSONObject("position").getDouble("longitude"));
                itensInventario.add(item);
            }

            addClusters(root.optJSONArray("clusters"), false);
        }

        private void extrairItensRelato(String raw) throws Exception {
            CategoriaRelatoService service = new CategoriaRelatoService();
            JSONObject root = new JSONObject(raw);
            JSONArray array = root.getJSONArray("reports");
            for (int i = 0; i < array.length(); i++) {
                if (isCancelled()) return;
                JSONObject json = array.getJSONObject(i);
                ItemRelato item = new ItemRelato();
                item.setId(json.getLong("id"));
                item.setDescricao(json.getString("description"));
                item.setProtocolo(json.optString("protocol", null));
                item.setEndereco(json.getString("address"));
                item.setData(DateUtils.getIntervaloTempo(DateUtils.parseRFC3339Date(json.getString("created_at"))));
                item.setCategoria(service.getById(getActivity(), json.getJSONObject("category").getLong("id")));
                item.setLatitude(json.getJSONObject("position").getDouble("latitude"));
                item.setLongitude(json.getJSONObject("position").getDouble("longitude"));
                item.setIdItemInventario(json.optLong("inventory_item_id", -1));
                item.setIdStatus(json.optLong("status_id", -1));
                item.setReferencia(json.optString("reference", ""));

                JSONArray fotos = json.getJSONArray("images");
                for (int j = 0; j < fotos.length(); j++) {
                    item.getFotos().add(ViewUtils.isMdpiOrLdpi(getActivity()) ? fotos.getJSONObject(j).getString("low") : fotos.getJSONObject(j).getString("high"));
                }

                itensRelato.add(item);
            }

            addClusters(root.optJSONArray("clusters"), true);
        }

        private void addClusters(JSONArray array, boolean isReport) throws Exception {
            if (array == null) return;

            for (int i = 0; i < array.length(); i++) {
                if (isCancelled()) return;
                clusters.add(ConstantesBase.GSON.fromJson(array.get(i).toString(), Cluster.class)
                        .setReport(isReport));
            }
        }
    }

    private String getClusterQuery() {
        return "&clusterize=true&zoom=" + (int) zoom;
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
            }
        }
    }

    public Marker getKeyFromValue(Map<Marker, Object> map, Object value) {
        for (Marker m : map.keySet()) {
            if (map.get(m).equals(value)) {
                return m;
            }
        }
        return null;
    }
}
