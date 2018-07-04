package com.joaoaraujo.mindX_android;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PGraphics;
import processing.core.PImage;

public class Activity_Games extends AppCompatActivity {

    public static final String EXTRA_NAME = "com.joaoaraujo.mindX_android.Activity_Games.NAME";
    public static final String EXTRA_ADDRESS = "com.joaoaraujo.mindX_android.Activity_Games.ADDRESS";
    public static final String EXTRA_THRESHOLDS = "com.joaoaraujo.mindX_android.Activity_Games.THRESHOLDS";
    public static final String EXTRA_ATTRACTORWEIGHT = "com.joaoaraujo.mindX_android.Activity_Games.ATTRACTORWEIGHT";
    public static final String EXTRA_GAMEID = "com.joaoaraujo.mindX_android.Activity_Games.GAMEID";


    private final int NOTIFY_ACTIVATED = 1;
    private final int ENDING_GAME = 2;

    boolean positionBytes = false;
    public static SketchCursor sketchCursor;
    public static SketchSpace sketchSpace;
    public static SketchHeading sketchHeading;

    private double[] thresholds;
    private double attractor_weight;
    public static int gameId;

    public static boolean runUpdates;

    public static  RelativeLayout parentView;
    public static  View loadingView;
    public static  TextView loadingText;

    public static long time1;
    public static String ts;

    public static PImage shipSprite, spaceBack, portal, portal_success, portal_sprite;

    PFragment fragment;
    public static Handler bluetoothHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if(msg.obj != null)
                switch (msg.what) {
                    case BluetoothService.RECIEVE_MESSAGE:

                        long time1 = System.currentTimeMillis();

                        byte[] readBuf = (byte[]) msg.obj;

                        double decoderPos = 0;

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


                        //System.arraycopy(ordered_channels, 0, channel_1, 0, 125);
                        Log.e("Decoder Value",Double.toString(decoderPos));
                        loadingText.setVisibility(View.GONE);
                        loadingView.setVisibility(View.GONE);


                        switch(gameId){
                            case 0: { // Cursor
                                sketchCursor.BMI_update((float)decoderPos);
                                break;
                            }

                            case 1: { // Heading
                                sketchHeading.BMI_update((float)decoderPos);
                                break;
                            }

                            case 2: { // Space portals
                                sketchSpace.BMI_update((float)decoderPos);
                                break;
                            }

                            default: break;
                        }


                        break;
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

    public class Task extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void ... voids) {

            switch(gameId){
                case 0: { // Cursor
                    sketchCursor = new SketchCursor(thresholds,attractor_weight);
                    fragment = new PFragment(sketchCursor);
                    break;
                }

                case 1: { // Heading
                    sketchHeading = new SketchHeading(thresholds, attractor_weight);
                    fragment = new PFragment(sketchHeading);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    break;
                }

                case 2: { // Space portals
                    ImageView space_IV = findViewById(R.id.space_background);
                    Bitmap space_bitmap = ((BitmapDrawable)space_IV.getDrawable()).getBitmap();
                    spaceBack = new PImage(space_bitmap);
                    ImageView spaceship_IV = findViewById(R.id.spaceship);
                    Bitmap spaceship_bitmap = ((BitmapDrawable)spaceship_IV.getDrawable()).getBitmap();
                    shipSprite = new PImage(spaceship_bitmap);
                    ImageView portal_IV = findViewById(R.id.portal_yellow);
                    Bitmap portal_bitmap = ((BitmapDrawable)portal_IV.getDrawable()).getBitmap();
                    portal = new PImage(portal_bitmap);
                    ImageView portalSuccess_IV = findViewById(R.id.portal_green);
                    Bitmap portalSuccess_bitmap = ((BitmapDrawable)portalSuccess_IV.getDrawable()).getBitmap();
                    portal_success = new PImage(portalSuccess_bitmap);
                    sketchSpace = new SketchSpace(thresholds,attractor_weight,getApplicationContext());
                    fragment = new PFragment(sketchSpace);
                    break;
                }

                default: break;
            }

            return null;
        }

        protected void onPostExecute(Void voids) {
            fragment.setView(findViewById(R.id.sketch_container), activity);

           }
    }


    FragmentActivity activity;
    Bundle saved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saved = savedInstanceState;
        setContentView(R.layout.activity_games);

        activity = this;

        parentView = findViewById(R.id.parentView);
        loadingView = parentView.findViewById(R.id.loading_background);
        loadingText = parentView.findViewById(R.id.loading_text);

        Intent intent = getIntent();
        //name = intent.getStringExtra(Activity_Games.EXTRA_NAME);
        //address = intent.getStringExtra(Activity_Games.EXTRA_ADDRESS);

        thresholds = intent.getDoubleArrayExtra(Activity_Games.EXTRA_THRESHOLDS);
        attractor_weight = intent.getDoubleExtra(Activity_Games.EXTRA_ATTRACTORWEIGHT,0.0 );
        gameId = intent.getIntExtra(Activity_Games.EXTRA_GAMEID,1 );
        new Task().execute();

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

    public static byte data[] = new byte[501];
    public static byte incoming_byte;
    public static int  cursor = 0;
    public static boolean channelIdFound = false;
    public static boolean characteristicUpdated = false;
    private byte[] previous_packet;

    private Thread position_update = new Thread() {

        @Override
        public void run() {
            //while(runUpdates)
                try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if(characteristicUpdated) {
                                    characteristicUpdated = false;
                                    byte[] incoming_data = Activity_Start.crucialUpdate.getValue();
                                    int packets_lost = 20 - incoming_data.length;
                                    if (packets_lost != 0) {
                                        Activity_Games.channelIdFound = true;
                                        long time2 = System.currentTimeMillis();
                                        java.sql.Timestamp timestamp = new Timestamp(time2 - Activity_Games.time1);
                                        int nanos = (timestamp.getNanos() / 1000000);
                                        Activity_Games.ts = Integer.toString(nanos);
                                        Log.e("Timestamp", Activity_Games.ts);
                                        Activity_Games.time1 = time2;
                                        //Log.e("Bytes lost", Integer.toString(packets_lost));
                                    }
                                }


                                //if (!(Activity_Games.channelIdFound)) {
            /*for(byte incoming_byte : incoming_data)
                if (incoming_byte == 0x06) {
                    Activity_Games.channelIdFound = true;
                    long time2 = System.currentTimeMillis();
                    java.sql.Timestamp timestamp = new Timestamp(time2 - Activity_Games.time1);
                    int nanos = (timestamp.getNanos() / 1000000);
                    Activity_Games.ts = Integer.toString(nanos);
                    Log.e("Timestamp", Activity_Games.ts);
                    Activity_Games.time1 = time2;
                }*/
                                //}

            /*if (Activity_Games.channelIdFound) {

                int length_to_copy = 0;
                if (incoming_data.length + Activity_Games.cursor <= Activity_Games.data.length)
                    length_to_copy = incoming_data.length;
                else
                    length_to_copy = incoming_data.length - ((incoming_data.length + Activity_Games.cursor) - Activity_Games.data.length);

                System.arraycopy(incoming_data, 0, Activity_Games.data, Activity_Games.cursor, length_to_copy);
                Activity_Games.cursor += length_to_copy;
            }

            if (Activity_Games.cursor == 501) {

                Activity_Games.channelIdFound = false;
                Activity_Games.cursor = 0;

                //float[] channel_array = new float[125];
                //int cursor_float = 0;

                //byte[] datacopy = new byte[4];

                                    /*for (int i = 0; i < 500; i++) {

                                        data[i + 1] = datacopy[3 - (i % 4)];

                                        if (i > 0 && (i % 4) == 0) {
                                            channel_array[cursor_float] = ByteBuffer.wrap(data).getFloat();
                                            cursor_float += 1;
                                        }


                                    }*/

                                    /*StringBuffer stringBuffer = new StringBuffer();
                                    for (float b : channel_array) {
                                        stringBuffer.append(String.format("%.2f, ", b));
                                    }
                                    String logString = stringBuffer.toString();*/

                                //Log.e("data", logString);


                                    /*StringBuffer hexString = new StringBuffer();
                                    for (byte b : data) {
                                        hexString.append(String.format("0x%02X ", b));
                                    }
                                    String positionHexString = hexString.toString();*/

                                //float position_float = ByteBuffer.wrap(data).getFloat();
                                //Log.e("Position", Float.toString(position_float));
                                //Log.e("Bytes received: ", positionHexString);
                                    /*switch(gameId){
                                        case 0: { // Cursor
                                            sketchCursor.BMI_update(position_float);
                                            break;
                                        }

                                        case 1: { // Heading
                                            sketchHeading.BMI_update(position_float);
                                            break;
                                        }

                                        case 2: { // Space portals
                                            sketchSpace.BMI_update(position_float);
                                            break;
                                        }

                                        default: break;
                                    }*/
                                //}

                                    if (loadingText.getVisibility() != View.GONE) {
                                        loadingText.setVisibility(View.GONE);
                                        loadingView.setVisibility(View.GONE);
                                    }

                            }
                        });
                } catch (Exception e) {

                }
        }
    };

}
