package com.example.adibhat.threadexample;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadExampleActivity extends AppCompatActivity {

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

        //@Override
        public void handleMessageOld(Message msg) {
            Bundle bundle = msg.getData();
            String string = bundle.getString("myKey");
            TextView myTextView =
                    (TextView) findViewById(R.id.myTextView);
            myTextView.setText(string);


            mBuilder.setSmallIcon(R.drawable.notification_template_icon_bg);
            mBuilder.setContentTitle("Things to do here!");
            mBuilder.setContentText("Hi, This is Android Notification Detail!");

            // notificationID allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_example);

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


    public void startAppActivitiesBgThread() {

        Runnable taskPoller = new Runnable() {

            @Override
            public void run() {
                while (true) {
                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();

                    //TODO :
                    setCurrentLocation();
                    ArrayList<String> relevantTasks = getRelevantTasks();

                    // temp
                    relevantTasks.add("Task 1 : do this");
                    relevantTasks.add("Task 2 : do that");

                    if (relevantTasks.size() > 0) {
                        //bundle.putString("numberOfTasks", String.valueOf(relevantTasks.size()));
                        bundle.putStringArrayList("relevantTasks", relevantTasks);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread taskPollThread = new Thread(taskPoller);
        taskPollThread.start();
    }

    //TODO: inject location code
    private void setCurrentLocation() {
        // temp
        this.currentLat = Double.parseDouble("0.0");
        this.currentLong = Double.parseDouble("0.0");
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
        ArrayList<String> relevantTasks = new ArrayList<String>();

        for (Task task : allTasks.values()) {
            if (task.getStatus() == Task.ST_DONE || task.getStatus() == Task.ST_CANCELED)
                continue;
            if (Utils.distance(currentLat, task.getLatitude(), currentLong, task.getLongitude()) <= task.getRadius())
                relevantTasks.add(task.toNotificationString());
        }

        return relevantTasks;
    }


    public void startBgThreadOld() {
        Runnable runnable = new Runnable() {
            public void run() {
                while (true) {
                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    SimpleDateFormat dateformat =
                            new SimpleDateFormat("HH:mm:ss MM/dd/yyyy",
                                    Locale.US);
                    String dateString = dateformat.format(new Date());
                    bundle.putString("myKey", dateString);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        System.out.println("Thread was interrupted");
                    }
                }
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();
    }

    public void switchActivity(View view)
    {
        Intent intent = new Intent(this, ViewAllTasksActivity.class);
        startActivity(intent);
    }

}
