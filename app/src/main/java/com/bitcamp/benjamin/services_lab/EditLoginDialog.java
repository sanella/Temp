package com.bitcamp.benjamin.services_lab;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
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
public class EditLoginDialog extends DialogFragment implements OnEditorActionListener {

    private SharedPreferences mSharedPreferences;
    private static final String TAG = "Sanelita";


    public interface EditNameDialogListener {
        void onFinishEditDialog(String inputText, String s);
    }

    private EditText mEditEmail;
    private EditText mEditPassword;

    public EditLoginDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mSharedPreferences = getActivity().getSharedPreferences("numberPicker.preferences", 0);


        String email = mSharedPreferences.getString(
                getString(R.string.key_user_email),
                null
        );

        String password = mSharedPreferences.getString(
                getString(R.string.key_user_password),
                null
        );

        if(email != null && password != null){
            setUserData(email, password);
            loginUser();
        }



        View view = inflater.inflate(R.layout.edit_login, container);
        mEditEmail = (EditText) view.findViewById(R.id.edit_text_email);
        mEditPassword = (EditText) view.findViewById(R.id.edit_text_password);
        getDialog().setTitle("Login");


        //loginUser();
        // Show soft keyboard automatically
        mEditEmail.requestFocus();
        mEditPassword.requestFocus();
        mEditEmail.setOnEditorActionListener(this);
        mEditPassword.setOnEditorActionListener(this);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

/*
        String email2= mEditEmail.getText().toString();
        String password2 = mEditPassword.getText().toString();
        setUserData(email2, password2);
        loginUser();
*/
        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
/*
            String email= mEditEmail.getText().toString();
            String password = mEditPassword.getText().toString();
            setUserData(email, password);
            loginUser();
*/
           /* Intent i = new Intent(getActivity(), FragmentLoginDialog.class);
            i.putExtra("email" , mEditEmail.getText().toString());
            i.putExtra("password", mEditPassword.getText().toString());
            startActivity(i);*/
            EditNameDialogListener activity = (EditNameDialogListener) getActivity();
            activity.onFinishEditDialog(mEditEmail.getText().toString(), mEditPassword.getText().toString());
           // activity.onFinishEditDialog(mEditPassword.getText().toString());



/*
            mSharedPreferences = getActivity().getSharedPreferences("numberPicker.preferences", 0);


            String email = mSharedPreferences.getString(
                    getString(R.string.key_user_email),
                    null
            );

            String password = mSharedPreferences.getString(
                    getString(R.string.key_user_password),
                    null
            );

            if(email != null && password != null){
                setUserData(email, password);
                loginUser();
            }

*/





            this.dismiss();
            return true;
        }
        return false;
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
                        Toast.makeText(getActivity(),
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