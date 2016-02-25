package com.ogadai.ogadai_secure;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;

import com.ogadai.ogadai_secure.awaystatus.AwayStatusUpdate;
import com.ogadai.ogadai_secure.awaystatus.IAwayStatusUpdate;
import com.ogadai.ogadai_secure.awaystatus.ManageAwayStatus;

/**
 * Created by alee on 25/02/2016.
 */
public class SetHomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("setting user to home");
        actionSetToHome();

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(MainActivity.EXTRA_SETHOME, false);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent mainActivityIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            mainActivityIntent.send();
        } catch (PendingIntent.CanceledException e) {
            System.out.println("error sending main activity intent - " + e.getMessage());
        }
    }


    private void actionSetToHome() {
        final IAwayStatusUpdate statusUpdate = new AwayStatusUpdate(this);
        AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... urls) {
                try {
                    statusUpdate.updateStatus(ManageAwayStatus.ENTERED_EVENT);
                } catch(Exception e) {
                    System.out.println("error setting to home - " + e.getMessage());
                }
                return null;
            }
        };
        task.execute();
    }}
