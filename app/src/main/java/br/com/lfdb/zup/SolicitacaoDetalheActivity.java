package br.com.lfdb.zup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import br.com.lfdb.zup.util.AuthHelper;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.PageIndicator;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.lfdb.zup.base.BaseActivity;
import br.com.lfdb.zup.core.Constantes;
import br.com.lfdb.zup.core.ConstantesBase;
import br.com.lfdb.zup.domain.ComentarioRelato;
import br.com.lfdb.zup.domain.SolicitacaoListItem;
import br.com.lfdb.zup.service.LoginService;
import br.com.lfdb.zup.util.DateUtils;
import br.com.lfdb.zup.util.FontUtils;
import br.com.lfdb.zup.util.ImageUtils;
import br.com.lfdb.zup.widget.ImagePagerAdapter;
import butterknife.ButterKnife;
import butterknife.Bind;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SolicitacaoDetalheActivity extends BaseActivity {

    @Bind(R.id.categoryIcon)
    ImageView categoryIcon;
    @Bind(R.id.categoryName)
    TextView categoryName;
    @Bind(R.id.subcategoryName)
    TextView subcategoryName;

    @Bind(R.id.comment_container)
    ViewGroup commentContainer;

    SolicitacaoListItem solicitacao;

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitacao_detalhe);
        ButterKnife.bind(this);

        solicitacao = (SolicitacaoListItem) getIntent().getExtras().getSerializable("solicitacao");
        boolean alterarLabel = getIntent().getExtras().getBoolean("alterar_botao", false);

        TextView protocolo = (TextView) findViewById(R.id.protocolo);
        if (solicitacao.getProtocolo() == null || solicitacao.getProtocolo().equalsIgnoreCase("null")) {
            protocolo.setVisibility(View.GONE);
        } else {
            protocolo.setText(getString(R.string.protocolo) + " " + solicitacao.getProtocolo());
            protocolo.setTypeface(FontUtils.getBold(this));
        }

        TextView endereco = (TextView) findViewById(R.id.endereco);
        endereco.setText(solicitacao.getEndereco());
        endereco.setTypeface(FontUtils.getLight(this));

        TextView referencia = (TextView) findViewById(R.id.referencia);
        referencia.setText(solicitacao.getReferencia());
        referencia.setTypeface(FontUtils.getLight(this));

        TextView data = (TextView) findViewById(R.id.data);
        data.setText(getString(R.string.enviada) + " " + solicitacao.getData());
        data.setTypeface(FontUtils.getBold(this));

        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        if (solicitacao.getFotos().isEmpty()) {
            mPager.setVisibility(View.GONE);
            findViewById(R.id.indicator).setVisibility(View.GONE);

            GoogleMap map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            map.getUiSettings().setAllGesturesEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);

            CameraPosition p = new CameraPosition.Builder().target(new LatLng(solicitacao.getLatitude(),
                    solicitacao.getLongitude())).zoom(15).build();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
            map.moveCamera(update);
            try {
                MarkerOptions marker = new MarkerOptions();
                LatLng latLong = new LatLng(solicitacao.getLatitude(), solicitacao.getLongitude());
                marker.position(latLong);
                //marker.icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getScaled(this, "reports", solicitacao.getCategoria().getMarcador())));
                map.addMarker(marker);
            } catch (Exception e) {
                e.getMessage();
            }
        } else {
            findViewById(R.id.map).setVisibility(View.GONE);
            ImagePagerAdapter mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), solicitacao.getFotos());
            mPager.setAdapter(mAdapter);
            PageIndicator mIndicator = (IconPageIndicator) findViewById(R.id.indicator);
            mIndicator.setViewPager(mPager);
        }

        TextView comentario = (TextView) findViewById(R.id.comentario);
        comentario.setTypeface(FontUtils.getRegular(this));
        comentario.setText(solicitacao.getComentario());

        TextView botaoVoltar = (TextView) findViewById(R.id.botaoVoltar);
        if (alterarLabel) {
            botaoVoltar.setText(R.string.solicitaces_maiusculo);
        }
        botaoVoltar.setTypeface(FontUtils.getRegular(this));
        botaoVoltar.setOnClickListener(v -> finish());

        TextView indicadorStatus = (TextView) findViewById(R.id.indicadorStatus);
        indicadorStatus.setTypeface(FontUtils.getBold(this));
        int fiveDp = (int) ImageUtils.dpToPx(this, 5);
        int tenDp = (int) ImageUtils.dpToPx(this, 10);
        indicadorStatus.setPadding(tenDp, fiveDp, tenDp, fiveDp);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            indicadorStatus.setBackgroundDrawable(ImageUtils.getStatusBackground(this, solicitacao.getStatus().getCor()));
        } else {
            indicadorStatus.setBackground(ImageUtils.getStatusBackground(this, solicitacao.getStatus().getCor()));
        }
        indicadorStatus.setText(solicitacao.getStatus().getNome());

        if (solicitacao.getCategoria().getCategoriaMae() != null) {
            categoryIcon.setImageBitmap(ImageUtils.getScaledCustom(this, "reports",
                    solicitacao.getCategoria().getCategoriaMae().getIconeAtivo(), 0.75f));
            categoryName.setText(solicitacao.getCategoria().getNome());
            subcategoryName.setText(solicitacao.getCategoria().getCategoriaMae().getNome());
        } else {
            categoryIcon.setImageBitmap(ImageUtils.getScaledCustom(this, "reports",
                    solicitacao.getCategoria().getIconeAtivo(), 0.75f));
            categoryName.setText(solicitacao.getCategoria().getNome());
            subcategoryName.setVisibility(View.GONE);
        }

        loadComments();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void loadComments() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .addHeader("X-App-Token", new LoginService().getToken(this))
                        .url(Constantes.REST_URL + String.format("/reports/%d/comments", solicitacao.getId()))
                        .build();
                Response response = ConstantesBase.OK_HTTP_CLIENT.newCall(request).execute();
                if (response.isSuccessful()) {
                    String raw = response.body().string();
                    fillData(new JSONObject(raw).getJSONArray("comments"));
                } else if (response.code() == 401) {
                    AuthHelper.redirectSessionExpired(getApplicationContext());
                }
            } catch (Exception e) {
                Log.e("ZUP", "Couldn't retrieve comments", e);
                if (!BuildConfig.DEBUG) Crashlytics.logException(e);
            }
        }).start();
    }

    private void fillData(JSONArray list) throws Exception {
        final List<ComentarioRelato> comentarios = new ArrayList<>();
        for (int i = 0; i < list.length(); i++) {
            JSONObject json = list.getJSONObject(i);
            if (json.getInt("visibility") == 0 || (json.getInt("visibility") == 1 &&
                    new LoginService().getUserId(this) == solicitacao.getCreatorId())) {
                ComentarioRelato comentario = new ComentarioRelato();
                comentario.setMensagem(json.getString("message"));
                comentario.setCriacao(new DateTime(json.getString("created_at")).toDate());
                comentarios.add(comentario);
            }
        }

        runOnUiThread(() -> buildUi(comentarios));
    }

    private void buildUi(List<ComentarioRelato> comentarios) {
        for (ComentarioRelato comentario : comentarios) {
            View view = getLayoutInflater().inflate(R.layout.item_comentario_relato, commentContainer, false);
            TextView message = ButterKnife.findById(view, R.id.message);
            message.setText(comentario.getMensagem());
            TextView autor = ButterKnife.findById(view, R.id.author);
            autor.setText(String.format("Resposta do município enviada: %s", DateUtils.formatDate(this, comentario.getCriacao())));
            commentContainer.addView(view);
        }
    }

    @Override
    protected String getScreenName() {
        return "Detalhes de um Relato";
    }
}
