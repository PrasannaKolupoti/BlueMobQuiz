package com.mc.grp6.bluemobquiz;

import android.content.Intent;
import android.os.Bundle;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class StudentAvailableQuiz extends SalesforceActivity {
    //Variable declaration and initialization
    public RestClient client;
    public String userID, quizID, quizName, ownerDevices;
    public int quizPosition;
    private ArrayAdapter<String> listAdapter;
    public ArrayList<String> quizIDList = new ArrayList<String>();
    public ArrayList<String> quizNameList = new ArrayList<String>();
    public ArrayList<String> uniqueQuizIDList = new ArrayList<String>();
    public ArrayList<String> uniqueQuizNameList = new ArrayList<String>();
    public ArrayList<String> quizStatusList = new ArrayList<String>();
    public ArrayList<String> ownerDevicesList = new ArrayList<String>();
    public ArrayList<String> scannedDevicesList = new ArrayList<String>();
    @Override
    public void onResume() {
        // Create list adapter
        listAdapter = new ArrayAdapter<String>(this, R.layout.student_listviewavailablequizzes,R.id.availableQuiz, new ArrayList<String>());
        //Setting the adapter
        ((ListView) findViewById(R.id.availableQuizList)).setAdapter(listAdapter);
        super.onResume();
    }
    //Method that is called after the activity resumes once we have a RestClient.
    @Override
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;
        try {
            //calling searchQuizzes() with query to get the quizzes as parameter
            searchQuizzes("SELECT ID,Name,Is_Active__c,Owner_Devices__c FROM Quiz__c");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //Setting the adapter
        ((ListView) findViewById(R.id.availableQuizList)).setAdapter(listAdapter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_available_quiz);
        userID = getIntent().getExtras().getString("userID");
        ListView availableQuizList = (ListView) findViewById(R.id.availableQuizList);
        //Listening to item click and performing action
        availableQuizList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String uniqueQuizID = uniqueQuizIDList.get(position);
                String uniqueQuizName = uniqueQuizNameList.get(position);
                //Redirect to StudentAttemptingQuiz page to answer the available quiz
                Intent intent = new Intent(StudentAvailableQuiz.this, StudentAttemptingQuiz.class);
                intent.putExtra("userID",userID);
                intent.putExtra("quizID",uniqueQuizID);
                intent.putExtra("quizName",uniqueQuizName);
                startActivity(intent);
            }
        });
    }
    //Retrieving the quizzes from database
    private void searchQuizzes(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            listAdapter.clear();
                            //adding quiz details to array lists
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < records.length(); i++) {
                                quizIDList.add(records.getJSONObject(i).getString("Id"));
                                quizNameList.add(records.getJSONObject(i).getString("Name"));
                                quizStatusList.add(records.getJSONObject(i).getString("Is_Active__c"));
                                ownerDevicesList.add(records.getJSONObject(i).getString("Owner_Devices__c"));
                                userID = getIntent().getExtras().getString("userID");
                                scannedDevicesList = getIntent().getStringArrayListExtra("scannedDevicesList");
                                //if quiz is shared and professor device is in the bluetooth range add quiz to array list
                                if(quizStatusList.get(i).equals("true")){
                                    quizID = quizIDList.get(i);
                                    quizName = quizNameList.get(i);
                                    ownerDevices = ownerDevicesList.get(i);
                                    quizPosition = i;
                                    for(String id: scannedDevicesList){
                                        if(ownerDevices.contains(id) && (listAdapter.getPosition(quizName))==-1){
                                            //Adding quiz to adapter
                                            listAdapter.add(quizName);
                                            uniqueQuizIDList.add(quizID);
                                            uniqueQuizNameList.add(quizName);
                                        }
                                    }
                                }
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
                        Toast.makeText(StudentAvailableQuiz.this,
                                StudentAvailableQuiz.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
