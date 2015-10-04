package turing.com.flint;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Profile extends ActionBarActivity implements View.OnClickListener {

    TextView tvFullName,tvUserName,tvDOB,tvGender;
    Button bEmail,bPhone,bFacebook,bTwitter;
    String guemail,profile_pic_url,fname,uname,phone_num,fb,twitter,dob,gender;
    ImageView profile_pic;
    SharedPreferences pref,cache;
    ProgressDialog progress;
    //Context context = getApplicationContext();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setHomeButtonEnabled(true);
        profile_pic = (ImageView) findViewById(R.id.iprofile_pic);
        tvFullName = (TextView) findViewById(R.id.ifn);
        tvUserName = (TextView) findViewById(R.id.iun);
        tvDOB = (TextView) findViewById(R.id.idob);
        tvGender = (TextView) findViewById(R.id.igender);
        bEmail = (Button) findViewById(R.id.iemail);
        bEmail.setOnClickListener(this);
        bPhone = (Button) findViewById(R.id.iphone);
        bPhone.setOnClickListener(this);
        bFacebook = (Button) findViewById(R.id.ifb);
        bFacebook.setOnClickListener(this);
        bTwitter = (Button) findViewById(R.id.itwitter);
        bTwitter.setOnClickListener(this);


        pref =  getApplicationContext().getSharedPreferences("unid", MODE_APPEND);
        cache = getApplicationContext().getSharedPreferences("cache",MODE_APPEND);
        guemail= pref.getString("unid", "null");
        progress = ProgressDialog.show(Profile.this, "Profile",
                "Loading...", true);
        if(isNetworkConnected()){

            new getDetails().execute();
        }else{


            Toast.makeText(getApplicationContext(),"Please connect to the internet",Toast.LENGTH_LONG).show();
            parseJson(cache.getString("cacheprofile","null"));

        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iemail:
                Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + guemail)); // or just "mailto:" for blank
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
                startActivity(intent);
                break;
            case R.id.iphone:
                //int tel = Integer.parseInt(phone_num);
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+phone_num));
                startActivity(callIntent);
                break;
            case R.id.ifb:
                String facebookUrl = "https://www.facebook.com/"+fb;
                try {
                    int versionCode = getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
                    if (versionCode >= 3002850) {
                        Uri uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));;
                    } else {
                        // open the Facebook app using the old method (fb://profile/id or fb://page/id)
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/336227679757310")));
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // Facebook is not installed. Open the browser
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
                }
                break;
            case R.id.itwitter:
                Intent intent1 = null;
                intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"+twitter));

                this.startActivity(intent1);
                break;
        }
    }

    private class getDetails extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://84.200.84.218/flint/getProfileDetails.php");
            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(1);
            nameValuePairList.add(new BasicNameValuePair("guemail", guemail));
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
            Log.d("check", "doing in back done");
            return data;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
           // cache=getApplicationContext().getSharedPreferences("cache",MODE_APPEND);
            SharedPreferences.Editor editor = cache.edit();
            editor.putString("cacheprofile", s);
            editor.commit();

          //  Toast.makeText(getApplicationContext(),cache.getString("cacheprofile","null"),Toast.LENGTH_LONG).show();

            parseJson(s);

        }


    }
    public void parseJson(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            //  Toast.makeText(getApplicationContext(),jsonObject.getString("0"),Toast.LENGTH_LONG).show();
            JSONObject jsonObject1 = new JSONObject(jsonObject.getString("0"));
            // Toast.makeText(getApplicationContext(),jsonObject1.getString("id"),Toast.LENGTH_LONG).show();
            profile_pic_url=jsonObject1.getString("gpic");
            fname=jsonObject1.getString("full_name");;
            uname=jsonObject1.getString("user_name");;
            phone_num=jsonObject1.getString("ph_num");
            fb=jsonObject1.getString("fb");
            twitter=jsonObject1.getString("twitter");
            dob=jsonObject1.getString("bday");
            gender=jsonObject1.getString("gender");
            tvFullName.setText(fname);
            tvUserName.setText(uname);
            tvDOB.setText(dob);
            tvGender.setText(gender);

            //new setImg(profile_pic).execute(profile_pic_url);
            Picasso.with(getApplicationContext()).load(profile_pic_url).error(R.mipmap.ic_launcher).placeholder(R.mipmap.ic_launcher).into(profile_pic);
             progress.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {

            return false;
        } else
            return true;
    }



}
