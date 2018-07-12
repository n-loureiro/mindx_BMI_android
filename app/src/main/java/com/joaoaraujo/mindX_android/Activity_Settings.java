package com.joaoaraujo.mindX_android;

import android.Manifest;
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
import com.joaoaraujo.mindX_android.R;
import com.joaoaraujo.mindX_android.Activity_Games;
import com.joaoaraujo.mindX_android.Utils;

import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Activity_Settings extends AppCompatActivity implements View.OnClickListener {

    SeekBar attractor_bar;
    TextView attractor_string, connectFeedbackTextView;
    EditText[] threshold_edit = new EditText[6];

    private double[] thresholds = {3.9548, 2.2470, 1.0005, -0.9172, -2.1637, -3.8715};
    private double attractor_weight = .4;

    boolean isReadyToStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        isReadyToStart = false;

        findViewById(R.id.btn_scan).setOnClickListener(this);
        findViewById(R.id.btn_calcThresholds).setOnClickListener(this);

        attractor_bar = findViewById(R.id.attract_bar);
        attractor_string = findViewById(R.id.attract_text);
        connectFeedbackTextView = findViewById(R.id.connectFeedback);

        attractor_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){

                attractor_weight = ((double)progress)/20.0;
                if((attractor_weight*10.0) % 1 != 0)
                    attractor_string.setText("Attractor force: " + attractor_weight);
                else
                    attractor_string.setText("Attractor force: " + attractor_weight + "0");
            }
            public void onStartTrackingTouch(SeekBar arg0) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        threshold_edit[0] = findViewById(R.id.upRangeBox);
        threshold_edit[1] = findViewById(R.id.upGoalBox);
        threshold_edit[2] = findViewById(R.id.upBaseBox);
        threshold_edit[3] = findViewById(R.id.downBaseBox);
        threshold_edit[4] = findViewById(R.id.downGoalBox);
        threshold_edit[5] = findViewById(R.id.downRangeBox);

        /*for(int i = 0; i < 6; i++){
            String threshold_string = Double.toString(thresholds[i]);
            threshold_edit[i].setText(threshold_string);
            threshold_edit[i].setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }*/
        readThresholdsFromFile();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_scan:
                Utils.toast(getApplicationContext(), "Scanning for BLE headset...");
                 //ActivityKeepBluetooth.startScan();
                break;
            case R.id.btn_calcThresholds:
                Intent intent = new Intent(this, Activity_CalcThresholds.class);
                startActivity(intent);
                break;

            default:
                break;
        }

    }

    private void readThresholdsFromFile() {
        try {
            FileInputStream fileIn=openFileInput("config.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);
            BufferedReader bufferedReader = new BufferedReader(InputRead);

            for(int i = 0; i < 6; i++){
                String line = bufferedReader.readLine();
                threshold_edit[i].setText(line);
                threshold_edit[i].setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            }

            InputRead.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        for (int i = 0; i < 6; i++) {
            thresholds[i] = Double.parseDouble(threshold_edit[i].getText().toString());
            if (i > 0) {
                if (thresholds[i] > thresholds[i - 1]) {
                    Utils.toast(getApplicationContext(), "Invalid thresholds!!");
                    isReadyToStart = false;
                    for (int j = 0; j < 6; j++)
                        threshold_edit[j].setTextColor(Color.rgb(255, 0, 0));

                    break;
                }
            }
        }
        //Activity_Menu.address_headset = this.address;
        //Activity_Menu.name_headset = this.name;
        Activity_Menu.attractor_weight = this.attractor_weight;
        Activity_Menu.thresholds = this.thresholds;

        writeThresholdsToFile(this);
        //Utils.toast(getApplicationContext(), "Updating game variables remotely!");
        super.onBackPressed();
    }

    public static String[] thresholds_string;


    private void writeThresholdsToFile(Context context) {
        try {

            thresholds_string = new String[thresholds.length];
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            for(int i = 0; i < thresholds_string.length; i++)
            {
                thresholds_string[i] = String.valueOf(thresholds[i]);
                thresholds_string[i] = thresholds_string[i] + "\n";
                outputStreamWriter.write((thresholds_string[i]), 0, thresholds_string[i].length());
            }
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
