package com.mc.grp6.bluemobquiz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.SalesforceActivity;

public class StudentAttemptingQuiz extends SalesforceActivity {

    @Override
    public void onResume(RestClient client) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attempting_quiz);
    }
}