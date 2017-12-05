package com.mc.grp6.bluemobquiz;

import android.content.Intent;
import android.os.Bundle;
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

/**
 * Created by prasa on 2017-11-10.
 */

public class ProfessorUpdatingQuizName extends SalesforceActivity {
    //Variable declaration and initialization
    public EditText quizName;
    public Button OKButton;
    public String quizNameValue, updatedQuizNameValue, userID;
    private RestClient client;
    public String quizID="";
    //Method that is called after the activity resumes once we have a RestClient.
    @Override
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;
        userID = getIntent().getExtras().getString("userID");
        quizID = getIntent().getExtras().getString("quizID");
        quizNameValue = getIntent().getExtras().getString("quizName");
        //Calling setQuizName() with quiz name as parameter
        setQuizName(quizNameValue);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_adding_quiz_name);
        userID = getIntent().getExtras().getString("userID");
        quizID = getIntent().getExtras().getString("quizID");
        quizNameValue = getIntent().getExtras().getString("quizName");
        quizName = (EditText) findViewById(R.id.quizNameValue);
        OKButton = (Button) findViewById(R.id.addQuizName);
        //OK button click listener
        OKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedQuizNameValue = quizName.getText().toString();
                boolean successValidation = validateData();
                if (successValidation) {
                    Map<String, Object> quizRecord = new HashMap<String, Object>();
                    quizRecord.put("Name", updatedQuizNameValue);
                    quizRecord.put("Topic__c", updatedQuizNameValue);
                    //Calling updateQuizName() with quiz ID and name as parameter
                    updateQuizName(quizID,quizRecord);
                }
            }
        });
    }
    private void updateQuizName(String quizID, Map<String, Object> quizRecord) {
        RestRequest restRequest;
        try {
            restRequest = RestRequest.getRequestForUpdate(getString(R.string.api_version), "Quiz__c",quizID, quizRecord);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "catch" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return ;
        }
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.isSuccess()) {
                            try {
                                quizNameValue = getIntent().getExtras().getString("quizName");
                                //checking if name is changed or not
                                if(!quizNameValue.equalsIgnoreCase(updatedQuizNameValue)) {
                                    Toast.makeText(getApplicationContext(), "Quiz name updated", Toast.LENGTH_SHORT).show();
                                }
                                //Redirecting to updating quiz page with quizname and id as parameters
                                Intent intent = new Intent(ProfessorUpdatingQuizName.this, ProfessorUpdatingQuiz.class);
                                intent.putExtra("userID",userID);
                                intent.putExtra("quizID",quizID);
                                intent.putExtra("quizName",updatedQuizNameValue);
                                startActivity(intent);
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
    //Validating if quiz name is entered or not
    private boolean validateData() {
        if (quizNameValue.equals("") || quizNameValue.equals("Enter name for the quiz")) {
            Toast.makeText(getApplicationContext(), "Please input quiz name.", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }
    //setting quiz name in the page
    private void setQuizName(String quizNameValue) {
        quizName.setText(quizNameValue);
    }
}

