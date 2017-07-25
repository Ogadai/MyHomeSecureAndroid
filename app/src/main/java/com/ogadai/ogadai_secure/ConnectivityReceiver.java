package com.ogadai.ogadai_secure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConnectivityReceiver extends BroadcastReceiver {
    public ConnectivityReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        ConnectivityManager cm =
//                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        boolean connected = activeNetwork != null &&
//                activeNetwork.isConnectedOrConnecting();
//
//        if (connected) {
//            IManageAwayStatus manageAwayStatus = new ManageAwayStatus(context);
//            manageAwayStatus.retryPending();
//        }
//        System.out.println("Network is " + (connected ? "connected" : "disconnected"));
    }
}
