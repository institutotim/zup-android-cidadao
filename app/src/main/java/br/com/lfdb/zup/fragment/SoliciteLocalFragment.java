package br.com.lfdb.zup.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import br.com.lfdb.zup.domain.Solicitacao;
import br.com.lfdb.zup.util.FontUtils;
import br.com.lfdb.zup.util.GPSUtils;
import br.com.lfdb.zup.util.ImageUtils;

public class SoliciteLocalFragment extends Fragment implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    // Local inicial: São Paulo
    private static final double INITIAL_LATITUDE = -23.5501283;
    private static final double INITIAL_LONGITUDE = -46.6338553;

    private GoogleMap map;
    private static View view;
    public static double latitude, longitude;
    private String file;
    private String endereco = "";
    private TimerEndereco task;
    private TextView tvEndereco;

    private Address enderecoAtual = null;

    private String rua = "", numero = "";
    private float zoomAtual;
    private boolean ignoreUpdate = false;

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
                    //if (ignoreUpdate) {

                        //System.out.println("modificou");
                    //}
                    latitude = cameraPosition.target.latitude;
                    longitude = cameraPosition.target.longitude;
                    zoomAtual = cameraPosition.zoom;
                }
            });

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            CameraPosition p = new CameraPosition.Builder().target(new LatLng(INITIAL_LATITUDE,
                    INITIAL_LONGITUDE)).zoom(12).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
            map.moveCamera(update);
        }

        tvEndereco = (TextView) view.findViewById(R.id.endereco);
        tvEndereco.setTypeface(FontUtils.getRegular(getActivity()));

        view.findViewById(R.id.editar).setOnClickListener(this);

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
            ((ImageView) view.findViewById(R.id.marcador)).setImageBitmap(ImageUtils.getScaled(getActivity(), "reports", file));
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
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
        return rua + ", " + numero;
    }

    private void atualizarEndereco() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            new AddressTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new AddressTask().execute();
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
        final View dialogView = getDialogView();
        ((TextView) dialogView.findViewById(R.id.endereco)).setText(rua);
        ((TextView) dialogView.findViewById(R.id.numero)).setText(numero);

        new AlertDialog.Builder(getActivity())
                .setTitle("Endereço do Relato")
                .setView(dialogView)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        final String num = ((TextView) dialogView.findViewById(R.id.numero)).getText().toString();
                        final String r = ((TextView) dialogView.findViewById(R.id.endereco)).getText().toString();

                        if (validarEndereco(r, num)) {
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    List<Address> addresses = GPSUtils.getFromLocationName(getActivity(), rua + ", " + numero + " - " + enderecoAtual.getSubAdminArea());
                                    if (addresses.isEmpty()) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), "Endereço não encontrado", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        dialog.dismiss();
                                    } else {
                                        final Address address = addresses.get(0);
                                        rua = address.getThoroughfare();
                                        numero = num;
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ignoreUpdate = true;
                                                CameraPosition position = new CameraPosition.Builder().target(new LatLng(address.getLatitude(), address.getLongitude())).zoom(zoomAtual).build();
                                                CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
                                                map.animateCamera(update);

                                                tvEndereco.setText(new StringBuilder().append(rua).append(", ").append(numero));
                                            }
                                        });
                                    }
                                    return null;
                                }
                            }.execute();
                            dialog.dismiss();
                        }
                    }
                })
                .show();
    }

    public boolean validarEndereco() {
        return validarEndereco(rua, numero);
    }

    private boolean validarEndereco(final String r, final String num) {
        if (r.isEmpty()) return false;

        if ("".equals(num)) {
            final View dialogView = getDialogNumberView();
            new AlertDialog.Builder(getActivity())
                    .setTitle(r)
                    .setView(dialogView)
                    .setNegativeButton("Sem número", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            numero = "s/n";
                            atualizarCampoEndereco();
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            final String num = ((EditText) dialogView.findViewById(R.id.numero)).getText().toString();
                            if (!num.isEmpty()) {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        List<Address> addresses = GPSUtils.getFromLocationName(getActivity(), new StringBuilder(rua).append(", ").append(num).append(" - ").append(enderecoAtual.getSubAdminArea()).toString());
                                        if (addresses.isEmpty()) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), "Endereço não encontrado", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            dialog.dismiss();
                                        } else {
                                            final Address address = addresses.get(0);
                                            rua = address.getThoroughfare();
                                            numero = num;
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ignoreUpdate = true;
                                                    CameraPosition position = new CameraPosition.Builder().target(new LatLng(address.getLatitude(), address.getLongitude())).zoom(zoomAtual).build();
                                                    CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
                                                    map.animateCamera(update);

                                                    tvEndereco.setText(new StringBuilder().append(rua).append(", ").append(numero));
                                                }
                                            });
                                        }
                                        return null;
                                    }
                                }.execute();
                            }
                        }
                    })
                    .show();
            return false;
        }

        return true;
    }

    private void atualizarCampoEndereco() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvEndereco.setText(new StringBuilder().append(rua).append(", ").append(numero));
            }
        });
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

                System.out.println(lat + " " + latitude + " / " + lon + " - " + longitude);
                if (lat != latitude && lon != longitude) {
                    lat = latitude;
                    lon = longitude;

                    if (ignoreUpdate) {
                        ignoreUpdate = false;
                        continue;
                    }

                    System.out.println("passou");

                    List<Address> addresses = GPSUtils.getFromLocation(getActivity(), lat, lon);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        final StringBuilder builder = new StringBuilder().append(address.getThoroughfare());
                        if (address.getThoroughfare() != null) {
                            enderecoAtual = address;
                            rua = address.getThoroughfare();

                            if (StringUtils.isNumeric(address.getFeatureName())) {
                                builder.append(", ").append(address.getFeatureName());
                                numero = address.getFeatureName();
                            } else {
                                numero = "";
                            }
                            if (!builder.toString().startsWith("null")) {
                                publishProgress(builder.toString());
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            endereco = values[0];
            tvEndereco.setText(values[0]);
        }
    }

    private class AddressTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                Address address = GPSUtils.getFromLocation(getActivity(), latitude, longitude).get(0);
                return address.getThoroughfare() + ", " + address.getFeatureName();
            } catch (Exception e) {
                Log.e("ZUP", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String addr) {
            if (addr != null) {
                endereco = addr;
                tvEndereco.setText(endereco);
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
