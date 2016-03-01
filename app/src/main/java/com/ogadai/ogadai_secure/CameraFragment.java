package com.ogadai.ogadai_secure;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.net.HttpURLConnection;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends MainFragment {

    private ImageView mImageView;
    private boolean mInitialising = false;

    private static Bitmap mLastImage = null;

    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.initialise();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        // Create an adapter to bind the items with the view
        mImageView = (ImageView) view.findViewById(R.id.imageView);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mLastImage != null) {
            mImageView.setImageBitmap(mLastImage);
        } else {
            showProgressBar();
            mInitialising = true;
        }
        startVideoTask();

        System.out.println("Started camera fragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        stopVideoTask();

        if (mInitialising) {
            hideProgressBar();
            mInitialising = false;
        }

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

    private StreamingStatus mStatus = null;

    private void startVideoTask() {
        stopVideoTask();
        mStatus = new StreamingStatus();

        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startVideo();
                } catch(Exception e) {
                    System.out.println("Error streaming camera snapshots - " + e.getMessage());
                    createAndShowDialogFromTask(e, "Error streaming camera snapshots");
                }
            }
        });
        worker.start();
    }
    private void stopVideoTask() {
        if (mStatus != null) {
            mStatus.setStreaming(false);
            mStatus = null;
        }
    }

    private void startVideo() {
        final StreamingStatus status = mStatus;
        if (status.getStreaming()) return;
        status.setStreaming(true);

        while(status.getStreaming()) {

            HttpURLConnection urlConnection = null;
            try {
                urlConnection = ServerRequest.setupConnectionWithAuth(getActivity(), "GET", "camerasnapshot?node=garage", null);
                mLastImage = BitmapFactory.decodeStream(urlConnection.getInputStream());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(mLastImage);
                    }
                });
            } catch(Exception e) {
                System.out.println("Error downloading snapshot - " + e.getMessage());
                createAndShowDialogFromTask(e, "Error downloading snapshot");
                status.setStreaming(false);
            } finally {
                if (urlConnection != null) urlConnection.disconnect();

                if (mInitialising) {
                    hideProgressBar();
                    mInitialising = false;
                }
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
