package com.mc.grp6.bluemobquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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

public class ProfessorCreatingQuiz extends SalesforceActivity {
    //Variable declaration and initialization
    public EditText question, option1, option2, option3, option4;
    public TextView questionNumber, quizName;
    public Spinner profAnswerSelection, profDifficultySelection;
    public Button nextButton, submitButton;
    public String selectedAnswer, selectedDifficulty, questionValue, option1Value, option2Value, option3Value, option4Value, quizNameValue,userID;
    public boolean questionAdded = false, quizNameAdded = false;
    private RestClient client;
    public String questionID, quizID="";
    public int questionNumberValue = 1;
    //Method that is called after the activity resumes once we have a RestClient.
    @Override
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_creating_quiz);
        userID = getIntent().getExtras().getString("userID");
        quizID = getIntent().getExtras().getString("quizID");
        quizNameValue = getIntent().getExtras().getString("quizName");
        questionNumber = (TextView) findViewById(R.id.questionNumber);
        quizName = (TextView) findViewById(R.id.quizName);
        quizName.setText(quizNameValue);
        question = (EditText) findViewById(R.id.question);
        option1 = (EditText) findViewById(R.id.option1);
        option2 = (EditText) findViewById(R.id.option2);
        option3 = (EditText) findViewById(R.id.option3);
        option4 = (EditText) findViewById(R.id.option4);
        profAnswerSelection = (Spinner) findViewById(R.id.answerSpinner);
        profDifficultySelection = (Spinner) findViewById(R.id.difficultySpinner);
        nextButton = (Button) findViewById(R.id.profNext);
        submitButton = (Button) findViewById(R.id.profSubmit);
        //spinner selection listener
        profAnswerSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedAnswer = String.valueOf(profAnswerSelection.getSelectedItem());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //spinner selection listener
        profDifficultySelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDifficulty = String.valueOf(profDifficultySelection.getSelectedItem());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //Next button click listener
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setting the quiz name
                quizName.setText(quizNameValue);
                //getting values from the page
                questionValue = question.getText().toString();
                option1Value = option1.getText().toString();
                option2Value = option2.getText().toString();
                option3Value = option3.getText().toString();
                option4Value = option4.getText().toString();
                boolean successValidation = validateData();
                //if successful validation
                if (successValidation) {
                    Map<String, Object> questionRecord = new HashMap<String, Object>();
                    questionRecord.put("Question__c", questionValue);
                    questionRecord.put("Choice1__c", option1Value);
                    questionRecord.put("Choice2__c", option2Value);
                    questionRecord.put("Choice3__c", option3Value);
                    questionRecord.put("Choice4__c", option4Value);
                    questionRecord.put("Quiz__c", quizID);
                    questionRecord.put("Difficulty_Level__c", selectedDifficulty);
                    //Calling addQuestion() with question questionRecord and answer as parameter
                    addQuestion(questionRecord,selectedAnswer);
                    clearPage();
                }
            }
        });
        // submit button click listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setting the quiz name
                quizName.setText(quizNameValue);
                //getting values from the page
                questionValue = question.getText().toString();
                option1Value = option1.getText().toString();
                option2Value = option2.getText().toString();
                option3Value = option3.getText().toString();
                option4Value = option4.getText().toString();
                userID = getIntent().getExtras().getString("userID");
                boolean successValidation = validateData();
                //on successful validation
                if (successValidation) {
                    Map<String, Object> questionRecord = new HashMap<String, Object>();
                    questionRecord.put("Question__c", questionValue);
                    questionRecord.put("Choice1__c", option1Value);
                    questionRecord.put("Choice2__c", option2Value);
                    questionRecord.put("Choice3__c", option3Value);
                    questionRecord.put("Choice4__c", option4Value);
                    questionRecord.put("Quiz__c", quizID);
                    questionRecord.put("Difficulty_Level__c", selectedDifficulty);
                    //Calling addQuestion() with question questionRecord and answer as parameter
                    addQuestion(questionRecord,selectedAnswer);

                }
                Toast.makeText(getApplicationContext(), "Quiz created", Toast.LENGTH_SHORT).show();
                //Redirecting to ProfessorHome page once quiz is submitted
                Intent intent = new Intent(ProfessorCreatingQuiz.this, ProfessorHome.class);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
        });
    }
    // Setting the page to empty values
    private void clearPage() {
        questionNumberValue++;
        quizName.setText(quizNameValue);
        questionNumber.setText(Integer.toString(questionNumberValue));
        question.getText().clear();
        option1.getText().clear();
        option2.getText().clear();
        option3.getText().clear();
        option4.getText().clear();
        profAnswerSelection.setSelection(0);
        profDifficultySelection.setSelection(0);

    }
    // Adding answer of a particular question in database
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
                    }
                }
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //Adding question to database
    private void addQuestion(Map<String, Object> questionRecord,String selectedAnswer) {
        RestRequest restRequest;
        try {
            restRequest = RestRequest.getRequestForCreate(getString(R.string.api_version), "Quiz_Questions__c", questionRecord);
        } catch (Exception e) {

            Toast.makeText(getApplicationContext(), "catch" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request,final RestResponse result)  {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.isSuccess()) {
                            try {
                                questionID = result.toString().substring(7, 25);
                                Map<String, Object> answerRecord = new HashMap<String, Object>();
                                answerRecord.put("Quiz__c",quizID);
                                answerRecord.put("Question__c", questionID);
                                answerRecord.put("Answer__c", selectedAnswer);
                                //Calling addAnswer() with questionID and selected answer
                                addAnswer(answerRecord);
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
    //Validating quiz data
    private boolean validateData() {
        if (questionValue.equals("") || questionValue.equals("Enter the questionField")) {
            Toast.makeText(getApplicationContext(), "Please input the questionField.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (option1Value.equals("") || option1Value.equals("Option 1")) {
            Toast.makeText(getApplicationContext(), "Please input Option 1.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (option2Value.equals("") || option2Value.equals("Option 1")) {
            Toast.makeText(getApplicationContext(), "Please input Option 2.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (option3Value.equals("") || option3Value.equals("Option 1")) {
            Toast.makeText(getApplicationContext(), "Please input Option 3.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (option4Value.equals("") || option4Value.equals("Option 1")) {
            Toast.makeText(getApplicationContext(), "Please input Option 4.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (selectedAnswer.equals(profAnswerSelection.getItemAtPosition(0))) {
            Toast.makeText(getApplicationContext(), "Please select an answer.", Toast.LENGTH_SHORT).show();
            return false;
        }  else if (selectedDifficulty.equals(profDifficultySelection.getItemAtPosition(0))) {
            Toast.makeText(getApplicationContext(), "Please select difficulty level.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}
