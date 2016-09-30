package com.ogadai.ogadai_secure;


import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends MainFragment {
    private CameraView mCameraView;
    private ArrayList<CameraFeed> mCameraFeeds;
    private AsyncTask<String, Void, Void> mTask = null;

    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.initialise();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        mCameraView = (CameraView)view.findViewById(R.id.cameraView);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        connect();

        System.out.println("Started camera fragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        disconnect();
        System.out.println("Stopped camera fragment");
    }

    private void connect(){
        showProgressBar();

        clearTask();
        disconnect();

        mTask = new AsyncTask<String, Void, Void>() {
            protected Void doInBackground(String... urls) {
                try {
                    final CameraInfo[] cameras = ServerRequest.get(getActivity(), "cameralist", CameraInfo[].class);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            CameraInfo[] dummy = new CameraInfo[] {
//                                    new CameraInfo("Garage", "garage"),
//                                    new CameraInfo("Other", "garage")
//                            };
                            initialiseCameras(cameras);
                        }
                    });
                    mTask = null;
                    return null;
                }
                catch(AuthenticationException authEx) {
                    System.out.println("error getting cameras: " + authEx.toString());
                    doAuthenticate(true);
                    return null;
                }
                catch(Exception e) {
                    System.out.println("error getting JSON: " + e.toString());
                    createAndShowDialogFromTask(e, "Error getting cameras");
                    return null;
                }
                finally {
                    hideProgressBar();
                }
            }

            protected void onPostExecute() {

            }
        };
        mTask.execute();
    }

    private void initialiseCameras(CameraInfo[] cameras) {
        if (cameras.length > 0) {
            mCameraFeeds = new ArrayList<>();
            for (CameraInfo camera: cameras) {
                CameraFeed cameraFeed = new CameraFeed(getActivity(), camera.getNode());
                cameraFeed.startVideoTask();
                mCameraFeeds.add(cameraFeed);
            }

            mCameraView.setCameras(mCameraFeeds);
        } else {
            createAndShowDialogFromTask("No cameras to show", "Cameras");
        }
    }

    private void disconnect() {
        if (mCameraFeeds != null) {
            for(CameraFeed cameraFeed: mCameraFeeds) {
                cameraFeed.stopVideoTask();
            }
            mCameraFeeds = null;
        }
    }

    private void clearTask() {
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

}
