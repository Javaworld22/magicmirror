package com.ai.beta.magicmirror;

/**
 * Created by user on 9/13/2017.
 */


import java.util.List;
import java.util.StringTokenizer;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
//import okhttp3.Response;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.Response;


public interface MagicMirrorApiInterface {

    @Multipart
    @POST(MagicMirror.SIGN_UP_API)
    Call<ResponseBody> signIn(@Part("username") RequestBody username,
                                          @Part("password") RequestBody password,
                                          @Part("gender") RequestBody gender,
                                          @Part MultipartBody.Part file);
}
