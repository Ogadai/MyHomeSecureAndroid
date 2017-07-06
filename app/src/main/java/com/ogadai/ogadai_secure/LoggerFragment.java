package com.ogadai.ogadai_secure;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by alee on 06/07/2017.
 */

public class LoggerFragment extends MainFragment {
    private ArrayList<String> mLogList;
    private ArrayAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LoggerFragment() {
        mLogList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.initialise();
        View view = inflater.inflate(R.layout.fragment_logger, container, false);

        mAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1,
                mLogList);

        ListView listView = (ListView)view.findViewById(R.id.list);
        listView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateLogList(Logger.getMessages());
    }

    private void updateLogList(final ArrayList<String> items) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mLogList.clear();
                mLogList.addAll(items);
                mAdapter.notifyDataSetChanged();
            }
        });
    }
}
