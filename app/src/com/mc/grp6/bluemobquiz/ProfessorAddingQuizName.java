package com.mc.grp6.bluemobquiz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import java.util.HashMap;
import java.util.Map;

public class ProfessorAddingQuizName extends SalesforceActivity {

    public EditText quizName;
    public Button OKButton;
    public String quizNameValue,userID;
    private RestClient client;
    public String questionID, quizID="";
    public int questionNumberValue = 1;
    @Override
    public void onResume(RestClient client) {
        this.client = client;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_adding_quiz_name);
        userID = getIntent().getExtras().getString("userID");
        quizName = (EditText) findViewById(R.id.quizNameValue);
        OKButton = (Button) findViewById(R.id.addQuizName);
        OKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quizNameValue = quizName.getText().toString();
                boolean successValidation = validateData();
                if (successValidation) {
                    Map<String, Object> quizRecord = new HashMap<String, Object>();
                    quizRecord.put("Name", quizNameValue);
                    quizRecord.put("Quiz_Owner__c", userID);
                    quizRecord.put("Topic__c", quizNameValue);
                    addQuiz(quizRecord);


                }
            }
        });
    }
    private void addQuiz(Map<String, Object> quizRecord) {
        RestRequest restRequest;
        try {
            restRequest = RestRequest.getRequestForCreate(getString(R.string.api_version), "Quiz__c", quizRecord);
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
                                //quizNameAdded = true;
                                quizID = result.toString().substring(7, 25);
                                System.out.println("*********************QuizID:"+quizID);
                                Intent intent = new Intent(ProfessorAddingQuizName.this, ProfessorCreatingQuiz.class);
                                intent.putExtra("userID",userID);
                                intent.putExtra("quizID",quizID);
                                intent.putExtra("quizName",quizNameValue);
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
    private boolean validateData() {
        if (quizNameValue.equals("") || quizNameValue.equals("Enter name for the quiz")) {
            Toast.makeText(getApplicationContext(), "Please input quiz name.", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }
}
