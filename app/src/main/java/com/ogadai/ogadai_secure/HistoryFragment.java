package com.ogadai.ogadai_secure;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ogadai.ogadai_secure.auth.CachedToken;
import com.ogadai.ogadai_secure.auth.ITokenCache;
import com.ogadai.ogadai_secure.auth.TokenCache;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private HistoryRecyclerViewAdapter mHistoryAdapter;
    private List<HistoryItem> mHistoryList = new ArrayList<HistoryItem>();

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

    public void connect(){
        ITokenCache tokenCache = new TokenCache(getActivity());
        CachedToken cachedToken = tokenCache.get();
        if (cachedToken == null) {
            getMainActivity().doAuthenticate(true);
            return;
        }

        getMainActivity().showProgressBar();
        mServerRequest = new ServerRequest("RhCLppCOuzkwkzZcDDLGcZQTOTwUBj90", cachedToken.getToken());

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
                catch(FileNotFoundException fileNotFound) {
                    System.out.println("error getting history: " + fileNotFound.toString());
                    getMainActivity().doAuthenticate(true);
                    return "";
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

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("Stopped history fragment");
    }

    @Override
    public void onStart() {
        super.onStart();
        connect();

        System.out.println("Started history fragment");
    }
}
