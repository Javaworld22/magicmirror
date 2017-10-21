package com.ai.beta.magicmirror;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    private ImageView imageHolder;
    private Button captureImageButton;
    private static final int CAMERA_REQUEST = 1888;
    private MagicMirrorApiInterface mirrorApiInterface;

    private String mUsername;
    private String mPassword;
    private String mGender;

    private File mediaFile;

    private File mMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_camera);
        Intent intent = getIntent();
        mUsername = intent.getStringExtra("username");
       mPassword =  intent.getStringExtra("password");
        mGender = intent.getStringExtra("gender");

     //   Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      //  setSupportActionBar(toolbar);

      /**  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); **/

        imageHolder = (ImageView) findViewById(R.id.captured_photo);
        captureImageButton = (Button) findViewById(R.id.photo_button);
        captureImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
                    public void onClick(View view){
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,CAMERA_REQUEST);
            }
        });

        mirrorApiInterface = MagicMirrorApiClient.getClient().create(MagicMirrorApiInterface.class);


    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == CAMERA_REQUEST){
            Bitmap photo = (Bitmap)data.getExtras().get("data");
            imageHolder.setImageBitmap(photo);
           // mMediaUri = data.getData();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
            byte[] reducedData = outputStream.toByteArray();
            Log.e(TAG, "  Username "+mUsername+" Password: "+mPassword+" Gender: "+mGender);
            if(mUsername != null && mPassword != null && mGender != null)
              createAccount(mUsername,mPassword,mGender,reducedData);
        }
    }

    private void createAccount(String username, String password, String gender, byte[] bitmapdata){
        Log.e(TAG, "CreateAccount");
        if(isExternalStorageAvailable()){
            Log.e(TAG, "isExternalStorageAvalable");
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "MagicMirror");

            if(!mediaStorageDir.exists()){
                if(!mediaStorageDir.mkdirs()){
                    return ;
                }
            }

            Date now = new Date();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now);

            String path = mediaStorageDir.getPath() + File.separator;

            mediaFile = new File(path+"IMG_"+timestamp+".jpg");

            try {
                FileOutputStream fos = new FileOutputStream(mediaFile); //FileNotFound
                fos.write(bitmapdata);//IOException
                fos.flush();
                fos.close();
            }catch (FileNotFoundException e){
                Log.e(TAG, "Error occured. FileNotFoundException"+e.getMessage());
            }catch (IOException a){
                Log.e(TAG, "Error occured IOException"+a.getMessage());
            }
        }
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), mediaFile);


      MultipartBody.Part body =
                MultipartBody.Part.createFormData("zip", mediaFile.getName(), requestFile);

        RequestBody fileUsername =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), username);

        RequestBody filePassword =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), password);

        RequestBody fileGender =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), gender);



        Call<ResponseBody> call = mirrorApiInterface.signIn(fileUsername,filePassword,fileGender,body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //TODO: set photo to imageview, toast success
                if (response.code() == 200) {
                    Log.e(TAG, response.body().toString());
                 //   Picasso.with(EditProfileActivity.this)
                    //        .load(mediaFile)
                     //       .into(mPhoto);
                    Toast.makeText(CameraActivity.this, "Successful registration", Toast.LENGTH_SHORT).show();
                   // currentUser.setProfilePhoto(fileName);
                   // mSharedPref.setCurrentUser(currentUser.toString());
                } else {
                    Log.e(TAG, response.body().toString());
                    Log.e(TAG, " "+response.code());
                    Toast.makeText(CameraActivity.this, "Could not update profile photo", Toast.LENGTH_SHORT).show();
                }

               // mProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.e(TAG, "Retrofit: "+ t.getMessage());
                Toast.makeText(CameraActivity.this, "Error uploading photo. Try again", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onFailure "+t.getMessage());
               // mProgressDialog.dismiss();
            }
        });

    }

    private boolean isExternalStorageAvailable(){
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)){
            Log.e(TAG, " true");
            return true;
        }
        else{
            Log.e(TAG, " false");
            return false;
        }
    }

}
