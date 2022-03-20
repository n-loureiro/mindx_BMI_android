package com.joaoaraujo.mindX_android;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;

import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.Button;

import com.jjoe64.graphview.series.DataPoint;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;


public class Activity_Impedance extends AppCompatActivity {

    @SuppressLint("HandlerLeak")
    public static Handler bluetoothHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void handleMessage(android.os.Message msg) {
            if(msg.obj != null) {
                if(msg.what ==  BluetoothService.RECIEVE_MESSAGE) {

                    byte[] readBuf = (byte[]) msg.obj;

                    double[] imp_channel = new double[5];
                    for(int chnIdx = 0; chnIdx < 5; chnIdx++){

                        byte[] byte_float = new byte[4];
                        byte_float[0] = readBuf[(chnIdx+1)*4+3];
                        byte_float[1] = readBuf[(chnIdx+1)*4+2];
                        byte_float[2] = readBuf[(chnIdx+1)*4+1];
                        byte_float[3] = readBuf[(chnIdx+1)*4];
                        imp_channel[chnIdx] = ByteBuffer.wrap(byte_float).getFloat();


                        updateImpedance(imp_channel[chnIdx], chnIdx);
                    }
                }
            }
        }
    };


    public static Handler getHandler() {
        return bluetoothHandler;
    }

    Intent bluetoothServiceIntent;
    boolean serviceIsBound = false;

    ServiceConnection BluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            msg("Service bound");
            serviceIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            msg("Service unbound");
            serviceIsBound = false;
        }
    };


    public static boolean doUpdates;
    private static Context myContext;
    private static TextView textChn, textChn1, textChn2, textChn3, textChn4, textChn5;
    private static Button buttonChn, buttonChn1, buttonChn2, buttonChn3, buttonChn4, buttonChn5;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impedance);

        myContext = this;


        textChn1 = findViewById(R.id.textChn1);
        textChn1.setText(Html.fromHtml("100k&#8486"));
        buttonChn1 = findViewById(R.id.chn1);
        buttonChn1.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_light));

        textChn2 = findViewById(R.id.textChn2);
        textChn2.setText(Html.fromHtml("110k&#8486"));
        buttonChn2 = findViewById(R.id.chn2);
        buttonChn2.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_light));

        textChn3 = findViewById(R.id.textChn3);
        textChn3.setText(Html.fromHtml("120&#8486"));
        buttonChn3 = findViewById(R.id.chn3);
        buttonChn3.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_light));

        textChn4 = findViewById(R.id.textChn4);
        textChn4.setText(Html.fromHtml("140k&#8486"));
        buttonChn4 = findViewById(R.id.chn4);
        buttonChn4.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_purple));

        textChn5 = findViewById(R.id.textChn5);
        textChn5.setText(Html.fromHtml("150k&#8486"));
        buttonChn5 = findViewById(R.id.chn5);
        buttonChn5.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light));


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
    public void onStart(){
        super.onStart();
        doUpdates = true;
        Utils.boardMode = 2;
        bluetoothServiceIntent = new Intent(this, BluetoothService.class);
        startService(bluetoothServiceIntent);
        bindService(bluetoothServiceIntent,BluetoothServiceConnection , Context.BIND_AUTO_CREATE);
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void updateImpedance(double data, int channelId) {

        if(channelId == 3){
                textChn = textChn1;
                buttonChn = buttonChn1;
            }
        else if(channelId == 4){
                textChn = textChn2;
                buttonChn = buttonChn2;
            }
        else if(channelId == 2){
                textChn = textChn3;
                buttonChn = buttonChn3;
            }
        else if(channelId == 1){
                textChn = textChn4;
                buttonChn = buttonChn4;
            }
        else{
                textChn = textChn5;
                buttonChn = buttonChn5;
            }

        textChn.setText(Html.fromHtml(String.format("%.0f", Math.ceil(data/25)*25) + "k&#8486"));

        if(data>8900 && data < 9800) {
            buttonChn.setBackgroundTintList( myContext.getResources().getColorStateList(android.R.color.holo_green_light));
        } else if(data >=8000 && data<8900 || data>9800 && data < 10000) {
            buttonChn.setBackgroundTintList( myContext.getResources().getColorStateList(android.R.color.holo_orange_light));
        } else {
            buttonChn.setBackgroundTintList( myContext.getResources().getColorStateList(android.R.color.holo_red_light));
        }
    }





}