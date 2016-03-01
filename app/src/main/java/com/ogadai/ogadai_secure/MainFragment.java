package com.ogadai.ogadai_secure;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by alee on 01/03/2016.
 */
public class MainFragment extends Fragment {
    private IMainActivity mMainActivity;

    protected void initialise() {
        mMainActivity = getMainActivity();
    }

    protected void showProgressBar() {
        mMainActivity.showProgressBar();
    }
    protected void hideProgressBar() {
        mMainActivity.hideProgressBar();
    }

    protected void doAuthenticate(boolean update) {
        IMainActivity activity = getMainActivity();
        if (activity != null) {
            activity.doAuthenticate(update);
        }
    }

    protected IMainActivity getMainActivity() {
        return (IMainActivity)getActivity();
    }

    protected void runOnUiThread(Runnable runnable) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(runnable);
        }
    }

    protected void createAndShowDialogFromTask(final String message, final String title) {
        IMainActivity main = getMainActivity();
        if (main != null) {
            main.createAndShowDialogFromTask(message, title);
        }
    }
    protected void createAndShowDialogFromTask(final Exception exception, final String title) {
        IMainActivity main = getMainActivity();
        if (main != null) {
            main.createAndShowDialogFromTask(exception, title);
        }
    }}

