package com.mc.grp6.bluemobquiz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
    private ArrayAdapter<String> listAdapter;
    public ArrayList<String> quizIDList = new ArrayList<String>();
    public ArrayList<String> quizNameList = new ArrayList<String>();
    public ArrayList<String> quizStatusList = new ArrayList<String>();

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
            displayQuizzes("SELECT ID,Name,Is_Active__c FROM Quiz__c");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ((ListView) findViewById(R.id.quizListView)).setAdapter(listAdapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_home);
        userID = getIntent().getExtras().getString("userID");
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
                            System.out.println("*********Is_Active__c:"+result.toString());
                            listAdapter.clear();
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < records.length(); i++) {
                                quizIDList.add(records.getJSONObject(i).getString("Id"));
                                quizNameList.add(records.getJSONObject(i).getString("Name"));
                                quizStatusList.add(records.getJSONObject(i).getString("Is_Active__c"));
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
