package br.com.ntxdev.zup.social;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.Toast;
import br.com.ntxdev.zup.domain.Usuario;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;

public class FacebookAuth extends Activity {
	
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setIndeterminate(true);
		dialog.setMessage("Aguarde...");
		dialog.show();

		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		Session session = Session.getActiveSession();
		if (session == null) {
			session = new Session(this);
			Session.setActiveSession(session);
		}

		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this).setPermissions("basic_info", "email").setCallback(callback));
		} else {
			Session.openActiveSession(this, true, callback);
		}
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state, Exception exception) {
			if (session.isOpened()) {
				Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {

					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (user != null) {
							Usuario usuario = new Usuario();
							usuario.setNome(user.getName());
							usuario.setEmail(user.asMap().get("email").toString());

							Intent i = new Intent();
							i.putExtra("usuario", usuario);
							setResult(Activity.RESULT_OK, i);
							
							Session session = Session.getActiveSession();
							Editor editor = getSharedPreferences("facebook-session", Context.MODE_PRIVATE).edit();
							editor.putString("access_token", session.getAccessToken());
							editor.putLong("expires_in", session.getExpirationDate().getTime());
							editor.commit();
							
							SocialUtil.saveSigned(FacebookAuth.this, SocialUtil.FACEBOOK);
							
							dialog.dismiss();
							finish();
						}

						if (response.getError() != null) {
							Toast.makeText(FacebookAuth.this, response.getError().getErrorMessage(), Toast.LENGTH_LONG).show();
						}
					}
				});
				request.executeAsync();
			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
}
