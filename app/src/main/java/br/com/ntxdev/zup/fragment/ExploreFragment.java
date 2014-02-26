package br.com.ntxdev.zup.fragment;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
import java.util.concurrent.CopyOnWriteArrayList;

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
import br.com.ntxdev.zup.util.GeoUtils;
import br.com.ntxdev.zup.util.ImageUtils;
import br.com.ntxdev.zup.util.PreferenceUtils;
import br.com.ntxdev.zup.util.ViewUtils;
import br.com.ntxdev.zup.widget.AutoCompleteAdapter;

public class ExploreFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMyLocationChangeListener,
        GoogleMap.OnCameraChangeListener, AdapterView.OnItemClickListener {

    // Local inicial: São Paulo
    private static final double INITIAL_LATITUDE = -23.6824124;
    private static final double INITIAL_LONGITUDE = -46.5952992;

    private class Request {
        double latitude, longitude;
        long raio;
    }

    private static final int MAX_ITEMS_PER_REQUEST = 50;

    private Request request = null;

    private GoogleMap map;
    private static View view;
    private BuscaExplore busca;
    private int zoom = 2;
    private double latitude = 0.0, longitude = 0.0;

    private SparseArray<Set<Object>> itens = new SparseArray<Set<Object>>();
    private SparseArray<Map<Marker, Object>> marcadores = new SparseArray<Map<Marker, Object>>();

    private Marker pontoBusca;
    private long raio = 0l;

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

        map = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        TextView botaoFiltrar = (TextView) view.findViewById(R.id.botaoFiltrar);
        botaoFiltrar.setTypeface(FontUtils.getRegular(getActivity()));
        botaoFiltrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FiltroExploreActivity.class);
                intent.putExtra("busca", busca);
                getActivity().startActivityForResult(intent, MainActivity.FILTRO_CODE);
            }
        });

        busca = PreferenceUtils.obterBuscaExplore(getActivity());
        if (busca == null) {
            busca = new BuscaExplore();
            CategoriaRelatoService service = new CategoriaRelatoService();
            for (CategoriaRelato categoria : service.getCategorias(getActivity())) {
                busca.getIdsCategoriaRelato().add(categoria.getId());
            }
        }

        if (map != null) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.setOnInfoWindowClickListener(this);
            map.setOnMyLocationChangeListener(this);
            map.setOnCameraChangeListener(this);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            CameraPosition p = new CameraPosition.Builder().target(new LatLng(INITIAL_LATITUDE,
                    INITIAL_LONGITUDE)).zoom(15).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
            map.moveCamera(update);
        }

        AutoCompleteTextView autoCompView = (AutoCompleteTextView) view.findViewById(R.id.autocomplete);
        autoCompView.setAdapter(new AutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item));
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

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

        Object marcador = marcadores.get(zoom).get(marker);
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

    private void adicionarMarker(ItemInventario item) {
        if (itens.get(zoom) == null) itens.put(zoom, new HashSet<Object>());
        if (marcadores.get(zoom) == null) marcadores.put(zoom, new HashMap<Marker, Object>());

        itens.get(zoom).add(item);
        marcadores.get(zoom).put(map.addMarker(new MarkerOptions()
                .position(new LatLng(item.getLatitude(), item.getLongitude()))
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getScaled(getActivity(), item.getCategoria().getMarcador())))
                .title(item.getCategoria().getNome())), item);
    }

    private void adicionarMarker(ItemRelato item) {
        if (itens.get(zoom) == null) itens.put(zoom, new HashSet<Object>());
        if (marcadores.get(zoom) == null) marcadores.put(zoom, new HashMap<Marker, Object>());

        itens.get(zoom).add(item);
        marcadores.get(zoom).put(map.addMarker(new MarkerOptions()
                .position(new LatLng(item.getLatitude(), item.getLongitude()))
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getScaled(getActivity(), item.getCategoria().getMarcador())))
                .title(item.getCategoria().getNome())), item);
    }

    @Override
    public void onMyLocationChange(Location location) {
        zoom = 15;
        CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(),
                location.getLongitude())).zoom(zoom).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        map.animateCamera(update);
        map.setOnMyLocationChangeListener(null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        realizarBuscaAutocomplete((String) adapterView.getItemAtPosition(i));
    }

    private void realizarBuscaAutocomplete(String str) {
        try {
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

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (cameraPosition.target.latitude == 0.0 && cameraPosition.target.longitude == 0.0) return;

        int zoom = (int) cameraPosition.zoom;
        if (zoom != this.zoom) {
            this.zoom = zoom;
            removerTodosItensMapa();
        }
        latitude = cameraPosition.target.latitude;
        longitude = cameraPosition.target.longitude;
        raio = GeoUtils.getVisibleRadius(map);
        removerItensMapa();
        exibirElementos();
        refresh();
    }

    private void exibirElementos() {
        if (itens.get(zoom) == null) return;

        for (Object pontoMapa : itens.get(zoom)) {
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
        if (marcadores.get(zoom) == null) return;

        Iterator<Marker> it = marcadores.get(zoom).keySet().iterator();
        while (it.hasNext()) {
            Marker marker = it.next();
            if (!GeoUtils.isVisible(map.getProjection().getVisibleRegion(), marker.getPosition())) {
                marker.setVisible(false);
                it.remove();
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
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                            new MarkerRetriever(request).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            new MarkerRetriever(request).execute();
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

    private class MarkerRetriever extends AsyncTask<Void, Object, Void> {

        private List<ItemInventario> itensInventario = new CopyOnWriteArrayList<ItemInventario>();
        private List<ItemRelato> itensRelato = new CopyOnWriteArrayList<ItemRelato>();

        private Request request;

        public MarkerRetriever(Request request) {
            this.request = request;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i("ZUP", "Request started");
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet get;
                HttpResponse response;

                for (Long id : busca.getIdsCategoriaInventario()) {
                    get = new HttpGet(Constantes.REST_URL + "/inventory/items?position[latitude]=" + request.latitude + "&position[longitude]="
                            + request.longitude + "&position[distance]=" + request.raio + "&max_items=" + MAX_ITEMS_PER_REQUEST + "&inventory_category_id=" + id);
                    get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));
                    response = client.execute(get);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        extrairItensInventario(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    }
                }

                for (Long id : busca.getIdsCategoriaRelato()) {
                    String query = Constantes.REST_URL + "/reports/items?position[latitude]=" + request.latitude + "&position[longitude]="
                            + request.longitude + "&position[distance]=" + request.raio + "&max_items=" + MAX_ITEMS_PER_REQUEST + "&category_id=" + id + "&begin_date="
                            + busca.getPeriodo().getDateString();
                    if (busca.getStatus() != null) {
                        query += "&statuses=" + busca.getStatus().getId();
                    }
                    get = new HttpGet(query);
                    get.setHeader("X-App-Token", new LoginService().getToken(getActivity()));
                    response = client.execute(get);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        extrairItensRelato(EntityUtils.toString(response.getEntity(), "UTF-8"));
                    }
                }
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage() != null ? e.getMessage() : "null", e);
                return null;
            }

            for (ItemInventario item : itensInventario) {
                if (marcadores.get(zoom) == null || !marcadores.get(zoom).containsValue(item)) {
                    publishProgress(item);
                }
            }

            for (ItemRelato item : itensRelato) {
                if (marcadores.get(zoom) == null || !marcadores.get(zoom).containsValue(item)) {
                    publishProgress(item);
                }
            }

            Log.i("ZUP", "Request completed");

            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            if (values[0] instanceof ItemInventario)
                adicionarMarker((ItemInventario) values[0]);
            else if (values[0] instanceof ItemRelato)
                adicionarMarker((ItemRelato) values[0]);
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

    @Override
    public void onDestroy() {
        PreferenceUtils.salvarBusca(getActivity(), busca);
        super.onDestroy();
    }
}
