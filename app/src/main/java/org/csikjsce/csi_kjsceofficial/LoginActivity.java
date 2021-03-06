package org.csikjsce.csi_kjsceofficial;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener,ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGNIN = 007;
    private GoogleApiClient mGoogleApiClient;
    private SignInButton btnSignin;
    private ConstraintLayout parentLayout;

    private ProgressDialog mProgressDialog;
    Context context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        try {
            saveLogcatToFile(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        parentLayout = findViewById(R.id.login_parent_layout);
        btnSignin = (SignInButton)findViewById(R.id.btn_sign_in);
        btnSignin.setOnClickListener(this);

        checkConnection();

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }
    private void showSnack(boolean isConnected) {
        String message = "";
        int color= Color.RED;

        if (isConnected) {
            //do nothing
        } else {
            message = getString(R.string.msg_no_internet);

            Snackbar snackbar = Snackbar
                    .make(parentLayout, message, Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
    }

    @Override
    protected void onResume() {
       super.onResume();
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }
    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGNIN);
    }

    private void handleSignInResult(GoogleSignInResult result){

        Log.d(TAG,"handleSignInResult():"+result.isSuccess());
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            String pname = account.getDisplayName();
            String emailid = account.getEmail();
            Uri pic_uri = account.getPhotoUrl();
            String pic_url;
            if(pic_uri==null)
                pic_url = "default";
            else pic_url = pic_uri.toString();
            SharedPreferences userInfo = context.getSharedPreferences(getString(R.string.USER_INFO),Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = userInfo.edit();
            editor.putString(getString(R.string.pref_key_name),pname);
            if(emailid.contains(getString(R.string.somaiya_edu))) {
                editor.putString(getString(R.string.pref_key_svv_mail), emailid);
                editor.putBoolean(getString(R.string.pref_key_signed_in_with_svv), true);
            }
            if(!emailid.contains(getString(R.string.somaiya_edu))) {
                editor.putString(getString(R.string.pref_key_email), emailid);
                editor.putBoolean(getString(R.string.pref_key_signed_in_with_svv), false);
            }
            editor.putString(getString(R.string.pref_key_pic_url),pic_url);


            editor.commit();
            updateUI(true);
        } else {
            updateUI(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_sign_in:
                signIn();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGNIN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if(pendingResult.isDone()){
            Log.d(TAG,"onStart(): Cached sign in");
            GoogleSignInResult result = pendingResult.get();
            handleSignInResult(result);
        } else {
            showProgressDialog();
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG,"onConnectionFailed: "+connectionResult);
    }
    private void updateUI(boolean isSignedIn){
        boolean profileComplete = Utils.isProfileComplete(this);
        Intent intent;
        if(isSignedIn){
            if(profileComplete)
                intent = new Intent(this, MainActivity.class);
            else {
                intent = new Intent(this, ProfileActivity.class);
                intent.putExtra(getString(R.string.pref_key_edit_mode),true);
                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
                Toast.makeText(this, R.string.kindly_complete_your_profile,Toast.LENGTH_SHORT)
                        .show();
            }
            startActivity(intent);
        } else {
            Snackbar.make(parentLayout, R.string.not_signed_in,Snackbar.LENGTH_LONG)
                    .show();
        }
    }
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.authenticating_));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }
    public static void saveLogcatToFile(Context context) throws IOException {
        String fileName = "logcat_"+System.currentTimeMillis()+".txt";
        File outputFile = new File(context.getExternalCacheDir(),fileName);
        @SuppressWarnings("unused")
        Process process = Runtime.getRuntime().exec("logcat -df "+outputFile.getAbsolutePath());
    }
}
