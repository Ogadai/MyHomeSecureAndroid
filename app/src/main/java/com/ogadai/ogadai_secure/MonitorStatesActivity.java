package com.ogadai.ogadai_secure;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;

public class MonitorStatesActivity extends Activity implements IAuthenticateClient, IHomeSecureSocketClient {

    /**
     * Mobile Service Client reference
     */
    private MobileServiceClient mClient;

    //Offline Sync
    /**
     * Mobile Service Table used to access and Sync data
     */
    //private MobileServiceSyncTable<ToDoItem> mToDoTable;

    /**
     * Adapter to sync the state list with the view
     */
    private StateItemAdapter mAdapter;
    private ArrayList<StateItem> mStates;

    /**
     * Progress spinner to use for table operations
     */
    private ProgressBar mProgressBar;

    private IGoogleAuthenticator mAuthenticator;

    /**
     * Initializes the activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_states);

        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        // Create an adapter to bind the items with the view
        mStates = new ArrayList<StateItem>();
        mAdapter = new StateItemAdapter(this, R.layout.row_list_monitor_states, mStates);

        ListView listViewStates = (ListView) findViewById(R.id.listViewMonitorStates);
        listViewStates.setAdapter(mAdapter);

        // Initialize the progress bar
        mProgressBar.setVisibility(ProgressBar.GONE);
        mSocket = new HomeSecureSocket(this);

        doAuthenticate(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        mSocket.Disconnect();

        System.out.println("Stopped activity");
    }

    @Override
    public void onRestart() {
        super.onRestart();

        MobileServiceUser user = mClient.getCurrentUser();
        if (user != null) {
            connectWebSocket(user);
        } else {
            doAuthenticate(false);
        }

        System.out.println("Restarted activity");
    }

    private void doAuthenticate(boolean update)
    {
        try {
            // Create the Mobile Service Client instance, using the provided

            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://ogadai-secure.azure-mobile.net/",
                    "RhCLppCOuzkwkzZcDDLGcZQTOTwUBj90",
                    this).withFilter(new ProgressFilter());
            mAuthenticator = new GoogleAuthenticator();
            mAuthenticator.authenticate(mClient, update, this);
        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }
    }

    private IHomeSecureSocket mSocket;
    private void connectWebSocket(MobileServiceUser user) {
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        String token = user.getAuthenticationToken();
        mSocket.Connect(token);
    }

    public void Authenticated(MobileServiceUser user) {
        connectWebSocket(user);
    }

    /**
     * Initializes the activity menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void showError(Exception exception, String title) {
        createAndShowDialog(exception, title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception The exception to show in the dialog
     * @param title     The dialog title
     */
    private void createAndShowDialogFromTask(final Exception exception, final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, title);
            }
        });
    }

    private void createAndShowDialogFromTask(final String message, final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(message, title);
            }
        });
    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception The exception to show in the dialog
     * @param title     The dialog title
     */
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if (exception.getCause() != null) {
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message The dialog message
     * @param title   The dialog title
     */
    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    @Override
    public void Connected() {
        System.out.println("Connected to server");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
            }
        });
    }

    @Override
    public void Disconnected(boolean error) {
        System.out.println("Disconnected from server - " + (error ? "error" : "no error"));

        class HandleDisconnect implements Runnable {
            boolean _error;
            HandleDisconnect(boolean error) { _error = error; }

            public void run() {
                if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);

                if(_error) {
                    doAuthenticate(true);
                }
            }
        }

        runOnUiThread(new HandleDisconnect(error));
    }

    @Override
    public void ShowError(Exception e, String error) {
        createAndShowDialogFromTask(e, error);
    }

    @Override
    public void MessageReceived(String message) {
        System.out.println(message);

        final UpdateStatesMessage statesMessage;
        try {
            statesMessage = UpdateStatesMessage.FromJSON(message);

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    for (StateItem state : statesMessage.getStates()) {

                        boolean found = false;
                        for (StateItem existing : mStates) {
                            if (existing.getName().equals(state.getName())) {
                                existing.setActive(state.getActive());
                                found = true;
                            }
                        }

                        if (!found) mStates.add(state);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            createAndShowDialogFromTask(e, "Error showing states");
        }
    }

    private class ProgressFilter implements ServiceFilter {

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
                        }
                    });

                    resultFuture.set(response);
                }
            });

            return resultFuture;
        }
    }
}