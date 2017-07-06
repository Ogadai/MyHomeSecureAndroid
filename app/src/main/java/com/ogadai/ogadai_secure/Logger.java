package com.ogadai.ogadai_secure;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by alee on 06/07/2017.
 */

public class Logger {
    private static Context mContext;
    private static ArrayList<String> mMessages;
    private static Semaphore mMessagesLock = new Semaphore(1);

    private static ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);;
    private static ScheduledFuture mSaveTimer;

    private static SimpleDateFormat mDateFormatter = new SimpleDateFormat("HH:mm");
    private static String LOG_FILENAME = "home_secure_log";
    private static String LINE_SEPARATOR = System.getProperty("line.separator");

    public static void setContext(Context context) {
        mContext = context;
    }

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
            load();

            if (mMessages != null) {
                String time = mDateFormatter.format(new Date());

                mMessages.add(time + " | " + message);
                if (mMessages.size() > 100) {
                    mMessages.remove(0);
                }
                save(mMessages.toArray(new String[mMessages.size()]));
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
            load();

            if (mMessages != null) {
                return (ArrayList<String>) mMessages.clone();
            }
            return null;
        } catch (InterruptedException e) {
            Log.e("Logger", "Couldn't get lock", e);
        } finally {
            mMessagesLock.release();
        }
        return new ArrayList<>();
    }


    private static void save(final String[] entries) {
        if (mSaveTimer != null) {
            mSaveTimer.cancel(false);
        }

        mSaveTimer = mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                mSaveTimer = null;

                OutputStreamWriter outputStreamWriter = null;
                try {
                    outputStreamWriter = new OutputStreamWriter(mContext.openFileOutput(LOG_FILENAME, Context.MODE_PRIVATE));

                    for(String line: entries) {
                        outputStreamWriter.write(line);
                        outputStreamWriter.write(LINE_SEPARATOR);
                    }
                } catch (Exception e) {
                    Log.e("Logger", "Error saving log file", e);
                } finally {
                    if (outputStreamWriter != null) {
                        try {
                            outputStreamWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Write log file
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }

    private static void load() {
        if (mMessages == null && mContext != null) {
            mMessages = new ArrayList<>();

            // load log file
            InputStreamReader inputStreamReader = null;
            try {
                inputStreamReader = new InputStreamReader(mContext.openFileInput(LOG_FILENAME));
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line = "";
                while( (line = bufferedReader.readLine()) != null ) {
                    mMessages.add(line);
                }
            } catch (Exception e) {
                Log.e("Logger", "Error loading log file", e);
            } finally {
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
