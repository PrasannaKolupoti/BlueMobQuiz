package com.mc.grp6.bluemobquiz;

import android.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import java.util.ArrayList;

public class StudentHome extends SalesforceActivity {

    private static final String TAG = "StudentHome";
    public ArrayList<String> deviceAddressList = new ArrayList<>();
    public Boolean deviceListAdded = false;
    BluetoothAdapter mBluetoothAdapter;
    @Override
    public void onResume(RestClient client) {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);
        Button searchQuiz = (Button) findViewById(R.id.searchQuizButton);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("My Device ID",mBluetoothAdapter.getAddress());
        enableDisableBT();
        searchQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                if(!deviceListAdded){
                    deviceDiscover(v);

                }
                if(deviceListAdded){
                    for(String address: deviceAddressList){
                        Log.d("address",address);
                    }

                    Intent i = new Intent(StudentHome.this, StudentAvailableQuiz.class);
                    i.putStringArrayListExtra("deviceList", deviceAddressList);
                    //startActivity(i);
                }
            }
        });
    }
    public void deviceDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDiscover: Canceling discovery.");

                    //check BT permissions in manifest
                    checkBTPermissions();
                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);


                }
                if(!mBluetoothAdapter.isDiscovering()){

                    //check BT permissions in manifest
                    checkBTPermissions();
                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);

                }
            }
        });
    }
    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
    /**
     * Broadcast Receiver for listing devices that are not yet paired
     *
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                if(!deviceAddressList.contains(device.getAddress())) {
                    deviceAddressList.add(device.getAddress());
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                }

            }
            deviceListAdded = true;
        }
    };
    public void onDestroy(){
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        //unregisterReceiver(mBroadcastReceiver2);
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
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
       /* if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }*/

    }


}
