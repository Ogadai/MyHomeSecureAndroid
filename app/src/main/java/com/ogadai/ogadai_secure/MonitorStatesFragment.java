package com.ogadai.ogadai_secure;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.ogadai.ogadai_secure.auth.CachedToken;
import com.ogadai.ogadai_secure.auth.ITokenCache;
import com.ogadai.ogadai_secure.auth.TokenCache;
import com.ogadai.ogadai_secure.awaystatus.AwayStatusUpdate;
import com.ogadai.ogadai_secure.awaystatus.IAwayStatusUpdate;
import com.ogadai.ogadai_secure.awaystatus.ManageAwayStatus;
import com.ogadai.ogadai_secure.messages.ConnectionStatusMessage;
import com.ogadai.ogadai_secure.messages.MessageBase;
import com.ogadai.ogadai_secure.messages.UpdateStatesMessage;
import com.ogadai.ogadai_secure.messages.UserCheckInOutMessage;
import com.ogadai.ogadai_secure.socket.HomeSecureSocket;
import com.ogadai.ogadai_secure.socket.IHomeSecureSocket;
import com.ogadai.ogadai_secure.socket.IHomeSecureSocketClient;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class MonitorStatesFragment extends MainFragment implements IHomeSecureSocketClient {
    /**
     * Adapter to sync the state list with the view
     */
    private StateItemAdapter mAdapter;
    private ArrayList<StateItem> mStates = new ArrayList<StateItem>();

    private ListView mListViewStates;

    private IHomeSecureSocket mSocket;

    private Switch mAwaySwitch;
    private TextView mHubDisconnected;

    private StateView mStateView;

    private static ArrayList<StateImage> mStateImages = null;

    public MonitorStatesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.initialise();
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_monitor_states, container, false);

        // Create an adapter to bind the items with the view
        mAdapter = new StateItemAdapter(this.getContext(), R.layout.row_list_monitor_states, mStates);
        mAdapter.setStateClickListener(new StateItemAdapter.OnStateClickListener() {
            @Override
            public void StateClicked(StateItem item) {
                ChangedAwayState(item);
            }
        });

        mListViewStates = (ListView) rootView.findViewById(R.id.listViewMonitorStates);
//        mListViewStates.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });
        mListViewStates.setAdapter(mAdapter);

        mAwaySwitch = (Switch) rootView.findViewById(R.id.switchAway);
        mAwaySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean away = mAwaySwitch.isChecked();
                final String action = away ? ManageAwayStatus.EXITED_EVENT : ManageAwayStatus.ENTERED_EVENT;
                ChangedAwayState(action);
            }
        });

        mStateView = (StateView) rootView.findViewById(R.id.stateDiagram);
        mStateView.setStates(mStates);

        mHubDisconnected = (TextView) rootView.findViewById(R.id.hubConnectedStatus);

        return rootView;
    }

    public void connect() {
        ITokenCache tokenCache = new TokenCache(getActivity(), TokenCache.GOOGLE_PREFFILE);
        CachedToken cachedToken = tokenCache.get();
        if (cachedToken == null) {
            doAuthenticate(true);
            return;
        }
        showProgressBar();

        if (mSocket != null) {
            mSocket.Disconnect();
        } else {
            mSocket = new HomeSecureSocket(this);
        }
        mSocket.Connect(cachedToken.getToken());
    }

    @Override
    public void onStart() {
        super.onStart();
        connect();

        System.out.println("Started monitor states fragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        mSocket.Disconnect();

        System.out.println("Stopped monitor states fragment");
    }

    @Override
    public void connected() {
        System.out.println("Connected to server");
        hideProgressBar();

        if (mStateImages == null) {
            getStatusImageInfo();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStateView.setStateImages(mStateImages);
                    mListViewStates.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    @Override
    public void connectionError(Exception ex) {
        System.out.println("Error connecting to server - " + ex.getMessage());
        hideProgressBar();

        if (ex instanceof AuthenticationException && ((AuthenticationException) ex).getHttpStatusCode() == 401) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doAuthenticate(true);
                }
            });
        } else {
            createAndShowDialogFromTask(ex, "Connection error");
        }
    }

    @Override
    public void disconnected(boolean error) {
        System.out.println("Disconnected from server - " + (error ? "error" : "no error"));
        if (error) {
            createAndShowDialogFromTask("Disconnected from server", "Disconnected");
        }

        mHubDisconnected.setVisibility(View.VISIBLE);
        mHubDisconnected.setText("Phone disconnected");
    }

    private void ChangedAwayState(StateItem state) {
        final String action = state.getActive() ? ManageAwayStatus.EXITED_EVENT : ManageAwayStatus.ENTERED_EVENT;
        ChangedAwayState(action);
    }

    private void ChangedAwayState(final String action) {
        ManageAwayStatus.setAwayStatus(getActivity(), action);
    }

    @Override
    public void showError(Exception e, String error) {
        System.out.println(error + " - " + e.getMessage());
        createAndShowDialogFromTask(e, error);
    }

    @Override
    public void messageReceived(final String message) {
        System.out.println(message);

        try {
            MessageBase baseMessage = MessageBase.FromJSON(message);

            if (baseMessage.getMethod().compareTo("ChangeStates") == 0) {
                final UpdateStatesMessage statesMessage = UpdateStatesMessage.FromJSON(message);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        updatedStatesMessage(statesMessage);
                    }
                });
            } else if (baseMessage.getMethod().compareTo("UserCheckInOut") == 0) {
                final UserCheckInOutMessage userCheckInOutMessage = UserCheckInOutMessage.FromJSON(message);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        userAwayMessage(userCheckInOutMessage);
                    }
                });
            } else if (baseMessage.getMethod().compareTo("ConnectionStatus") == 0) {
                final ConnectionStatusMessage connectionStatusMessage = ConnectionStatusMessage.FromJSON(message);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        hubConnectionStatusMessage(connectionStatusMessage);
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
            createAndShowDialogFromTask(e, "Error showing states");
        }
    }

    private void updatedStatesMessage(final UpdateStatesMessage statesMessage) {
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

        try {
            mStateView.notifyDataSetChanged();
        } catch(Exception e) {
            createAndShowDialogFromTask(e, "Error updating state view");
        }
    }

    private void userAwayMessage(UserCheckInOutMessage userCheckInOutMessage) {
        if (userCheckInOutMessage.getCurrentUser()) {
            mAwaySwitch.setChecked(userCheckInOutMessage.getAway());
        }
    }


    private void hubConnectionStatusMessage(ConnectionStatusMessage connectionStatusMessage) {
        boolean connected = connectionStatusMessage.getConnected();
        mHubDisconnected.setVisibility(connected ? View.INVISIBLE : View.VISIBLE);

        if (!connected) {
            mHubDisconnected.setText("Hub disconnected");
        }
    }


    private void getStatusImageInfo() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @SuppressWarnings("ResourceType")
            @Override
            protected Void doInBackground(Void... urls) {
                try {
                    StatusImageInfo[] items = ServerRequest.get(getActivity(), "statusimage", StatusImageInfo[].class);

                    Arrays.sort(items, new Comparator<StatusImageInfo>() {
                        @Override
                        public int compare(StatusImageInfo lhs, StatusImageInfo rhs) {
                            return lhs.getZIndex() - rhs.getZIndex();
                        }
                    });

                    final ArrayList<StateImage> stateImages = new ArrayList<>();
                    for(int n = 0; n < items.length; n++) {
                        StatusImageInfo imageInfo = items[n];
                        StateImage stateImage = null;
                        for(int m = 0; m < stateImages.size(); m++) {
                            StateImage enumItem = stateImages.get(m);
                            if (enumItem.getState().compareTo(imageInfo.getState()) == 0) {
                                stateImage = enumItem;
                            }
                        }
                        if (stateImage == null) {
                            stateImage = new StateImage(imageInfo.getState());
                            stateImages.add(stateImage);
                        }

                        Bitmap stateBitmap = downloadBitmap(imageInfo.getFileName(), imageInfo.getUpdated());
                        if (imageInfo.getActive()) {
                            stateImage.setActiveBitmap(stateBitmap);
                        } else {
                            stateImage.setInactiveBitmap(stateBitmap);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Set the images in the view
                            mStateImages = stateImages;
                            mStateView.setStateImages(mStateImages);

                            if (mStateImages.size() > 0) {
                                mListViewStates.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                } catch (Exception e) {
                    showError(e, "Getting state images");
                }
                return null;
            }
        };
        task.execute();
        // StatusImageInfo[]
    }

    private Bitmap downloadBitmap(String fileName, Date updated) {
        Bitmap cachedBitmap = getCachedBitmap(fileName, updated);
        if (cachedBitmap != null) {
            return cachedBitmap;
        }

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = ServerRequest.setupConnectionWithAuth(getActivity(), "GET", "statusimage/" + fileName, null);

            cacheBitmapStream(fileName, urlConnection.getInputStream());

            return getCachedBitmap(fileName, updated);
        } catch(Exception e) {
            System.out.println("Error downloading snapshot - " + e.getMessage());
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }

        return null;
    }

    private Bitmap getCachedBitmap(String fileName, Date updated) {
        try {
            File bitmapFile = getCacheFile(fileName);

            if (bitmapFile.exists()) {
                Date lastModified = new Date(bitmapFile.lastModified());
                if (lastModified.compareTo(updated) > 0) {
                    return BitmapFactory.decodeStream(new FileInputStream(bitmapFile));
                }
            }
            return null;
        } catch (FileNotFoundException e) {
            System.out.println("Error getting cached bitmap - " + e.getMessage());
            return null;
        }
    }

    private void cacheBitmapStream(String fileName, InputStream inStream) {
        try {
            File bitmapFile = getCacheFile(fileName);
            OutputStream outStream = new FileOutputStream(bitmapFile);

            copyStream(inStream, outStream);

            inStream.close();
            outStream.close();
        } catch (IOException e) {
            System.out.println("Error caching bitmap - " + e.getMessage());
        }
    }


    private File getCacheFile(String fileName) {
        return new File(getContext().getCacheDir(), fileName + ".png");
    }

    private void copyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
