package com.mc.grp6.bluemobquiz;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
    //Variable declaration and initialization
    private RestClient client;
    public String userID;
    private GestureDetectorCompat gestureObject;
    private ArrayAdapter<String> createdQuizAdapter;
    public ArrayList<String> quizIDList = new ArrayList<String>();
    public ArrayList<String> quizNameList = new ArrayList<String>();
    public ArrayList<String> quizStatusList = new ArrayList<String>();
    BluetoothAdapter mBluetoothAdapter;
    @Override
    public void onResume() {
        // Create list adapter
        createdQuizAdapter = new ArrayAdapter<String>(this, R.layout.professor_listviewquizzes,R.id.quiz, new ArrayList<String>());
        ((ListView) findViewById(R.id.quizListView)).setAdapter(createdQuizAdapter);
        super.onResume();
    }
    //Method that is called after the activity resumes once we have a RestClient.
    @Override
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;
        try {
            //calling displayCreatedQuizzes() with query to get the quizzes as parameter
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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Button createQuizButton = (Button) findViewById(R.id.createQuizButton);
        ListView quizList = (ListView) findViewById(R.id.quizListView);
        //Listening to item click and performing action
        quizList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String quizID = quizIDList.get(position);
                String quizName = quizNameList.get(position);
                //Redirect to ProfessorUpdatingQuizName page to update that particular quiz
                Intent intent = new Intent(ProfessorHome.this, ProfessorUpdatingQuizName.class);
                intent.putExtra("userID",userID);
                intent.putExtra("quizID",quizID);
                intent.putExtra("quizName",quizName);
                startActivity(intent);
            }
        });
        //Listening to item long click and performing action
        quizList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String quizID = quizIDList.get(position);
                String isActive  = quizStatusList.get(position) ;
                //checking if bluetooth is enabled or not
                if(!mBluetoothAdapter.isEnabled()) {
                    //calling enableDisableBT() to enable bluetooth
                    enableDisableBT();
                    checkBTPermissions();
                    //calling makeBtDiscoverable() to make the device discoverable
                    makeBtDiscoverable(view);
                }
                //Checking if quiz is not shared
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
                                    //Calling sharingQuiz with quizId and active status
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
                //Checking if quiz is already shared
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
                                    //Calling sharingQuiz with quizId and active status
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
        //Create quiz button click listener
        createQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Redirect to ProfessorAddingQuizName page
                Intent intent = new Intent(ProfessorHome.this, ProfessorAddingQuizName.class);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
        });
    }
    //Destroying the broadcast receivers
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
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
    // Making the device discoverable for 600 seconds
    public void makeBtDiscoverable(View view) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);

    }
    //Broadcast Receiver for changes made to bluetooth states such as: Discoverability mode on/off or expire.
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        break;
                }

            }
        }
    };
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
                        //Redirecting to ProfessorAttendance page
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
    //Setting the status of quiz based on whether it is shared or unshared
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
    //Retrieving the list of created quizzes from database
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
                                //Adding the list of created quizzes to array adapter
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
// Referenced Android developer to understand bluetooth enabling/discovering functionality
// https://developer.android.com/guide/topics/connectivity/bluetooth.html
