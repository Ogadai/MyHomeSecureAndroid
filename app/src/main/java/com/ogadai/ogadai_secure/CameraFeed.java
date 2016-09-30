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
    private Listener mListener;

    private StreamingStatus mStatus = null;

    private static HashMap<String, Bitmap> mLastImages = new HashMap<String, Bitmap>();
    private static Random mRandGen = new Random();

    public static void setLastImage(String node, Bitmap image) {
        mLastImages.put(node, image);
    }

    public CameraFeed(Activity activity, String node) {
        mActivity = activity;
        mNode = node;
    }

    public void startVideoTask() {
        if (mListener != null) {
            mListener.updated();
        }

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
    public void stopVideoTask() {
        if (mStatus != null) {
            mStatus.setStreaming(false);
            mStatus = null;
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

    private void startVideo() {
        final StreamingStatus status = mStatus;
        if (status.getStreaming()) return;
        status.setStreaming(true);

        int index = mRandGen.nextInt(1000000);
        while(status.getStreaming()) {

            HttpURLConnection urlConnection = null;
            try {
                urlConnection = ServerRequest.setupConnectionWithAuth(mActivity, "GET", "camerasnapshot?node=" + mNode + "&i=" + Integer.toString(index), null);
                final Bitmap cameraImage = BitmapFactory.decodeStream(urlConnection.getInputStream());
                mLastImages.put(mNode, cameraImage);
                index++;

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null) {
                            mListener.updated();
                        }
                    }
                });
            } catch(Exception e) {
                System.out.println("Error downloading snapshot - " + e.getMessage());
                status.setStreaming(false);
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

    public interface Listener
    {
        void updated();
    }
}
