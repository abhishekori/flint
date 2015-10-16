package turing.com.flint;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Contact_feed extends ActionBarActivity {
    List<FeedList> feedItemList = new ArrayList<FeedList>();
    RecyclerView recyclerView;
    MyAdap adap;
    SharedPreferences pref,cache;
    ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_feed);
        recyclerView = (RecyclerView) findViewById(R.id.irecycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(isNetworkConnected()){
            progress = ProgressDialog.show(Contact_feed.this, "Your feed",
                    "Loading...", true);

            new getFreinds().execute();
        }else{
            cache = getApplicationContext().getSharedPreferences("cache",MODE_APPEND);
            Toast.makeText(getApplicationContext(),"Please connect to the internet",Toast.LENGTH_LONG).show();


            parseData(cache.getString("feed", "null"));


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


    public class FeedList {
        String fname, uname, thumb,email;


        public void setFname(String fname) {
            this.fname = fname;
        }

        public void setUname(String uname) {
            this.uname = uname;
        }

        public void setThumb(String thumb) {
            this.thumb = thumb;
        }

        public void setEmail(String email){ this.email = email;}
        public String getFname() {
            return fname;
        }

        public String getUname() {
            return uname;
        }

        public String getThumb() {
            return thumb;
        }

        public String getEmail(){return email;}

    }


    public class FeedListRowHolder extends RecyclerView.ViewHolder {
        TextView fname, uname;
        ImageView thumb;

        public FeedListRowHolder(View itemView) {
            super(itemView);
            this.thumb = (ImageView) itemView.findViewById(R.id.ithumb);
            this.fname = (TextView) itemView.findViewById(R.id.ititle);
            this.uname = (TextView) itemView.findViewById(R.id.iuname);
        }
    }

    public class MyAdap extends RecyclerView.Adapter<FeedListRowHolder> {

        List<FeedList> feedlist;
        Context context;

        public MyAdap(Context context, List<FeedList> feedlist) {
            this.feedlist = feedlist;
            this.context = context;
        }


        @Override
        public FeedListRowHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row, null);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int itemPosition = recyclerView.getChildPosition(v);
                 //   Toast.makeText(getApplicationContext(), feedlist.get(itemPosition).getEmail(),Toast.LENGTH_LONG).show();
                    Intent i = new Intent(Contact_feed.this, ProfileAfterScan.class);
                    i.putExtra("email", feedlist.get(itemPosition).getEmail());
                    i.putExtra("from","feed");
                    if(isNetworkConnected()){
                        startActivity(i);

                    }else {
                        Toast.makeText(getApplicationContext(),"Please connect to the internet",Toast.LENGTH_LONG).show();
                    }
                }
            });


            return new FeedListRowHolder(v);
        }

        @Override
        public void onBindViewHolder(FeedListRowHolder holder, int position) {
            FeedList feed = feedlist.get(position);
            Picasso.with(context).load(feed.getThumb()).error(R.mipmap.ic_launcher).placeholder(R.mipmap.ic_launcher).into(holder.thumb);
            holder.fname.setText(feed.getFname());
            holder.uname.setText(feed.getUname());
        }

        @Override
        public int getItemCount() {
            return (null != feedlist ? feedlist.size() : 0);
        }
    }

    private class getFreinds extends AsyncTask<String, Integer, Integer> {
        Integer OK = 0;

        @Override
        protected Integer doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            pref = getApplicationContext().getSharedPreferences("unid",MODE_APPEND);
            HttpPost httpPost = new HttpPost("http://84.200.84.218/flint/getConnections.php");
            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(1);
            nameValuePairList.add(new BasicNameValuePair("email", pref.getString("unid","null")));

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
            progress.dismiss();
            HttpEntity ent = httpResponse.getEntity();
            try {
                data = EntityUtils.toString(ent);
                cache = getApplicationContext().getSharedPreferences("cache",MODE_APPEND);
                SharedPreferences.Editor editor = cache.edit();
                editor.putString("feed",data);
                editor.commit();
                parseData(data);

                OK=1;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return OK;
        }

        @Override
        protected void onPostExecute(Integer s) {
            super.onPostExecute(s);
            if(s==1){
                adap = new MyAdap(Contact_feed.this,feedItemList);
                recyclerView.setAdapter(adap);
            }

        }
    }

    private void parseData(String data) {
        try {
            Log.d("parse",data);
           JSONArray jarr= new JSONArray(data);
            Log.d("obj",jarr.toString());
            for(int i=0;i<jarr.length();i++){
                JSONObject jobj = jarr.getJSONObject(i);
                String fulln=jobj.getString("full_name");
                //Log.d("obj",fulln);
                String uname=jobj.getString("user_name");
                String img=jobj.getString("gpic");
                String uemail = jobj.getString("email");
                FeedList fitem = new FeedList();
                fitem.setFname(fulln);
                fitem.setUname(uname);
                fitem.setThumb(img);
                fitem.setEmail(uemail);
                feedItemList.add(fitem);


                //Log.d("obj",img);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_feed, menu);
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
        if(id==R.id.pro_icon){
            startActivity(new Intent(Contact_feed.this,Profile.class));
        }
        if(id==R.id.addu_icon){
            startActivity(new Intent(Contact_feed.this,Search.class));
        }

        return super.onOptionsItemSelected(item);
    }


}
