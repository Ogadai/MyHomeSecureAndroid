package com.ogadai.ogadai_secure.awaystatus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.ogadai.ogadai_secure.Logger;
import com.ogadai.ogadai_secure.notifications.ShowNotification;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by alee on 22/02/2016.
 */
public class ManageAwayStatus extends ConnectivityManager.NetworkCallback implements IManageAwayStatus {
    public final static String EXITED_EVENT = "exited";
    public final static String ENTERED_EVENT = "entered";

    private Context mContext;
    private ConnectivityManager mConnectivityManager;

    private NetworkRequest mNetworkRequest;

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    private static final String PREFFILE = "pending_status";
    private static final String ACTIONPREF = "action";
    private static final String ATTEMPTSPREF = "attempts";

    private static final int MAXATTEMPTS = 10;
    private static final int RETRYDELAYSECONDS = 10;
    private static final int EXITDELAYSECONDS = 120;

    private static final ScheduledExecutorService mScheduler =
            Executors.newScheduledThreadPool(1);
    private AlarmManager mAlarmManager = null;
    private PendingIntent mAlarmIntent = null;

    private static final String TAG = "ManageAwayStatus";

    public ManageAwayStatus(Context context) {
        mContext = context;
        mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mPowerManager = (PowerManager) mContext.getSystemService(POWER_SERVICE);
    }

    public static ScheduledExecutorService getScheduler() {
        return mScheduler;
    }

    @Override
    public void setAwayStatus(String action) {
        PendingStatus status = new PendingStatus(action, 0);
        set(status);

        if (action.compareTo(EXITED_EVENT) == 0) {
            // Exit delay to avoid frequent out/in notifications
            trySubmitAfterDelay(EXITDELAYSECONDS);
        } else {
            trySubmitOnThread(status);
        }
    }

    public void retryPending() {
        retryPending(false);
    }

    public void retryPending(boolean forceTry) {
        PendingStatus status = get();
        if (status != null && status.getAction().length() > 0) {
            trySubmitOnThread(status, forceTry);
        }
    }

    private boolean isConnected() {
        NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private void trySubmitOnThread(final PendingStatus status) {
        trySubmitOnThread(status, false);
    }
    private void trySubmitOnThread(final PendingStatus status, boolean forceTry) {
        if (mAlarmManager != null) {
            mAlarmManager.cancel(mAlarmIntent);
            mAlarmIntent = null;
        }
        acquireWakeLock();

        boolean connected = isConnected();
        if (forceTry || connected) {
            String message = connected
                            ? "Connected and about to try submitting status : "
                            : "Not connected but attempting to submit status anyway : ";

            Logger.i(TAG, message + status.getAction());
            AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
                @Override
                protected Void doInBackground(String... urls) {
                    trySubmit(status);
                    return null;
                }
            };
            task.execute();
        } else {
            if (mNetworkRequest == null) {
                Logger.i(TAG, "Not connected so requesting network for status : " + status.getAction());
                requestNetwork();
            } else {
                Logger.i(TAG, "Not connected so delaying submitting status : " + status.getAction());
            }

            retryUpToMaxAttempts(status);
        }
    }

    private void requestNetwork() {
        mNetworkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        mConnectivityManager.requestNetwork(mNetworkRequest, this);
    }

    @Override
    public void onAvailable(Network network) {
        Logger.i(TAG, "Network available, so about to attempt request");
        retryPending(true);
    }

    private void trySubmit(final PendingStatus status) {
        try {
            postStatus(status.getAction());
            clear();
            Logger.i(TAG, "Successfully updated away status with action : " + status.getAction());
        }
        catch(Exception e) {
            Logger.e(TAG, "Failed to updated away status with action : " + status.getAction() + " - " + e.getMessage());
            retryUpToMaxAttempts(status);
        }
    }

    private void retryUpToMaxAttempts(final PendingStatus status) {
        status.setAttempts(status.getAttempts() + 1);
        set(status);

        if (status.getAttempts() >= MAXATTEMPTS) {
            Logger.e(TAG, "Exceeded maximum attempts to update status");
            clear();

            ShowNotification test = new ShowNotification(mContext);
            test.show("Failed to update Away Status", "Tried '" + status.getAction() + "' " + MAXATTEMPTS + " times");
        } else {
            int retries = MAXATTEMPTS - status.getAttempts();
            Logger.i(TAG, "Will attempt to update status " + retries + " more time" + (retries == 1 ? "" : "s"));

            // Retry after delay
            trySubmitAfterDelay(RETRYDELAYSECONDS);
        }
    }

    private void trySubmitAfterDelay(int delaySeconds) {
        Logger.i(TAG, "Submitting status after delay");

        mAlarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, AwayStatusDelayReceiver.class);
        mAlarmIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delaySeconds * 1000, mAlarmIntent);

        releaseWakeLock();
    }

    private void postStatus(String action) throws IOException, AuthenticationException {
        IAwayStatusUpdate statusUpdate = new AwayStatusUpdate(mContext);
        statusUpdate.updateStatus(action);
    }

    private void set(PendingStatus pendingStatus)
    {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFFILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ACTIONPREF, pendingStatus.getAction());
        editor.putInt(ATTEMPTSPREF, pendingStatus.getAttempts());
        editor.commit();
    }

    private PendingStatus get()
    {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFFILE, Context.MODE_PRIVATE);
        String action = prefs.getString(ACTIONPREF, "undefined");
        if (action == "undefined")
            return null;
        int attempts = prefs.getInt(ATTEMPTSPREF, 0);

        return new PendingStatus(action, attempts);
    }

    private void clear()
    {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFFILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(ACTIONPREF);
        editor.remove(ATTEMPTSPREF);
        editor.commit();

        if (mNetworkRequest != null) {
            mConnectivityManager.unregisterNetworkCallback(this);
            mNetworkRequest = null;
        }
        releaseWakeLock();
    }

    private void acquireWakeLock() {
        if (mWakeLock == null) {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HomeSecureWatcherLock");
            mWakeLock.acquire();
        }
    }
    private void releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private class PendingStatus {
        private String mAction;
        private int mAttempts;

        public PendingStatus(String action, int attempts) {
            setAction(action);
            setAttempts(attempts);
        }

        public String getAction() { return mAction; }
        public void setAction(String action) { mAction = action; }

        public int getAttempts() { return mAttempts; }
        public void setAttempts(int attempts) { mAttempts = attempts; }
    }
}
