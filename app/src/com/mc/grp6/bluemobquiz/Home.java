package com.mc.grp6.bluemobquiz;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.SalesforceActivity;

public class Home extends SalesforceActivity {
    //Variable declaration
    Button loginButton;
    Button registerButton;
    @Override
    public void onResume(RestClient client) {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        loginButton = (Button) findViewById(R.id.homeLoginButton);
        registerButton = (Button) findViewById(R.id.homeRegisterButton);
        //Login Button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Redirect to LoginPage
                Intent intent = new Intent(Home.this, LoginPage.class);
                startActivity(intent);
            }
        });
        //Register Button click listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Redirect to RegistrationPage
                Intent intent = new Intent(Home.this, RegistrationPage.class);
                startActivity(intent);
            }
        });
    }
}
