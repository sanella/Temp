package com.bitcamp.benjamin.services_lab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.bitcamp.benjamin.services_lab.service.ServiceRequest;
import com.bitcamp.benjamin.services_lab.singletons.UserData;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Sanela on 4/18/2015.
 */
public class FragmentLoginDialog extends FragmentActivity implements EditLoginDialog.EditNameDialogListener{

    private SharedPreferences mSharedPreferences;
    private static final String TAG = "Sanelita";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        showEditDialog();
    }

    private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        EditLoginDialog editLoginDialog = new EditLoginDialog();
        editLoginDialog.show(fm, "dlg_edit_name");
    }

    @Override
    public void onFinishEditDialog(String inputText, String d) {

        String email=inputText;
        String password = d;

        setUserData(email, password);
        loginUser();



        Toast.makeText(this, "Email, " + inputText , Toast.LENGTH_SHORT).show();
       // Toast.makeText(this, "Hi, " + b, Toast.LENGTH_SHORT).show();

        Toast.makeText(this, "Password, " + d , Toast.LENGTH_SHORT).show();

        Intent login = new Intent(this, MainActivity.class);
        startActivity(login);
    }


    private void loginUser(){
        String url = getString(R.string.service_login);
        Callback callback = loginVerification();
        String json = UserData.getInstance().toJson();

        ServiceRequest.post(url, json, callback);
    }

    private Callback loginVerification(){
        return new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                makeToast(R.string.toast_try_again);
                Log.d("onfailure", "On failure");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseJson = response.body().string();
                try {
                    JSONObject user = new JSONObject(responseJson);
                    int id = user.getInt("id");
                    if(id > 0){
                        Log.d(TAG, response.body().toString());

                        String username = user.getString("name");
                        Log.d(TAG, response.body().toString());
                        UserData userData = UserData.getInstance();
                        userData.setId(id);
                        userData.setUsername(username);
                        saveUserCredentials();
                    }
                } catch (JSONException e) {
                    makeToast(R.string.toast_try_again);
                    e.printStackTrace();
                }
            }
        };
    }

    private void saveUserCredentials(){
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        UserData userData = UserData.getInstance();

        editor.putString(
                getString(R.string.key_user_email),
                userData.getEmail()
        );

        editor.putString(
                getString(R.string.key_user_password),
                userData.getPassword()
        );
        editor.commit();
    }

    private void makeToast(final int messageId){

        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FragmentLoginDialog.this,
                                messageId,
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void setUserData(String email, String password){
        UserData userData = UserData.getInstance();
        userData.setEmail(email);
        userData.setPassword(password);
    }



}

