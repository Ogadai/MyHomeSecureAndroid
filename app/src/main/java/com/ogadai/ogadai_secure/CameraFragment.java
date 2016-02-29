package com.ogadai.ogadai_secure;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

import com.ogadai.ogadai_secure.awaystatus.AwayStatusUpdate;
import com.ogadai.ogadai_secure.awaystatus.IAwayStatusUpdate;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {

    private ImageView mImageView;

    public CameraFragment() {
        // Required empty public constructor
    }

    private IMainActivity getMainActivity() {
        return (IMainActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        // Create an adapter to bind the items with the view
        mImageView = (ImageView) view.findViewById(R.id.imageView);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startVideoTask();

        System.out.println("Started camera fragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        stopVideoTask();

        System.out.println("Stopped camera fragment");
    }
//
//    private void startVideoTask()
//    {
//        showSnapshot();
//    }
//
//    private void stopVideoTask()
//    {
//
//    }
//
//    private void showSnapshot()
//    {
//        String imageUrl = "http://digital-photography-school.com/wp-content/uploads/2007/07/landscape.jpg";
//        mWebView.loadUrl(imageUrl);
//    }

    private final StreamingStatus mStatus = new StreamingStatus();

    private void startVideoTask() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    startVideo();
                } catch(Exception e) {
                    System.out.println("Error streaming camera snapshots - " + e.getMessage());
                    getMainActivity().createAndShowDialogFromTask(e, "Error streaming camera snapshots");
                }
                return null;
            }
        };
        task.execute();
    }
    private void stopVideoTask() {
        mStatus.setStreaming(false);
    }

    private void startVideo() {
        if (mStatus.getStreaming()) return;
        mStatus.setStreaming(true);

        while(mStatus.getStreaming()) {

            HttpURLConnection urlConnection = null;
            try {
                urlConnection = ServerRequest.setupConnectionWithAuth(getActivity(), "GET", "camerasnapshot?node=garage", null);
                final Bitmap bmp = BitmapFactory.decodeStream(urlConnection.getInputStream());

                Activity activity = getActivity();
                if (mStatus.getStreaming() && activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImageView.setImageBitmap(bmp);
                        }
                    });
                }
            } catch(Exception e) {
                System.out.println("Error downloading snapshot - " + e.getMessage());
                getMainActivity().createAndShowDialogFromTask(e, "Error downloading snapshot");
                mStatus.setStreaming(false);
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
        }
    }

    private class StreamingStatus
    {
        private boolean mStreaming;
        public StreamingStatus() { mStreaming = false; }

        public boolean getStreaming() { return mStreaming; }
        public void setStreaming(boolean streaming){ mStreaming = streaming; }
    }

}
