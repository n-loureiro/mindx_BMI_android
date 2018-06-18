package com.joaoaraujo.mindX_android;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityKeepBluetooth extends AppCompatActivity {
    private final static String TAG = Activity_Games.class.getSimpleName();
    public static final int REQUEST_ENABLE_BT = 1;
    public final int REQUEST_BLUETOOTH_PERMISSIONS = 2;
    public final int REQUEST_COARSE_LOCATION_PERMISSIONS = 3;
    public final int REQUEST_BLUETOOTH_ADMIN_PERMISSIONS = 2;

    private ArrayList<BluetoothGattService> services_ArrayList;
    private HashMap<String, BluetoothGattCharacteristic> characteristics_HashMap;
    private HashMap<String, ArrayList<BluetoothGattCharacteristic>> characteristics_HashMapList;

    private Intent mBTLE_Service_Intent = null;
    public static com.joaoaraujo.mindX_android.Service_BTLE_GATT mBTLE_Service = null;
    public boolean mBTLE_Service_Bound;
    public static com.joaoaraujo.mindX_android.BroadcastReceiver_BTLE_GATT mGattUpdateReceiver = null;

    private String name;
    private String address;

    public static BluetoothGattCharacteristic crucialUpdate = null;

    public static boolean isNotifying = false;

    public  com.joaoaraujo.mindX_android.BroadcastReceiver_BTState mBTStateUpdateReceiver;
    public  com.joaoaraujo.mindX_android.Scanner_BTLE mBTLeScanner;

    private void getPermissionsCompat() {
        int hasPermissionLocation = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int hasPermissionBluetooth = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH);
        int hasPermissionBtAdmin = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN);

        if (hasPermissionBluetooth == PackageManager.PERMISSION_GRANTED) {
            //Permission granted
        } else {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH},
                    REQUEST_BLUETOOTH_PERMISSIONS);
        }

        if (hasPermissionBtAdmin == PackageManager.PERMISSION_GRANTED) {
            //Permission granted
        } else {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_ADMIN},
                    REQUEST_BLUETOOTH_ADMIN_PERMISSIONS);
        }

        if (hasPermissionLocation == PackageManager.PERMISSION_GRANTED) {
            //Permission granted
        }

        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION_PERMISSIONS);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,
                    "Permissions granted!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,
                    "Permissions Denied! App may not work properly!",
                    Toast.LENGTH_LONG).show();
        }
    }


    public void startScan(){
        //connectFeedbackTextView.setText("No headset found");
        //connectFeedbackTextView.setTextColor(Color.rgb(255,0,0));

        if(mBTLeScanner.isScanning()){
            mBTLeScanner.stop();
        }
        mBTLeScanner.start();
    }

    public void addDevice(BluetoothDevice device, int rssi) {

        if(device.getAddress().equals("DC:C4:F5:F0:79:F4") && device.getName().equals("LAIRD BL652")) {

            Toast.makeText(this,
                    "Found device: " + device.getName(),
                    Toast.LENGTH_LONG).show();

            Log.e("FOUND IT!","FOUND DEVICE!");

            mBTLeScanner.stop();


            // Start update receiver
            mGattUpdateReceiver = new com.joaoaraujo.mindX_android.BroadcastReceiver_BTLE_GATT(this);
            registerReceiver(mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());

            mBTLE_Service_Intent = new Intent(this, com.joaoaraujo.mindX_android.Service_BTLE_GATT.class);
            bindService(mBTLE_Service_Intent, mBTLE_ServiceConnection, Context.BIND_AUTO_CREATE);
            startService(mBTLE_Service_Intent);

            address = device.getAddress();
            name = device.getName();

        }
    }

    public ServiceConnection mBTLE_ServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            com.joaoaraujo.mindX_android.Service_BTLE_GATT.BTLeServiceBinder binder = (com.joaoaraujo.mindX_android.Service_BTLE_GATT.BTLeServiceBinder) service;
            mBTLE_Service = binder.getService();
            mBTLE_Service_Bound = true;

            if (!mBTLE_Service.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            mBTLE_Service.connect(address);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBTLE_Service = null;
            mBTLE_Service_Bound = false;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toast(getApplicationContext(), "BLE not supported");
            finish();
        }

        super.onCreate(savedInstanceState);

        mBTStateUpdateReceiver = new com.joaoaraujo.mindX_android.BroadcastReceiver_BTState(getApplicationContext());
        mBTLeScanner = new com.joaoaraujo.mindX_android.Scanner_BTLE(this, -75);
        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        getPermissionsCompat();

        //name = "LAIRD BL652";
        //address = "DC:C4:F5:F0:79:F4";
        services_ArrayList = new ArrayList<>();
        characteristics_HashMap = new HashMap<>();
        characteristics_HashMapList = new HashMap<>();
        if(mBTLE_Service!= null) {mBTLE_Service.disconnect(); mBTLE_Service.close();}
        mBTLE_Service = null;
        mGattUpdateReceiver = null;

    }

    @Override
    protected void onStart() {
        super.onStart();

        startScan();
    }


    @Override
    public void onBackPressed() {
        if(mBTStateUpdateReceiver != null) unregisterReceiver(mBTStateUpdateReceiver);
        mBTLeScanner.stop();
        mBTLeScanner = null;
        if(mGattUpdateReceiver != null) unregisterReceiver(mGattUpdateReceiver);
        if(mBTLE_ServiceConnection != null) unbindService(mBTLE_ServiceConnection);
        mBTLE_ServiceConnection = null;
        mBTLE_Service_Intent = null;
        mGattUpdateReceiver = null;
        if(mBTLE_Service != null){mBTLE_Service.disconnect(); mBTLE_Service.close();}
        mBTLE_Service = null;
        mBTLE_Service_Bound = false;
        mBTLE_Service_Intent = null;
        super.onBackPressed();
    }

    public void updateServices() {

        if (mBTLE_Service != null) {

            services_ArrayList.clear();
            characteristics_HashMap.clear();
            characteristics_HashMapList.clear();

            List<BluetoothGattService> servicesList = mBTLE_Service.getSupportedGattServices();

            for (BluetoothGattService service : servicesList) {

                //Log.e("SERVICE UUID STRINGS",service.getUuid().toString());

                if (service.getUuid().toString().equals("569a1101-b87f-490c-92cb-11ba5ea5167c")) {

                    services_ArrayList.add(service);
                    Log.e("PARENT SERVICE FOUND",service.getUuid().toString());

                    List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();

                    ArrayList<BluetoothGattCharacteristic> newCharacteristicsList = new ArrayList<>();

                    for (BluetoothGattCharacteristic characteristic : characteristicsList) {
                        if(characteristic.getUuid().toString().equals("569a2000-b87f-490c-92cb-11ba5ea5167c")) {
                            newCharacteristicsList.add(characteristic);
                            crucialUpdate = characteristic;
                            isNotifying = true;

                            Log.e("NOTIFY CHARACTERISTIC", characteristic.getUuid().toString());

                            // ENABLE NOTIFICATION
                            if (mBTLE_Service != null) {
                                mBTLE_Service.setCharacteristicNotification(crucialUpdate, true);
                            }

                            break;

                        }
                    }

                    characteristics_HashMapList.put(service.getUuid().toString(), newCharacteristicsList);
                }
            }
        }
    }

}
