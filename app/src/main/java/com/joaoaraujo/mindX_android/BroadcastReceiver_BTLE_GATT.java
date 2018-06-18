package com.joaoaraujo.mindX_android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadcastReceiver_BTLE_GATT extends BroadcastReceiver {

    private boolean mConnected = false;

    private com.joaoaraujo.mindX_android.ActivityKeepBluetooth activity = null;

    public BroadcastReceiver_BTLE_GATT(ActivityKeepBluetooth activity) {
        this.activity = activity;
    }

    public boolean isConnected() {
        return mConnected;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read or notification operations.

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (Service_BTLE_GATT.ACTION_GATT_CONNECTED.equals(action)) {
            mConnected = true;
        }
        else if (Service_BTLE_GATT.ACTION_GATT_DISCONNECTED.equals(action)) {
            mConnected = false;
            Utils.toast(activity.getApplicationContext(), "Disconnected From Headset");
            Intent intentRestart = new Intent(activity.getApplicationContext(), com.joaoaraujo.mindX_android.Activity_Start.class);
            intentRestart.putExtra("RESTART BLE",false); // isFirstActivity = false
            activity.startActivity(intentRestart);
            Activity_Games.runUpdates = false;
            activity.finish();
        }
        else if (Service_BTLE_GATT.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            activity.updateServices();
        }


        return;
    }
}
