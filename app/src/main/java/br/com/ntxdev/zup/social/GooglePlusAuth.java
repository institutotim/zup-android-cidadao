package br.com.ntxdev.zup.social;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.widget.Toast;
import br.com.ntxdev.zup.domain.Usuario;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;

public class GooglePlusAuth extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {

	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

	private ProgressDialog mConnectionProgressDialog;
	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPlusClient = new PlusClient.Builder(this, this, this)
				.setActions("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
				.setScopes(Scopes.PLUS_LOGIN)				
				.build();
		
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Signing in...");
		mConnectionProgressDialog.show();
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        mPlusClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlusClient.disconnect();
    }


	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (mConnectionProgressDialog.isShowing()) {
			if (result.hasResolution()) {
				try {
					result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
				} catch (SendIntentException e) {
					mPlusClient.connect();
				}
			}
		}

		mConnectionResult = result;
		Toast.makeText(this, "Error " + mConnectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		mConnectionProgressDialog.dismiss();
		
		if (mPlusClient.getCurrentPerson() != null) {
	        Person currentPerson = mPlusClient.getCurrentPerson();
	        
	        Usuario usuario = new Usuario();
	        usuario.setEmail(mPlusClient.getAccountName());
	        usuario.setNome(currentPerson.getDisplayName());
	        Intent i = new Intent();
	        i.putExtra("usuario", usuario);
	        setResult(Activity.RESULT_OK, i);
	        
	        SocialUtil.saveSigned(this, SocialUtil.GOOGLE_PLUS);
	        
	        finish();
	    }        
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Something gets wrong...", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
			mConnectionResult = null;
			mPlusClient.connect();
		}
	}
}
