package com.joaoaraujo.mindX_android;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.sql.Timestamp;

import processing.android.PFragment;
import processing.core.PImage;

public class Activity_CalcThresholds extends AppCompatActivity {

    public static  RelativeLayout parentView;

    private static double decoderPos =0 ;

    PFragment fragment;
    @SuppressLint("HandlerLeak")
    public  Handler bluetoothHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.obj != null)
                switch (msg.what) {
                    case BluetoothService.RECIEVE_MESSAGE:

                        long time1 = System.currentTimeMillis();

                        byte[] readBuf = (byte[]) msg.obj;
                        byte[] byte_float = new byte[4];

                        for (int i = 0; i < 8; i++) {

                            if (i == 0) {
                                //Log.e("Mode Byte",Byte.toString(readBuf[i+3]));
                                i += 3;
                            } else {

                                byte_float[3 - (i % 4)] = readBuf[i];

                                if (i > 0 && (i % 4) == 3) {
                                    decoderPos = ByteBuffer.wrap(byte_float).getFloat();
                                }

                            }
                        }

                        appendDecoderToFile( decoderPos);

                        //System.arraycopy(ordered_channels, 0, channel_1, 0, 125);
                        Log.e("Decoder Value",Double.toString(decoderPos));
                        break;
                }
        }
    };

//    public static Handler getHandler() {
//        return bluetoothHandler;
//    }

    Intent bluetoothServiceIntent;
    boolean serviceIsBound = false;

    ServiceConnection BluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            msg("Loading...");
            serviceIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            msg("Service unbound");
            serviceIsBound = false;
        }
    };

    public void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    FragmentActivity activity;
    Bundle saved;
    Context myContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saved = savedInstanceState;
        setContentView(R.layout.activity_calc_thresholds);

        myContext = this;

    }

    @Override
    protected void onStart(){
        super.onStart();
        Utils.boardMode = 1;
        bluetoothServiceIntent = new Intent(this, BluetoothService.class);
        startService(bluetoothServiceIntent);
        bindService(bluetoothServiceIntent,BluetoothServiceConnection , Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!serviceIsBound) {
            bindService(bluetoothServiceIntent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
            serviceIsBound = true;
        }
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

    @Override
    public void onStop(){
        if(serviceIsBound) {
            unbindService(BluetoothServiceConnection);
            serviceIsBound = false;
        }
        stopService(bluetoothServiceIntent);

        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(serviceIsBound) {
            unbindService(BluetoothServiceConnection);
            serviceIsBound = false;
        }
    }


    private void appendDecoderToFile(double decoderPos) {
        try {

            String decoderString =  String.valueOf(decoderPos) + "\n";
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(myContext.openFileOutput("calcThrehsolds_decoder.txt", Context.MODE_APPEND));
            outputStreamWriter.write((decoderString), 0, decoderString.length());

            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
