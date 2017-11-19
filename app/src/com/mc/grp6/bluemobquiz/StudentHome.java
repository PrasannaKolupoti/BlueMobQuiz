package com.mc.grp6.bluemobquiz;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class StudentHome extends SalesforceActivity {

    private static final String TAG = "StudentHome";
    private ArrayAdapter<String> attemptedQuizNameAdapter;
    private ArrayAdapter<Integer> scoreAdapter;
    private ArrayAdapter<String> rankAdapter;
    private RestClient client;
    public String quizID,quizName, userID;
    public int score, rank;
    public ArrayList<String> quizStatusList = new ArrayList<String>();
    public ArrayList<String> deviceAddressList = new ArrayList<>();
    public Button searchQuiz;
    BluetoothAdapter mBluetoothAdapter;
    ArrayList<DisplayResults>  finalResult;
    @Override
    public void onResume(RestClient client) {
        this.client = client;
        try {
            userID = getIntent().getExtras().getString("userID");
            displayAttemptedQuizzes("SELECT Quiz__c, Quiz__r.Name, Score__c FROM User_Results__c where User__c = \'"+userID+"\'");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        /*attemptedQuizNameAdapter = new ArrayAdapter<String>(this, R.layout.student_listviewattemptedquizzes,R.id.answeredQuiz, new ArrayList<String>());
        ((ListView) findViewById(R.id.attemptedQuizListView)).setAdapter(attemptedQuizNameAdapter);
        scoreAdapter = new ArrayAdapter<Integer>(this, R.layout.student_listviewattemptedquizzes,R.id.marks, new ArrayList<Integer>());
        ((ListView) findViewById(R.id.attemptedQuizListView)).setAdapter(scoreAdapter);*/
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);
        finalResult = new ArrayList<DisplayResults>();
        userID = getIntent().getExtras().getString("userID");
        searchQuiz = (Button) findViewById(R.id.searchQuizButton);
        findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enableDisableBT();
        searchQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.loadingProgressBar).setVisibility(View.VISIBLE);
                deviceDiscover(v);
            }
        });
    }
    // Register for broadcasts when a device is discovered.
    public void deviceDiscover(View view) {
        //check BT permissions in manifest
        checkBTPermissions();
        IntentFilter discoverDevicesIntent = new IntentFilter();
        discoverDevicesIntent.addAction(BluetoothDevice.ACTION_FOUND);
        discoverDevicesIntent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        discoverDevicesIntent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        mBluetoothAdapter.startDiscovery();
    }
    // This method is required for all devices running API23+. Android must programmatically check the permissions for bluetooth. Putting the proper permissions in the manifest is not enough.
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
        }
    }
    // Broadcast Receiver for listing devices that are not yet paired
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Action Discovery: "+action);
            BluetoothDevice device = null;
            if(mBluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            } else if(mBluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
                for (String address : deviceAddressList) {
                    Log.d("address", address);
                }
                Intent i = new Intent(StudentHome.this, StudentAvailableQuiz.class);
                i.putExtra("userID",userID);
                i.putStringArrayListExtra("scannedDevicesList", deviceAddressList);
                startActivity(i);
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)){
                device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                if(!deviceAddressList.contains(device.getAddress())) {
                    deviceAddressList.add(device.getAddress());
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                }
            }
        }
    };
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver3);
    }
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };
    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
        }
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }
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
                            //attemptedQuizNameAdapter.clear();
                            //scoreAdapter.clear();
                            finalResult = new ArrayList<DisplayResults>();
                            DisplayResults displayResults;
                            JSONArray resultTable = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < resultTable.length(); i++) {
                                displayResults = new DisplayResults();
                                quizID = resultTable.getJSONObject(i).getString("Quiz__c");
                                score = (((int) Double.parseDouble(resultTable.getJSONObject(i).getString("Score__c"))));
                                JSONObject quizTable = resultTable .getJSONObject(i).getJSONObject("Quiz__r");
                                quizName = quizTable.getString("Name");
                                System.out.println("*************quizID:"+quizID);
                                System.out.println("*************score:"+score);
                                System.out.println("*************quizName:"+quizName);
                                displayResults.setQuizName(quizName);
                                displayResults.setMarks(score);
                                displayResults.setRank(rank);
                                finalResult.add(displayResults);
                                //attemptedQuizNameAdapter.add(quizName);
                                //scoreAdapter.add(score);
                            }

                            final ListView lv = (ListView) findViewById(R.id.attemptedQuizListView);
                            lv.setAdapter(new CustomBaseAdapter(getApplicationContext(), finalResult));
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
                        Toast.makeText(StudentHome.this,
                                StudentHome.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
