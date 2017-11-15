package com.mc.grp6.bluemobquiz;

import android.content.Intent;
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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginPage extends SalesforceActivity {

    private RestClient client;
    public String userName, Password,id;

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
                        String currentDeviceID = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");
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
                        try {
                            if(result.isSuccess()){

                                String userID = result.toString().substring(result.toString().indexOf(":\"a01")+2,result.toString().length()-5);
                                System.out.println("************"+result.toString()+"\n*********"+userID);
                                if(result.toString().contains("Professor")){
                                    Intent intent = new Intent(LoginPage.this, ProfessorHome.class);
                                    intent.putExtra("userID",userID);
                                    startActivity(intent);
                                }
                                else if(result.toString().contains("Student")){
                                    Intent intent = new Intent(LoginPage.this, StudentHome.class);
                                    intent.putExtra("userID",userID);
                                    startActivity(intent);
                                }
                                else  Toast.makeText(getApplicationContext(), "Invalid User Name or Password - Please" +
                                            "try again", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(LoginPage.this,
                                LoginPage.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), "Error: "+exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
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
