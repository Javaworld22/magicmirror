package com.ai.beta.magicmirror;

/**
 * Created by user on 9/13/2017.
 */

import android.app.Application;



public class MagicMirror extends Application { //Application{

    public static final String SERVER_BASE_URL = "http://magicmirrordotai.pythonanywhere.com";
    public static final String SIGN_UP_API = "/sign_up"; //method = post; params = username,gender,password

}
