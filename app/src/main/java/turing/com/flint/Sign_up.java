package turing.com.flint;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Presentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class Sign_up extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    EditText  uname,phnum,etfb,ettwitter;
    String suname="",snum="",fb=" ",twitter=" ",guname,guemail,guserProfilePicUrl;
    private RadioGroup radioSexGroup;
    private RadioButton radioSexButton;
    private Button btnDisplay;
    private int myear;
    private int mmonth;
    private int mday;
    String bday;
    String gender;
    ProgressDialog progress;

    static final int DATE_PICKER_ID = 1111;
    SharedPreferences pref,prefAct;

    private boolean mSignedInClicked,mIntentInProgress;
    private ConnectionResult mconnectionResult;

    private GoogleApiClient mGoogleApiClient;
    private static final int SIGN_IN_REQUEST_CODE = 10;
    private static final int ERROR_DIALOG_REQUEST_CODE = 11;






    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        uname = (EditText) findViewById(R.id.etun);
        phnum = (EditText) findViewById(R.id.editText2);
        etfb = (EditText) findViewById(R.id.ifb);
        ettwitter = (EditText) findViewById(R.id.itwitter);
        prefAct = getApplicationContext().getSharedPreferences("reg", MODE_PRIVATE);
        final Calendar c = Calendar.getInstance();
        myear  = c.get(Calendar.YEAR);
        mmonth = c.get(Calendar.MONTH);
        mday   = c.get(Calendar.DAY_OF_MONTH);


        mGoogleApiClient = buildGoogleAPIClient();


        if (!isNetworkConnected()) {
            Toast.makeText(getApplicationContext(), "Please connect to the internet", Toast.LENGTH_LONG).show();
        }


         findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (!isNetworkConnected()) {
                     Toast.makeText(getApplicationContext(), "Please connect to the internet", Toast.LENGTH_LONG).show();
                 } else {

                    // Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_LONG).show();
                     InputMethodManager inputManager = (InputMethodManager)
                             getSystemService(Context.INPUT_METHOD_SERVICE);

                     inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                             InputMethodManager.HIDE_NOT_ALWAYS);

                     progress = ProgressDialog.show(Sign_up.this, "Sign up",
                             "Loading...", true);


                     getDetails();

                     if(suname.equals("") || snum.equals("")){
                         Toast.makeText(getApplicationContext(),"Please enter all the details",Toast.LENGTH_LONG).show();

                     }else{
                         new checkuname().execute();
                     }

                 }
             }



         });

    }



    private GoogleApiClient buildGoogleAPIClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }




    public void getDetails(){


        suname = uname.getText().toString();
        snum = phnum.getText().toString();
        fb = etfb.getText().toString();
        twitter=ettwitter.getText().toString();


    }
    /*CHECK NETWORK FUNCTION*/
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {

            return false;
        } else
            return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

    @Override
    public void onConnected(Bundle bundle) {
        mSignedInClicked=false;
      //  Toast.makeText(getApplicationContext(),"Signed in successfully",Toast.LENGTH_LONG).show();

        setGResult();

    }

    private void setGResult() {
        Person signedInUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

        if(signedInUser!=null){
            if(signedInUser.hasDisplayName()){
                guname = signedInUser.getDisplayName();

            }

            guemail=Plus.AccountApi.getAccountName(mGoogleApiClient);

           // Toast.makeText(getApplicationContext(),guemail,Toast.LENGTH_LONG).show();
            if(signedInUser.hasImage()){
                String[] profileparts=signedInUser.getImage().getUrl().split("\\?");
                guserProfilePicUrl = profileparts[0];





            }
            if(signedInUser.hasBirthday()){
                bday= signedInUser.getBirthday();
                Toast.makeText(getApplicationContext(),bday,Toast.LENGTH_LONG).show();

            }
            if(signedInUser.hasGender()){
                gender= String.valueOf(signedInUser.getGender());
                Toast.makeText(getApplicationContext(),gender,Toast.LENGTH_LONG).show();

            }
            pref = getApplicationContext().getSharedPreferences("unid",MODE_APPEND);
            SharedPreferences.Editor editor= pref.edit();
            editor.putString("unid",guemail);
            editor.commit();


            new Makerqust().execute();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(!connectionResult.hasResolution()){
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, ERROR_DIALOG_REQUEST_CODE).show();
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

    private void processSignIn() {
        Log.d("g sign","process sign in");
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

    private class Makerqust extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://84.200.84.218/flint/signup.php");

            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(9);

            nameValuePairList.add(new BasicNameValuePair("uname", suname));
            nameValuePairList.add(new BasicNameValuePair("uph", snum));
            nameValuePairList.add(new BasicNameValuePair("gender", gender));
            nameValuePairList.add(new BasicNameValuePair("dob", bday));
            nameValuePairList.add(new BasicNameValuePair("guname", guname));
            nameValuePairList.add(new BasicNameValuePair("gemail", guemail));
            nameValuePairList.add(new BasicNameValuePair("gpic", guserProfilePicUrl));
            nameValuePairList.add(new BasicNameValuePair("fb", fb));
            nameValuePairList.add(new BasicNameValuePair("twitter", twitter));



            HttpResponse httpResponse = null;
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                httpResponse = httpClient.execute(httpPost);
                Log.d("response:", httpResponse.toString());
                // Toast.makeText(getApplicationContext(),httpResponse.toString(),Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String data = null;
            HttpEntity ent=httpResponse.getEntity();
            try {
                data = EntityUtils.toString(ent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);



            if(!s.equals("false")){

                SharedPreferences.Editor editor1 = prefAct.edit();
                editor1.putBoolean("signup", true);
                editor1.commit();
                progress.dismiss();
               Toast.makeText(getApplicationContext(),"Signup complete, Welcome to Flint!",Toast.LENGTH_LONG).show();
startActivity(new Intent(Sign_up.this, Contact_feed.class));
                finish();
            }else
            {
                Toast.makeText(getApplicationContext(),"Some thing went wrong",Toast.LENGTH_LONG).show();
            }
        }
    }

    private class checkuname extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... params) {
            Log.d("check","doing in back");
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://84.200.84.218/flint/checkuname.php");
            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(1);
            nameValuePairList.add(new BasicNameValuePair("uname", suname));
            HttpResponse httpResponse = null;
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                httpResponse = httpClient.execute(httpPost);
                Log.d("response:", httpResponse.toString());
                // Toast.makeText(getApplicationContext(),httpResponse.toString(),Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String data = null;
            HttpEntity ent=httpResponse.getEntity();
            try {
                data = EntityUtils.toString(ent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("check","doing in back done");
            return data;


        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("check", "post exec");
            if(!s.equals("false")){
               // findViewById(R.id.sign_in_button).setEnabled(true);
                processSignIn();

            }else{
                Toast.makeText(getApplicationContext(),"User name aready taken",Toast.LENGTH_SHORT).show();
            }
            //Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
        }
    }
}