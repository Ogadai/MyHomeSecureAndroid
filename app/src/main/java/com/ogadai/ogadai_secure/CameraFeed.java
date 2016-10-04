package com.ogadai.ogadai_secure;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by alee on 30/09/2016.
 */

public class CameraFeed {
    private Activity mActivity;
    private String mNode;
    private String mName;
    private Listener mListener;
    private boolean mStreaming;

    private StreamingStatus mStatus = null;

    private static HashMap<String, Bitmap> mLastImages = new HashMap<String, Bitmap>();
    private static Random mRandGen = new Random();

    public static void setLastImage(String node, Bitmap image) {
        mLastImages.put(node, image);
    }

    public CameraFeed(Activity activity, String node, String name) {
        mActivity = activity;
        mNode = node;
        mName = name;
        takeSnapshotTask();
    }

    public void close() {
        mListener = null;
        stopVideoTask();
    }

    public String getNode() { return mNode; }
    public String getName() { return mName; }

    public boolean getStreaming() {
        return mStreaming;
    }
    public void setStreaming(boolean streaming) {
        mStreaming = streaming;
        if (streaming) {
            startVideoTask();
        } else {
            stopVideoTask();
        }
    }

    public void setFeedListener(Listener listener) {
        mListener = listener;

        if (mListener != null) {
            mListener.updated();
        }
    }

    public Bitmap getImage() {
        return mLastImages.get(mNode);
    }

    private void startVideoTask() {
        stopVideoTask();
        mStatus = new StreamingStatus();

        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                startVideo();
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

        System.out.println("Started stream from " + mNode);
        while(status.getStreaming()) {
            if (!downloadSnapshot(true)) {
                status.setStreaming(false);
            }
        }
        System.out.println("Closed stream from " + mNode);
    }

    private void takeSnapshotTask() {
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                downloadSnapshot(false);
            }
        });
        worker.start();
    }

    private boolean downloadSnapshot(boolean streaming) {
        HttpURLConnection urlConnection = null;
        boolean result = false;
        try {
            int index = mRandGen.nextInt(1000000);
            String suffix = streaming ? "" : "&singleImage=true";
            System.out.println("Downloading " + (streaming ? "stream" : "single") + " snapshot from " + mNode);

            urlConnection = ServerRequest.setupConnectionWithAuth(mActivity, "GET", "camerasnapshot?node=" + mNode +
                    "&i=" + Integer.toString(index) + suffix, null);
            final Bitmap cameraImage = BitmapFactory.decodeStream(urlConnection.getInputStream());
            mLastImages.put(mNode, cameraImage);

            System.out.println("Downloaded " + (streaming ? "stream" : "single") + " snapshot from " + mNode);

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.updated();
                    }
                }
            });
            result = true;
        } catch(Exception e) {
            System.out.println("Error downloading snapshot - " + e.getMessage());
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
        return result;
    }

    private class StreamingStatus
    {
        private boolean mStreaming;
        public StreamingStatus() { mStreaming = false; }

        public boolean getStreaming() { return mStreaming; }
        public void setStreaming(boolean streaming){ mStreaming = streaming; }
    }

    public interface Listener
    {
        void updated();
    }
}
