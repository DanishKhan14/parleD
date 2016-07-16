package com.example.gkhandel.parlegtest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import com.example.gkhandel.parlegtest.models.Task;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "LJKLJ";

    private Button button;
    private EditText nameEdit;
    private EditText descEdit;
    private EditText longEdit;
    private EditText latEdit;
    private EditText radEdit;
    private EditText stEdit;
    private EditText endEdit;
    private EditText priorityEdit;
    private Task newTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListenerOnButton();
    }

    public void addListenerOnButton() {

        button = (Button) findViewById(R.id.addTask);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                //Intent browserIntent =
                //        new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/"));
                //startActivity(browserIntent);
                nameEdit = (EditText) findViewById(R.id.editName);
                descEdit = (EditText) findViewById(R.id.editDesc);
                longEdit = (EditText) findViewById(R.id.editLong);
                latEdit = (EditText) findViewById(R.id.editLat);
                radEdit = (EditText) findViewById(R.id.editRadius);
                stEdit = (EditText) findViewById(R.id.editStTime);
                endEdit = (EditText) findViewById(R.id.editEndTime);
                priorityEdit = (EditText) findViewById(R.id.editPriority);

                SimpleDateFormat formatter = new SimpleDateFormat("mm-dd-yyyy");

                try {
                    newTask = new Task(nameEdit.getText().toString(), descEdit.getText().toString(),
                            Double.parseDouble(longEdit.getText().toString()), Double.parseDouble(latEdit.getText().toString()),
                            Double.parseDouble(radEdit.getText().toString()),formatter.parse(stEdit.getText().toString()),
                            formatter.parse(endEdit.getText().toString()),Integer.parseInt(priorityEdit.getText().toString()), Task.ST_NOT_DONE);
                    new LongOperation().execute();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private class LongOperation  extends AsyncTask<String, Void, Void> {

        private HttpClient Client ;

        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);
        private String res = null;
        //TextView uiUpdate = (TextView) findViewById(R.id.output);

        protected void onPreExecute() {
            Dialog.setMessage("Connecting server .. Please Wait..");
            Dialog.show();
        }

        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {
            try {

                HttpPost httppost = new HttpPost("https://intern-hack-dkhan.c9users.io/addTask");
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("taskId", newTask.getTaskId()));
                params.add(new BasicNameValuePair("taskValue", newTask.getJSONObject().toString()));
                //params.add(new BasicNameValuePair("task", "shit"));
                httppost.setEntity(new UrlEncodedFormEntity(params));

                HttpParams httpParameters = new BasicHttpParams();
                int timeoutConnection = 30000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                int timeoutSocket = 30000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
                Client = new DefaultHttpClient(httpParameters);
                HttpResponse response = Client.execute(httppost);
                res = EntityUtils.toString(response.getEntity());


            } catch (ClientProtocolException e) {
                Error = e.getMessage();
                Dialog.dismiss();

                //cancel(true);
            } catch (IOException e) {
                Error = e.getMessage();



                //cancel(true);
            }
            catch (Exception e) {
                Error = e.getMessage();

                Log.i(TAG, Error);

                //cancel(true);
            }


            return null;
        }

        protected void onPostExecute(Void unused) {
            // NOTE: You can call UI Element here.

            // Close progress dialog
            Dialog.dismiss();

            if (Error != null) {

                Dialog.setMessage("Error : "+ Error);
                //Dialog.setMessage("Whooo!!: Network Problem.. Check your Internet Connection or try after sometime");
                Dialog.show();
                cancel(true);


            } else {
                
                /*try {
                    JSONObject jsonObj = new JSONObject(res);
                    if (jsonObj.has("message"))
                    {

                    }
                    else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/



            }
        }

    }

}
