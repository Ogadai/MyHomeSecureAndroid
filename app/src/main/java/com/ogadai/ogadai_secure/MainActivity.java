package com.ogadai.ogadai_secure;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.widget.ProgressBar;

//import com.google.common.util.concurrent.FutureCallback;
//import com.google.common.util.concurrent.Futures;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.common.util.concurrent.SettableFuture;
//import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
//import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
//import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
//import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
//import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.ogadai.ogadai_secure.auth.CachedToken;
import com.ogadai.ogadai_secure.auth.TokenCache;
import com.ogadai.ogadai_secure.notifications.ShowNotification;

import java.net.MalformedURLException;

public class MainActivity extends FragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, IMainActivity,
                GoogleApiClient.OnConnectionFailedListener {
    public static final String EXTRA_SHOWFRAGMENT = "com.ogadai.ogadai_secure.showCamera";

    private GoogleApiClient mGoogleApiClient;
    private MainContent mMainContent;

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

    private static final int RC_SIGN_IN = 1983;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();
        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        mProgressBar.setVisibility(ProgressBar.GONE);

        int currentPosition = -1;
        Intent intent = getIntent();
        String targetFragment = intent.getStringExtra(EXTRA_SHOWFRAGMENT);
        if (targetFragment != null && targetFragment.compareTo("camera") == 0) {
            currentPosition = 2;
        }

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                currentPosition);

        mMainContent = (MainContent)findViewById(R.id.main_layout);
        mMainContent.init(getActionBar());

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
        clearFragments();

        boolean doAuth = update;
        if (!doAuth) {
            TokenCache tokenCache = new TokenCache(this, TokenCache.GOOGLE_PREFFILE);
            doAuth = (tokenCache.get() == null);
        }

        if (doAuth) {
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("724129164049-r7bo4g8l7b3d9n0fb2mqeak4tri0nojn.apps.googleusercontent.com")
                    .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                    .requestScopes(new Scope(Scopes.PLUS_ME))
                    .requestEmail()
                    .build();

            // Build a GoogleApiClient with access to the Google Sign-In API and the
            // options specified by gso.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else {
            authenticateSuccessful();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();

                System.out.println("Google SignIn successful as " + acct.getEmail());

                TokenCache tokenCache = new TokenCache(this, TokenCache.GOOGLE_PREFFILE);
                tokenCache.set(new CachedToken(acct.getEmail(), acct.getIdToken()));

                authenticateSuccessful();
            } else {
                // Signed out, show unauthenticated UI.
                System.out.println("Google SignIn was not successful");
            }
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
        mFragments = new Fragment[4];
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
                    newFragment = new CameraFragment();
                    break;
                case 3:
                    newFragment = new SettingsFragment();
                    break;
            }
            mFragments[position] = newFragment;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, newFragment)
                .commit();
        onSectionAttached(position);

//        mMainContent.setFullScreen(position == 2);
//        setRequestedOrientation(position == 2
//                ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//                : ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
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
            case 3:
                mTitle = getString(R.string.title_section4);
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog("Failed to connecto for Google SignIn", "Google SignIn");
            }
        });
    }
//
//    public class ProgressFilter implements ServiceFilter {
//
//        @Override
//        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {
//
//            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();
//
//
//            runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
//                }
//            });
//
//            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);
//
//            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
//                @Override
//                public void onFailure(Throwable e) {
//                    resultFuture.setException(e);
//                }
//
//                @Override
//                public void onSuccess(ServiceFilterResponse response) {
//                    runOnUiThread(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
//                        }
//                    });
//
//                    resultFuture.set(response);
//                }
//            });
//
//            return resultFuture;
//        }
//    }
}
