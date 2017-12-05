package com.mc.grp6.bluemobquiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by prasa on 2017-11-08.
 */

public class ProfessorUpdatingQuiz extends SalesforceActivity {
    //Variable declaration and initialization
    public EditText questionField, option1Field, option2Field, option3Field, option4Field;
    public TextView questionNumber,quizNameField;
    public Spinner profAnswerSelection, profDifficultySelection;
    public Button nextButton, submitButton;
    public String selectedAnswer, selectedDifficulty, questionValue, option1Value, option2Value, option3Value, option4Value, quizNameValue,userID,quizName;
    private RestClient client;
    public String quizID="";
    public int questionNumberValue;
    public ArrayList<String> questionIDList = new ArrayList<String>();
    public ArrayList<String> answerIDList = new ArrayList<String>();
    public ArrayList<String> questionList = new ArrayList<String>();
    public ArrayList<String> option1List = new ArrayList<String>();
    public ArrayList<String> option2List = new ArrayList<String>();
    public ArrayList<String> option3List = new ArrayList<String>();
    public ArrayList<String> option4List = new ArrayList<String>();
    public ArrayList<String> difficultyLevelList = new ArrayList<String>();
    public ArrayList<String> answerList = new ArrayList<String>();
    public void onResume() {
        super.onResume();
    }
    //Method that is called after the activity resumes once we have a RestClient.
    @Override
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;
        quizID = getIntent().getExtras().getString("quizID");
        try {
            //Calling setQuestion() with query to get the questions in quiz as parameter
            setQuestion("SELECT id, Question__c, Quiz__c, Quiz__r.Name,  Question__r.Question__c, Question__r.Choice1__c, Question__r.Choice2__c, Question__r.Choice3__c, Question__r.Choice4__c, Question__r.Difficulty_Level__c, Answer__c from Quiz_Answers__c where Quiz__c =\'" + quizID + "\'");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_creating_quiz);
        userID = getIntent().getExtras().getString("userID");
        quizID = getIntent().getExtras().getString("quizID");
        quizName = getIntent().getExtras().getString("quizName");
        questionNumber = (TextView) findViewById(R.id.questionNumber);
        quizNameField = (TextView) findViewById(R.id.quizName);
        quizNameField.setText(quizName);
        questionField = (EditText) findViewById(R.id.question);
        option1Field = (EditText) findViewById(R.id.option1);
        option2Field = (EditText) findViewById(R.id.option2);
        option3Field = (EditText) findViewById(R.id.option3);
        option4Field = (EditText) findViewById(R.id.option4);
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

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getting values from the page
                quizNameValue = quizNameField.getText().toString();
                questionValue = questionField.getText().toString();
                option1Value = option1Field.getText().toString();
                option2Value = option2Field.getText().toString();
                option3Value = option3Field.getText().toString();
                option4Value = option4Field.getText().toString();
                selectedDifficulty = String.valueOf(profDifficultySelection.getSelectedItem());
                selectedAnswer = String.valueOf(profAnswerSelection.getSelectedItem());
                boolean successValidation = validateData();
                //if successful validation
                if (successValidation) {
                    Map<String, Object> questionRecord = new HashMap<String, Object>();
                    questionRecord.put("Question__c", questionValue);
                    questionRecord.put("Choice1__c", option1Value);
                    questionRecord.put("Choice2__c", option2Value);
                    questionRecord.put("Choice3__c", option3Value);
                    questionRecord.put("Choice4__c", option4Value);
                    questionRecord.put("Difficulty_Level__c", selectedDifficulty);
                    //Calling updateQuestion() with question questionRecord and answer as parameter
                    updateQuestion(questionRecord,selectedAnswer);
                }
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getting values from the page
                quizNameValue = quizNameField.getText().toString();
                questionValue = questionField.getText().toString();
                option1Value = option1Field.getText().toString();
                option2Value = option2Field.getText().toString();
                option3Value = option3Field.getText().toString();
                option4Value = option4Field.getText().toString();
                userID = getIntent().getExtras().getString("userID");
                selectedDifficulty = String.valueOf(profDifficultySelection.getSelectedItem());
                selectedAnswer = String.valueOf(profAnswerSelection.getSelectedItem());
                boolean successValidation = validateData();
                //if successful validation
                if (successValidation) {
                    Map<String, Object> questionRecord = new HashMap<String, Object>();
                    questionRecord.put("Question__c", questionValue);
                    questionRecord.put("Choice1__c", option1Value);
                    questionRecord.put("Choice2__c", option2Value);
                    questionRecord.put("Choice3__c", option3Value);
                    questionRecord.put("Choice4__c", option4Value);
                    questionRecord.put("Difficulty_Level__c", selectedDifficulty);
                    //Calling updateQuestion() with question questionRecord and answer as parameter
                    updateQuestion(questionRecord,selectedAnswer);
                }
                Toast.makeText(getApplicationContext(), "Quiz updated", Toast.LENGTH_SHORT).show();
                //Redirecting to ProfessorHome page once quiz is updated
                Intent intent = new Intent(ProfessorUpdatingQuiz.this, ProfessorHome.class);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
        });
    }
    // Retrieving the questions of quiz from database
    private void setQuestion(String soql) throws UnsupportedEncodingException {
       RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) throws JSONException {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.isSuccess()) {
                            try {
                                JSONArray answerTable = result.asJSONObject().getJSONArray("records");
                                for (int i = 0; i < answerTable.length(); i++) {
                                    //adding the question details in lists
                                    answerIDList.add(answerTable.getJSONObject(i).getString("Id"));
                                    questionIDList.add(answerTable.getJSONObject(i).getString("Question__c"));
                                    answerList.add(answerTable.getJSONObject(i).getString("Answer__c"));
                                    JSONObject questionsTable = answerTable.getJSONObject(i).getJSONObject("Question__r");
                                    questionList.add(questionsTable.getString("Question__c"));
                                    option1List.add(questionsTable.getString("Choice1__c"));
                                    option2List.add(questionsTable.getString("Choice2__c"));
                                    option3List.add(questionsTable.getString("Choice3__c"));
                                    option4List.add(questionsTable.getString("Choice4__c"));
                                    difficultyLevelList.add(questionsTable.getString("Difficulty_Level__c"));
                                }
                                questionNumberValue = 1;
                                //calling setPage() to set the values of fields
                                setPage();
                            } catch (Exception e) {
                            }

                        }
                    }
                });
            }

            @Override
            public void onError(final Exception exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ProfessorUpdatingQuiz.this,
                                ProfessorUpdatingQuiz.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    //Setting the values of page
    private void setPage() {
        int positionD = 0,positionA = 0;
        questionNumber.setText(Integer.toString(questionNumberValue));
        quizNameField.setText(quizName);
        questionField.setText(questionList.get(questionNumberValue-1));
        option1Field.setText(option1List.get(questionNumberValue-1));
        option2Field.setText(option2List.get(questionNumberValue-1));
        option3Field.setText(option3List.get(questionNumberValue-1));
        option4Field.setText(option4List.get(questionNumberValue-1));
        if(difficultyLevelList.get(questionNumberValue-1).equalsIgnoreCase("1"))
            positionD = 1;
        else if(difficultyLevelList.get(questionNumberValue-1).equalsIgnoreCase("2"))
            positionD = 2;
        else if(difficultyLevelList.get(questionNumberValue-1).equalsIgnoreCase("3"))
            positionD = 3;
        if(answerList.get(questionNumberValue-1).equalsIgnoreCase("A"))
            positionA = 1;
        else if(answerList.get(questionNumberValue-1).equalsIgnoreCase("B"))
            positionA = 2;
        else if(answerList.get(questionNumberValue-1).equalsIgnoreCase("C"))
            positionA = 3;
        else if(answerList.get(questionNumberValue-1).equalsIgnoreCase("D"))
            positionA = 4;
        profAnswerSelection.setSelection(positionA);
        profDifficultySelection.setSelection(positionD);
    }
    //updating the answer for a particular question in database
    private void updateAnswer(Map<String, Object> answerRecord) {
        RestRequest restRequest;
        try {
            restRequest = RestRequest.getRequestForUpdate(getString(R.string.api_version), "Quiz_Answers__c",(answerIDList.get(questionNumberValue-1)), answerRecord);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "catch" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return ;
        }
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            questionNumberValue++;
                            setPage();

                        } catch(Exception e){
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
    //updating question to database
    private void updateQuestion(Map<String, Object> questionRecord,String selectedAnswer) {
        RestRequest restRequest;
        try {
            restRequest = RestRequest.getRequestForUpdate(getString(R.string.api_version), "Quiz_Questions__c",(questionIDList.get(questionNumberValue-1)), questionRecord);
            //Checking if this is last question in the quiz
            if(questionNumberValue>=questionIDList.size()){
                //if last question then redirect to ProfessorHome page after updating
                Toast.makeText(getApplicationContext(), "Quiz updated", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfessorUpdatingQuiz.this, ProfessorHome.class);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }

        } catch (Exception e) {
            return;
        }
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request,final RestResponse result)  {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                            try {
                                Map<String, Object> answerRecord = new HashMap<String, Object>();
                                answerRecord.put("Answer__c", selectedAnswer);
                                //Calling updateAnswer() with questionID and updated answer
                                updateAnswer(answerRecord);
                            } catch (Exception e) {
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
    //Validating the fields in the page
    private boolean validateData() {
        if (quizNameValue.equals("") || quizNameValue.equals("Enter name for the quiz")) {
            Toast.makeText(getApplicationContext(), "Please input quiz name.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (questionValue.equals("") || questionValue.equals("Enter the questionField")) {
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
