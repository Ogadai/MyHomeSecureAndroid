package com.ogadai.ogadai_secure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryFragment extends Fragment implements IAuthenticateClient {

    private HistoryRecyclerViewAdapter mHistoryAdapter;
    private List<HistoryItem> mHistoryList = new ArrayList<HistoryItem>();

    private IGoogleAuthenticator mAuthenticator;
    private IServerRequest mServerRequest;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HistoryFragment() {
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
        View view = inflater.inflate(R.layout.fragment_history_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            mHistoryAdapter = new HistoryRecyclerViewAdapter(mHistoryList);
            recyclerView.setAdapter(mHistoryAdapter);
        }

        return view;
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

        mServerRequest = new ServerRequest("RhCLppCOuzkwkzZcDDLGcZQTOTwUBj90", token);

        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            protected String doInBackground(String... urls) {
                try {
                    final String result = mServerRequest.get("https://ogadai-secure.azure-mobile.net/api/log");

                    System.out.println("Response: " + result);

                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            HistoryItem[] items = HistoryItem.FromJSONArray(result);
                            mHistoryList.clear();
                            for(HistoryItem item : items) {
                                mHistoryList.add(item);
                            }
                            mHistoryAdapter.notifyDataSetChanged();
                        }
                    });

                    return result;
                }
                catch(Exception e) {
                    System.out.println("error getting JSON: " + e.toString());
                    getMainActivity().createAndShowDialogFromTask(e, "Error getting log");
                    return "";
                }
                finally {
                    getMainActivity().hideProgressBar();
                }
            }

            protected void onPostExecute() {

            }
        };
        task.execute();
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getActivity().getSharedPreferences(name, mode);
    }

    @Override
    public void showError(Exception e, String error)
    {
        System.out.println(error + " - " + e.getMessage());
        getMainActivity().createAndShowDialogFromTask(e, error);
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("Stopped fragment");
    }

    @Override
    public void onStart() {
        super.onStart();
        doAuthenticate(false);

        System.out.println("Started fragment");
    }
}
