package com.mc.grp6.bluemobquiz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.SalesforceActivity;

public class Home extends SalesforceActivity {
    Button loginButton;
    Button registerButton;

    @Override
    public void onResume(RestClient client) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Intent intent = getIntent();
        loginButton = (Button) findViewById(R.id.homeLoginButton);
        registerButton = (Button) findViewById(R.id.homeRegisterButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, LoginPage.class);
                startActivity(intent);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, RegistrationPage.class);
                startActivity(intent);
            }
        });

    }

    public void gotoCreateQ(View view) {
        Intent intent = new Intent(Home.this, ProfessorCreatingQuiz.class);
        startActivity(intent);
    }
}
