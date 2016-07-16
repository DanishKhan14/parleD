package com.example.gkhandel.parlegtest;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import com.example.gkhandel.parlegtest.models.Task;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    private AutoCompleteTextView mAutocompleteView;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(47.6062, -122.3321), new LatLng(47.6740, -122.1215));

    public static final String TAG = "LJKLJ";

    private Button button;
    private EditText nameEdit;
    private EditText descEdit;
    private EditText radEdit;
    private EditText stEdit;
    private EditText endEdit;
    private EditText priorityEdit;
    private Task newTask;
    private double latitude;
    private double longitude;
    public String Content;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private String provider;
    double latitudeFromGPS, longitudeFromGPS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        setContentView(R.layout.activity_main);
        addListenerOnButton();
        mAutocompleteView = (AutoCompleteTextView)
                findViewById(R.id.autocomplete_places);

        // Register a listener that receives callbacks when a suggestion has been selected
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY,
                null);
        mAutocompleteView.setAdapter(mAdapter);

        // for our app
        Utils.allTasks = new ConcurrentHashMap<String, Task>();

        // notification stuff
        mBuilder = new NotificationCompat.Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // for notification click to lead to new activity
        resultIntent = new Intent(this, ViewAllTasksActivity.class);
        stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ViewAllTasksActivity.class);

        //startBgThreadOld();
        startAppActivitiesBgThread();
    }

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }

            Place place = places.get(0);
            latitude = place.getLatLng().latitude;
            longitude = place.getLatLng().longitude;

            final CharSequence thirdPartyAttribution = places.getAttributions();
            Log.i(TAG, "Place details received: " + place.getName());
            Log.i(TAG, "Place details received: " + place.getLatLng().toString());

            places.release();
        }
    };

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
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

                radEdit = (EditText) findViewById(R.id.editRadius);
                //stEdit = (EditText) findViewById(R.id.editStTime);
                //endEdit = (EditText) findViewById(R.id.editEndTime);
                priorityEdit = (EditText) findViewById(R.id.editPriority);

                SimpleDateFormat formatter = new SimpleDateFormat("mm-dd-yyyy");


               // try {
                    //newTask = new Task(nameEdit.getText().toString(), descEdit.getText().toString(),
                      //      new Double(place.getLatLng().longitude), new Double(place.getLatLng().latitude),
                        //    Double.parseDouble(radEdit.getText().toString()),formatter.parse(stEdit.getText().toString()),
                          //  formatter.parse(endEdit.getText().toString()),Integer.parseInt(priorityEdit.getText().toString()), Task.ST_NOT_DONE);

                    newTask = new Task(nameEdit.getText().toString(), descEdit.getText().toString(),
                            new Double(longitude), new Double(latitude),
                            Double.parseDouble(radEdit.getText().toString()),new Date(),
                            new Date(),Integer.parseInt(priorityEdit.getText().toString()), Task.ST_NOT_DONE);
                    new LongOperation().execute();
                    //new GetOperation().execute();
                    //new GetOperation().execute();
                //} catch (ParseException e) {
                  //  e.printStackTrace();
                //}
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

    private class GetOperation  extends AsyncTask<String, Void, Void> {

        private  HttpClient Client = new DefaultHttpClient();

        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);

        //TextView uiUpdate = (TextView) findViewById(R.id.output);

        protected void onPreExecute() {
            // NOTE: You can call UI Element here.

            //UI Element
            //uiUpdate.setText("Output : ");
            //Dialog.setMessage("Connecting server .. Please Wait..");
           // Dialog.show();
        }

        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {
            try {

                // Call long running operations here (perform background computation)
                // NOTE: Don't call UI Element here.

                // Server url call by GET method
                //HttpGet httpget = new HttpGet("http://httpbin.org/ip");
                HttpGet httpget =new HttpGet("https://intern-hack-dkhan.c9users.io/pollTasks");
                ResponseHandler<String> responseHandler = new BasicResponseHandler();

                /*final HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 15000);
                Client = new DefaultHttpClient(httpParams);*/


                HttpParams httpParameters = new BasicHttpParams();
// Set the timeout in milliseconds until a connection is established.
// The default value is zero, that means the timeout is not used.
                int timeoutConnection = 30000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
// Set the default socket timeout (SO_TIMEOUT)
// in milliseconds which is the timeout for waiting for data.
                int timeoutSocket = 30000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                Client = new DefaultHttpClient(httpParameters);
                Content = Client.execute(httpget, responseHandler);

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
                //Dialog.setMessage("Network Problem.. Check your Internet Connection or try after sometime");
                Dialog.show();
                cancel(true);


            } else {
                //Dialog.setMessage("Content : "+ Content);
                //Dialog.show();
                try {
                    JSONArray contentArray = new JSONArray(Content);
                    for(int i=0;i<contentArray.length();i++) {
                        Task task = new Task((JSONObject) contentArray.get(i));
                        Log.i(TAG, task.getJSONObject().toString() + " Distance: " +Utils.distance(task.getLatitude(), currentLat, task.getLongitude(), currentLong));

                        if(!Utils.allTasks.containsKey(task.getTaskId())) {
                            Utils.allTasks.put(task.getTaskId(), task);
                        }
                    }
                   // Dialog.setMessage("Success : "+ Content);
                    //Dialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch(ParseException e) {
                    e.printStackTrace();
                }
                //Dialog.setMessage("Network Problem.. Check your Internet Connection or try after sometime");

                //parse Json Array over here
                //uiUpdate.setText("Output : "+Content);
                /*try {
                    //jsonArray=new JSONArray(Content);
                    //initialDisplay();
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/


            }
        }

    }


    // Concurrency Logic
    // for notifications
    int notificationNum;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    Intent resultIntent;
    TaskStackBuilder stackBuilder;

    // for our app
    Double currentLat, currentLong;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();

            // Details for notification
            ArrayList<String> relevantTasks = bundle.getStringArrayList("relevantTasks");
            StringBuilder notificationContent = new StringBuilder();
            for (String task : relevantTasks) {
                notificationContent.append(task);
                notificationContent.append("\n");
            }

            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            // Populate notification details
            mBuilder.setSmallIcon(R.drawable.notification_template_icon_bg);
            mBuilder.setContentTitle(relevantTasks.size() + " Todo tasks here!");
            mBuilder.setContentText(notificationContent.toString());

            // notificationID allows you to update the notification later on.
            mNotificationManager.notify(++notificationNum, mBuilder.build());
        }


    };

    public void startAppActivitiesBgThread() {

        Runnable taskPoller = new Runnable() {

            @Override
            public void run() {
                while (true) {
                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();

                    //TODO :
                    setCurrentLocation();
                    Log.i(TAG,"Current Location: " + currentLat.toString() + " " +currentLong.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new GetOperation().execute();
                        }
                    });
                    //updateAllTasks();
                    ArrayList<String> relevantTasks = getRelevantTasks();

                    // temp
                    //relevantTasks.add("Task 1 : do this");
                    //relevantTasks.add("Task 2 : do that");

                    if (relevantTasks.size() > 0) {
                        //bundle.putString("numberOfTasks", String.valueOf(relevantTasks.size()));
                        bundle.putStringArrayList("relevantTasks", relevantTasks);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }

                    try {
                        Thread.sleep(8000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread taskPollThread = new Thread(taskPoller);
        taskPollThread.start();
    }

    private void updateAllTasks() {
        new GetOperation().execute();
        // populate task via Content String
    }

    //TODO: inject location code
    private void setCurrentLocation() {
        // temp
        getLocation();
        this.currentLat = latitudeFromGPS;
        this.currentLong = longitudeFromGPS;
    }

    /**
     * Uses Utils.allTasks, this.currentLat, this.currentLong, Task.radius, Task.latitude, Task.longitude
     *
     * @return List of matching tasks in string format for displaya s notification
     */
    private ArrayList<String> getRelevantTasks() {

        // IFF we get a concurrentModexception or something,
        // clone this map instead of copying reference
        ConcurrentHashMap<String, Task> allTasks = Utils.allTasks;
        Utils.relevantTasks = new ArrayList<String>();

        for (Task task : allTasks.values()) {
            if (task.getStatus() == Task.ST_DONE || task.getStatus() == Task.ST_CANCELED)
                continue;
            if (Utils.distance(currentLat, task.getLatitude(), currentLong, task.getLongitude()) <= task.getRadius())
                if(!task.getNotified()) {
                    task.setNotified(true);
                    Utils.relevantTasks.add(task.getTaskId());
                }
        }

        return Utils.relevantTasks;
    }

    void getLocation()
    {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


// Initialize the location fields
        if (location != null)
        {
            System.out.println("Provider " + provider + " has been selected.");

        }
        latitudeFromGPS = (double) (location.getLatitude());
        longitudeFromGPS = (double) (location.getLongitude());





    }

}
