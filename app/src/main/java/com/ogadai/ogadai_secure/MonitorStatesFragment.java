package com.ogadai.ogadai_secure;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ogadai.ogadai_secure.auth.CachedToken;
import com.ogadai.ogadai_secure.auth.ITokenCache;
import com.ogadai.ogadai_secure.auth.TokenCache;
import com.ogadai.ogadai_secure.awaystatus.AwayStatusUpdate;
import com.ogadai.ogadai_secure.awaystatus.IAwayStatusUpdate;
import com.ogadai.ogadai_secure.awaystatus.ManageAwayStatus;
import com.ogadai.ogadai_secure.messages.MessageBase;
import com.ogadai.ogadai_secure.messages.UpdateStatesMessage;
import com.ogadai.ogadai_secure.socket.HomeSecureSocket;
import com.ogadai.ogadai_secure.socket.IHomeSecureSocket;
import com.ogadai.ogadai_secure.socket.IHomeSecureSocketClient;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.IOException;
import java.util.ArrayList;

public class MonitorStatesFragment extends MainFragment implements IHomeSecureSocketClient {
    /**
     * Adapter to sync the state list with the view
     */
    private StateItemAdapter mAdapter;
    private ArrayList<StateItem> mStates = new ArrayList<StateItem>();

    private IHomeSecureSocket mSocket;

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

        ListView listViewStates = (ListView) rootView.findViewById(R.id.listViewMonitorStates);
        listViewStates.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        listViewStates.setAdapter(mAdapter);

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
    }

    @Override
    public void connectionError(Exception ex)
    {
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
    }

    private void ChangedAwayState(StateItem state) {
        final String action = state.getActive() ? ManageAwayStatus.EXITED_EVENT : ManageAwayStatus.ENTERED_EVENT;

        AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... urls) {
                try {
                    IAwayStatusUpdate statusUpdate = new AwayStatusUpdate(getActivity());
                    statusUpdate.updateStatus(action);
                } catch(Exception e) {
                    showError(e, "Setting away status");
                }
                return null;
            }
        };
        task.execute(action);
    }

    @Override
    public void showError(Exception e, String error)
    {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
            createAndShowDialogFromTask(e, "Error showing states");
        }
    }

    private void updatedStatesMessage(final UpdateStatesMessage statesMessage)
    {
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
