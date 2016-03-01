package com.ogadai.ogadai_secure;

import android.app.ActionBar;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by alee on 01/03/2016.
 */
public class MainContent extends RelativeLayout
        implements View.OnSystemUiVisibilityChangeListener, View.OnClickListener {
    private ActionBar mActionBar;
    private boolean mFullScreen;
    private int mLastSystemUiVis;

    Runnable mNavHider = new Runnable() {
        @Override public void run() {
            setNavVisibility(false);
        }
    };

    private static final int BASE_VISIBILITY = 0;

    public MainContent(Context context) {
        super(context);
    }
    public MainContent(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnClickListener(this);
        setOnSystemUiVisibilityChangeListener(this);
    }

    public void init(ActionBar actionBar) {
        mActionBar = actionBar;
        setFullScreen(false);
    }

    public boolean getFullScreen() { return mFullScreen; }
    public void setFullScreen(boolean fullScreen) {
        mFullScreen = fullScreen;
        setNavVisibility(!fullScreen);
    }

    @Override public void onSystemUiVisibilityChange(int visibility) {
        // Detect when we go out of low-profile mode, to also go out
        // of full screen.  We only do this when the low profile mode
        // is changing from its last state, and turning off.
        int diff = mLastSystemUiVis ^ visibility;
        mLastSystemUiVis = visibility;
        if ((diff&SYSTEM_UI_FLAG_FULLSCREEN) != 0
                && (visibility&SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            setNavVisibility(true);
        }
    }

    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);

        // When we become visible, we show our navigation elements briefly
        // before hiding them.
        setNavVisibility(true);
        if (mFullScreen) {
            getHandler().postDelayed(mNavHider, 2000);
        }
    }

    @Override public void onClick(View v) {
        if (mFullScreen) {
            // When the user clicks, we toggle the visibility of navigation elements.
            int curVis = getSystemUiVisibility();
            setNavVisibility((curVis & SYSTEM_UI_FLAG_FULLSCREEN) != 0);
        }
    }

    private void setNavVisibility(boolean visible) {
        int visibilityFlags = BASE_VISIBILITY;

        if (!visible) {
            visibilityFlags = visibilityFlags
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        setSystemUiVisibility(visibilityFlags);

        if (visible) {
            mActionBar.show();
        } else {
            mActionBar.hide();
        }
    }
}
