package com.ai.beta.magicmirror.helper;

/**
 * Created by user on 8/13/2017.
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
}
