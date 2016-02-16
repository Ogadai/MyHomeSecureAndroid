package com.ogadai.ogadai_secure;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {

    private final List<HistoryItem> mValues;

    public HistoryRecyclerViewAdapter(List<HistoryItem> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_history, parent, false);
        return new ViewHolder(view);
    }

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("dd MMM");
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("h:mm a");

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mMessageView.setText(mValues.get(position).getMessage());

        Date messageTime = mValues.get(position).getTime();

        boolean dateVisible = false;
        if (position == 0) {
            dateVisible = !sameDate(messageTime, new Date());
        } else {
            Date previousTime = mValues.get(position - 1).getTime();
            dateVisible = !sameDate(messageTime, previousTime);
        }
        holder.mDateView.setVisibility(dateVisible ? View.VISIBLE : View.GONE);

        String dateMessage = sameDate(messageTime, getYesterday())
              ? "Yesterday" : mDateFormat.format(messageTime);

        holder.mDateView.setText(dateMessage);
        holder.mTimeView.setText(mTimeFormat.format(messageTime));
    }

    private final SimpleDateFormat mDateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
    private boolean sameDate(Date lhs, Date rhs) {
        return  mDateOnlyFormat.format(lhs).compareTo(mDateOnlyFormat.format(rhs)) == 0;
    }

    private Date getYesterday() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mDateView;
        public final TextView mMessageView;
        public final TextView mTimeView;
        public HistoryItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDateView = (TextView) view.findViewById(R.id.date);
            mMessageView = (TextView) view.findViewById(R.id.message);
            mTimeView = (TextView) view.findViewById(R.id.time);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mMessageView.getText() + "'";
        }
    }
}
