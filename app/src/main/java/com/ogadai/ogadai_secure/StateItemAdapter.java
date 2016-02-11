package com.ogadai.ogadai_secure;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Switch;

import java.util.List;

/**
 * Created by alee on 11/02/2016.
 */
public class StateItemAdapter extends ArrayAdapter<StateItem> {
    /**
     * Adapter context
     */
    Context mContext;

    /**
     * Adapter View layout
     */
    int mLayoutResourceId;

    public StateItemAdapter(Context context, int layoutResourceId, List<StateItem> states) {
        super(context, layoutResourceId, states);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    /**
     * Returns the view for a specific item on the list
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        final StateItem currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }

        row.setTag(currentItem);
        final Switch theSwitch = (Switch) row.findViewById(R.id.switchStateActive);
        theSwitch.setText(currentItem.getName());
        theSwitch.setChecked(currentItem.getActive());

        theSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                theSwitch.setChecked(currentItem.getActive());
            }
        });

        return row;
    }
}
