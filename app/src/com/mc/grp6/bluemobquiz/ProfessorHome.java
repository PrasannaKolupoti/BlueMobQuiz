package com.mc.grp6.bluemobquiz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.SalesforceActivity;

public class ProfessorHome extends SalesforceActivity {

    private RestClient client;

    @Override
    public void onResume(RestClient client) {
        this.client = client;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_home);

    }
}
