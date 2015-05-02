package br.com.lfdb.zup.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import br.com.lfdb.zup.R;
import br.com.lfdb.zup.SoliciteActivity;
import br.com.lfdb.zup.api.ZupApi;
import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.domain.Place;
import br.com.lfdb.zup.domain.Solicitacao;
import br.com.lfdb.zup.util.FontUtils;
import br.com.lfdb.zup.util.GPSUtils;
import br.com.lfdb.zup.util.GeoUtils;
import br.com.lfdb.zup.util.ImageUtils;
import br.com.lfdb.zup.util.ViewUtils;
import br.com.lfdb.zup.widget.PlacesAutoCompleteAdapter;

public class SoliciteLocalFragment extends Fragment implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener, View.OnClickListener,
        AdapterView.OnItemClickListener {

    private GoogleMap map;
    private static View view;
    public static double latitude, longitude;
    private String file;
    private TimerEndereco task;
    private AutoCompleteTextView tvEndereco;
    private TextView tvNumero;
    private View error;

    private TextView message;

    private Address enderecoAtual = null;

    private String rua = "", numero = "";
    private float zoomAtual;
    private boolean ignoreUpdate = false;

    private LocationClient mLocationClient;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private double userLongitude;
    private double userLatitude;

    private SearchTask searchTask = null;
    private GeocoderTask geocoderTask = null;
    private AddressTask addressTask = null;

    private boolean updateCameraUser = true;

    private boolean valid = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

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
        ((SoliciteActivity) getActivity()).enableNextButton(valid);

        try {
            view = inflater.inflate(R.layout.fragment_solicite_local, container, false);
        } catch (InflateException e) {
            Log.w("ZUP", e.getMessage());
        }

        error = view.findViewById(R.id.error);

        map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapaLocal)).getMap();
        if (map != null) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(true
            );

            map.setOnCameraChangeListener(cameraPosition -> {
                latitude = cameraPosition.target.latitude;
                longitude = cameraPosition.target.longitude;
                zoomAtual = cameraPosition.zoom;
                tvEndereco.setAdapter(null);
                tvEndereco.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item, ExploreFragment.class));
                tvEndereco.dismissDropDown();
            });

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            CameraPosition p = new CameraPosition.Builder().target(new LatLng(Constantes.INITIAL_LATITUDE,
                    Constantes.INITIAL_LONGITUDE)).zoom(12).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
            map.moveCamera(update);
        }

        tvEndereco = (AutoCompleteTextView) view.findViewById(R.id.autocompleteEndereco);
        tvEndereco.setTypeface(FontUtils.getRegular(getActivity()));
        tvEndereco.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item, ExploreFragment.class));
        tvEndereco.setOnItemClickListener(this);
        tvEndereco.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                realizarBuscaAutocomplete(v.getText().toString());
                ViewUtils.hideKeyboard(getActivity(), v);
                handled = true;
            }
            return handled;
        });

        tvNumero = (TextView) view.findViewById(R.id.numero);
        tvNumero.setTypeface(FontUtils.getRegular(getActivity()));

        message = (TextView) view.findViewById(R.id.message);
        message.setTypeface(FontUtils.getSemibold(getActivity()));

        view.findViewById(R.id.editar).setOnClickListener(this);

        task = new TimerEndereco();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }

        view.findViewById(R.id.locationButton).setOnClickListener(this);

        return view;
    }

    private void setAddressLoaderVisible(boolean visible) {
        if (view != null)
            view.findViewById(R.id.loadingIndicator).setVisibility(visible ? View.VISIBLE : View.GONE);
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
                            solicitacao.getLongitude())).zoom(16f).build();
                    CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
                    map.moveCamera(update);
                }
            }
        }

        if (file != null && !file.isEmpty()) {
            ((ImageView) view.findViewById(R.id.marcador)).setImageBitmap(ImageUtils.getScaled(getActivity(), "reports", file));
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((SoliciteActivity) getActivity()).enableNextButton(valid);
            ((SoliciteActivity) getActivity()).setInfo(R.string.selecione_o_local);
            if (file != null && !file.isEmpty()) {
                ((ImageView) view.findViewById(R.id.marcador)).setImageBitmap(ImageUtils.getScaled(getActivity(), "reports", file));
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
        StringBuilder builder = new StringBuilder();
        builder.append(rua).append(", ").append(numero);

        if (enderecoAtual.getSubLocality() != null) {
            builder.append(" - ").append(enderecoAtual.getSubLocality());
        }

        if (getCity() != null) {
            builder.append(", ").append(getCity());
        }

        if (enderecoAtual.getPostalCode() != null) {
            builder.append(", ").append(enderecoAtual.getPostalCode());
        }

        return builder.toString();
    }

    private void atualizarEndereco() {
        if (addressTask != null) {
            addressTask.cancel(true);
        }

        addressTask = new AddressTask();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            addressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            addressTask.execute();
        }
    }

    private View getDialogView() {
        return LayoutInflater.from(getActivity()).inflate(R.layout.dialog_endereco, null);
    }

    private View getDialogNumberView() {
        return LayoutInflater.from(getActivity()).inflate(R.layout.dialog_numero, null);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.locationButton) {
            CameraPosition position = new CameraPosition.Builder().target(new LatLng(userLatitude,
                    userLongitude)).zoom(16f).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            map.animateCamera(update);
            return;
        }

        final View dialogView = getDialogView();
        ((TextView) dialogView.findViewById(R.id.endereco)).setText(rua);
        ((TextView) dialogView.findViewById(R.id.numero)).setText(numero);
        ((TextView) dialogView.findViewById(R.id.referencia)).setText(((SoliciteActivity) getActivity()).getReferencia());

        new AlertDialog.Builder(getActivity())
                .setTitle("Endereço do Relato")
                .setView(dialogView)
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("OK", (dialog, which) -> {
                    final String num = ((TextView) dialogView.findViewById(R.id.numero)).getText().toString();
                    final String r = ((TextView) dialogView.findViewById(R.id.endereco)).getText().toString();
                    String referencia = ((TextView) dialogView.findViewById(R.id.referencia)).getText().toString();
                    if (referencia != null && !referencia.trim().isEmpty())
                        ((SoliciteActivity) getActivity()).setReferencia(referencia);

                    if (validarEndereco(r, num)) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                List<Address> addresses = GPSUtils.getFromLocationName(getActivity(), r + ", " + num + " - " + (
                                        enderecoAtual.getSubAdminArea() != null ? enderecoAtual.getSubAdminArea() : enderecoAtual.getLocality()));
                                if (addresses.isEmpty()) {
                                    getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Endereço não encontrado", Toast.LENGTH_SHORT).show());
                                    dialog.dismiss();
                                } else {
                                    final Address address = addresses.get(0);

                                    rua = address.getThoroughfare();
                                    if (!num.isEmpty() && StringUtils.isNumeric(num.substring(0, 1))) {
                                        numero = num;
                                    } else {
                                        numero = "";
                                    }

                                    getActivity().runOnUiThread(() -> {
                                        ignoreUpdate = true;
                                        CameraPosition position = new CameraPosition.Builder().target(new LatLng(address.getLatitude(), address.getLongitude())).zoom(zoomAtual).build();
                                        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
                                        map.animateCamera(update);

                                        tvEndereco.setAdapter(null);
                                        tvEndereco.setText(rua);
                                        tvEndereco.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item, ExploreFragment.class));
                                        tvNumero.setText(numero);
                                    });
                                }
                                return null;
                            }
                        }.execute();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public boolean validarEndereco() {
        if (!rua.equalsIgnoreCase(tvEndereco.getText().toString())) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Endereço inválido")
                    .setMessage("O endereço inserido é inválido")
                    .setNegativeButton("OK", null)
                    .show();
            return false;
        }

        return validarEndereco(rua, numero);
    }

    private boolean validarEndereco(final String r, final String num) {
        if (r.isEmpty()) return false;

        if ("".equals(num)) {
            final View dialogView = getDialogNumberView();
            new AlertDialog.Builder(getActivity())
                    .setTitle(r)
                    .setView(dialogView)
                    .setNegativeButton("Sem número", (dialog, which) -> {
                        dialog.dismiss();
                        numero = "s/n";
                        atualizarCampoEndereco();
                    })
                    .setPositiveButton("OK", (dialog, which) -> {
                        final String num1 = ((EditText) dialogView.findViewById(R.id.numero)).getText().toString();
                        if (!num1.isEmpty()) {
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    List<Address> addresses = GPSUtils.getFromLocationName(getActivity(), rua + ", " + num1 + " - " + (enderecoAtual.getSubAdminArea() != null ? enderecoAtual.getSubAdminArea() : enderecoAtual.getLocality()));
                                    if (addresses.isEmpty()) {
                                        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Endereço não encontrado", Toast.LENGTH_SHORT).show());
                                        dialog.dismiss();
                                    } else {
                                        final Address address = addresses.get(0);
                                        rua = address.getThoroughfare();
                                        if (!num1.isEmpty() && StringUtils.isNumeric(num1.substring(0, 1))) {
                                            numero = num1;
                                        } else {
                                            numero = "";
                                        }
                                        getActivity().runOnUiThread(() -> {
                                            ignoreUpdate = true;
                                            CameraPosition position = new CameraPosition.Builder().target(new LatLng(address.getLatitude(), address.getLongitude())).zoom(zoomAtual).build();
                                            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
                                            map.animateCamera(update);

                                            tvEndereco.setAdapter(null);
                                            tvEndereco.setText(rua);
                                            tvEndereco.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item, ExploreFragment.class));

                                            tvNumero.setText(numero);
                                        });
                                    }
                                    return null;
                                }
                            }.execute();
                        }
                    })
                    .show();
            return false;
        }

        return true;
    }

    private void atualizarCampoEndereco() {
        getActivity().runOnUiThread(() -> {
            tvEndereco.setText(rua);
            tvNumero.setText(numero);
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        realizarBuscaAutocomplete((Place) parent.getItemAtPosition(position));
        ViewUtils.hideKeyboard(getActivity(), tvEndereco);
    }

    private void realizarBuscaAutocomplete(Place place) {
        if (geocoderTask != null) {
            geocoderTask.cancel(true);
        }

        geocoderTask = new GeocoderTask();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            geocoderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, place);
        } else {
            geocoderTask.execute(place);
        }
    }

    private void realizarBuscaAutocomplete(String query) {
        if (searchTask != null) {

            searchTask.cancel(true);
        }

        searchTask = new SearchTask();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
        } else {
            searchTask.execute(query);
        }
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
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.e("ZUP", e.getMessage(), e);
                }

                if (lat != latitude && lon != longitude) {
                    lat = latitude;
                    lon = longitude;

                    if (ignoreUpdate) {
                        ignoreUpdate = false;
                        continue;
                    }

                    getActivity().runOnUiThread(() -> setAddressLoaderVisible(true));

                    List<Address> addresses = GPSUtils.getFromLocation(getActivity(), lat, lon);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        if (address.getThoroughfare() != null) {
                            enderecoAtual = address;
                            try {
                                valid = ZupApi.validateCityBoundary(getActivity(), lat, lon);
                            } catch (Exception e) {
                                Log.e("Boundary validation", "Failed to validate boundary", e);
                            }

                            if (!address.getThoroughfare().startsWith("null")) {

                                publishProgress(address.getThoroughfare(), address.getFeatureName());
                            }
                        }
                    }

                    getActivity().runOnUiThread(() -> setAddressLoaderVisible(false));
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            rua = values[0];
            tvEndereco.setAdapter(null);
            tvEndereco.setText(values[0]);

            verifyValid();

            try {
                if (!values[1].isEmpty() && StringUtils.isNumeric(values[1].substring(0, 1))) {
                    numero = values[1];
                    tvNumero.setText(values[1]);
                } else {
                    numero = "";
                    tvNumero.setText("");
                }
            } catch (Exception e) {
                Log.w("ZUP", e.getMessage() != null ? e.getMessage() : "null", e);
                numero = "";
                tvNumero.setText("");
            }
            if (getActivity() != null) {
                tvEndereco.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item, SoliciteLocalFragment.class));
                tvEndereco.dismissDropDown();
            }
        }
    }

    private class AddressTask extends AsyncTask<Void, Void, Address> {

        @Override
        protected void onPreExecute() {
            setAddressLoaderVisible(true);
        }

        @Override
        protected Address doInBackground(Void... params) {
            try {
                valid = ZupApi.validateCityBoundary(getActivity(), latitude, longitude);
                return GPSUtils.getFromLocation(getActivity(), latitude, longitude).get(0);
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Address addr) {
            setAddressLoaderVisible(false);
            verifyValid();
            if (addr != null) {
                rua = addr.getThoroughfare();
                if (!addr.getFeatureName().isEmpty() && StringUtils.isNumeric(addr.getFeatureName().substring(0, 1))) {
                    numero = addr.getFeatureName();
                    tvNumero.setText(addr.getFeatureName());
                } else {
                    numero = "";
                    tvNumero.setText("");
                }
                tvEndereco.setAdapter(null);
                tvEndereco.setText(addr.getThoroughfare());
                if (getActivity() != null) {
                    tvEndereco.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item, SoliciteLocalFragment.class));
                    tvEndereco.dismissDropDown();
                }
            }
        }
    }

    private void verifyValid() {
        ((SoliciteActivity) getActivity()).enableNextButton(valid);
        if (!valid && error.getVisibility() != View.VISIBLE) {
            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(1000);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    error.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            error.startAnimation(anim);
        } else if (valid && error.getVisibility() != View.GONE){
            AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
            anim.setDuration(1000);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    error.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            error.startAnimation(anim);
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
        userLatitude = location.getLatitude();
        userLongitude = location.getLongitude();

        if (updateCameraUser) {
            CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(),
                    location.getLongitude())).zoom(16f).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            map.moveCamera(update);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            atualizarEndereco();
            wasLocalized = true;
            updateCameraUser = false;
        }
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

    private class SearchTask extends AsyncTask<String, Void, Address> {

        @Override
        protected void onPreExecute() {
            setAddressLoaderVisible(true);
        }

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
            setAddressLoaderVisible(false);
            if (!isCancelled()) {
                if (addr != null) {
                    CameraPosition p = new CameraPosition.Builder().target(new LatLng(addr.getLatitude(),
                            addr.getLongitude())).zoom(16f).build();
                    CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
                    map.animateCamera(update);
                }
            }
        }
    }

    private class GeocoderTask extends AsyncTask<Place, Void, Address> {

        @Override
        protected void onPreExecute() {
            setAddressLoaderVisible(true);
        }

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
            setAddressLoaderVisible(false);
            if (addr != null) {
                CameraPosition p = new CameraPosition.Builder().target(new LatLng(addr.getLatitude(),
                        addr.getLongitude())).zoom(16f).build();
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
                map.animateCamera(update);
            }
        }
    }

    private String getCity() {
        return enderecoAtual.getSubAdminArea() != null ? enderecoAtual.getSubAdminArea() : enderecoAtual.getLocality();
    }
}
