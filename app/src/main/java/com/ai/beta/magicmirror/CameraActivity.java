package com.ai.beta.magicmirror;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;

import com.ai.beta.magicmirror.helper.ImageResizer;

import com.ai.beta.magicmirror.helper.MyToolBox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ConnectTimeoutException;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";


    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int CHOOSE_PHOTO_REQUEST = 1;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public  static final int FILE_SIZE_LIMIT = 1024*1024*10; //10MB

    private ImageView imageHolder;
    private Button captureImageButton;
    private static final int CAMERA_REQUEST = 1888;
    private MagicMirrorApiInterface mirrorApiInterface;

    private String mUsername;
    private String mPassword;
    private String mGender;


    protected Uri mMediaUri;

    private File mediaFile;

    private File mMedia;

    private byte[] fileBytes;


    protected DialogInterface.OnClickListener mDialogListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0:
                            //Capture Image
                            Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); //cos of the broadcast...
                            if (mMediaUri == null){
                                Toast.makeText(CameraActivity.this, "There was a problem capturing your photo", Toast.LENGTH_LONG).show();
                            }
                            else{
                                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                startActivityForResult(captureImageIntent, TAKE_PHOTO_REQUEST);
                            }
                            break;
                        case 1:
                            //Choose Image
                            Intent chooseImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            chooseImageIntent.setType("image/*");
                            //Toast.makeText(getActivity(), "The size of your video must be less than 10MB", Toast.LENGTH_LONG).show();
                            startActivityForResult(chooseImageIntent, CHOOSE_PHOTO_REQUEST);
                            break;
                    }
                }

                private Uri getOutputMediaFileUri(int mediaType) {
                    if(isExternalStorageAvailable()){
                        //storage dir
                        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                getString(R.string.app_name));
                        //subdir
                        if(!mediaStorageDir.exists()){
                            if(!mediaStorageDir.mkdirs()){
                                return null;
                            }
                        }
                        //file name and create the file
                        //mediaFile;
                        Date now = new Date();
                        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now);

                        String path = mediaStorageDir.getPath() + File.separator;
                        if (mediaType == MEDIA_TYPE_IMAGE){
                            mediaFile = new File(path+"IMG_"+timestamp+".jpg");

                        }
                        else if (mediaType == MEDIA_TYPE_VIDEO){
                            mediaFile = new File(path+"VID_"+timestamp+".mp4");
                        }
                        else{
                            return null;
                        }
                        Toast.makeText(CameraActivity.this,"File: "+Uri.fromFile(mediaFile),Toast.LENGTH_LONG).show();
                        return Uri.fromFile(mediaFile);
                    }
                    else{
                        return null;
                    }
                }

                private boolean isExternalStorageAvailable(){
                    String state = Environment.getExternalStorageState();

                    if (state.equals(Environment.MEDIA_MOUNTED)){
                        return true;
                    }
                    else{
                        return false;
                    }
                }
            };

    View.OnClickListener editPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            cameraChoices();
        }
    };

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
        captureImageButton.setOnClickListener(editPhoto);
       /** captureImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
                    public void onClick(View view){
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,CAMERA_REQUEST);
            }
        }); **/

        mirrorApiInterface = MagicMirrorApiClient.getClient().create(MagicMirrorApiInterface.class);


    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
       /** if(requestCode == CAMERA_REQUEST){
            Bitmap photo = (Bitmap)data.getExtras().get("data");
            imageHolder.setImageBitmap(photo);
           // mMediaUri = data.getData();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
            byte[] reducedData = outputStream.toByteArray();
            Log.e(TAG, "  Username "+mUsername+" Password: "+mPassword+" Gender: "+mGender);
            if(mUsername != null && mPassword != null && mGender != null)
              createAccount(mUsername,mPassword,mGender,reducedData);
        } **/


        if (resultCode == RESULT_OK){
           // mProgressDialog.show();
            //add to gallery
            if (requestCode == CHOOSE_PHOTO_REQUEST){
                if (data == null){
                    Toast.makeText(this,"there was an error",Toast.LENGTH_LONG).show();
                }
                else{
                    mMediaUri = data.getData();
                }
                int fileSize = 0;
                InputStream inputStream = null;
                try{
                    inputStream = getContentResolver().openInputStream(mMediaUri);
                    assert inputStream != null;
                    fileSize = inputStream.available();

                }
                catch (FileNotFoundException e){
                    Toast.makeText(CameraActivity.this,"Error opening image. Please try again.",Toast.LENGTH_LONG).show();
                    return;
                }
                catch (IOException e){
                    Toast.makeText(CameraActivity.this,"Error opening image. Please try again.",Toast.LENGTH_LONG).show();
                    return;
                }
                finally {
                    try{
                        assert inputStream != null;
                        inputStream.close();
                    } catch (IOException e){/*Intentionally blank*/ }
                }

                if (fileSize >= FILE_SIZE_LIMIT){
                    Toast.makeText(CameraActivity.this,"The selected image is too large. Please choose another image.",Toast.LENGTH_LONG).show();
                    return;
                }


                fileBytes = getByteArrayFromFile(CameraActivity.this, mMediaUri);
                if(mediaFile==null) {
                    //create mediaFile
                    Log.e(TAG, "creating media file to write chosen image into");
                    mediaFile = createFile(MEDIA_TYPE_IMAGE);
                    Log.e(TAG, "media file succesfully written");


                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mediaFile);
                    fos.write(fileBytes);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (MyToolBox.isNetworkAvailable(this)) {
                        //TODO: do both here - upload file using mediaFile global variable ?
                       // uploadFile(mediaFile);
                        createAccount(mUsername,mPassword,mGender,mediaFile);
                    } else {
                        MyToolBox.AlertMessage(this, "Oops", "Network Error. Please check your connection");
                    }
                }



            }
            else{
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);

                /*
                 So it begins... ahhhhhhh!
                */
                try {
                    fileBytes = getByteArrayFromFile(this, mMediaUri);
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeByteArray(fileBytes, 0, fileBytes.length);
                    } catch (OutOfMemoryError memoryError) {
                        Toast.makeText(CameraActivity.this, memoryError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                   /** if (bitmap != null) {
                        try {
                            bitmap = rotateImageIfRequired(bitmap, mMediaUri);
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
                            byte[] bitmapdata = bos.toByteArray();
                            FileOutputStream fos = new FileOutputStream(mediaFile);
                            fos.write(bitmapdata);
                            fos.flush();
                            fos.close();

                            Log.e(TAG, "media file succesfully written on Captured...");

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } **/

                } finally {
                    if (MyToolBox.isNetworkAvailable(this)) {
//                    postProfilePhoto();
                        //TODO: do both here - upload file using mediaFile global variable ?
                        Log.e(TAG, "... Captured... Finally... ");
                        //uploadFile(mediaFile);
                        createAccount(mUsername,mPassword,mGender,mediaFile);
                    } else {
                        MyToolBox.AlertMessage(this, "Oops", "Network Error. Please check your connection");
                    }
                }

            }

//            mProgressDialog.show();
            if (mediaFile != null ) { // captured image

            } else { //

            }

        }
        else if (resultCode != RESULT_CANCELED){
            Toast.makeText(this,"There was an error saving your photo",Toast.LENGTH_LONG).show();
        }

    }

    private void createAccount(String username, String password, String gender, File bitmapdata){
        Log.e(TAG, "CreateAccount");
       /** if(isExternalStorageAvailable()){
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

            Log.e(TAG, "File path:  "+mediaFile);

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
        } **/
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), bitmapdata);


      MultipartBody.Part body =
                MultipartBody.Part.createFormData("selfie", bitmapdata.getName(), requestFile);

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
                    Log.e(TAG, " "+response.code());
                    Log.e(TAG, " "+response.code());
                    Log.e(TAG, " "+response.code());
                    Log.e(TAG, " "+response.code());
                 //   Picasso.with(EditProfileActivity.this)
                    //        .load(mediaFile)
                     //       .into(mPhoto);
                    Toast.makeText(CameraActivity.this, "Successful registration", Toast.LENGTH_SHORT).show();
                   // currentUser.setProfilePhoto(fileName);
                   // mSharedPref.setCurrentUser(currentUser.toString());
                } else {
                    Log.e(TAG, " "+response.code());
                    Log.e(TAG, " "+response.code());
                    Log.e(TAG, " "+response.code());
                    Log.e(TAG, " "+response.code());
                //    Log.e(TAG, response.body().toString());
                    Log.e(TAG, " "+response.errorBody());
                    Log.e(TAG, " "+response.errorBody());
                    Log.e(TAG, " "+response.errorBody());
                    Toast.makeText(CameraActivity.this, "Could not update profile photo", Toast.LENGTH_SHORT).show();
                }

               // mProgressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.e(TAG, "Retrofit: "+ t.getMessage());
                Toast.makeText(CameraActivity.this, "Error uploading photo. Try again", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onFailure "+t.getMessage());
                Log.e(TAG, "onFailure "+t.getMessage());
                if(t instanceof SocketTimeoutException){
                    Log.e(TAG, "onFailure "+t.getMessage());
                    Log.e(TAG, "onFailure "+t.getMessage());
                }
                if(t instanceof ConnectTimeoutException){
                    Log.e(TAG, "onFailure "+t.getMessage());
                    Log.e(TAG, "onFailure "+t.getMessage());
                }
               // t.printStackTrace();
               // mProgressDialog.dismiss();
            }
        });

    }

    private void cameraChoices() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.camera_choices, mDialogListener);
        AlertDialog dialog = builder.create();
        dialog.show();
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

    private byte[] getByteArrayFromFile(Context context, Uri uri) {
        byte[] fileBytes = null;
        InputStream inStream = null;
        ByteArrayOutputStream outStream = null;
        Log.e(TAG, uri.getScheme());

        if (uri.getScheme().equals("http")) {
//            new urlToBytes();
        }
        else if (uri.getScheme().equals("content")) {
            try {
                inStream = context.getContentResolver().openInputStream(uri);
                outStream = new ByteArrayOutputStream();

                byte[] bytesFromFile = new byte[1024 * 1024]; // buffer size (1 MB)
                assert inStream != null;
                int bytesRead = inStream.read(bytesFromFile);
                while (bytesRead != -1) {
                    outStream.write(bytesFromFile, 0, bytesRead);
                    bytesRead = inStream.read(bytesFromFile);
                }

                fileBytes = outStream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                try {
                    assert inStream != null;
                    inStream.close();
                    assert outStream != null;
                    outStream.close();
                } catch (IOException e) { /*( Intentionally blank */ }
            }
        }
        else {
            try {
                File file = new File(uri.getPath());
                FileInputStream fileInput = new FileInputStream(file);
                fileBytes = IOUtils.toByteArray(fileInput);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        Random r = new Random();
        int shortSide = r.nextInt(880 - 500) + 500;
        return reduceImageForUpload(fileBytes, shortSide);
    }

    private File createFile(int mediaType) {
        if(isExternalStorageAvailable()){
            //storage dir
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    getString(R.string.app_name));
            //subdir
            if(!mediaStorageDir.exists()){
                if(!mediaStorageDir.mkdirs()){
                    return null;
                }
            }
            //file name and create the file
            //mediaFile;
            Date now = new Date();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now);

            String path = mediaStorageDir.getPath() + File.separator;
            if (mediaType == MEDIA_TYPE_IMAGE){
                mediaFile = new File(path+"IMG_"+timestamp+".jpg");

            }
            else if (mediaType == MEDIA_TYPE_VIDEO){
                mediaFile = new File(path+"VID_"+timestamp+".mp4");
            }
            else{
                return null;
            }
            Toast.makeText(CameraActivity.this,"File: "+Uri.fromFile(mediaFile),Toast.LENGTH_LONG).show();
            return mediaFile;
        }
        else{
            return null;
        }
    }

    public static byte[] reduceImageForUpload(byte[] imageData, int shortSide) {
        Bitmap bitmap = ImageResizer.resizeImageMaintainAspectRatio(imageData, shortSide);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
        byte[] reducedData = outputStream.toByteArray();
        try {
            outputStream.close();
        }
        catch (IOException e) {
            // Intentionally blank
        }

        return reducedData;
    }


}
