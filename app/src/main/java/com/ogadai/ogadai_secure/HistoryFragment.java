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

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class HistoryFragment extends Fragment {

    private HistoryRecyclerViewAdapter mHistoryAdapter;
    private List<HistoryItem> mHistoryList = new ArrayList<HistoryItem>();

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
        getMainActivity().showProgressBar();

        AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
            protected Void doInBackground(String... urls) {
                try {
                    HistoryItem[] items = ServerRequest.get(getActivity(), "log", HistoryItem[].class);
                    Arrays.sort(items, new Comparator<HistoryItem>() {
                        @Override
                        public int compare(HistoryItem lhs, HistoryItem rhs) {
                            return rhs.getTime().compareTo(lhs.getTime());
                        }
                    });

                    updateHistoryList(items);

                    return null;
                }
                catch(AuthenticationException authEx) {
                    System.out.println("error getting history: " + authEx.toString());
                    getMainActivity().doAuthenticate(true);
                    return null;
                }
                catch(Exception e) {
                    System.out.println("error getting JSON: " + e.toString());
                    getMainActivity().createAndShowDialogFromTask(e, "Error getting log");
                    return null;
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

    private void updateHistoryList(final HistoryItem[] items) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mHistoryList.clear();
                for(HistoryItem item : items) {
                    mHistoryList.add(item);
                }
                mHistoryAdapter.notifyDataSetChanged();
            }
        });
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
