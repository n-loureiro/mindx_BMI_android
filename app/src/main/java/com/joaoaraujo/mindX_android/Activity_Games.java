package com.joaoaraujo.mindX_android;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import processing.android.CompatUtils;
import processing.android.PFragment;
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
    public SketchCursor sketchCursor;
    public SketchSpace sketchSpace;
    public SketchHeading sketchHeading;

    private double[] thresholds;
    private double attractor_weight;
    private int gameId;

    public static boolean runUpdates;

    RelativeLayout parentView; View loadingView; TextView loadingText;

    public static long time1;
    public static String ts;

    public static PImage shipSprite, spaceBack, portal, portal_success, portal_sprite;

    PFragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_games);

        parentView = findViewById(R.id.parentView);
        loadingView = parentView.findViewById(R.id.loading_background);
        loadingText = parentView.findViewById(R.id.loading_text);

        Intent intent = getIntent();
        //name = intent.getStringExtra(Activity_Games.EXTRA_NAME);
        //address = intent.getStringExtra(Activity_Games.EXTRA_ADDRESS);

        thresholds = intent.getDoubleArrayExtra(Activity_Games.EXTRA_THRESHOLDS);
        attractor_weight = intent.getDoubleExtra(Activity_Games.EXTRA_ATTRACTORWEIGHT,0.0 );
        gameId = intent.getIntExtra(Activity_Games.EXTRA_GAMEID,0 );

        if(gameId == 2){
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
        }

        time1 = System.currentTimeMillis();

        switch(gameId){
            case 0: { // Cursor
                sketchCursor = new SketchCursor(thresholds,attractor_weight);
                fragment = new PFragment(sketchCursor);
                fragment.setView(findViewById(R.id.sketch_container), this);
                break;
            }

            case 1: { // Heading
                sketchHeading = new SketchHeading(thresholds, attractor_weight);
                fragment = new PFragment(sketchHeading);
                fragment.setView(findViewById(R.id.sketch_container), this);
                break;
            }

            case 2: { // Space portals
                sketchSpace = new SketchSpace(thresholds,attractor_weight,getApplicationContext());
                fragment = new PFragment(sketchSpace);
                fragment.setView(findViewById(R.id.sketch_container), this);
                break;
            }

            default: break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        runUpdates = true;
        position_update.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        runUpdates = false;
        if(Activity_Start.crucialUpdate != null)
            Activity_Start.crucialUpdate.setValue(new byte[0]);
        if(position_update != null && position_update.isAlive()) position_update.interrupt();
        position_update = null;
        finish();
    }

    @Override
    public void onStop(){
        runUpdates = false;
        if(position_update != null && position_update.isAlive()) position_update.interrupt();
        position_update = null;
        super.onStop();
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
