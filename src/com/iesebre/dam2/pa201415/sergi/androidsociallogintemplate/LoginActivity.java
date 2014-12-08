package com.iesebre.dam2.pa201415.sergi.androidsociallogintemplate;

import java.io.InputStream;

import com.facebook.AppEventsLogger;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.iesebre.dam2.pa201415.sergi.R;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class LoginActivity extends FragmentActivity implements 
	OnClickListener,ConnectionCallbacks,OnConnectionFailedListener, LoginActivityFragment.OnFragmentInteractionListener {
	
	// Google client to interact with Google API
	private GoogleApiClient mGoogleApiClient;
	
	private static final int RC_SIGN_IN_GOOGLE = 3889;
	
	private static final String TAG = "LoginActivity";
	
	private boolean mSignInClicked;
	
	// Profile pic image size in pixels
	private static final int PROFILE_PIC_SIZE = 400;

	private static final int REQUEST_CODE_GOOGLE_LOGIN = 91;
	private static final int REQUEST_CODE_FACEBOOK_LOGIN = 92;
	private static final int REQUEST_CODE_TWITTER_LOGIN = 93;
	
	private static final int SOCIAL_LOGIN_GOOGLE = 101;
	private static final int SOCIAL_LOGIN_FACEBOOk = 102;
	private static final int SOCIAL_LOGIN_TWITTER = 103;
	
	private boolean OnStartAlreadyConnected = false;
	
	private int socialLoginType=-1;
	
	//FACEBOOK
	private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    private boolean isResumed = false;
    
    /**
	 * A flag indicating that a PendingIntent is in progress and prevents us
	 * from starting further intents.
	 */
	private boolean mIntentInProgress;
	
	private ConnectionResult mConnectionResult;
	
	private Button btnGoogleSignIn;

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"onCreate!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.login_activitycontainer, new LoginActivityFragment()).commit();
		}
		
		//GOOGLE
		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this).addApi(Plus.API)
		.addScope(Plus.SCOPE_PLUS_LOGIN).build();
		
		//FACEBOOK
		uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		Log.d(TAG,"onSessionStateChange!");
        if (isResumed) {
        	Log.d(TAG,"is resumed!");
            FragmentManager manager = getSupportFragmentManager();
            int backStackSize = manager.getBackStackEntryCount();
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            // check for the OPENED state instead of session.isOpened() since for the
            // OPENED_TOKEN_UPDATED state, the selection fragment should already be showing.
            if (state.equals(SessionState.OPENED)) {
            	Log.d(TAG,"onSessionStateChange: Logged to facebook");
            	Intent i = new Intent(LoginActivity.this, MainActivity.class);
        		startActivityForResult(i, REQUEST_CODE_FACEBOOK_LOGIN);
            } else if (state.isClosed()) {
                //NO logged to facebook
            	Log.d(TAG,"onSessionStateChange: Not logged to facebook");
            }
        }
    }
	
	@Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);
    }
	
	@Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;

        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be launched into.
        AppEventsLogger.deactivateApp(this);
    }
	
	@Override
    protected void onResumeFragments() {
		Log.d(TAG,"onResumeFragments!");
        super.onResumeFragments();
        Session session = Session.getActiveSession();

        if (session != null && session.isOpened()) {
            // if the session is already open, try to show the selection fragment
            
        	//Login ok
        	Log.d(TAG,"Login to facebook Ok!");
        	Intent i = new Intent(LoginActivity.this, MainActivity.class);
    		startActivityForResult(i, REQUEST_CODE_FACEBOOK_LOGIN);
        	
            //userSkippedLogin = false;
        } /*else if (userSkippedLogin) {
            showFragment(SELECTION, false);
        }*/ else {
            // otherwise present the splash screen and ask the user to login, unless the user explicitly skipped.
        	//showFragment(SPLASH, false);
        	Log.d(TAG,"Login to facebook not Ok!");
        }
    }
	
	@Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);

        //outState.putBoolean(USER_SKIPPED_LOGIN_KEY, userSkippedLogin);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		Log.d(TAG, "onActivityResult. RequestCode: " + requestCode + " ResponseCode:" + responseCode + "!");
		super.onActivityResult(requestCode, responseCode, intent);
		
		//Facebook:
		uiHelper.onActivityResult(requestCode, responseCode, intent);
		
		switch (requestCode) {
		case RC_SIGN_IN_GOOGLE:
			if (responseCode != RESULT_OK) {
				mSignInClicked = false;
			}

			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
			break;

		case REQUEST_CODE_GOOGLE_LOGIN:
			Log.d(TAG, "LOGOUT from google!");
			//LOGOUT from google
			if (responseCode == RESULT_OK) {
				Log.d(TAG, "LOGOUT from google withe response code RESULT_OK. Signing Out from Gplus!...");
				//Check if revoke exists!
				Bundle extras = intent.getExtras();
				boolean revoke = false;
				if (extras != null) {
					revoke = extras.getBoolean(AndroidSkeletonUtils.REVOKE_KEY);
				}
				
				if (revoke == true) {
					Log.d(TAG, "LOGOUT and also revoke!...");
					revokeGplusAccess();
				} else {
					Log.d(TAG, "Only LOGOUT (no revoke)...");
					signOutFromGplus();
				}
			}
			break;
		case REQUEST_CODE_FACEBOOK_LOGIN:
			//LOGOUT from facebook
			Log.d(TAG, "LOGOUT from facebook!");
			if (responseCode == RESULT_OK) {
				Log.d(TAG, "LOGOUT from facebook with response code RESULT_OK. Signing Out from facebook!...");
				//Check if revoke exists!
				Bundle extras = intent.getExtras();
				boolean revoke = false;
				if (extras != null) {
					revoke = extras.getBoolean(AndroidSkeletonUtils.REVOKE_KEY);
				}
				
				Session session = Session.getActiveSession();
				
				//TODO
				/*
				 
				  if (session != null) {
						Log.d("Logout","Pasa el if de session : "+session);
				  if (session.isClosed()) {
				  
				 */
				
				if (revoke == true) {
					Log.d(TAG, "LOGOUT and also revoke!...");
					Log.d(TAG, "First revoke...");
					progressDialog = ProgressDialog.show(
				            LoginActivity.this, "", "Revocant permisos...", true);
					new Request(
							   session,
							    "/me/permissions",
							    null,
							    HttpMethod.DELETE,
							    new Request.Callback() {
							        public void onCompleted(Response response) {
							            /* handle the result */
							        	Log.d(TAG, "Revoked. Response:" + response);
							        	progressDialog.dismiss(); 
							        }
							    }
							).executeAsync();
					session.closeAndClearTokenInformation();
				} else {
					Log.d(TAG, "Only LOGOUT (no revoke)...");					
					session.closeAndClearTokenInformation();
				}
			}
		
		default:
			break;
		}
		
		
	}
	
	protected void onStart() {
		Log.d(TAG, "onStart!");
		super.onStart();
		
		if (!this.OnStartAlreadyConnected) {
			//NOT ALREADY CONNECTED: CONNECT!
			Log.d(TAG, "NOT Already connected! Connecting to...");
			mGoogleApiClient.connect();
			Log.d(TAG, "...connected to Google API!");
		} else{
			Log.d(TAG, "Already connected!");
		}
		
	}

	protected void onStop() {
		Log.d(TAG, "onStop!");
		super.onStop();
		/* NOT DISCONNECT! WE will come back after result and OnActivityResult does not executes previously onStart!
		 
			Log.d(TAG, "onStop mGoogleApiClient isConnected. Disconnecting...!");
			mGoogleApiClient.disconnect();
		}*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * Revoking access from google
	 * */
	private void revokeGplusAccess() {
		if (mGoogleApiClient.isConnected()) {
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
					.setResultCallback(new ResultCallback<Status>() {
						@Override
						public void onResult(Status arg0) {
							Log.e(TAG, "User access revoked!");
							mGoogleApiClient.disconnect();
							mGoogleApiClient.connect();
						}

					});
		}
	}
	
	/**
	 * Sign-in into google
	 * */
	private void signInWithGplus() {
		Log.d(TAG, "signInWithGplus!");
		if (!mGoogleApiClient.isConnecting()) {
			Log.d(TAG, "Error signing in!");
			mSignInClicked = true;
			resolveSignInError();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_google_sign_in:
			// Signin button clicked
			signInWithGplus();
			break;
			/*
		case R.id.btn_sign_out:
			// Signout button clicked
			signOutFromGplus();
			break;
		case R.id.btn_revoke_access:
			// Revoke access button clicked
			revokeGplusAccess();
			break;*/
		}
	}
	
	/**
	 * Sign-out from google
	 * */
	private void signOutFromGplus() {
		Log.d(TAG, "signOutFromGplus!");
		if (mGoogleApiClient.isConnected()) {
			Log.d(TAG, "signOutFromGplus is connected then disconnecting!");
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "onConnected!");
		// TODO Auto-generated method stub
		
		mSignInClicked = false;
		
		//Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
		Log.d(TAG, "User is connected!");
		
		this.socialLoginType=SOCIAL_LOGIN_GOOGLE;

		// Get user's information
		getProfileInformation();

		Intent i = new Intent(LoginActivity.this, MainActivity.class);
		startActivityForResult(i, REQUEST_CODE_GOOGLE_LOGIN);
		
		// Update the UI after signin
		//updateUI(true);
		
	}
	
	/**
	 * Fetching user's information name, email, profile pic
	 * */
	private void getProfileInformation() {
		try {
			if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
				Person currentPerson = Plus.PeopleApi
						.getCurrentPerson(mGoogleApiClient);
				String personName = currentPerson.getDisplayName();
				String personPhotoUrl = currentPerson.getImage().getUrl();
				String personGooglePlusProfile = currentPerson.getUrl();
				String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

				Log.d(TAG, "Name: " + personName + ", plusProfile: "
						+ personGooglePlusProfile + ", email: " + email
						+ ", Image: " + personPhotoUrl);

				//TODO: Save to shared preferences?
			

				// by default the profile url gives 50x50 px image only
				// we can replace the value with whatever dimension we want by
				// replacing sz=X
				personPhotoUrl = personPhotoUrl.substring(0,
						personPhotoUrl.length() - 2)
						+ PROFILE_PIC_SIZE;

				//new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);

			} else {
				Toast.makeText(getApplicationContext(),
						"Person information is null", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub
		mGoogleApiClient.connect();
		//updateUI(false);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!result.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					0).show();
			return;
		}

		if (!mIntentInProgress) {
			// Store the ConnectionResult for later usage
			mConnectionResult = result;

			if (mSignInClicked) {
				// The user has already clicked 'sign-in' so we attempt to
				// resolve all
				// errors until the user is signed in, or they cancel.
				resolveSignInError();
			}
		}
	}
	
	/**
	 * Method to resolve any signin errors
	 * */
	private void resolveSignInError() {
		if (mConnectionResult!=null){
			if (mConnectionResult.hasResolution()) {
				try {
					mIntentInProgress = true;
					mConnectionResult.startResolutionForResult(this, RC_SIGN_IN_GOOGLE);
				} catch (SendIntentException e) {
					mIntentInProgress = false;
					mGoogleApiClient.connect();
				}
			}
		}
	}
	
	/**
	 * Background Async task to load user profile picture from url
	 * */
	private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public LoadProfileImage(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
		}
	}

	@Override
	public void onFragmentInteraction(Uri uri) {
		// TODO Auto-generated method stub
		
	}

}
