package com.joaoaraujo.mindX_android;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static android.os.SystemClock.sleep;


public class Activity_Start extends AppCompatActivity implements View.OnClickListener{


    private double[] thresholds = {3.9548, 2.2470, 1.0005, -0.9172, -2.1637, -3.8715};
    public static String[] thresholds_string;

    private double[] decoderHist = {};

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
    };

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    final int RECIEVE_MESSAGE = 1;
    final int RETRY_CONNECTION = 2;

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    private boolean isBtConnected = false;
    //private ProgressDialog progress, alarmProgress;
    //private ConnectedThread connectedThread;
    BluetoothSocket btSocket = null;
    String address = null;
    public static BluetoothGattCharacteristic crucialUpdate = null;
    boolean isStarting = true;
    //private ConnectBT connectTask;
    private boolean isFirst = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //connectTask = new ConnectBT();

        setContentView(R.layout.activity_start);
        findViewById(R.id.get_started_button).setOnClickListener(this);
        readThresholdsFromFile();
        Activity_Raw.doUpdates = false;

        checkPermissions(); // check manifest permissions

        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null) {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        } else if (!myBluetooth.isEnabled()) {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 10);
        }

        else    {

        }


        //else pairedDevicesList();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10 && resultCode == RESULT_OK) {
            //String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_MODE);
            //if (provider == null || provider.isEmpty()) {
            //pairedDevicesList();
            //startService(new Intent(this, BluetoothService.class));

        }

        else {
            finish();
        }
    }

    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
// check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
// request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions,
                    REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS,
                    REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
// exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
        }
    }

    /*private void pairedDevicesList() {

        msg("Connecting to device...");

        isFirst = false;

        pairedDevices = myBluetooth.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                String name = bt.getName();
                //check if the name and ID correspond to the arduino board
                //if (bt.getAddress().equals("7C:F9:0E:F6:70:D4")) {
                    if (bt.getName().equals("MindreachBT") && bt.getAddress().equals("00:07:80:3C:55:04")) {
                    //msg("FOUND MINDREACH HEADSET");
                    address = bt.getAddress();
                    connectTask.execute();
                    break;
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired BT devices found", Toast.LENGTH_LONG).show();
        }

    }*/

    public void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    /*private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            //progress = ProgressDialog.show(MainActivity.this, "Connecting to Arduino", "Please wait...");  //show a progress dialog
            //
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    BluetoothDevice arduinoShield = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = arduinoShield.createRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
                msg("Device connection failed");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                //progress.dismiss();
                //msg("Connection Failed. Trying again.");
                try {btSocket.close();} catch (Exception e) {
                    msg("Error closing btSocket. Exception "+e);
                }
                btSocket = null;
                this.restart();

            } else {
                msg("Connected.");
                isBtConnected = true;
                connectedThread = new ConnectedThread(btSocket);
                connectedThread.start();
            }
        }

        private void restart(){
            connectTask.cancel(true);
            connectTask = null;
            connectTask = new ConnectBT();
            connectTask.execute();
        }

    }

    private class ConnectedThread extends Thread {
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        byte[] start_byte = {0x77,0x62};
        int reading_cursor = 0;
        boolean foundFirst = false;
        byte[] buffer = new byte[2520];

        private ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                msg("Socket exception in input and output stream: "+e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

        public void run() {

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // buffer store for the stream
                    int bytes; // bytes returned from read()
                    // Read from the InputStream
                    if(mmInStream.available() > 0) {
                        isStarting = false;
                        byte[] temp_buffer = new byte[2520];

                        if(!foundFirst){
                            bytes = mmInStream.read(temp_buffer);

                            if(bytes >= 4
                                    && (temp_buffer[0] & 0xFF) == 0xFF
                                    && (temp_buffer[1] & 0xFF) == 0xFF
                                    && (temp_buffer[2] & 0xFF) == 0x00){

                                for (int i = 0; i < bytes; i++) {
                                    buffer[i + reading_cursor] = (byte) (temp_buffer[i] & 0xFF);
                                }

                                reading_cursor += bytes;
                                foundFirst = true;
                            }
                        }
                        else {

                            if (mmInStream.available() + reading_cursor > 2520)
                                bytes = mmInStream.read(temp_buffer, 0, mmInStream.available()
                                        - ((mmInStream.available() + reading_cursor) - 2520));

                            else bytes = mmInStream.read(temp_buffer, 0, mmInStream.available());

                            for (int i = 0; i < bytes; i++) {
                                buffer[i + reading_cursor] = (byte) (temp_buffer[i] & 0xFF);
                            }

                            reading_cursor += bytes;


                            if (reading_cursor == 2520) {
                                reading_cursor = 0;
                                bluetoothHandler.obtainMessage(RECIEVE_MESSAGE, buffer).sendToTarget(); // Send to message queue Handler
                                buffer = new byte[2520];
                                foundFirst = false;
                            }
                        }
                    }

                    else if(isStarting) {
                        mmOutStream.write(start_byte);
                        //isStarting = false;
                    }

                    else{
                        int var = 0;
                    }

                } catch (IOException e) {
                    msg("Stream input/output exception: "+e);
                    break;
                }

            }
        }

        public void resetConnection() {
            if (mmInStream != null) {
                try {mmInStream.close();} catch (Exception e) {}
                mmInStream = null;
            }

            if (mmOutStream != null) {
                try {mmOutStream.close();} catch (Exception e) {}
                mmOutStream = null;
            }

            if (btSocket != null) {
                try {btSocket.close();} catch (Exception e) {}
                btSocket = null;
            }

        }
    }*/


    private void readThresholdsFromFile() {
        try {
            FileInputStream fileIn=openFileInput("config.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);
            BufferedReader bufferedReader = new BufferedReader(InputRead);

            for(int i = 0; i < 6; i++){
                String line = bufferedReader.readLine();
                thresholds[i] = Double.parseDouble(line);
            }

            InputRead.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileInputStream fileIn=openFileInput("calcThrehsolds_decoder.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);
            BufferedReader bufferedReader = new BufferedReader(InputRead);

            String line = "";

            int i=0;
            while ((line = bufferedReader.readLine()) != null) {
                decoderHist[i] = Double.parseDouble(line);
                    i++;
            }

            Log.e("Decoder HIST: ",decoderHist.toString());


            InputRead.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        //if(connectedThread != null)
          //  connectedThread.resetConnection();

    }
}
