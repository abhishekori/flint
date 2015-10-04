package turing.com.flint;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity  {
    SharedPreferences pref;

    ImageView improfile;
    TextView tvname,tvemail;

private boolean mSignedInClicked,mIntentInProgress;
    private ConnectionResult mconnectionResult;

    private GoogleApiClient mGoogleApiClient;
    private static final int SIGN_IN_REQUEST_CODE = 10;
    private static final int ERROR_DIALOG_REQUEST_CODE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getApplicationContext().getSharedPreferences("reg", MODE_APPEND);


//         findViewById(R.id.sign_in_button).setOnClickListener(this);

        // SharedPreferences.Editor editor=pref.edit();

      /*  if(pref.getInt("page",-1)==-1){
            startActivity(new Intent(MainActivity.this,Sign_up.class));
        }
        if(pref.getInt("page",-1)==0){
            startActivity(new Intent(MainActivity.this,Full_details.class));
        }
        if(pref.getInt("page",-1)==1){
            startActivity(new Intent(MainActivity.this,Contact_feed.class));
        }*/
       if(!pref.getBoolean("signup",false)){
           startActivity(new Intent(MainActivity.this,Sign_up.class));
       }else{
           startActivity(new Intent(MainActivity.this,Contact_feed.class));
       }
        finish();
//mGoogleApiClient = buildGoogleAPIClient();

            }

  /*  private GoogleApiClient buildGoogleAPIClient() {

        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.sign_in_button){
            processSignIn();


        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                mSignedInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }

        }
    }
    private void processSignIn() {

        if(!mGoogleApiClient.isConnecting()){
            processSignInError();
            mSignedInClicked=true;

        }
    }
    private void processSignInError() {
        if(mconnectionResult!=null && mconnectionResult.hasResolution()){

            try {
                mIntentInProgress = true;
                mconnectionResult.startResolutionForResult(this,SIGN_IN_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {

                mIntentInProgress=false;
                mGoogleApiClient.connect();
            }
        }

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(!connectionResult.hasResolution()){
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(),this,ERROR_DIALOG_REQUEST_CODE).show();
            return;
        }
        if(!mIntentInProgress)
        {
            mconnectionResult=connectionResult;
            if(mSignedInClicked){
                processSignInError();
            }
        }


    }
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }



    @Override
    public void onConnected(Bundle bundle) {
       *//* mSignedInClicked=false;
        Toast.makeText(getApplicationContext(),"Signed in successfully",Toast.LENGTH_LONG).show();

        setGResult();*//*
        Log.d("check","onConnected");
       // startActivity(new Intent(MainActivity.this,Contact_feed.class));

    }

    private void setGResult() {

        Person signedInUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

        if(signedInUser!=null){
            if(signedInUser.hasDisplayName()){
                String uname = signedInUser.getDisplayName();
                this.tvname.setText(uname);
                Toast.makeText(getApplicationContext(),uname,Toast.LENGTH_LONG).show();
            }

           String uemail=Plus.AccountApi.getAccountName(mGoogleApiClient);

            Toast.makeText(getApplicationContext(),uemail,Toast.LENGTH_LONG).show();
            if(signedInUser.hasImage()){
                String userProfilePicUrl=signedInUser.getImage().getUrl();
//Toast.makeText(getApplicationContext(),userProfilePicUrl,Toast.LENGTH_LONG).show();
                this.tvemail.setText(userProfilePicUrl);
                int profilePicRequestSize = 250;
                userProfilePicUrl = userProfilePicUrl.substring(0,userProfilePicUrl.length()-2)+profilePicRequestSize;
                new setImg(improfile).execute(userProfilePicUrl);

            }
        }
    }


private class setImg extends AsyncTask<String,Void,Bitmap>{

    WeakReference profileView;



    public setImg(ImageView img){
        profileView = new WeakReference(img);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap profilePic = null;
        try {
            URL downloadURL = new URL(params[0]);
            HttpURLConnection conn = (HttpURLConnection) downloadURL
                    .openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200)
                throw new Exception("Error in connection");
            InputStream is = conn.getInputStream();
            profilePic = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profilePic;
    }
    @Override
    protected void onPostExecute(Bitmap result) {
        // TODO Auto-generated method stub
        Toast.makeText(getApplicationContext(),"on post execute",Toast.LENGTH_LONG).show();
        if (result != null && profileView != null) {
            ImageView view = (ImageView) profileView.get();
            if (view != null)
                view.setImageBitmap(result);
        }
    }
}

*/

}
