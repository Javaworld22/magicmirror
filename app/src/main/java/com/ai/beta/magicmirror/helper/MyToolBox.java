package com.ai.beta.magicmirror.helper;

/**
 * Created by user on 8/13/2017.
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.support.v7.app.AlertDialog;
import android.util.Log;


public class MyToolBox {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
            Log.e("NETWORK","network avail...");
        }
        else{
            Log.e("NETWORK", "no network avail...");
        }

        return isAvailable;
    }

    public static boolean isMinimumCharacters(String field, int min) {
        return field.length() > min;
    }

    public static void AlertMessage(Context context, String alertMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String mTitle = "Oops!";
        if(alertMessage.contains("An email containing a password reset link has been sent to")){
            mTitle = "Yay!";
        }
        builder.setTitle(mTitle)
                .setMessage(alertMessage)
                .setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void AlertMessage(Context context, String alertTitle, String alertMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String mTitle = alertTitle;
        if(alertMessage.contains("An email containing a password reset link has been sent to")){
            mTitle = "Yay!";
        }
        builder.setTitle(mTitle)
                .setMessage(alertMessage)
                .setPositiveButton(android.R.string.ok, null);


        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
