package com.ogadai.ogadai_secure;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by alee on 06/07/2017.
 */

public class Logger {
    private static ArrayList<String> mMessages = new ArrayList<>();
    private static Semaphore mMessagesLock = new Semaphore(1);

    private static SimpleDateFormat mDateFormatter = new SimpleDateFormat("HH:mm");

    public static void i(String tag, String message) {
        Log.i(tag, message);
        addMessage(tag + " - " + message);
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
        addMessage("Error: " + tag + " - " + message);
    }

    public static void e(String tag, String message, Throwable e) {
        Log.e(tag, message, e);
        addMessage("Error: " + tag + " - " + message);
    }

    private static void addMessage(String message) {
        try {
            mMessagesLock.acquire();

            String time = mDateFormatter.format(new Date());

            mMessages.add(time + " | " + message);
            if (mMessages.size() > 100) {
                mMessages.remove(0);
            }
        } catch (InterruptedException e) {
            Log.e("Logger", "Couldn't get lock", e);
        } finally {
            mMessagesLock.release();
        }
    }

    public static ArrayList<String> getMessages() {
        try {
            mMessagesLock.acquire();
            return (ArrayList<String>)mMessages.clone();
        } catch (InterruptedException e) {
            Log.e("Logger", "Couldn't get lock", e);
        } finally {
            mMessagesLock.release();
        }
        return new ArrayList<>();
    }
}
