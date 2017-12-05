package com.mc.grp6.bluemobquiz;

import android.content.Intent;
import android.support.v4.view.GestureDetectorCompat;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class ProfessorAttendance extends SalesforceActivity {
    //Variable declaration and initialization
    private RestClient client;
    public String userID;
    private ArrayAdapter<String> attemptedQuizAdapter;
    private GestureDetectorCompat gestureObject;
    public ArrayList<String> quizIDList = new ArrayList<String>();
    public ArrayList<String> quizNameList = new ArrayList<String>();
    //Method that is called after the activity resumes once we have a RestClient.
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;
        try {
            //Calling displayAttemptedQuizzes() with a query to display the list of attempted quizzes as parameter
            displayAttemptedQuizzes("SELECT Quiz__c, Quiz__r.Name FROM User_Results__c group by Quiz__r.Name, Quiz__c");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // Create list adapter
        attemptedQuizAdapter = new ArrayAdapter<String>(this, R.layout.professor_listviewattendance,R.id.quizAttendance, new ArrayList<String>());
        //Setting the adapter
        ((ListView) findViewById(R.id.quizAttendanceList)).setAdapter(attemptedQuizAdapter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_attendance);

        userID = getIntent().getExtras().getString("userID");
        gestureObject = new GestureDetectorCompat(this, new CustomGesture());
        ListView quizList = (ListView) findViewById(R.id.quizAttendanceList);
        //Listening to item click and performing action
        quizList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String quizName = quizNameList.get(position);
                String quizID = quizIDList.get(position);
                //Redirect to ProfessorQuizAttendance page to view attendance,score and rank of that particular quiz
                Intent intent = new Intent(ProfessorAttendance.this, ProfessorQuizAttendance.class);
                intent.putExtra("userID",userID);
                intent.putExtra("quizID", quizID);
                intent.putExtra("quizName",quizName);
                startActivity(intent);
            }
        });
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
                            //Redirect to ProfessorHome page
                            Intent intent = new Intent(ProfessorAttendance.this, ProfessorHome.class);
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
    //Retrieving the list of attempted quizzes from database
    private void displayAttemptedQuizzes(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            attemptedQuizAdapter.clear();
                            JSONArray resultTable = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < resultTable.length(); i++) {
                                quizIDList.add(resultTable.getJSONObject(i).getString("Quiz__c"));
                                quizNameList.add(resultTable.getJSONObject(i).getString("Name"));
                                //Adding the list of attempted quizzes to array adapter
                                attemptedQuizAdapter.add(quizNameList.get(i));
                            }
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
                        Toast.makeText(ProfessorAttendance.this,
                                ProfessorAttendance.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

}
