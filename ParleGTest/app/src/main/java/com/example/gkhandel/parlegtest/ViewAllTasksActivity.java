package com.example.gkhandel.parlegtest;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.gkhandel.parlegtest.models.Task;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewAllTasksActivity extends AppCompatActivity  {

    List<Button> btn = new ArrayList<Button>();
    LinearLayout linearLayout;
    LinearLayout linearLayouttmp; // no fucking idea
    ScrollView scrollview;
    final static Map<Integer, String> idToTaskId = new HashMap<>();
    private GestureDetectorCompat gestureDetectorCompat;
    static Boolean clickFlag = true;

    /*@Override
    public void onStop() {
        super.onStop();
        finish();
    }*/

    @Override
    public void onBackPressed() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancelAll();
        finish();
    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_tasks);
        linearLayouttmp = (LinearLayout) findViewById(R.id.scrollLayout);
        //scrollView = new ScrollView(this);
        //linearLayout = (LinearLayout)findViewById(R.id.scrollLayout);
        initial();

        idToTaskId.clear();
        for(String taskId:Utils.relevantTasks) {
            AddingButton(Utils.allTasks.get(taskId));
        }

    }

    public void initial()
    {

        scrollview = new ScrollView(this);
        linearLayouttmp.addView(scrollview);
        linearLayout = new LinearLayout(this);

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollview.addView(linearLayout);

    }

    public void AddingButton(Task task)
    {

        Button blocal;
        //final resultClass arrLocal = arr;
        LinearLayout linear1 = new LinearLayout(this);
        linear1.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(linear1);
        blocal = new Button(this);

        blocal.setText(task.getName());
        int id = task.getTaskId().hashCode();
        blocal.setId(id);
        idToTaskId.put(id, task.getTaskId());

        blocal.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);


       // blocal.setBackground(getResources().getDrawable(R.drawable.btn));


        blocal.setTypeface(Typeface.SERIF, Typeface.BOLD_ITALIC);
        blocal.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(0, 8, 0, 4);

        linear1.setOrientation(LinearLayout.HORIZONTAL);
        blocal.setLayoutParams(params);
        linear1.addView(blocal);
        linear1.setGravity(Gravity.CENTER_HORIZONTAL);
        blocal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        // design of buttons
        LinearLayout.LayoutParams paramsButton = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        paramsButton.setMargins(24,8,24,8);
        blocal.setPadding(30,8,30,8);
        blocal.setLayoutParams(paramsButton);



        btn.add(blocal);
        blocal.canScrollHorizontally(1);
        blocal.setLines(1);
        blocal.setEllipsize(TextUtils.TruncateAt.END);

        blocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Integer id = v.getId();
                String taskId = idToTaskId.get(id);
                Task task = Utils.allTasks.get(taskId);

                showDialog(ViewAllTasksActivity.this,task.getName(), task.getDesc(), task.getTaskId());



            }
        });
    }

    public void showDialog(Activity activity, String title, String message, String taskId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);


        if (title != null) builder.setTitle(title);

        builder.setMessage(message);

        MyLovelyOnClickListener listener = new MyLovelyOnClickListener(taskId);

        builder.setPositiveButton("Accept", listener);
        builder.setNegativeButton("Ignore", null);
        builder.show();


    }

    class MyLovelyOnClickListener implements DialogInterface.OnClickListener
    {
        private Task doneTask;
        private String taskId;
        public MyLovelyOnClickListener(String taskId) {
            this.taskId = taskId;
            doneTask = Utils.allTasks.get(taskId);

            // update task status to done
            doneTask.setStatus(Task.ST_DONE);

            // remove task from relevant list
            int removeIndex = Utils.relevantTasks.indexOf(taskId);
            if(removeIndex>=0) {

                Utils.relevantTasks.remove(removeIndex);
            }

            // Send done message to server
            new LongOperation().execute();
            /*while(clickFlag) {

            }*/
            //String ns = Context.NOTIFICATION_SERVICE;
            //NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
            //nMgr.cancelAll();
            //finish();

        }



        @Override
        public void onClick(DialogInterface dialog, int id) {
            Log.i(MainActivity.TAG, taskId);
        }



        /*@Override
        public void onbackpressed()
        {

        }*/
        private class LongOperation  extends AsyncTask<String, Void, Void> {


            private HttpClient Client ;

            private String Error = null;
            private ProgressDialog Dialog = new ProgressDialog(ViewAllTasksActivity.this);
            private String res = null;
            //TextView uiUpdate = (TextView) findViewById(R.id.output);

            protected void onPreExecute() {
                Dialog.setMessage("Connecting server .. Please Wait..");
                Dialog.show();
            }

            // Call after onPreExecute method
            protected Void doInBackground(String... urls) {
                try {

                    HttpPost httppost = new HttpPost("https://intern-hack-dkhan.c9users.io/doneTask");
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("taskId", doneTask.getTaskId()));
                    params.add(new BasicNameValuePair("taskValue", doneTask.getJSONObject().toString()));
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

                    Log.i(MainActivity.TAG, Error);

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
                   clickFlag=false;
                    //Intent intent = new Intent(Intent.ACTION_MAIN);
                    //intent.addCategory(Intent.CATEGORY_HOME);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //startActivity(intent);

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




}


