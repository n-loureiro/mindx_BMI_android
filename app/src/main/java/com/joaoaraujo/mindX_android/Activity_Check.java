package com.joaoaraujo.mindX_android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.nio.ByteBuffer;
import java.util.Iterator;


public class Activity_Check extends AppCompatActivity {

    public static Handler bluetoothHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if(msg.obj != null)
            switch (msg.what) {
                case BluetoothService.RECIEVE_MESSAGE:

                    long time1 = System.currentTimeMillis();

                    byte[] readBuf = (byte[]) msg.obj;

                    double[] channel_array = new double[625];
                    int [] channel_id = new int[5];
                    int cursor_float = 0;
                    int cursor_channel_id = 0;

                    byte[] byte_float = new byte[4];

                    for (int i = 0; i < 2520; i++) {

                        if (i == 0 || i == 504 || i == 1008 || i == 1512 || i == 2016) {
                            //Log.e("i",Byte.toString(readBuf[i+3]));
                            channel_id[cursor_channel_id] = readBuf[i+3];
                            cursor_channel_id +=1;
                            i += 3;
                        } else {

                            byte_float[3 - (i % 4)] = readBuf[i];

                            if (i > 0 && (i % 4) == 3) {
                                channel_array[cursor_float] = ByteBuffer.wrap(byte_float).getFloat();
                                cursor_float += 1;
                                byte_float = new byte[4];
                            }

                        }
                    }

                    double[] ordered_channels = new double[625];
                    int channel_cursor = 1;
                    for(int i = 0; i < channel_id.length; i++){
                        if(channel_id[i] == channel_cursor){
                            System.arraycopy(channel_array, i*125, ordered_channels,
                                    (channel_cursor-1)*125, 125);
                            channel_cursor+=1;
                            i = -1;

                            if(channel_cursor > 5)
                                break;
                        }
                    }
                    double [] channel_1 = new double[125];
                    System.arraycopy(ordered_channels, 0, channel_1, 0, 125);
                    //Log.e("Mean Channel 1",Double.toString(mean(channel_1)));

                    if(doUpdates) {

                        //updateData(125, ordered_channels);
                    }

                    break;
            }
        }
    };

    public static Handler getHandler() {
        return bluetoothHandler;
    }

    Intent bluetoothServiceIntent;

    ServiceConnection BluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            msg("Service Started");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            msg("Service Ended");
        }
    };

    public static boolean doUpdates;
    boolean serviceIsBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw);

        doUpdates = true;

        Utils.boardMode = 0;
        bluetoothServiceIntent = new Intent(this, BluetoothService.class);
        startService(bluetoothServiceIntent);
        bindService(bluetoothServiceIntent,BluetoothServiceConnection , Context.BIND_AUTO_CREATE);

    }

    public void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!serviceIsBound) {
            bindService(bluetoothServiceIntent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
            serviceIsBound = true;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(serviceIsBound) {
            unbindService(BluetoothServiceConnection);
            serviceIsBound = false;
        }
    }


    @Override
    protected void onStop() {
        if(serviceIsBound) {
            unbindService(BluetoothServiceConnection);
            serviceIsBound = false;
        }
        stopService(bluetoothServiceIntent);

        super.onStop();
    }


    @Override
    public void onBackPressed() {
        if(serviceIsBound) {
            unbindService(BluetoothServiceConnection);
            serviceIsBound = false;
        }
        stopService(bluetoothServiceIntent);

        finish();
    }


}
