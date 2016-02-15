package com.ogadai.ogadai_secure;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class MonitorStatesFragment extends Fragment implements IAuthenticateClient, IHomeSecureSocketClient {
    /**
     * Adapter to sync the state list with the view
     */
    private StateItemAdapter mAdapter;
    private ArrayList<StateItem> mStates;
    private IGoogleAuthenticator mAuthenticator;

    private IHomeSecureSocket mSocket;

    public MonitorStatesFragment() {
        // Required empty public constructor
    }

    private IMainActivity getMainActivity() {
        return (IMainActivity)getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_monitor_states, container, false);

        // Create an adapter to bind the items with the view
        mStates = new ArrayList<StateItem>();
        mAdapter = new StateItemAdapter(this.getContext(), R.layout.row_list_monitor_states, mStates);

        ListView listViewStates = (ListView) rootView.findViewById(R.id.listViewMonitorStates);
        listViewStates.setAdapter(mAdapter);

        // Initialize the progress bar
        mSocket = new HomeSecureSocket(this);

        return rootView;
    }


    private void doAuthenticate(boolean update)
    {
        try {
            getMainActivity().showProgressBar();

            mAuthenticator = new GoogleAuthenticator();
            mAuthenticator.authenticate(update, this);
        } catch (Exception e) {
            System.out.println("Authenticate error - " + e.getMessage());
            getMainActivity().createAndShowDialog(e, "Error");
        }
    }

    public void authenticated(MobileServiceUser user){
        String token = user.getAuthenticationToken();
        mSocket.Connect(token);
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getActivity().getSharedPreferences(name, mode);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSocket.Disconnect();

        System.out.println("Stopped fragment");
    }

    @Override
    public void onStart() {
        super.onStart();
        doAuthenticate(false);

        System.out.println("Started fragment");
    }

    @Override
    public void connected() {
        System.out.println("Connected to server");
        getMainActivity().hideProgressBar();
    }

    @Override
    public void disconnected(boolean error) {
        System.out.println("Disconnected from server - " + (error ? "error" : "no error"));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doAuthenticate(true);
            }
        });
    }

    @Override
    public void showError(Exception e, String error)
    {
        System.out.println(error + " - " + e.getMessage());
        getMainActivity().createAndShowDialogFromTask(e, error);
    }

    @Override
    public void messageReceived(String message) {
        System.out.println(message);

        final UpdateStatesMessage statesMessage;
        try {
            statesMessage = UpdateStatesMessage.FromJSON(message);

            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
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
            });
        } catch (IOException e) {
            e.printStackTrace();
            getMainActivity().createAndShowDialogFromTask(e, "Error showing states");
        }
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
