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

public class ThreadExampleActivity extends AppCompatActivity {

    int notificationNum;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    Intent resultIntent;
    TaskStackBuilder stackBuilder;

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

        // notification stuff
        mBuilder = new NotificationCompat.Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // for notification click to lead to new activity
        resultIntent = new Intent(this, ViewAllTasksActivity.class);
        stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ViewAllTasksActivity.class);
    }


    public void buttonClick(View view) {

        Runnable taskPoller = new Runnable() {

            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();

                ArrayList<String> relevantTasks = new ArrayList<String>();
                //TODO :
                // get current location
                // get tasks, add details in relevantTasks
                relevantTasks.add("Task 1 : do this");
                relevantTasks.add("Task 2 : do that");

                bundle.putString("numberOfTasks", String.valueOf(relevantTasks.size()));
                bundle.putStringArrayList("relevantTasks", relevantTasks);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        };
        Thread taskPollThread = new Thread(taskPoller);
        taskPollThread.start();
    }


    public void buttonClickOld(View view) {
        Runnable runnable = new Runnable() {
            public void run() {
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                SimpleDateFormat dateformat =
                        new SimpleDateFormat("HH:mm:ss MM/dd/yyyy",
                                Locale.US);
                String dateString = dateformat.format(new Date());
                bundle.putString("myKey", dateString);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();
    }

}