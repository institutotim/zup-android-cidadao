package br.com.ntxdev.zup;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.SessionSocialNetwork;
import br.com.ntxdex.zup.twitter.TwitterApp;
import br.com.ntxdex.zup.twitter.TwitterSession;
import br.com.ntxdex.zup.twitter.TwitterApp.TwDialogListener;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;

public class CadastroActivity extends Activity implements OnClickListener {

	private Button botaoCancelar;
	private Button botaoCriar;

	private EditText campoNome;
	private EditText campoEmail;
	private EditText campoSenha;
	private EditText campoConfirmarSenha;
	private EditText campoCPF;
	private EditText campoTelefone;
	private EditText campoEndereco;
	private EditText campoComplemento;
	private EditText campoCEP;
	private EditText campoBairro;
	
	private SessionSocialNetwork sessionSocialNetwork;

	// Integração Facebook
	private ImageButton buttonLoginFacebook;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	// Integração Twitter
	private TwitterApp mTwitter;
	private TwitterSession mSession;
	ImageButton mBtnTwitter;
	private static final String CONSUMER_KEY = "6JyIkj71ZqG4wk3YF0Y4hw";
	private static final String CONSUMER_SECRET = "sJl9aRVqlEt7nxlKvpMVK6tLULz5FSQ2KUOW0yie4";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cadastro);

		((TextView) findViewById(R.id.novaConta)).setTypeface(FontUtils
				.getLight(this));
		((TextView) findViewById(R.id.textView1)).setTypeface(FontUtils
				.getLight(this));
		((TextView) findViewById(R.id.textView2)).setTypeface(FontUtils
				.getLight(this));

		botaoCancelar = (Button) findViewById(R.id.botaoCancelar);
		botaoCancelar.setTypeface(FontUtils.getRegular(this));
		botaoCancelar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();				
			}
		});
		botaoCriar = (Button) findViewById(R.id.botaoCriar);
		botaoCriar.setTypeface(FontUtils.getRegular(this));

		campoNome = (EditText) findViewById(R.id.campoNome);
		campoNome.setTypeface(FontUtils.getLight(this));

		campoSenha = (EditText) findViewById(R.id.campoSenha);
		campoSenha.setTypeface(FontUtils.getLight(this));

		campoConfirmarSenha = (EditText) findViewById(R.id.campoConfirmarSenha);
		campoConfirmarSenha.setTypeface(FontUtils.getLight(this));

		campoEmail = (EditText) findViewById(R.id.campoEmail);
		campoEmail.setTypeface(FontUtils.getLight(this));
		
		campoCPF = (EditText) findViewById(R.id.campoCPF);
		campoCPF.setTypeface(FontUtils.getLight(this));
		
		campoTelefone = (EditText) findViewById(R.id.campoTelefone);
		campoTelefone.setTypeface(FontUtils.getLight(this));
		
		campoEndereco = (EditText) findViewById(R.id.campoEndereco);
		campoEndereco.setTypeface(FontUtils.getLight(this));
		
		campoComplemento = (EditText) findViewById(R.id.campoComplemento);
		campoComplemento.setTypeface(FontUtils.getLight(this));
		
		campoCEP = (EditText) findViewById(R.id.campoCEP);
		campoCEP.setTypeface(FontUtils.getLight(this));
		
		campoBairro = (EditText) findViewById(R.id.campoBairro);
		campoBairro.setTypeface(FontUtils.getLight(this));
		
		sessionSocialNetwork = SessionSocialNetwork.getInstance();		

		buttonLoginFacebook = (ImageButton) findViewById(R.id.botao_logar_facebook);
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session sessionFacebook = Session.getActiveSession();
		if (sessionFacebook == null) {
			if (savedInstanceState != null) {
				sessionFacebook = Session.restoreSession(this, null,
						statusCallback, savedInstanceState);
			}
			if (sessionFacebook == null) {
				sessionFacebook = new Session(this);
			}
			Session.setActiveSession(sessionFacebook);
			if (sessionFacebook.getState().equals(
					SessionState.CREATED_TOKEN_LOADED)) {
				sessionFacebook.openForRead(new Session.OpenRequest(this)
						.setCallback(statusCallback).setPermissions(
								Arrays.asList("email")));
			}
		}

		updateViewFacebook();

		ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll()
				.build();
		StrictMode.setThreadPolicy(policy);

		mTwitter = new TwitterApp(this, CONSUMER_KEY, CONSUMER_SECRET);
		mBtnTwitter = (ImageButton) findViewById(R.id.botao_logar_twitter);
		mBtnTwitter.setOnClickListener(this);
	}

	// Integra�‹o Facebook
	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	private void updateViewFacebook() {
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			getProfileInformation(session);
			buttonLoginFacebook.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogout();
				}
			});
		} else {
			buttonLoginFacebook.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogin();
				}
			});
		}
	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
					.setCallback(statusCallback));
		} else {
			Session.openActiveSession(this, true, statusCallback);
		}
	}

	private void onClickLogout() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			updateViewFacebook();
		}
	}

	private void getProfileInformation(final Session session) {
		// Make an API call to get user data and define a
		// new callback to handle the response.
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						// If the response is successful
						if (session == Session.getActiveSession()) {
							if (user != null) {
								campoNome.setText(user.getName());
								campoEmail.setText(user.asMap().get("email").toString());
								sessionSocialNetwork.setNome(user.getName());
								sessionSocialNetwork.setEmail(user.asMap().get("email").toString());
								sessionSocialNetwork.setSessionFacebook(session);
							}
						}
						if (response.getError() != null) {
							// Handle errors, will do so later.
						}
					}
				});
		request.executeAsync();
	}

	// Integração Twitter
	private enum FROM {
		TWITTER_POST, TWITTER_LOGIN
	};

	private enum MESSAGE {
		SUCCESS, DUPLICATE, FAILED, CANCELLED
	}

	@Override
	public void onClick(View v) {
		mTwitter.setListener(mTwLoginDialogListener);
		mTwitter.resetAccessToken();
		if (mTwitter.hasAccessToken() == true) {
			try {
				mTwitter.updateStatus(String.valueOf(Html
						.fromHtml(TwitterApp.MESSAGE)));
				campoNome.setText(mTwitter.getNomeUsuario());
			} catch (Exception e) {
				e.printStackTrace();
			}
			mTwitter.resetAccessToken();
		} else {
			mTwitter.authorize();
		}
	}

	private TwDialogListener mTwLoginDialogListener = new TwDialogListener() {

		public void onError(String value) {
			Log.e("TWITTER", value);
			mTwitter.resetAccessToken();
		}

		public void onComplete(String value) {
			//Mostra o campo nome do Twitter no formul�rio
			String nomeUsuario = mTwitter.getNomeUsuario();
			sessionSocialNetwork.setNome(nomeUsuario);
			sessionSocialNetwork.setSessionTwitter(mTwitter);
			campoNome.setText(nomeUsuario);
			try {
				//mTwitter.updateStatus(TwitterApp.MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//mTwitter.resetAccessToken();
		}
	};
}
