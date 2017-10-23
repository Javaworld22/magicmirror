package com.ai.beta.magicmirror;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Michael on 9/20/2017.
 */

public class MagicMirrorApiClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
        //    OkHttpClient client = new OkHttpClient();
        //    client.
            retrofit = new Retrofit.Builder()
                    .baseUrl(MagicMirror.SERVER_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
