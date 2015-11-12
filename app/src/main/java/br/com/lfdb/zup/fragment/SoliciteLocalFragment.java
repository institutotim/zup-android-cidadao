package br.com.lfdb.zup.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import br.com.lfdb.zup.R;
import br.com.lfdb.zup.SoliciteActivity;
import br.com.lfdb.zup.api.ZupApi;
import br.com.lfdb.zup.base.BaseFragment;
import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.domain.Place;
import br.com.lfdb.zup.domain.Solicitacao;
import br.com.lfdb.zup.util.FontUtils;
import br.com.lfdb.zup.util.GPSUtils;
import br.com.lfdb.zup.util.GeoUtils;
import br.com.lfdb.zup.util.ImageUtils;
import br.com.lfdb.zup.util.ViewUtils;
import br.com.lfdb.zup.widget.PlacesAutoCompleteAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.commons.lang3.StringUtils;

@EFragment(R.layout.fragment_solicite_local)
public class SoliciteLocalFragment extends BaseFragment
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        AdapterView.OnItemClickListener {

    @ViewById
    View posicaoCentral;

    @ViewById
    View div;

    @ViewById
    View error;

    @ViewById
    ImageView locationButton;

    @ViewById
    ImageView marcador;

    @ViewById
    ProgressBar loadingIndicator;

    @ViewById
    AutoCompleteTextView autocompleteEndereco;

    @ViewById
    RelativeLayout editar;

    @ViewById
    TextView tvNumero;

    @ViewById
    TextView message;

    private String street = "";
    private String number = "";
    private String file;
    private double userLongitude;
    private double userLatitude;
    private boolean updateCameraUser = true;
    private boolean valid = false;
    private boolean ignoreUpdate = false;
    private boolean wasLocalized = false;
    private float currentZoom;

    private Address currentAddress = null;
    private GoogleMap map;
    private GoogleApiClient googleApiClient;

    public static double latitude, longitude;

    private static final LocationRequest REQUEST =
            LocationRequest.create().setInterval(5000)         // 5 seconds
                    .setFastestInterval(16)    // 16ms = 60fps
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    @AfterViews
    void init() {
        setRetainInstance(true);
        ((SoliciteActivity) getActivity()).exibirBarraInferior(true);
        ((SoliciteActivity) getActivity()).setInfo(R.string.selecione_o_local);
        ((SoliciteActivity) getActivity()).enableNextButton(valid);
        map =
                ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapaLocal)).getMap();
        if (map != null) {
            mapInit();
        }
        PlacesAutoCompleteAdapter placesAutoCompleteAdapter =
                new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item,
                        ExploreFragment.class);
        autocompleteEndereco.setTypeface(FontUtils.getRegular(getActivity()));
        autocompleteEndereco.setAdapter(placesAutoCompleteAdapter);
        autocompleteEndereco.setOnItemClickListener(this);
        autocompleteEndereco.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchTask(v.getText().toString());
                ViewUtils.hideKeyboard(getActivity(), v);
                handled = true;
            }
            return handled;
        });
        tvNumero.setTypeface(FontUtils.getRegular(getActivity()));
        message.setTypeface(FontUtils.getSemibold(getActivity()));
        timerEnderecoTask(latitude, longitude);
    }

    @Click
    void locationButton() {
        CameraPosition position =
                new CameraPosition.Builder().target(new LatLng(userLatitude, userLongitude))
                        .zoom(16f)
                        .build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        map.animateCamera(update);
    }

    @Click
    void editar() {
        final View dialogView =
                LayoutInflater.from(getActivity()).inflate(R.layout.dialog_endereco, null);
        ((TextView) dialogView.findViewById(R.id.endereco)).setText(street);
        ((TextView) dialogView.findViewById(R.id.numero)).setText(number);
        ((TextView) dialogView.findViewById(R.id.referencia)).setText(
                ((SoliciteActivity) getActivity()).getReferencia());

        new AlertDialog.Builder(getActivity()).setTitle("Endereço do Relato")
                .setView(dialogView)
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("OK", (dialog, which) -> {
                    final String num = ((TextView) dialogView.findViewById(R.id.numero)).getText().toString();
                    final String r = ((TextView) dialogView.findViewById(R.id.endereco)).getText().toString();
                    String referencia =
                            ((TextView) dialogView.findViewById(R.id.referencia)).getText().toString();
                    if (referencia != null && !referencia.trim().isEmpty()) {
                        ((SoliciteActivity) getActivity()).setReferencia(referencia);
                    }
                    if (validarEndereco(r, num)) {
                        task(r, num, dialog);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    void mapInit() {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setOnCameraChangeListener(cameraPosition -> {
            latitude = cameraPosition.target.latitude;
            longitude = cameraPosition.target.longitude;
            currentZoom = cameraPosition.zoom;
            autocompleteEndereco.setAdapter(null);
            autocompleteEndereco.setAdapter(
                    new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item,
                            ExploreFragment.class));
            autocompleteEndereco.dismissDropDown();
        });
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraPosition p = new CameraPosition.Builder().target(
                new LatLng(Constantes.INITIAL_LATITUDE, Constantes.INITIAL_LONGITUDE)).zoom(12).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
        map.moveCamera(update);
    }

    public boolean validarEndereco() {
        if (!street.equalsIgnoreCase(autocompleteEndereco.getText().toString())) {
            new AlertDialog.Builder(getActivity()).setTitle("Endereço inválido")
                    .setMessage("O endereço inserido é inválido")
                    .setNegativeButton("OK", null)
                    .show();
            return false;
        }
        return validarEndereco(street, number);
    }

    boolean validarEndereco(final String r, final String num) {
        if (r.isEmpty()) return false;
        if ("".equals(num)) {
            dialogNumberView(r);
            return false;
        }
        return true;
    }

    void dialogNumberView(String r) {
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_numero, null);
        new AlertDialog.Builder(getActivity()).setTitle(r)
                .setView(dialogView)
                .setNegativeButton("Sem número", (dialog, which) -> {
                    dialog.dismiss();
                    number = "s/n";
                    atualizarCampoEndereco();
                })
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    final String num1 =
                            ((EditText) dialogView.findViewById(R.id.numero)).getText().toString();
                    if (!num1.isEmpty()) {
                        dialogNumberTask(num1, dialog);
                    }
                })
                .show();
    }

    void verifyValid() {
        if (!isAdded()) {
            return;
        }
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
        } else if (valid && error.getVisibility() != View.GONE) {
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

    void setUpLocationClientIfNeeded() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @Background
    void dialogNumberTask(String num1, DialogInterface dialog) {
        List<Address> addresses = GPSUtils.getFromLocationName(getActivity(),
                street + ", " + num1 + " - " + (currentAddress.getSubAdminArea() != null
                        ? currentAddress.getSubAdminArea() : currentAddress.getLocality()));
        if (addresses.isEmpty()) {
            toast("Endereço não encontrado");
            dialog.dismiss();
        } else {
            final Address address = addresses.get(0);
            street = address.getThoroughfare();
            if (!num1.isEmpty() && StringUtils.isNumeric(num1.substring(0, 1))) {
                number = num1;
            } else {
                number = "";
            }
            updateCameraAndAutoComplete(address);
        }
    }

    @Background
    void timerEnderecoTask(double lat, double lon) {
        boolean run = true;
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
                showHideLoading(true);
                List<Address> addresses = GPSUtils.getFromLocation(getActivity(), lat, lon);
                if (addresses.isEmpty()) {
                    showHideLoading(false);
                    return;
                }
                Address address = addresses.get(0);
                if (address.getThoroughfare() == null) {
                    showHideLoading(false);
                    return;
                }
                currentAddress = address;
                try {
                    valid = ZupApi.validateCityBoundary(getActivity(), lat, lon);
                } catch (Exception e) {
                    Log.e("Boundary validation", "Failed to validate boundary", e);
                }
                showHideLoading(false);
            }
        }
    }

    @Background
    void addressTask() {
        showHideLoading(true);
        try {
            valid = ZupApi.validateCityBoundary(getActivity(), latitude, longitude);
            Address addr = GPSUtils.getFromLocation(getActivity(), latitude, longitude).get(0);
            showHideLoading(false);
            verifyValid();
            if (addr == null) {
                return;
            }
            street = addr.getThoroughfare();
            if (!addr.getFeatureName().isEmpty() && StringUtils.isNumeric(
                    addr.getFeatureName().substring(0, 1))) {
                number = addr.getFeatureName();
            } else {
                number = "";
            }
            updateUi(addr, number);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ZUP", e.getMessage(), e);
        }
    }

    @Background
    void searchTask(String query) {
        showHideLoading(true);
        try {
            Address addr = GeoUtils.search(query, latitude, longitude);
            showHideLoading(false);
            if (addr == null) {
                return;
            }
            CameraPosition p =
                    new CameraPosition.Builder().target(new LatLng(addr.getLatitude(), addr.getLongitude()))
                            .zoom(16f)
                            .build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
            map.animateCamera(update);
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
        }
    }

    @Background
    void geocoderTask(Place place) {
        showHideLoading(true);
        try {
            Address addr = GeoUtils.getFromPlace(place);
            showHideLoading(false);
            if (addr == null) {
                return;
            }
            LatLng latLong = new LatLng(addr.getLatitude(), addr.getLongitude());
            CameraPosition p = new CameraPosition.Builder().target(latLong).zoom(16f).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
            map.animateCamera(update);
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
        }
    }

    @Background
    void task(String r, String num, DialogInterface dialog) {
        if (currentAddress == null) {
            return;
        }
        String addressCompl =
                currentAddress.getSubAdminArea() != null ? currentAddress.getSubAdminArea()
                        : currentAddress.getLocality();
        List<Address> addresses =
                GPSUtils.getFromLocationName(getActivity(), r + ", " + num + " - " + (addressCompl));
        if (addresses.isEmpty()) {
            toast("Endereço não encontrado");
            dialog.dismiss();
        } else {
            final Address address = addresses.get(0);
            street = address.getThoroughfare();
            if (!num.isEmpty() && StringUtils.isNumeric(num.substring(0, 1))) {
                number = num;
            } else {
                number = "";
            }
            updateUiAdapter(address);
        }
    }

    @UiThread
    void showHideLoading(boolean visible) {
        loadingIndicator.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @UiThread
    void toast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @UiThread
    void updateUiAdapter(Address address) {
        ignoreUpdate = true;
        CameraPosition position = new CameraPosition.Builder().target(
                new LatLng(address.getLatitude(), address.getLongitude())).zoom(currentZoom).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        map.animateCamera(update);

        autocompleteEndereco.setAdapter(null);
        autocompleteEndereco.setText(street);
        autocompleteEndereco.setAdapter(
                new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item,
                        ExploreFragment.class));
        tvNumero.setText(number);
    }

    @UiThread
    void updateCameraAndAutoComplete(Address address) {
        ignoreUpdate = true;
        CameraPosition position = new CameraPosition.Builder().target(
                new LatLng(address.getLatitude(), address.getLongitude())).zoom(currentZoom).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        map.animateCamera(update);

        autocompleteEndereco.setAdapter(null);
        autocompleteEndereco.setText(street);
        autocompleteEndereco.setAdapter(
                new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item,
                        ExploreFragment.class));

        tvNumero.setText(number);
    }

    @UiThread
    void atualizarCampoEndereco() {
        autocompleteEndereco.setText(street);
        tvNumero.setText(number);
    }

    @UiThread
    void updateUi(Address addr, String number) {
        tvNumero.setText(number);
        autocompleteEndereco.setAdapter(null);
        autocompleteEndereco.setText(addr.getThoroughfare());
        if (getActivity() == null) {
            return;
        }
        autocompleteEndereco.setAdapter(
                new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item,
                        SoliciteLocalFragment.class));
        autocompleteEndereco.dismissDropDown();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (map == null) {
            return;
        }
        if (getArguments() == null) {
            return;
        }
        Solicitacao solicitacao = (Solicitacao) getArguments().getSerializable("solicitacao");
        if (solicitacao != null) {
            file = solicitacao.getCategoria().getMarcador();
            CameraPosition p = new CameraPosition.Builder().target(
                    new LatLng(solicitacao.getLatitude(), solicitacao.getLongitude())).zoom(16f).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
            map.moveCamera(update);
        }
        if (file != null && !file.isEmpty()) {
            marcador.setImageBitmap(ImageUtils.getScaled(getActivity(), "reports", file));
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (wasLocalized) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, REQUEST, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.screen_name_select_local);
    }

    @Override
    public void onLocationChanged(Location location) {
        userLatitude = location.getLatitude();
        userLongitude = location.getLongitude();
        if (map == null) {
            return;
        }
        if (!updateCameraUser) {
            return;
        }
        CameraPosition position = new CameraPosition.Builder().target(
                new LatLng(location.getLatitude(), location.getLongitude())).zoom(16f).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        map.moveCamera(update);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        addressTask();
        wasLocalized = true;
        updateCameraUser = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpLocationClientIfNeeded();
        googleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        geocoderTask((Place) parent.getItemAtPosition(position));
        ViewUtils.hideKeyboard(getActivity(), autocompleteEndereco);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            return;
        }
        ((SoliciteActivity) getActivity()).enableNextButton(valid);
        ((SoliciteActivity) getActivity()).setInfo(R.string.selecione_o_local);
        if (file != null && !file.isEmpty()) {
            marcador.setImageBitmap(ImageUtils.getScaled(getActivity(), "reports", file));
        }
    }

    private String getCity() {
        return currentAddress.getSubAdminArea() != null ? currentAddress.getSubAdminArea()
                : currentAddress.getLocality();
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

    public Address getRawAddress() {
        return currentAddress;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getCurrentAddress() {
        StringBuilder builder = new StringBuilder();
        builder.append(street).append(", ").append(number);
        if (currentAddress.getSubLocality() != null) {
            builder.append(" - ").append(currentAddress.getSubLocality());
        }
        if (getCity() != null) {
            builder.append(", ").append(getCity());
        }
        if (currentAddress.getPostalCode() != null) {
            builder.append(", ").append(currentAddress.getPostalCode());
        }
        return builder.toString();
    }
}
