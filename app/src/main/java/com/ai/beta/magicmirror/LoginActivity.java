package com.ai.beta.magicmirror;


import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.app.ProgressDialog;

import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.EditText;

//import com.ai.helper.HollaNowSharedPref;
import com.ai.beta.magicmirror.helper.MyToolBox;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */


    // UI references.
   // private AutoCompleteTextView mEmailView;
    private TextInputLayout passwordLayout;
    private TextInputLayout usernamwLatout;
    private EditText mUserName;
    private EditText mPasswordView;
    private ProgressDialog mProgressDialog;
    private Spinner spinner;
    private final String USERNAME = "username";
    private final String PASSWORD = "password";
    private final String GENDER = "gender";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_signin);
        // Set up the login form.

        passwordLayout = (TextInputLayout) findViewById(R.id.textcontainer3_signin) ;
        usernamwLatout = (TextInputLayout) findViewById(R.id.textcontainer_signin) ;
        mUserName = (EditText) findViewById(R.id.username_signin);
        mPasswordView = (EditText) findViewById(R.id.password_sigin);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Signing up ...");

        spinner = (Spinner)findViewById(R.id.gender_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);




        Button mEmailSignInButton = (Button) findViewById(R.id.button_signin);

        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {

                                                  @Override
                                                  public void onClick(View view) {
                                                      if (MyToolBox.isNetworkAvailable(LoginActivity.this)) {
                                                          mProgressDialog.show();
                                                          if (!MyToolBox.isMinimumCharacters( mUserName.getText().toString().trim(), 2)) {
                                                              usernamwLatout.setError("Your username");
                                                              mProgressDialog.dismiss();
                                                          }else if (!MyToolBox.isMinimumCharacters( mPasswordView.getText().toString().trim(), 4)) {
                                                              passwordLayout.setError("Your password should have at least 5 characters");
                                                              mProgressDialog.dismiss();
                                                          }else{
                                                              usernamwLatout.setErrorEnabled(false);
                                                              passwordLayout.setErrorEnabled(false);
                                                              goToActivity(CameraActivity.class);
                                                          }

                                                      } else {
                                                          Toast.makeText(LoginActivity.this, "Network Error. Check your connection", Toast.LENGTH_LONG).show();
                                                      }
                                                  }
        });



    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */




    /**
     * Shows the progress UI and hides the login form.
     */

    private void goToActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(USERNAME, mUserName.getText().toString().trim());   //name and value
        intent.putExtra(PASSWORD, mPasswordView.getText().toString().trim());
        intent.putExtra(GENDER, spinner.getSelectedItem().toString());
        //if (cls == HomeActivity.class) {
        //    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
       // }
        startActivity(intent);
    }


}

