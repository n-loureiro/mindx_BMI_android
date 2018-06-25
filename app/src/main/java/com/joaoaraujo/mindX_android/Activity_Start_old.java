package com.joaoaraujo.mindX_android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.OutputStreamWriter;


public class Activity_Start_old extends ActivityKeepBluetooth implements View.OnClickListener{


    private double[] thresholds = {3.9548, 2.2470, 1.0005, -0.9172, -2.1637, -3.8715};
    public static String[] thresholds_string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);
        findViewById(R.id.get_started_button).setOnClickListener(this);
        writeThresholdsToFile(this);

    }

    private void writeThresholdsToFile(Context context) {
        try {

            thresholds_string = new String[thresholds.length];
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            for(int i = 0; i < thresholds_string.length; i++)
            {
                thresholds_string[i] = String.valueOf(thresholds[i]);
                outputStreamWriter.write((thresholds_string[i]), 0, thresholds_string[i].length());
            }
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    @Override
    public void onClick(View v) {
            if(v.getId() == R.id.get_started_button){
                Intent intent = new Intent(this, Activity_Menu.class);
                startActivity(intent);
            }
    }


    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}
