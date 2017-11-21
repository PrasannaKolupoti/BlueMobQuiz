package com.mc.grp6.bluemobquiz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfessorHome extends SalesforceActivity {

    private RestClient client;
    public String userID;
    private GestureDetectorCompat gestureObject;
    private ArrayAdapter<String> createdQuizAdapter;
    public ArrayList<String> quizIDList = new ArrayList<String>();
    public ArrayList<String> quizNameList = new ArrayList<String>();
    public ArrayList<String> quizStatusList = new ArrayList<String>();

    @Override
    public void onResume() {
        // Create list adapter
        createdQuizAdapter = new ArrayAdapter<String>(this, R.layout.professor_listviewquizzes,R.id.quiz, new ArrayList<String>());
        ((ListView) findViewById(R.id.quizListView)).setAdapter(createdQuizAdapter);
        super.onResume();
    }
    @Override
    public void onResume(RestClient client) {
        this.client = client;
        try {
            displayCreatedQuizzes("SELECT ID,Name,Is_Active__c FROM Quiz__c");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ((ListView) findViewById(R.id.quizListView)).setAdapter(createdQuizAdapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_home);
        userID = getIntent().getExtras().getString("userID");
        gestureObject = new GestureDetectorCompat(this, new CustomGesture());
        Button createQuizButton = (Button) findViewById(R.id.createQuizButton);
        ListView quizList = (ListView) findViewById(R.id.quizListView);
        quizList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String quizID = quizIDList.get(position);
                String quizName = quizNameList.get(position);

                Intent intent = new Intent(ProfessorHome.this, ProfessorUpdatingQuizName.class);
                intent.putExtra("userID",userID);
                intent.putExtra("quizID",quizID);
                intent.putExtra("quizName",quizName);
                startActivity(intent);
            }
        });
        quizList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String quizID = quizIDList.get(position);
                String isActive  = quizStatusList.get(position) ;

                if(isActive.equals("false")){
                    String quizStatus = "shared";
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(ProfessorHome.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(ProfessorHome.this);
                    }
                    builder.setTitle("Sharing Quiz")
                            .setMessage("Do you want to share this quiz?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Map<String, Object> shareQuiz = new HashMap<String, Object>();
                                    shareQuiz.put("Is_Active__c", "true");

                                    sharingQuiz(quizID, quizStatus, shareQuiz);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else if(isActive.equals("true")){
                    String quizStatus = "unshared";
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(ProfessorHome.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(ProfessorHome.this);
                    }
                    builder.setTitle("Unsharing Quiz")
                            .setMessage("Do you want to unshare this quiz?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Map<String, Object> shareQuiz = new HashMap<String, Object>();
                                    shareQuiz.put("Is_Active__c", "false");
                                    sharingQuiz(quizID, quizStatus, shareQuiz);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                return true;
            }
        });
        createQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessorHome.this, ProfessorAddingQuizName.class);
                intent.putExtra("userID",userID);
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
                        Intent intent = new Intent(ProfessorHome.this, ProfessorAttendance.class);
                        intent.putExtra("userID",userID);
                        startActivity(intent);
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
    private void sharingQuiz(String quizID, String quizStatus,  Map<String, Object> shareQuizRecord) {
        RestRequest restRequest;
        try {
            restRequest = RestRequest.getRequestForUpdate(getString(R.string.api_version), "Quiz__c", quizID, shareQuizRecord);
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
                                    if(quizStatus.equalsIgnoreCase("shared"))
                                        Toast.makeText(getApplicationContext(), "Quiz Shared", Toast.LENGTH_SHORT).show();
                                    else if(quizStatus.equalsIgnoreCase("unshared"))
                                        Toast.makeText(getApplicationContext(), "Quiz Unshared", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ProfessorHome.this, ProfessorHome.class);
                                    intent.putExtra("userID",userID);
                                    startActivity(intent);

                            } catch (Exception e) {
                                System.out.println("*****exception"+e.getMessage());
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


    private void displayCreatedQuizzes(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            createdQuizAdapter.clear();
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < records.length(); i++) {
                                quizIDList.add(records.getJSONObject(i).getString("Id"));
                                quizNameList.add(records.getJSONObject(i).getString("Name"));
                                quizStatusList.add(records.getJSONObject(i).getString("Is_Active__c"));
                                createdQuizAdapter.add(records.getJSONObject(i).getString("Name"));
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
                        Toast.makeText(ProfessorHome.this,
                                ProfessorHome.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

}
