package br.com.ntxdev.zup.fragment;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.SoliciteActivity;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.SessionSocialNetwork;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;

public class SoliciteDetalhesFragment extends Fragment implements
		View.OnClickListener {

	private boolean publicar = false;
	private SessionSocialNetwork sessaoRedeSocial;

	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		((SoliciteActivity) getActivity())
				.setInfo(R.string.concluir_solicitacao);

		View view = inflater.inflate(R.layout.fragment_solicite_detalhes,
				container, false);
		view.findViewById(R.id.seletor_postagem).setOnClickListener(this);

		TextView comentario = (TextView) view.findViewById(R.id.comentario);
		comentario.setTypeface(FontUtils.getRegular(getActivity()));
		comentario
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						((SoliciteActivity) getActivity()).setComentario(v
								.getText().toString());
						return false;
					}
				});

		TextView redeSocial = (TextView) view.findViewById(R.id.redeSocial);
		redeSocial.setText(getString(R.string.compartilhar_rede_social,
				"Facebook"));
		redeSocial.setTypeface(FontUtils.getLight(getActivity()));

		sessaoRedeSocial = SessionSocialNetwork.getInstance();
		publicarFacebook(savedInstanceState);
		publicarTwitter();

		return view;
	}

	@Override
	public void onClick(View v) {
		publicar = !publicar;

		if (publicar) {
			((ImageView) v).setImageResource(R.drawable.switch_on);
		} else {
			((ImageView) v).setImageResource(R.drawable.switch_off);
		}
	}

	private void publicarFacebook(Bundle savedInstanceState) {
		Session session = Session.getActiveSession();

		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(getActivity()
						.getApplicationContext(), null, statusCallback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(getActivity().getApplicationContext());
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this).setCallback(
						statusCallback).setPermissions(Arrays.asList("email")));
			}
		}else{
			// Check for publish permissions
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				pendingPublishReauthorization = true;
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
						getActivity(), PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);
				return;
			}

			Bundle postParams = new Bundle();
			postParams.putString("name", "Nome do App");
			postParams.putString("caption", "Manchete");
			postParams.putString("description", "Descrição do relato");
			postParams.putString("link",
					"http://www.terabytetecnologia.com");
			postParams
					.putString("picture",
							"http://www.terabytetecnologia.com/css/images/logo.png");

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					JSONObject graphResponse = response.getGraphObject()
							.getInnerJSONObject();
					String postId = null;
					try {
						postId = graphResponse.getString("id");
					} catch (JSONException e) {
						Log.i("SoliciteDetalhe",
								"JSON error " + e.getMessage());
					}
					FacebookRequestError error = response.getError();
					if (error != null) {
						Toast.makeText(
								getActivity().getApplicationContext(),
								error.getErrorMessage(), Toast.LENGTH_SHORT)
								.show();
					} else {
						Toast.makeText(
								getActivity().getApplicationContext(),
								postId, Toast.LENGTH_LONG).show();
					}
				}
			};

			Request request = new Request(session, "me/feed", postParams,
					HttpMethod.POST, callback);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}

	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (session != null) {

				// Check for publish permissions
				List<String> permissions = session.getPermissions();
				if (!isSubsetOf(PERMISSIONS, permissions)) {
					pendingPublishReauthorization = true;
					Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
							getActivity(), PERMISSIONS);
					session.requestNewPublishPermissions(newPermissionsRequest);
					return;
				}

				Bundle postParams = new Bundle();
				postParams.putString("name", "Nome do App");
				postParams.putString("caption", "Manchete");
				postParams.putString("description", "Descrição do relato");
				postParams.putString("link",
						"http://www.terabytetecnologia.com");
				postParams
						.putString("picture",
								"http://www.terabytetecnologia.com/css/images/logo.png");

				Request.Callback callback = new Request.Callback() {
					public void onCompleted(Response response) {
						JSONObject graphResponse = response.getGraphObject()
								.getInnerJSONObject();
						String postId = null;
						try {
							postId = graphResponse.getString("id");
						} catch (JSONException e) {
							Log.i("SoliciteDetalhe",
									"JSON error " + e.getMessage());
						}
						FacebookRequestError error = response.getError();
						if (error != null) {
							Toast.makeText(
									getActivity().getApplicationContext(),
									error.getErrorMessage(), Toast.LENGTH_SHORT)
									.show();
						} else {
							Toast.makeText(
									getActivity().getApplicationContext(),
									postId, Toast.LENGTH_LONG).show();
						}
					}
				};

				Request request = new Request(session, "me/feed", postParams,
						HttpMethod.POST, callback);

				RequestAsyncTask task = new RequestAsyncTask(request);
				task.execute();
			}
		}
	}

	private boolean isSubsetOf(Collection<String> subset,
			Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}

	private void publicarTwitter() {
		if (sessaoRedeSocial.getSessionTwitter() != null) {
			try {
				sessaoRedeSocial.getSessionTwitter().updateStatus(
						"Descrição do relato (Teste)");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void publicarGooglePlus() {

	}
}
