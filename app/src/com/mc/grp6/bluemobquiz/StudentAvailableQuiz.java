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
    public RestClient client;
    public String userID, quizID, quizName, ownerDevices;
    public int quizposition;
    private ArrayAdapter<String> listAdapter;
    public ArrayList<String> quizIDList = new ArrayList<String>();
    public ArrayList<String> quizNameList = new ArrayList<String>();
    public ArrayList<String> quizStatusList = new ArrayList<String>();
    public ArrayList<String> ownerDevicesList = new ArrayList<String>();
    public ArrayList<String> scannedDevicesList = new ArrayList<String>();
    @Override
    public void onResume() {
        // Create list adapter
        listAdapter = new ArrayAdapter<String>(this, R.layout.student_listviewavailablequizzes,R.id.availableQuiz, new ArrayList<String>());
        ((ListView) findViewById(R.id.availableQuizList)).setAdapter(listAdapter);
        super.onResume();
    }
    @Override
    public void onResume(RestClient client) {
        this.client = client;
        try {
            searchQuizzes("SELECT ID,Name,Is_Active__c,Owner_Devices__c FROM Quiz__c");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ((ListView) findViewById(R.id.availableQuizList)).setAdapter(listAdapter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_available_quiz);
        userID = getIntent().getExtras().getString("userID");
        ListView availablequizList = (ListView) findViewById(R.id.availableQuizList);
        availablequizList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String quizID = quizIDList.get(quizposition);
                String quizName = quizNameList.get(quizposition);
                Intent intent = new Intent(StudentAvailableQuiz.this, StudentAttemptingQuiz.class);
                intent.putExtra("userID",userID);
                intent.putExtra("quizID",quizID);
                intent.putExtra("quizName",quizName);
                startActivity(intent);
            }
        });
    }
    private void searchQuizzes(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        System.out.println("*********restRequest:"+restRequest);
        System.out.println("*********restRequest:"+restRequest.toString());
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            listAdapter.clear();
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < records.length(); i++) {
                                quizIDList.add(records.getJSONObject(i).getString("Id"));
                                quizNameList.add(records.getJSONObject(i).getString("Name"));
                                quizStatusList.add(records.getJSONObject(i).getString("Is_Active__c"));
                                ownerDevicesList.add(records.getJSONObject(i).getString("Owner_Devices__c"));
                                userID = getIntent().getExtras().getString("userID");
                                System.out.println("*********userID:"+userID);
                                scannedDevicesList = getIntent().getStringArrayListExtra("scannedDevicesList");
                                for (String address : scannedDevicesList) {
                                    System.out.println("*********address:"+address);
                                }
                                if(quizStatusList.get(i).equals("true")){
                                    quizID = quizIDList.get(i);
                                    quizName = quizNameList.get(i);
                                    ownerDevices = ownerDevicesList.get(i);
                                    quizposition = i;
                                    System.out.println("*********quizID:"+quizID);
                                    System.out.println("*********quizName:"+quizName);
                                    System.out.println("*********ownerDevices:"+ownerDevices);

                                    for(String id: scannedDevicesList){
                                        System.out.println("*********id:"+id);
                                        if(ownerDevices.contains(id)){
                                            listAdapter.add(quizName);
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
