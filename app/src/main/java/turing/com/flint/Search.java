package turing.com.flint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Search extends ActionBarActivity {
    private GoogleApiClient mGoogleApiClient;
    private static final int SIGN_IN_REQUEST_CODE = 10;
    private boolean mSignedInClicked,mIntentInProgress;
    private ConnectionResult mconnectionResult;
    String guemail;
    ImageView barcode;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    Button scan;
    SharedPreferences pref,cache;
    ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setHomeButtonEnabled(true);
        barcode = (ImageView) findViewById(R.id.ibarcode);
        scan = (Button) findViewById(R.id.iscan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQR(v);

            }
        });

        pref = getApplicationContext().getSharedPreferences("unid",MODE_APPEND);

        cache = getApplicationContext().getSharedPreferences("cache", MODE_APPEND);
        SharedPreferences.Editor editor = cache.edit();

        guemail=pref.getString("unid", "null");
        String url = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data="+guemail;
       // new setBarcode().execute(url);

            if(isNetworkConnected()){
                progress = ProgressDialog.show(Search.this, "QR Code",
                        "Loading...", true);


                Picasso.with(getApplicationContext()).load(url).error(R.mipmap.ic_launcher).placeholder(R.mipmap.ic_launcher).into(barcode);

                progress.dismiss();
            }else{
                Picasso.with(getApplicationContext()).load(url).error(R.mipmap.ic_launcher).placeholder(R.mipmap.ic_launcher).into(barcode);
                Toast.makeText(getApplicationContext(),"Please connect to the internet",Toast.LENGTH_LONG).show();

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



    //product barcode mode
    public void scanBar(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(Search.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //product qr code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(Search.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }
    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
              // Toast toast = Toast.makeText(this, contents, Toast.LENGTH_LONG);
               // toast.show();
                Intent i = new Intent(Search.this, ProfileAfterScan.class);
                i.putExtra("email", contents);
                i.putExtra("from","search");
                if(isNetworkConnected()){
                    startActivity(i);

                }else {
                    Toast.makeText(getApplicationContext(),"Please connect to the internet",Toast.LENGTH_LONG).show();
                }

            }
        }
    }


}