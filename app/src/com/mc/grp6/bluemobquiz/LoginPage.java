package com.mc.grp6.bluemobquiz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginPage extends SalesforceActivity {

    private RestClient client;
    public String userName, Password,id, userID, currentDeviceID;
    public ArrayList<String> deviceIDList = new ArrayList<String>();
    @Override
    public void onResume(RestClient client) {
        this.client = client;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        Button login  = (Button) findViewById(R.id.loginButton);
        final  EditText usrName, pwd;
        usrName = (EditText) findViewById(R.id.userNameValue);
        pwd = (EditText) findViewById(R.id.passwordValue);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = usrName.getText().toString();
                Password = pwd.getText().toString();
                boolean isFieldsValidated = isFieldsValidated();
                if(isFieldsValidated){
                    try {
                        currentDeviceID = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");
                        sendRequest("select id,DeviceID__c, users__r.recordtype.name, users__r.id from Assigned_Devices__c " +
                                "where users__r.username__c =\'" + userName + "\' and users__r.password__c =\'" + Password+"\'",currentDeviceID);
                    }catch (UnsupportedEncodingException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void sendRequest(String soql, String currentDeviceID) throws UnsupportedEncodingException {
        final RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {

                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Boolean loginSuccess = false;
                        try {
                            if(result.isSuccess()){
                                JSONArray records = result.asJSONObject().getJSONArray("records");
                                JSONObject userIDRecord = records.getJSONObject(0).getJSONObject("Users__r");
                                userID = userIDRecord.getString("Id");
                                for (int i = 0; i < records.length(); i++) {
                                    deviceIDList.add(records.getJSONObject(i).getString("DeviceID__c"));
                                }
                                System.out.println("************"+result.toString()+"\n*********UserId:"+userID+"\n********Device ID:"+deviceIDList.get(0));
                                if(result.toString().contains("Professor")){
                                    for (int i = 0; i < deviceIDList.size(); i++) {
                                        if (currentDeviceID.equals(deviceIDList.get(i))) {
                                            loginSuccess = true;
                                            System.out.println("**********deviceIDList.get(i):" + deviceIDList.get(i));
                                            Intent intent = new Intent(LoginPage.this, ProfessorHome.class);
                                            intent.putExtra("userID",userID);
                                            startActivity(intent);
                                        }
                                    }
                                    if (!loginSuccess) {
                                        Toast.makeText(getApplicationContext(), "Invalid Device ", Toast.LENGTH_SHORT).show();
                                        AlertDialog.Builder builder;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            builder = new AlertDialog.Builder(LoginPage.this, android.R.style.Theme_Material_Dialog_Alert);
                                        } else {
                                            builder = new AlertDialog.Builder(LoginPage.this);
                                        }
                                        builder.setTitle("New Device")
                                                .setMessage("Do you want to add this device?")
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Map<String, Object> deviceRecord = new HashMap<String, Object>();
                                                        deviceRecord.put("Users__c", userID);
                                                        deviceRecord.put("DeviceID__c", currentDeviceID);
                                                        registerDevice(deviceRecord);
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Toast.makeText(getApplicationContext(), "You can't login with this device ", Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .show();
                                    }
                                }

                                else if(result.toString().contains("Student")) {
                                    for (int i = 0; i < deviceIDList.size(); i++) {
                                        if (currentDeviceID.equals(deviceIDList.get(i))) {
                                            loginSuccess = true;
                                            System.out.println("**********deviceIDList.get(i):" + deviceIDList.get(i));
                                            Intent intent = new Intent(LoginPage.this, StudentHome.class);
                                            intent.putExtra("userID", userID);
                                            startActivity(intent);

                                        }
                                    }
                                    if (!loginSuccess) {
                                        Toast.makeText(getApplicationContext(), "Invalid Device ", Toast.LENGTH_SHORT).show();
                                        AlertDialog.Builder builder;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            builder = new AlertDialog.Builder(LoginPage.this, android.R.style.Theme_Material_Dialog_Alert);
                                        } else {
                                            builder = new AlertDialog.Builder(LoginPage.this);
                                        }
                                        builder.setTitle("New Device")
                                                .setMessage("Do you want to add this device?")
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Map<String, Object> deviceRecord = new HashMap<String, Object>();
                                                        deviceRecord.put("Users__c", userID);
                                                        deviceRecord.put("DeviceID__c", currentDeviceID);
                                                        registerDevice(deviceRecord);
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Toast.makeText(getApplicationContext(), "You can't login with this device ", Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .show();
                                    }
                                }
                                else  Toast.makeText(getApplicationContext(), "Invalid User Name or Password - Please try again", Toast.LENGTH_SHORT).show();
                            }
                            else  Toast.makeText(getApplicationContext(), "Invalid Query", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            onError(e);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginPage.this,"User not registered",Toast.LENGTH_SHORT).show();
                    }
                });
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
                result.consumeQuietly(); // consume before going back to main thread
                System.out.println("**********"+result.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.isSuccess()) {
                            try {
                                Toast.makeText(getApplicationContext(), "Device added successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginPage.this, LoginPage.class);
                                startActivity(intent);
                            } catch (Exception e) {
                                // You might want to log the error
                                // or show it to the user
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

    public boolean isFieldsValidated(){
        if (userName.equals("") || userName.equals("Enter your username")) {
            Toast.makeText(getApplicationContext(), "Please enter your user name.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (Password.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter your password.", Toast.LENGTH_SHORT).show();
            return false;
        }else return true;
    }
}
