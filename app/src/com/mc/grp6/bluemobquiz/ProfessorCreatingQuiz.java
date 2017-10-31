package com.mc.grp6.bluemobquiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import java.util.HashMap;
import java.util.Map;

public class ProfessorCreatingQuiz extends SalesforceActivity {
    public EditText question, option1, option2, option3, option4, quizName;
    public Spinner profAnswerSelection, profDifficultySelection;
    public Button nextButton, submitButton;
    public String selectedAnswer, selectedDifficulty, questionValue, option1Value, option2Value, option3Value, option4Value, quizNameValue,userID;
    public boolean questionAdded = false, quizNameAdded = false;
    private RestClient client;
    public String questionID, quizID="";

    @Override
    public void onResume(RestClient client) {
        this.client = client;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_creating_quiz);
        quizName = (EditText) findViewById(R.id.quizName);
        question = (EditText) findViewById(R.id.question);
        option1 = (EditText) findViewById(R.id.option1);
        option2 = (EditText) findViewById(R.id.option2);
        option3 = (EditText) findViewById(R.id.option3);
        option4 = (EditText) findViewById(R.id.option4);
        profAnswerSelection = (Spinner) findViewById(R.id.answerSpinner);
        profDifficultySelection = (Spinner) findViewById(R.id.difficultySpinner);
        nextButton = (Button) findViewById(R.id.profNext);
        submitButton = (Button) findViewById(R.id.profSubmit);
        profAnswerSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedAnswer = String.valueOf(profAnswerSelection.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        profDifficultySelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDifficulty = String.valueOf(profDifficultySelection.getSelectedItem());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quizNameValue = quizName.getText().toString();
                questionValue = question.getText().toString();
                option1Value = option1.getText().toString();
                option2Value = option2.getText().toString();
                option3Value = option3.getText().toString();
                option4Value = option4.getText().toString();
                userID="a014100000Kl4Bw";
                boolean successValidation = validateData();
                if (successValidation) {
                    Map<String, Object> quizRecord = new HashMap<String, Object>();
                    quizRecord.put("Name",quizNameValue);
                    quizRecord.put("Quiz_Owner__c", userID);
                    quizRecord.put("Topic__c", quizNameValue);
                    addQuiz(quizRecord);
                    if(quizNameAdded){
                        Map<String, Object> questionRecord = new HashMap<String, Object>();
                        questionRecord.put("Question__c", questionValue);
                        questionRecord.put("Choice1__c", option1Value);
                        questionRecord.put("Choice2__c", option2Value);
                        questionRecord.put("Choice3__c", option3Value);
                        questionRecord.put("Choice4__c", option4Value);
                        questionRecord.put("Quiz__c", quizID);
                        questionRecord.put("Difficulty_Level__c", selectedDifficulty);
                        addQuestion(questionRecord);
                        if (questionAdded) {
                            Map<String, Object> answerRecord = new HashMap<String, Object>();
                            answerRecord.put("Quiz__c",quizID);
                            answerRecord.put("Question__c", questionID);
                            answerRecord.put("Answer__c", selectedAnswer);
                            addAnswer(answerRecord);
                            Toast.makeText(getApplicationContext(), "Question added", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ProfessorCreatingQuiz.this, ProfessorCreatingQuiz.class);
                            startActivity(intent);
                        }
                    }
                }
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Quiz created", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfessorCreatingQuiz.this, ProfessorHome.class);
                startActivity(intent);
            }
        });
    }
    private void addAnswer(Map<String, Object> answerRecord) {
        RestRequest restRequest;
        try {
            restRequest = RestRequest.getRequestForCreate(getString(R.string.api_version), "Quiz_Answers__c", answerRecord);
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
    private void addQuestion(Map<String, Object> questionRecord) {
        RestRequest restRequest;
        try {
            restRequest = RestRequest.getRequestForCreate(getString(R.string.api_version), "Quiz_Questions__c", questionRecord);
        } catch (Exception e) {

            Toast.makeText(getApplicationContext(), "catch" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, RestResponse result)  {
                System.out.println("****************"+result.toString());
                if (result.isSuccess()) {
                    try {
                        questionAdded = true;
                        questionID = result.toString().substring(7,25);
                    }
                    catch (Exception e) {
                    }
                }
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
            public void onSuccess(RestRequest request, RestResponse result) {
                if (result.isSuccess()) {
                    try {
                        quizNameAdded = true;
                        quizID = result.toString().substring(7,25);
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
    private boolean validateData() {
        return true;
    }
}
