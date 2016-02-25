package com.ogadai.ogadai_secure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by alee on 16/02/2016.
 */
public class HistoryItem {
    /**
     * Name
     */
    @com.google.gson.annotations.SerializedName("message")
    private String mMessage;

    /**
     * Active
     */
    @com.google.gson.annotations.SerializedName("time")
    private Date mTime;

    @Override
    public String toString() {
        return getMessage() + " - " + getTime();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StateItem && ((HistoryItem)o).getMessage() == getMessage();
    }

    public HistoryItem(String message, Date time) {
        setMessage(message);
        setTime(time);
    }

    public String getMessage() { return mMessage; }
    public final void setMessage(String message) { mMessage = message; }

    public Date getTime() { return mTime; }
    public final void setTime(Date time) { mTime = time; }
}
