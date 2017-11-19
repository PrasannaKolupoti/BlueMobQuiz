package com.mc.grp6.bluemobquiz;

import android.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.SystemClock;
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
    public Button searchQuiz;
    BluetoothAdapter mBluetoothAdapter;
    @Override
    public void onResume(RestClient client) {
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);
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
        }
    }
    /**
     * Broadcast Receiver for listing devices that are not yet paired
     *
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Action Discovery: "+action);
            BluetoothDevice device = null;
            if(mBluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

            }
            else if(mBluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
                for (String address : deviceAddressList) {
                    Log.d("address", address);
                }
                Intent i = new Intent(StudentHome.this, StudentAvailableQuiz.class);
                i.putExtra("userID","a014100000MFQqY");
                i.putStringArrayListExtra("scannedDevicesList", deviceAddressList);
                startActivity(i);
            }
            else if (action.equals(BluetoothDevice.ACTION_FOUND)){
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
       /* if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }*/

    }
}
