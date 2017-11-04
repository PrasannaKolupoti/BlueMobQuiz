package com.mc.grp6.bluemobquiz;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import java.util.HashMap;
import java.util.Map;

public class RegistrationPage extends SalesforceActivity {
    private RestClient client;
    public String name, userName, password, confirmPassword, deviceID;
    public boolean successRegistration = false;
    public String userID ;

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_page);

        Button register = (Button) findViewById(R.id.registerButton);
        final EditText nameValue, userNameValue, passwordValue, confirmPasswordValue;
        nameValue = (EditText) findViewById(R.id.nameValue);
        userNameValue = (EditText) findViewById(R.id.userNameValue);
        passwordValue = (EditText) findViewById(R.id.passwordValue);
        confirmPasswordValue = (EditText) findViewById(R.id.confirmPasswordValue);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = nameValue.getText().toString();
                userName = userNameValue.getText().toString();
                password = passwordValue.getText().toString();
                confirmPassword = confirmPasswordValue.getText().toString();
                boolean successValidation = validateData();
                if (successValidation) {
                    Map<String, Object> userRecord = new HashMap<String, Object>();
                    userRecord.put("Name", name);
                    userRecord.put("Username__c", userName);
                    userRecord.put("Password__c", password);
                    registerUser(userRecord);
                    //if (successRegistration) {
                        deviceID = getDeviceID();
                        Map<String, Object> deviceRecord = new HashMap<String, Object>();
                        deviceRecord.put("Users__c", userID);
                        deviceRecord.put("DeviceID__c", deviceID);
                        registerDevice(deviceRecord);
                        Toast.makeText(getApplicationContext(), "Successfully Registered", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegistrationPage.this, LoginPage.class);
                        startActivity(intent);
                    //}
                }
            }
        });
    }
    private void registerDevice(Map<String, Object> deviceRecord) {
        RestRequest restRequest;
        try {
            restRequest = RestRequest.getRequestForCreate(getString(R.string.api_version), "Assigned_Devices__c", deviceRecord);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "catch" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return ;
        }
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, RestResponse result) {
                if (result.isSuccess()) {
                    try {
                    } catch (Exception e) {
                        // You might want to log the error
                        // or show it to the user
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(Map<String, Object> userRecord) {
        RestRequest restRequest;
        try {
            restRequest = RestRequest.getRequestForCreate(getString(R.string.api_version), "Users__c", userRecord);
        } catch (Exception e) {

            Toast.makeText(getApplicationContext(), "catch" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result)  {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.isSuccess()) {
                            try {
                                successRegistration = true;
                                userID = result.toString().substring(7, 25);
                            } catch (Exception e) {
                            }
                        }
                    }
                });
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getDeviceID(){
        return Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
    }
    private boolean validateData() {
        if (name.equals("") || name.equals("Enter your name")) {
            Toast.makeText(getApplicationContext(), "Please enter your name.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (userName.equals("") || name.equals("Enter your username")) {
            Toast.makeText(getApplicationContext(), "Please enter your Username.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter your password.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.length() < 8) {
            Toast.makeText(getApplicationContext(), "Please enter atleast 8 character password", Toast.LENGTH_SHORT).show();
            return false;
        } else if (confirmPassword.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter your confirmPassword.", Toast.LENGTH_SHORT).show();
            return false;
        }  else if (!(password.equals(confirmPassword))) {
            Toast.makeText(getApplicationContext(), "Password and ConfirmPassword does not match.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}