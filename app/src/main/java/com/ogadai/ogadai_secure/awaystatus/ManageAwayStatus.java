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

import com.ogadai.ogadai_secure.Logger;
import com.ogadai.ogadai_secure.notifications.ShowNotification;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by alee on 22/02/2016.
 */
public class ManageAwayStatus extends ConnectivityManager.NetworkCallback {
    public final static String EXITED_EVENT = "exited";
    public final static String ENTERED_EVENT = "entered";

    private Context mContext;

    private ConnectivityManager mConnectivityManager;
    private PowerManager mPowerManager;
    private AlarmManager mAlarmManager;

    private NetworkRequest mNetworkRequest;
    private PowerManager.WakeLock mWakeLock;
    private PendingIntent mAlarmIntent;

    private static ManageAwayStatus _instance = new ManageAwayStatus();

    private static final int MAXATTEMPTS = 5;
    private static final int RETRYDELAYSECONDS = 30;
    private static final int EXITDELAYSECONDS = 120;
    private static final int NETWORKDELAYSECONDS = 60;

    private static final String TAG = "ManageAwayStatus";

    public static void setAwayStatus(Context context, String action) {
        setAwayStatus(context, action, false);
    }

    public static void setAwayStatus(Context context, String action, boolean transition) {
        _instance.setContext(context);
        _instance.setAwayStatus(action, transition);
    }

    public static void processStatus(Context context) {
        _instance.setContext(context);
        _instance.processStatus();
    }

    private ManageAwayStatus() {
    }

    private void setContext(Context context) {
        mContext = context;
        mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mPowerManager = (PowerManager) mContext.getSystemService(POWER_SERVICE);
        mAlarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
    }

    private synchronized void setAwayStatus(String action, boolean transition) {
        try {
            clear();
            set(new PendingStatus(action, 0, transition ? PendingStage.Transition : PendingStage.NewAction));

            acquireWakeLock();
            processAfterDelay(1);
        } catch (Exception exception) {
            Logger.e(TAG, "Error setting action " + action, exception);
        }
    }

    private synchronized void processStatus() {
        PendingStatus status = null;
        try {
            clearDelay();
            status = get();

            switch(status.getStage()) {
                case Transition:
                    if (status.getAction().equalsIgnoreCase(EXITED_EVENT)) {
                        Logger.i(TAG, "Delaying exit transition by " + EXITDELAYSECONDS + " seconds");
                        status.setStage(PendingStage.ExitDelay);
                        processAfterDelay(EXITDELAYSECONDS);
                        releaseWakeLock();
                    } else {
                        Logger.i(TAG, "Trying enter transition immediately");
                        trySubmit(status, false);
                    }
                    break;
                case NewAction:
                    Logger.i(TAG, "Trying " + status.getAction() + " transition immediately");
                    trySubmit(status, false);
                    break;
                case ExitDelay:
                    Logger.i(TAG, "Trying exit transition after delay");
                    trySubmit(status, false);
                    break;
                case WaitingForNetwork:
                    Logger.i(TAG, "Still waiting for network, but retrying anyway");
                    trySubmit(status, false);
                    break;
                case NetworkAvailable:
                    Logger.i(TAG, "Network available, so forcing a retry");
                    trySubmit(status, true);
                    break;
                case RetryDelay:
                    Logger.i(TAG, "Retrying after delay");
                    trySubmit(status, false);
                    break;
                case Complete:
                    Logger.i(TAG, "Completed");
                    status.clear(mContext);
                    status = null;
                    break;
            }

            if (status != null) {
                set(status);
            }

        } catch (Exception exception) {
            Logger.e(TAG, "Error processing status - " + (status != null ? (status.getAction() + " " + status.getStage().toString()) : "unknown"), exception);
        }
    }

    private void processAfterDelay(int delaySeconds) {
        Intent intent = new Intent(mContext, AwayStatusDelayReceiver.class);
        mAlarmIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delaySeconds * 1000, mAlarmIntent);
    }

    private void clearDelay() {
        if (mAlarmIntent != null) {
            mAlarmManager.cancel(mAlarmIntent);
            mAlarmIntent = null;
        }
    }

    private void trySubmit(final PendingStatus status, boolean forceTry) {
        acquireWakeLock();

        if (forceTry || isConnected()) {
            status.setStage(PendingStage.Submitting);

            AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
                @Override
                protected Void doInBackground(String... urls) {
                    tryPostStatus(status);
                    return null;
                }
            };
            task.execute();
        } else {
            Logger.i(TAG, "Not connected so waiting for network");
            status.setStage(PendingStage.WaitingForNetwork);
            requestNetwork();

            processAfterDelay(NETWORKDELAYSECONDS);
        }
    }

    private void requestNetwork() {
        if (mNetworkRequest == null) {
            mNetworkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();

            mConnectivityManager.requestNetwork(mNetworkRequest, this);
        }
    }

    @Override
    public synchronized void onAvailable(Network network) {
        try {
            PendingStatus status = get();
            status.setStage(PendingStage.NetworkAvailable);
            set(status);

            processAfterDelay(1);

            if (mNetworkRequest != null) {
                mConnectivityManager.unregisterNetworkCallback(this);
                mNetworkRequest = null;
            }
        } catch (Exception exception) {
            Logger.e(TAG, "Error setting stage to NetworkAvailable", exception);
        }
    }

    private boolean isConnected() {
        NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private void tryPostStatus(PendingStatus status) {
        try {
            IAwayStatusUpdate statusUpdate = new AwayStatusUpdate(mContext);
            statusUpdate.updateStatus(status.getAction());

            Logger.i(TAG, "Successfully updated away status with action : " + status.getAction());
            afterPostStatus(true);
        } catch (Exception exception) {
            Logger.e(TAG, "Failed to updated away status with action : " + (status != null ? status.getAction() : "unknown") + " - " + exception.getMessage());
            afterPostStatus(false);
        }
    }

    private synchronized void afterPostStatus(boolean successful) {
        try {
            PendingStatus status = get();

            if (status.getStage() == PendingStage.Submitting) {
                if (successful) {
                    clear(status);
                } else {
                    retryUpToMaxAttempts(status);
                }
            }

        } catch (Exception exception) {
            Logger.e(TAG, "Error handling PostStatus result", exception);
        }
    }

    private void retryUpToMaxAttempts(final PendingStatus status) {
        status.setAttempts(status.getAttempts() + 1);

        if (status.getAttempts() >= MAXATTEMPTS) {
            Logger.e(TAG, "Exceeded maximum attempts to update status");
            clear(status);

            ShowNotification test = new ShowNotification(mContext);
            test.show("Failed to update Away Status", "Tried '" + status.getAction() + "' " + MAXATTEMPTS + " times");
        } else {
            status.setStage(PendingStage.RetryDelay);
            set(status);

            int retries = MAXATTEMPTS - status.getAttempts();
            Logger.i(TAG, "Will attempt to update status " + retries + " more time" + (retries == 1 ? "" : "s"));

            // Retry after delay
            processAfterDelay(RETRYDELAYSECONDS);
            releaseWakeLock();
        }
    }

    private void set(PendingStatus pendingStatus)
    {
        pendingStatus.save(mContext);
    }

    private PendingStatus get()
    {
        return new PendingStatus(mContext);
    }

    private void clear() {
        clear(get());
    }

    private void clear(PendingStatus pendingStatus)
    {
        pendingStatus.clear(mContext);

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

    private enum PendingStage {
        Transition,
        NewAction,
        ExitDelay,
        WaitingForNetwork,
        NetworkAvailable,
        RetryDelay,
        Submitting,
        Complete
    }

    private class PendingStatus {
        private String mAction;
        private int mAttempts;
        private PendingStage mStage;

        private static final String PREFFILE = "pending_status";
        private static final String ACTIONPREF = "action";
        private static final String ATTEMPTSPREF = "attempts";
        private static final String STAGEPREF = "stage";

        public PendingStatus(Context context) {
            load(context);
        }

        public PendingStatus(String action, int attempts, PendingStage stage) {
            setAction(action);
            setAttempts(attempts);
            setStage(stage);
        }

        public String getAction() { return mAction; }
        public void setAction(String action) { mAction = action; }

        public int getAttempts() { return mAttempts; }
        public void setAttempts(int attempts) { mAttempts = attempts; }

        public PendingStage getStage() { return mStage; }
        public void setStage(PendingStage stage) { mStage = stage; }

        public void save(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREFFILE, Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(ACTIONPREF, getAction());
            editor.putInt(ATTEMPTSPREF, getAttempts());
            editor.putString(STAGEPREF, getStage().toString());
            editor.commit();
        }

        private void load(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREFFILE, Context.MODE_PRIVATE);
            String action = prefs.getString(ACTIONPREF, "undefined");
            if (action == "undefined") {
                mStage = PendingStage.Complete;
                return;
            }
            mAction = action;
            mAttempts = prefs.getInt(ATTEMPTSPREF, 0);
            String stage = prefs.getString(STAGEPREF, PendingStage.Complete.toString());
            mStage = PendingStage.valueOf(stage);
        }

        public void clear(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREFFILE, Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(ACTIONPREF);
            editor.remove(ATTEMPTSPREF);
            editor.remove(STAGEPREF);
            editor.commit();
        }
    }
}
