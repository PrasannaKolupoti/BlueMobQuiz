package com.mc.grp6.bluemobquiz;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
    //Variable declaration and initialization
    private RestClient client;
    public String quizID,quizName, userID;
    public int score, rank;
    public ArrayList<String> deviceAddressList = new ArrayList<>();
    public Button searchQuiz;
    BluetoothAdapter mBluetoothAdapter;
    ArrayList<DisplayResults>  finalResult;
    //Method that is called after the activity resumes once we have a RestClient.
    @Override
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;
        try {
            userID = getIntent().getExtras().getString("userID");
            //calling displayAttemptedQuizzes() with query to get the quiz name,score, rank as parameter
            displayAttemptedQuizzes("SELECT Quiz__c, Quiz__r.Name, Score__c, Rank__c FROM User_Results__c where User__c = \'"+userID+"\'");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
        //calling enableDisableBT() to enable bluetooth
        enableDisableBT();
        //Search button click listener
        searchQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.loadingProgressBar).setVisibility(View.VISIBLE);
                //calling deviceDiscover() with view as parameter to scan nearby devices
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
    //Checking the permissions for bluetooth.
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
            BluetoothDevice device = null;

            if(mBluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            } else if(mBluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
                Intent i = new Intent(StudentHome.this, StudentAvailableQuiz.class);
                i.putExtra("userID",userID);
                i.putStringArrayListExtra("scannedDevicesList", deviceAddressList);
                startActivity(i);
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)){
                device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                if(!deviceAddressList.contains(device.getAddress())) {
                    deviceAddressList.add(device.getAddress());
                }
            }
        }
    };
    //Destroying the broadcast receivers
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
    //Switching on bluetooth
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
    //Retrieving the list of quiz name,score, rank from database
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
                            //initializing an arraylist of type DisplayResults(has the setters and getters for student name,score,rank)
                            finalResult = new ArrayList<DisplayResults>();
                            DisplayResults displayResults;
                            JSONArray resultTable = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < resultTable.length(); i++) {
                                displayResults = new DisplayResults();
                                quizID = resultTable.getJSONObject(i).getString("Quiz__c");
                                score = (((int) Double.parseDouble(resultTable.getJSONObject(i).getString("Score__c"))));
                                rank = resultTable.getJSONObject(i).getInt("Rank__c");
                                JSONObject quizTable = resultTable .getJSONObject(i).getJSONObject("Quiz__r");
                                quizName = quizTable.getString("Name");
                                displayResults.setQuizName(quizName);
                                displayResults.setMarks(score);
                                displayResults.setRank(rank);
                                finalResult.add(displayResults);
                            }
                            final ListView lv = (ListView) findViewById(R.id.attemptedQuizListView);
                            //calling StudentCustomBaseAdapter class constructor with the context and result to set multiple textviews in listview
                            lv.setAdapter(new StudentCustomBaseAdapter(getApplicationContext(), finalResult));
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
// Referenced Android developer to understand bluetooth enabling/discovering functionality
// https://developer.android.com/guide/topics/connectivity/bluetooth.html
