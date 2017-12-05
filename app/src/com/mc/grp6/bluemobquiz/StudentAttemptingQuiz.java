package com.mc.grp6.bluemobquiz;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

public class StudentAttemptingQuiz extends SalesforceActivity {
    //Variable declaration and initialization
    public TextView questionField, option1Field, option2Field, option3Field, option4Field, questionNumber,quizNameField, timer;
    public Spinner studAnswerSelection;
    public Button nextButton, submitButton;
    public String selectedAnswer,userID,quizName, quizID, questionID;
    private RestClient client;
    public int questionNumberValue,quesNum=1,seconds, numQuestions,difficultyLevel=1;
    public int remainingSeconds = -1, countDiff1=0,countDiff2=0,countDiff3=0,currCountDiff1=0,currCountDiff2=0,currCountDiff3=0;
    public ArrayList<String> questionIDList = new ArrayList<String>();
    public ArrayList<String> answerIDList = new ArrayList<String>();
    public ArrayList<String> questionList = new ArrayList<String>();
    public ArrayList<String> option1List = new ArrayList<String>();
    public ArrayList<String> option2List = new ArrayList<String>();
    public ArrayList<String> option3List = new ArrayList<String>();
    public ArrayList<String> option4List = new ArrayList<String>();
    public ArrayList<String> difficultyLevelList = new ArrayList<String>();
    CountDownTimer countDownTimer;
    public Boolean next = false, isCorrectAnswer;
    HashSet<Integer> QuestionSet = new HashSet <Integer> ();
    //Method that is called after the activity resumes once we have a RestClient.
    @Override
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;
        quizID = getIntent().getExtras().getString("quizID");
        try {
            //Calling setQuestion() with query to get the questions in quiz as parameter
            setQuestion("SELECT id, Question__c, Question__r.Question__c, Question__r.Choice1__c, Question__r.Choice2__c, Question__r.Choice3__c, Question__r.Choice4__c,Question__r.Difficulty_Level__c from Quiz_Answers__c where Quiz__c =\'" + quizID + "\'");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attempting_quiz);
        userID = getIntent().getExtras().getString("userID");
        quizID = getIntent().getExtras().getString("quizID");
        quizName = getIntent().getExtras().getString("quizName");
        timer = (TextView) findViewById(R.id.timer);
        questionNumber = (TextView) findViewById(R.id.studentQuestionNumber);
        quizNameField = (TextView) findViewById(R.id.studentQuizName);
        questionField = (TextView) findViewById(R.id.studentQuestion);
        option1Field = (TextView) findViewById(R.id.studentOption1);
        option2Field = (TextView) findViewById(R.id.studentOption2);
        option3Field = (TextView) findViewById(R.id.studentOption3);
        option4Field = (TextView) findViewById(R.id.studentOption4);
        studAnswerSelection = (Spinner) findViewById(R.id.studentAnswerSpinner);
        nextButton = (Button) findViewById(R.id.studentNext);
        submitButton = (Button) findViewById(R.id.studentSubmit);
        //spinner selection listener
        studAnswerSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedAnswer = String.valueOf(studAnswerSelection.getSelectedItem());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //Next button click listener
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stop timer on Next button click
                countDownTimer.cancel();
                selectedAnswer = String.valueOf(studAnswerSelection.getSelectedItem());
                Map<String, Object> answerRecord = new HashMap<String, Object>();
                answerRecord.put("Answer__c", selectedAnswer);
                answerRecord.put("Quiz_Question__c", questionID);
                answerRecord.put("User__c", userID);
                //Calling insertAnswer() with questionID and selected answer
                insertAnswer(answerRecord);
            }
        });
        //Submit button click listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stop timer on Submit button click
                countDownTimer.cancel();
                //Setting timer to null
                countDownTimer = null;
                selectedAnswer = String.valueOf(studAnswerSelection.getSelectedItem());
                Map<String, Object> answerRecord = new HashMap<String, Object>();
                answerRecord.put("Answer__c", selectedAnswer);
                answerRecord.put("Quiz_Question__c", questionID);
                answerRecord.put("User__c", userID);
                //Calling insertAnswer() with questionID and selected answer
                insertAnswer(answerRecord);
                Toast.makeText(getApplicationContext(), "Quiz Submitted", Toast.LENGTH_SHORT).show();
                //Redirecting to StudentHome page once quiz is submitted
                Intent intent = new Intent(StudentAttemptingQuiz.this, StudentHome.class);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
        });

    }
    //Timer functionality
    private void countDownTimer(int sec, TextView timer) {
        //converting seconds to milliseconds
        countDownTimer =new CountDownTimer(sec * 1000 + 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                //converting milliseconds to the format hh:mm:ss
                int sec = (int) (millisUntilFinished / 1000);
                int hours = sec / (60 * 60);
                int tempMint = (sec - (hours * 60 * 60));
                int minutes = tempMint / 60;
                sec = tempMint - (minutes * 60);
                //set the value in the field
                timer.setText(String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", sec));
                remainingSeconds = (hours*60*60)+(minutes*60)+sec;
                //below code is if time is up then call function to insert current answer and then submit quiz
                if(remainingSeconds==0){
                    selectedAnswer = String.valueOf(studAnswerSelection.getSelectedItem());
                    Map<String, Object> answerRecord = new HashMap<String, Object>();
                    answerRecord.put("Answer__c", selectedAnswer);
                    answerRecord.put("Quiz_Question__c", questionID);
                    answerRecord.put("User__c", userID);
                    //Calling insertAnswer() with questionID and selected answer
                    insertAnswer(answerRecord);
                }
            }
            public void onFinish() {
                Toast.makeText(getApplicationContext(),"Time UP!!!",Toast.LENGTH_SHORT).show();
                selectedAnswer = String.valueOf(studAnswerSelection.getSelectedItem());
                Map<String, Object> answerRecord = new HashMap<String, Object>();
                answerRecord.put("Answer__c", selectedAnswer);
                answerRecord.put("Quiz_Question__c", questionID);
                answerRecord.put("User__c", userID);
                //Calling insertAnswer() with questionID and selected answer
                insertAnswer(answerRecord);
            }
        }.start();
    }
    //inserting the answer for a particular question in database
    private void insertAnswer(Map<String, Object> answerRecord) {
        RestRequest restRequest;
        try {
            restRequest = RestRequest.getRequestForCreate(getString(R.string.api_version), "User_Answers__c", answerRecord);
            //Checking if this is last question in the quiz and remaining seconds
            if(quesNum>questionIDList.size() || remainingSeconds==1){
                //stop timer on Submit button click
                countDownTimer.cancel();
                //Setting timer to null
                countDownTimer = null;
                next = true;
                Toast.makeText(getApplicationContext(), "Quiz Submitted", Toast.LENGTH_SHORT).show();
                //Redirecting to StudentHome page once quiz is submitted
                Intent intent = new Intent(StudentAttemptingQuiz.this, StudentHome.class);
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
                            //If time is not up and if not last question
                            if(remainingSeconds > 1 && quesNum <= questionIDList.size())
                                //calling nextQuestion with query to check if this answer is correct as parameter(for setting difficulty level)
                                nextQuestion("SELECT Is_Answer_Correct__c from Answer_Correction__c where User__c =\'" + userID + "\' and Quiz_Answer__r.Question__c =\'"+questionID+"\'");
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
    //getting questions from database
    private void nextQuestion(String soql) throws UnsupportedEncodingException {
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
                                JSONArray answerCorrectionTable = result.asJSONObject().getJSONArray("records");
                                isCorrectAnswer = Boolean.valueOf(answerCorrectionTable.getJSONObject(0).getString("Is_Answer_Correct__c"));
                                //if answer for current question is correct
                                if(isCorrectAnswer){
                                    //based on the difficulty level of current question increase the difficulty level for next question
                                    switch (difficultyLevel){
                                        case 1: if(currCountDiff2 < countDiff2)
                                                    difficultyLevel = 2;
                                                else if(currCountDiff3 < countDiff3)
                                                    difficultyLevel = 3;
                                                else if(currCountDiff1 < countDiff1)
                                                    difficultyLevel = 1;
                                                break;
                                        case 2: if(currCountDiff3 < countDiff3)
                                                    difficultyLevel = 3;
                                                else if(currCountDiff2 < countDiff2)
                                                    difficultyLevel = 2;
                                                else if(currCountDiff1 < countDiff1)
                                                    difficultyLevel = 1;
                                                break;
                                        case 3: if(currCountDiff3 < countDiff3)
                                                    difficultyLevel = 3;
                                                else if(currCountDiff2 < countDiff2)
                                                    difficultyLevel = 2;
                                                else if(currCountDiff1 < countDiff1)
                                                    difficultyLevel = 1;
                                                break;
                                    }
                                }
                                //else the answer for current question is wrong
                                else {
                                    //based on the difficulty level of current question decrease the difficulty level for next question
                                    switch (difficultyLevel){
                                        case 1: if(currCountDiff1 < countDiff1)
                                                    difficultyLevel = 1;
                                                else if(currCountDiff2 < countDiff2)
                                                    difficultyLevel = 2;
                                                else if(currCountDiff3 < countDiff3)
                                                    difficultyLevel = 3;
                                                break;
                                        case 2: if(currCountDiff1 < countDiff1)
                                                    difficultyLevel = 1;
                                                else if(currCountDiff2 < countDiff2)
                                                    difficultyLevel = 2;
                                                else if(currCountDiff3 < countDiff3)
                                                    difficultyLevel = 3;
                                                break;
                                        case 3: if(currCountDiff2 < countDiff2)
                                                    difficultyLevel = 2;
                                                else if(currCountDiff1 < countDiff1)
                                                    difficultyLevel = 1;
                                                 else if(currCountDiff3 < countDiff3)
                                                        difficultyLevel = 3;
                                                break;
                                    }
                                }
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
                        Toast.makeText(StudentAttemptingQuiz.this,
                                StudentAttemptingQuiz.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
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
                                    JSONObject questionsTable = answerTable.getJSONObject(i).getJSONObject("Question__r");
                                    questionList.add(questionsTable.getString("Question__c"));
                                    option1List.add(questionsTable.getString("Choice1__c"));
                                    option2List.add(questionsTable.getString("Choice2__c"));
                                    option3List.add(questionsTable.getString("Choice3__c"));
                                    option4List.add(questionsTable.getString("Choice4__c"));
                                    difficultyLevelList.add(questionsTable.getString("Difficulty_Level__c"));
                                    //counting the number of questions with difficulty level 1
                                    if(difficultyLevelList.get(i).equals("1")){
                                        countDiff1++;
                                    }
                                    //counting the number of questions with difficulty level 2
                                    else if(difficultyLevelList.get(i).equals("2")){
                                        countDiff2++;
                                    }
                                    //counting the number of questions with difficulty level 3
                                    else if(difficultyLevelList.get(i).equals("3")){
                                        countDiff3++;
                                    }
                                }
                                numQuestions = questionIDList.size();
                                //setting the time as total number of questions * 30 i.e. 30 seconds to answer a question
                                seconds = numQuestions * 30;
                                remainingSeconds=seconds;
                                questionNumberValue = 1;
                                //calling setPage() to set the values of fields
                                setPage();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
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
                        Toast.makeText(StudentAttemptingQuiz.this,
                                StudentAttemptingQuiz.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    private void setPage() {
        //selecting the first question randomly
        if(QuestionSet.size()==0) {
            Random rand = new Random();
            questionNumberValue = rand.nextInt(numQuestions) + 1;
            QuestionSet.add(questionNumberValue-1);
            //counting the number of answered questions with difficulty level 1
            if(difficultyLevelList.get(questionNumberValue-1).equals("1")){
                currCountDiff1++;
            }
            //counting the number of answered questions with difficulty level 2
            else if(difficultyLevelList.get(questionNumberValue-1).equals("2")){
                currCountDiff2++;
            }
            //counting the number of answered questions with difficulty level 3
            else if(difficultyLevelList.get(questionNumberValue-1).equals("3")) {
                currCountDiff3++;
            }
        }
        //if not first question then select the question with the appropriate difficulty level
        else{
            for(int i=1; i<=difficultyLevelList.size(); i++) {
                questionNumberValue = i;
                if(Integer.parseInt(difficultyLevelList.get(i-1)) != difficultyLevel || QuestionSet.contains(i-1)) {
                }
                else {
                    QuestionSet.add(questionNumberValue-1);
                    if(difficultyLevelList.get(questionNumberValue-1).equals("1")){
                        currCountDiff1++;
                    } else if(difficultyLevelList.get(questionNumberValue-1).equals("2")){
                        currCountDiff2++;
                    } else if(difficultyLevelList.get(questionNumberValue-1).equals("3")){
                        currCountDiff3++;
                    }
                    break;
                }
            }
        }
        //setting the next question in the fields of the page
        questionNumber.setText(Integer.toString(quesNum)+" ");
        quesNum++;
        questionID = questionIDList.get(questionNumberValue-1);
        quizNameField.setText(quizName);
        questionField.setText(questionList.get(questionNumberValue-1));
        option1Field.setText(option1List.get(questionNumberValue-1));
        option2Field.setText(option2List.get(questionNumberValue-1));
        option3Field.setText(option3List.get(questionNumberValue-1));
        option4Field.setText(option4List.get(questionNumberValue-1));
        studAnswerSelection.setSelection(0);
        if(remainingSeconds!=0)
            //calling countdowntimer with the time left value
            countDownTimer(remainingSeconds,timer);
    }
}
