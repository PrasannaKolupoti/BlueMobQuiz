package com.mc.grp6.bluemobquiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ProfessorQuizAttendance extends SalesforceActivity {
    //Variable declaration and initialization
    private RestClient client;
    public String quizID,userName, userID, quizName;
    private GestureDetectorCompat gestureObject;
    public int score, rank;
    ArrayList<DisplayAttendance> attendanceList;
    public TextView quizNameHeading;
    //Method that is called after the activity resumes once we have a RestClient.
    @Override
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;
        quizID = getIntent().getExtras().getString("quizID");
        try {
            //Calling displayAttendance() with a query to display name,score,marks of students who attepted a particular quiz as parameter
            displayAttendance("SELECT Score__c, Rank__c, User__r.Name FROM User_Results__c where Quiz__c = \'"+quizID+"\'");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_quiz_attendance);
        userID = getIntent().getExtras().getString("userID");
        quizName = getIntent().getExtras().getString("quizName");
        quizNameHeading = (TextView) findViewById(R.id.attendanceQuizName);
        quizNameHeading.setText(quizName);
        gestureObject = new GestureDetectorCompat(this, new CustomGesture());
    }
    //Gestures
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureObject.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    //Gestures
    //https://developer.android.com/reference/android/view/GestureDetector.OnGestureListener.html#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
    class CustomGesture extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = event2.getY() - event1.getY();
                float diffX = event2.getX() - event1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > 1 && Math.abs(velocityX) > 1) {
                        if (diffX > 0) {
                            // Swipe Right
                            //Redirecting to ProfessorAttendance page
                            Intent intent = new Intent(ProfessorQuizAttendance.this, ProfessorAttendance.class);
                            intent.putExtra("userID",userID);
                            startActivity(intent);
                        } else {
                            // Swipe Left
                        }
                    }
                    result = true;
                } else if (Math.abs(diffY) > 1 && Math.abs(velocityY) > 1) {
                    if (diffY > 0) {
                        // Swipe Bottom

                    } else {
                        // Swipe Top
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
    //Retrieving the list of student name,score, rank from database
    private void displayAttendance(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //initializing an arraylist of type DisplayAttendance(has the setters and getters for student name,score,rank)
                            attendanceList = new ArrayList<DisplayAttendance>();
                            DisplayAttendance displayAttendance;
                            JSONArray resultTable = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < resultTable.length(); i++) {
                                displayAttendance = new DisplayAttendance();
                                score = (((int) Double.parseDouble(resultTable.getJSONObject(i).getString("Score__c"))));
                                rank = resultTable.getJSONObject(i).getInt("Rank__c");
                                JSONObject quizTable = resultTable .getJSONObject(i).getJSONObject("User__r");
                                userName = quizTable.getString("Name");
                                displayAttendance.setStudentName(userName);
                                displayAttendance.setMarks(score);
                                displayAttendance.setRank(rank);
                                attendanceList.add(displayAttendance);
                            }
                            final ListView lv = (ListView) findViewById(R.id.studentList);
                            //calling ProfessorCustomBaseAdapter class constructor with the context and attendanceList to set mutiple textviews in listview
                            lv.setAdapter(new ProfessorCustomBaseAdapter(getApplicationContext(), attendanceList));
                        } catch (Exception e) {
                            onError(e);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ProfessorQuizAttendance.this,
                                ProfessorQuizAttendance.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
