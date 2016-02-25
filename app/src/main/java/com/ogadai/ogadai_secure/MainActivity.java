package com.ogadai.ogadai_secure;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
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
import com.ogadai.ogadai_secure.auth.GoogleAuthenticator;
import com.ogadai.ogadai_secure.auth.IAuthenticateClient;
import com.ogadai.ogadai_secure.notifications.ShowNotification;

import java.net.MalformedURLException;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, IMainActivity {
    public static final String EXTRA_SETHOME = "com.ogadai.ogadai_secure.setHome";

    /**
     * Mobile Service Client reference
     */
    private static MobileServiceClient mClient;

    public static MobileServiceClient getClient() {
        return mClient;
    }

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Progress spinner to use for table operations
     */
    private ProgressBar mProgressBar;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();
        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        mProgressBar.setVisibility(ProgressBar.GONE);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        doAuthenticate(false);

        clearNotifications();
    }

    protected void onStart() {
        super.onStart();
    }
    protected void onStop() {
        super.onStop();
    }

    public void doAuthenticate(boolean update)
    {
        try {
            // Create the Mobile Service Client instance, using the provided
            clearFragments();

            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://ogadai-secure.azure-mobile.net/",
                    "RhCLppCOuzkwkzZcDDLGcZQTOTwUBj90",
                    this).withFilter(new ProgressFilter());
            new GoogleAuthenticator(this).authenticate(mClient, update, new IAuthenticateClient() {
                @Override
                public void authenticated(MobileServiceUser user) {
                    authenticateSuccessful();
                }

                @Override
                public void showError(Exception exception, String title) {
                    createAndShowDialogFromTask(exception, title);
                }
            });
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void clearNotifications() {
        ShowNotification showNotification = new ShowNotification(this);
        showNotification.clear();
    }

    private void authenticateSuccessful() {
        mNavigationDrawerFragment.activate();
    }

    private Fragment[] mFragments;
    private void clearFragments() {
        mFragments = new Fragment[3];
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        Fragment newFragment = mFragments[position];
        if (newFragment == null) {
            switch (position) {
                case 0:
                    newFragment = new MonitorStatesFragment();
                    break;
                case 1:
                    newFragment = new HistoryFragment();
                    break;
                case 2:
                    newFragment = new SettingsFragment();
                    break;
            }
            mFragments[position] = newFragment;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, newFragment)
                .commit();
        onSectionAttached(position);
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_section1);
                break;
            case 1:
                mTitle = getString(R.string.title_section2);
                break;
            case 2:
                mTitle = getString(R.string.title_section3);
                break;
        }
        restoreActionBar();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public void showProgressBar() {
        setProgressBar(true);
    }

    public void hideProgressBar() {
        setProgressBar(false);
    }

    private void setProgressBar(final boolean visible) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mProgressBar != null) mProgressBar.setVisibility(visible ? ProgressBar.VISIBLE : ProgressBar.GONE);
            }
        });
    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception The exception to show in the dialog
     * @param title     The dialog title
     */
    public void createAndShowDialogFromTask(final Exception exception, final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, title);
            }
        });
    }

    public void createAndShowDialogFromTask(final String message, final String title) {
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
    public void createAndShowDialog(Exception exception, String title) {
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
    public void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
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
