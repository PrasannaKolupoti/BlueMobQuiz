package com.mc.grp6.bluemobquiz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

public class ProfessorHome extends SalesforceActivity {

    private RestClient client;
    public String userID;
    private ArrayAdapter<String> listAdapter;
    @Override
    public void onResume() {
        // Create list adapter
        listAdapter = new ArrayAdapter<String>(this, R.layout.professor_listviewquizzes,R.id.quiz, new ArrayList<String>());
        ((ListView) findViewById(R.id.quizListView)).setAdapter(listAdapter);
        super.onResume();
    }
    @Override
    public void onResume(RestClient client) {
        this.client = client;
        try {
            displayQuizzes("SELECT Name FROM Quiz__c");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ((ListView) findViewById(R.id.quizListView)).setAdapter(listAdapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_home);
        Button createQuizButton = (Button) findViewById(R.id.createQuizButton);

        createQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userID = getIntent().getExtras().getString("userID");
                Intent intent = new Intent(ProfessorHome.this, ProfessorCreatingQuiz.class);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
        });
    }

    private void displayQuizzes(String soql) throws UnsupportedEncodingException {
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
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < records.length(); i++) {
                                listAdapter.add(records.getJSONObject(i).getString("Name"));
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
