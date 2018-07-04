package com.joaoaraujo.mindX_android;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import static com.joaoaraujo.mindX_android.Constants.MESSAGE_STATE_CHANGE;
import static com.joaoaraujo.mindX_android.Constants.MESSAGE_TOAST;
import static com.joaoaraujo.mindX_android.Constants.TOAST;

public class BluetoothService extends Service {
    private BluetoothAdapter mBluetoothAdapter;
    public static final String BT_DEVICE = "MindreachBT";
    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static final int STATE_NONE = 0; // we're doing nothing
    public static final int STATE_LISTEN = 1; // now listening for incoming
    public static final int RECIEVE_MESSAGE = 1;
    // connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing
    // connection
    public static final int STATE_CONNECTED = 3; // now connected to a remote
    // device
    private ConnectThread mConnectThread;
    private static ConnectedThread mConnectedThread;
    // public mInHangler mHandler = new mInHangler(this);
    private static Handler mHandler = null;
    public static int mState = STATE_NONE;
    public static String deviceName;
    public Vector<Byte> packdata = new Vector<Byte>(2048);
    public static BluetoothDevice device = null;

    private Set<BluetoothDevice> pairedDevices;
    private boolean isBtConnected = false;
    String address = null;
    boolean isStarting, handshake;
    public static long time1;

    @Override
    public void onCreate() {
        isStarting = true;
        handshake = false;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        time1 = System.currentTimeMillis();


        Log.d("BluetoothService", "Service started");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        switch (Utils.boardMode){
            case 0:
                mHandler = Activity_Raw.getHandler();
                break;
            case 1:
                mHandler = Activity_Games.getHandler();
                break;

        }
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }


    private final IBinder mBinder = new LocalBinder();

    public void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                String name = bt.getName();
                //check if the name and ID correspond to the arduino board
                //if (bt.getAddress().equals("7C:F9:0E:F6:70:D4")) {
                if (bt.getName().equals("MindreachBT") && bt.getAddress().equals("00:07:80:3C:55:04")) {
                    //msg("FOUND MINDREACH HEADSET");
                    msg("Connecting to device...");
                    address = bt.getAddress();
                    connectToDevice(address);
                    break;
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired BT devices found", Toast.LENGTH_LONG).show();
        }
        return START_STICKY;
    }

    private synchronized void connectToDevice(String macAddress) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    private void setState(int state) {
        mState = state;
        if (mHandler != null) {
            mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        }
    }

    public synchronized void stop() {
        setState(STATE_NONE);
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        stopSelf();
    }

    @Override
    public boolean stopService(Intent name) {
        setState(STATE_NONE);
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mBluetoothAdapter.cancelDiscovery();
        return super.stopService(name);
    }

    private void connectionFailed() {
        isBtConnected = false;
        BluetoothService.this.stop();
        Toast.makeText(getApplicationContext(), "Connection Failed!", Toast.LENGTH_LONG).show();

    }

    private void connectionLost() {
        BluetoothService.this.stop();
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, "CONNECTION LOST");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private synchronized void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        isBtConnected = true;
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

        // Message msg =
        // mHandler.obtainMessage(AbstractActivity.MESSAGE_DEVICE_NAME);
        // Bundle bundle = new Bundle();
        // bundle.putString(AbstractActivity.DEVICE_NAME, "p25");
        // msg.setData(bundle);
        // mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            setName("ConnectThread");
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                connectionFailed();
                return;

            }
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("BluetoothService", "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        byte[] start_byte = new byte[2];
        private int packet_size = 0;
        byte[] buffer;
        byte handshakeByte;
        long time1 = System.currentTimeMillis();
        boolean isfirstTry = true;


        int reading_cursor = 0;
        boolean foundFirst = false;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("Printer Service", "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {

            switch (Utils.boardMode)
            {
                case 0: // Raw signal mode
                    start_byte[0] = 0x77;
                    start_byte[1] = 0x62;
                    packet_size = 2520;
                    buffer = new byte[packet_size];
                    handshakeByte = 0x05;
                    break;

                case 1: // Game (decoder) mode
                    start_byte[0] = 0x77;
                    start_byte[1] = 0x61;
                    packet_size = 8;
                    buffer = new byte[packet_size];
                    handshakeByte = 0x07;
                    break;

            }

            // Keep listening to the InputStream until an exception occurs
            while (isBtConnected) {
                try {
                    // buffer store for the stream
                    int bytes; // bytes returned from read()
                    // Read from the InputStream
                    if (mmInStream.available() > 0) {
                        byte[] temp_buffer = new byte[packet_size];

                        if (!foundFirst) {
                            bytes = mmInStream.read(temp_buffer);

                            if(!handshake){
                                for(int i = 0; i < bytes; i++)
                                if((temp_buffer[0] & 0xFF) == handshakeByte){
                                    //int mybyte = (temp_buffer[0] & 0xFF);
                                    handshake = true;
                                    break;

                                } else {
                                        if(i == 0) {
                                            if(isfirstTry) {
                                                mmOutStream.write(start_byte);
                                                time1 = System.currentTimeMillis();
                                                isfirstTry = false;
                                            }
                                        }
                                    }
                            }else {
                                if (bytes >= 4
                                        && (temp_buffer[0] & 0xFF) == 0xFF
                                        && (temp_buffer[1] & 0xFF) == 0xFF
                                        && (temp_buffer[2] & 0xFF) == 0x00) {

                                    for (int i = 0; i < bytes; i++) {
                                        buffer[i + reading_cursor] = (byte) (temp_buffer[i] & 0xFF);
                                    }

                                    reading_cursor += bytes;
                                    foundFirst = true;
                                }
                            }
                        }
                        else {

                            if (mmInStream.available() + reading_cursor > packet_size)
                                bytes = mmInStream.read(temp_buffer, 0, mmInStream.available()
                                        - ((mmInStream.available() + reading_cursor) - packet_size));

                            else bytes = mmInStream.read(temp_buffer, 0, mmInStream.available());

                            for (int i = 0; i < bytes; i++) {
                                buffer[i + reading_cursor] = (byte) (temp_buffer[i] & 0xFF);
                            }

                            reading_cursor += bytes;


                            if (reading_cursor == packet_size) {
                                reading_cursor = 0;
                                mHandler.obtainMessage(RECIEVE_MESSAGE, buffer).sendToTarget(); // Send to message queue Handler
                                buffer = new byte[packet_size];
                                //foundFirst = false;
                                long time2 = System.currentTimeMillis();
                                Log.e("Timestamp", Long.toString(time2-time1));
                                time1 = time2;
                            }
                        }
                    } else if (!handshake) {
                        if(isfirstTry) {
                            mmOutStream.write(start_byte);
                            time1 = System.currentTimeMillis();
                            isfirstTry = false;
                        }
                    } else {
                        //isStarting = true;
                    }

                } catch (IOException e) {
                    msg("Stream input/output exception: " + e);
                    break;
                }

            }
            try{
                byte[] exitMode = {0x4E,0x4F};
                mmOutStream.write(exitMode);
            } catch (IOException e) {
                msg("Stream input/output exception: " + e);
            }
        }


        public void cancel() {
            try {
                mmSocket.close();

            } catch (IOException e) {
                Log.e("BluetoothService", "close() of connect socket failed", e);
            }
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {

        isBtConnected = false;
        return false;
    }

    @Override
    public void onDestroy() {
        this.stop();
        super.onDestroy();
    }

}
