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
    private RestClient client;
    public String userID;
    private ArrayAdapter<String> attemptedQuizAdapter;
    private GestureDetectorCompat gestureObject;
    public ArrayList<String> quizIDList = new ArrayList<String>();
    public ArrayList<String> quizNameList = new ArrayList<String>();
    public void onResume(RestClient client) {
        this.client = client;
        try {
            displayAttemptedQuizzes("SELECT Quiz__c, Quiz__r.Name FROM User_Results__c group by Quiz__r.Name, Quiz__c");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        attemptedQuizAdapter = new ArrayAdapter<String>(this, R.layout.professor_listviewattendance,R.id.quizAttendance, new ArrayList<String>());
        ((ListView) findViewById(R.id.quizAttendanceList)).setAdapter(attemptedQuizAdapter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_attendance);

        userID = getIntent().getExtras().getString("userID");
        gestureObject = new GestureDetectorCompat(this, new CustomGesture());
        ListView quizList = (ListView) findViewById(R.id.quizAttendanceList);
        quizList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String quizName = quizNameList.get(position);
                String quizID = quizIDList.get(position);
                System.out.println("**************quizName:"+quizName);
                System.out.println("**************quizID:"+quizID);

                Intent intent = new Intent(ProfessorAttendance.this, ProfessorQuizAttendance.class);
                intent.putExtra("userID",userID);
                intent.putExtra("quizID", quizID);
                intent.putExtra("quizName",quizName);

                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureObject.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
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
    private void displayAttemptedQuizzes(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                System.out.println("**************Result:"+result.toString());
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            attemptedQuizAdapter.clear();
                            JSONArray resultTable = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < resultTable.length(); i++) {
                                //if(!(quizIDList.contains(resultTable.getJSONObject(i).getString("Quiz__c"))))
                                    quizIDList.add(resultTable.getJSONObject(i).getString("Quiz__c"));
                                //JSONObject quizTable = resultTable .getJSONObject(i).getJSONObject("Quiz__r");
                                //if(!(quizNameList.contains(quizTable.getString("Name")))){
                                    quizNameList.add(resultTable.getJSONObject(i).getString("Name"));
                                    attemptedQuizAdapter.add(quizNameList.get(i));
                                //}
                            }
                            for(int i = 0; i< quizIDList.size();i++){
                                System.out.println("**************quizIDList:"+quizIDList.get(i));
                                System.out.println("**************quizNameList:"+quizNameList.get(i));
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
