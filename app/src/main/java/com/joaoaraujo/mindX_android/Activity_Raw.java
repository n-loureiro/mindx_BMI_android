package com.joaoaraujo.mindX_android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.nio.ByteBuffer;
import java.util.Iterator;

import static java.lang.Math.max;
import static java.lang.Math.random;


public class Activity_Raw extends AppCompatActivity {

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


                    for (int i = 0; i < 2520; i+=4) {

                        if (i == 0 || i == 504 || i == 1008 || i == 1512 || i == 2016) {
                            //Log.e("i",Byte.toString(readBuf[i+3]));
                            channel_id[cursor_channel_id] = readBuf[i+3];
                            cursor_channel_id +=1;
                        } else {

                            byte[] byte_float = new byte[4];
                            byte_float[0] = readBuf[i+3];
                            byte_float[1] = readBuf[i+2];
                            byte_float[2] = readBuf[i+1];
                            byte_float[3] = readBuf[i];

                            channel_array[cursor_float] = ByteBuffer.wrap(byte_float).getFloat();
                            cursor_float += 1;

                        }
                    }

                    double[] ordered_channels = new double[625];
                    int channel_cursor = 1;
                    for(int i = 0; i < channel_id.length+1; i++){

                        if(channel_id[i] == channel_cursor){
                            System.arraycopy(channel_array, i*125, ordered_channels,
                                    (channel_cursor-1)*125, 125);
                            channel_cursor+=1;
                            i = -1;

                            if(channel_cursor > 5)
                                break;
                        }
                    }
                    //double [] channel_1 = new double[125];
                    //System.arraycopy(ordered_channels, 0, channel_1, 0, 125);
                    //Log.e("Mean Channel 1",Double.toString(mean(channel_1)));

                    if(doUpdates) {

                        updateData(125, channel_array,channel_id);
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
            msg("Service bound");
            serviceIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            msg("Service unbound");
            serviceIsBound = false;
        }
    };

    GraphView raw_eeg_graph1,raw_eeg_graph2,raw_eeg_graph3,raw_eeg_graph4,raw_eeg_graph5;
    private static LineGraphSeries<DataPoint> raw_eeg_series1,raw_eeg_series2,raw_eeg_series3,raw_eeg_series4,raw_eeg_series5;
    private static final int max_datapoints = 2500; // maximum datapoints = 5 seconds
    private final int Fs = 500; // sampling freq
    public static boolean doUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw);

        raw_eeg_graph1 = findViewById(R.id.ch_1);
        raw_eeg_graph1.getViewport().setScalable(true);
        raw_eeg_graph1.getViewport().setScalableY(true);
        raw_eeg_graph1.getViewport().setScrollable(true);
        raw_eeg_graph1.getViewport().setScalableY(true);
        raw_eeg_graph1.getViewport().setXAxisBoundsManual(true);
        raw_eeg_graph1.getViewport().setYAxisBoundsManual(true);
        raw_eeg_graph1.getViewport().setMinX(1);
        raw_eeg_graph1.getViewport().setMaxX(max_datapoints);
        raw_eeg_graph1.getViewport().setMinY(-0.1);
        raw_eeg_graph1.getViewport().setMaxY(0.1);
        //raw_eeg_graph1.setTitle("EEG channel 1");
        raw_eeg_graph1.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        raw_eeg_graph1.getGridLabelRenderer().setVerticalLabelsVisible(false);

        raw_eeg_graph2 = findViewById(R.id.ch_2);
        raw_eeg_graph2.getViewport().setScalable(true);
        raw_eeg_graph2.getViewport().setScalableY(true);
        raw_eeg_graph2.getViewport().setScrollable(true);
        raw_eeg_graph2.getViewport().setScalableY(true);
        raw_eeg_graph2.getViewport().setXAxisBoundsManual(true);
        raw_eeg_graph2.getViewport().setYAxisBoundsManual(true);
        raw_eeg_graph2.getViewport().setMinX(1);
        raw_eeg_graph2.getViewport().setMaxX(max_datapoints);
        raw_eeg_graph2.getViewport().setMinY(-0.1);
        raw_eeg_graph2.getViewport().setMaxY(0.1);
        //raw_eeg_graph2.setTitle("EEG channel 2");
        raw_eeg_graph2.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        raw_eeg_graph2.getGridLabelRenderer().setVerticalLabelsVisible(false);

        raw_eeg_graph3 = findViewById(R.id.ch_3);
        raw_eeg_graph3.getViewport().setScalable(true);
        raw_eeg_graph3.getViewport().setScalableY(true);
        raw_eeg_graph3.getViewport().setScrollable(true);
        raw_eeg_graph3.getViewport().setScalableY(true);
        raw_eeg_graph3.getViewport().setXAxisBoundsManual(true);
        raw_eeg_graph3.getViewport().setYAxisBoundsManual(true);
        raw_eeg_graph3.getViewport().setMinX(1);
        raw_eeg_graph3.getViewport().setMaxX(max_datapoints);
        raw_eeg_graph3.getViewport().setMinY(-0.1);
        raw_eeg_graph3.getViewport().setMaxY(0.1);
        //raw_eeg_graph3.setTitle("EEG channel 3");
        raw_eeg_graph3.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        raw_eeg_graph3.getGridLabelRenderer().setVerticalLabelsVisible(false);

        raw_eeg_graph4 = findViewById(R.id.ch_4);
        raw_eeg_graph4.getViewport().setScalable(true);
        raw_eeg_graph4.getViewport().setScalableY(true);
        raw_eeg_graph4.getViewport().setScrollable(true);
        raw_eeg_graph4.getViewport().setScalableY(true);
        raw_eeg_graph4.getViewport().setXAxisBoundsManual(true);
        raw_eeg_graph4.getViewport().setYAxisBoundsManual(true);
        raw_eeg_graph4.getViewport().setMinX(1);
        raw_eeg_graph4.getViewport().setMaxX(max_datapoints);
        raw_eeg_graph4.getViewport().setMinY(-0.1);
        raw_eeg_graph4.getViewport().setMaxY(0.1);
        //raw_eeg_graph4.setTitle("EEG channel 4");
        raw_eeg_graph4.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        raw_eeg_graph4.getGridLabelRenderer().setVerticalLabelsVisible(false);

        raw_eeg_graph5 = findViewById(R.id.ch_5);
        raw_eeg_graph5.getViewport().setScalable(true);
        raw_eeg_graph5.getViewport().setScalableY(true);
        raw_eeg_graph5.getViewport().setScrollable(true);
        raw_eeg_graph5.getViewport().setScalableY(true);
        raw_eeg_graph5.getViewport().setXAxisBoundsManual(true);
        raw_eeg_graph5.getViewport().setYAxisBoundsManual(true);
        raw_eeg_graph5.getViewport().setMinX(1);
        raw_eeg_graph5.getViewport().setMaxX(max_datapoints);
        raw_eeg_graph5.getViewport().setMinY(-0.1);
        raw_eeg_graph5.getViewport().setMaxY(0.1);
        //raw_eeg_graph5.setTitle("EEG channel 5");
        raw_eeg_graph5.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        raw_eeg_graph5.getGridLabelRenderer().setVerticalLabelsVisible(false);

        raw_eeg_series1 = new LineGraphSeries<>();
        raw_eeg_series2 = new LineGraphSeries<>();
        raw_eeg_series3 = new LineGraphSeries<>();
        raw_eeg_series4 = new LineGraphSeries<>();
        raw_eeg_series5 = new LineGraphSeries<>();

        for(int i = 0; i < max_datapoints; i++) {
            DataPoint v = new DataPoint(i,0);
            raw_eeg_series1.appendData(v, false, max_datapoints, true);
            raw_eeg_series2.appendData(v, false, max_datapoints, true);
            raw_eeg_series3.appendData(v, false, max_datapoints, true);
            raw_eeg_series4.appendData(v, false, max_datapoints, true);
            raw_eeg_series5.appendData(v, false, max_datapoints, true);
        }

        raw_eeg_series1.setColor(Color.BLUE);
        raw_eeg_series2.setColor(Color.BLUE);
        raw_eeg_series3.setColor(Color.BLUE);
        raw_eeg_series4.setColor(Color.BLUE);
        raw_eeg_series5.setColor(Color.BLUE);

        raw_eeg_graph1.addSeries(raw_eeg_series1);
        raw_eeg_graph2.addSeries(raw_eeg_series2);
        raw_eeg_graph3.addSeries(raw_eeg_series3);
        raw_eeg_graph4.addSeries(raw_eeg_series4);
        raw_eeg_graph5.addSeries(raw_eeg_series5);

    }

    public void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onStart(){
        super.onStart();
        doUpdates = true;
        Utils.boardMode = 0;
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
    protected void onResume(){
        super.onResume();
        if(!serviceIsBound) {
            bindService(bluetoothServiceIntent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
            serviceIsBound = true;
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

    /*private void generateData(int datapoints) {

        Iterator series_itr = raw_eeg_series1.getValues(0,  max_datapoints);
        DataPoint[] new_series = new DataPoint[max_datapoints];

        for(int i = 0; i< max_datapoints; i++) {
            if (series_itr.hasNext())
                new_series[i] = (DataPoint) series_itr.next();
        }

        for(int i = datapoints-1; i< max_datapoints; i++){
            DataPoint v = new DataPoint(new_series[i].getX()-(datapoints-1),
                    new_series[i].getY());
            new_series[i-(datapoints-1)] = v;
        }

        for(int i = max_datapoints-1-datapoints; i <max_datapoints; i++) {
            DataPoint v = new DataPoint(i,random()-0.5);
            new_series[i] = v;

        }
        raw_eeg_series1 = new LineGraphSeries<>(new_series);
        raw_eeg_series1.setColor(Color.BLUE);
        raw_eeg_graph1.removeAllSeries();
        raw_eeg_graph1.addSeries(raw_eeg_series1);

    }*/


    public static void updateData(int datapoints, double[] data, int[] channel_id) {

        long time1 = System.currentTimeMillis();

        Iterator series_itr1 = raw_eeg_series1.getValues(0,  max_datapoints);
        Iterator series_itr2 = raw_eeg_series2.getValues(0,  max_datapoints);
        Iterator series_itr3 = raw_eeg_series3.getValues(0,  max_datapoints);
        Iterator series_itr4 = raw_eeg_series4.getValues(0,  max_datapoints);
        Iterator series_itr5 = raw_eeg_series5.getValues(0,  max_datapoints);

        DataPoint[] new_series1 = new DataPoint[max_datapoints];
        DataPoint[] new_series2 = new DataPoint[max_datapoints];
        DataPoint[] new_series3 = new DataPoint[max_datapoints];
        DataPoint[] new_series4 = new DataPoint[max_datapoints];
        DataPoint[] new_series5 = new DataPoint[max_datapoints];

        for(int i = 0; i< max_datapoints; i++) {
            if (series_itr1.hasNext()){
                new_series1[i] = (DataPoint) series_itr1.next();
                new_series2[i] = (DataPoint) series_itr2.next();
                new_series3[i] = (DataPoint) series_itr3.next();
                new_series4[i] = (DataPoint) series_itr4.next();
                new_series5[i] = (DataPoint) series_itr5.next();
            }

        }

        for(int i = datapoints-1; i< max_datapoints; i++){
            DataPoint v1 = new DataPoint(new_series1[i].getX()-(datapoints-1),
                    new_series1[i].getY());
            new_series1[i-(datapoints-1)] = v1;

            DataPoint v2 = new DataPoint(new_series2[i].getX()-(datapoints-1),
                    new_series2[i].getY());
            new_series2[i-(datapoints-1)] = v2;

            DataPoint v3 = new DataPoint(new_series3[i].getX()-(datapoints-1),
                    new_series3[i].getY());
            new_series3[i-(datapoints-1)] = v3;

            DataPoint v4 = new DataPoint(new_series4[i].getX()-(datapoints-1),
                    new_series4[i].getY());
            new_series4[i-(datapoints-1)] = v4;

            DataPoint v5 = new DataPoint(new_series5[i].getX()-(datapoints-1),
                    new_series5[i].getY());
            new_series5[i-(datapoints-1)] = v5;
        }

        int cursor_float = 0;
        for(int j = 0; j < channel_id.length; j++) {
            int channel = channel_id[j];
            for (int i = max_datapoints - datapoints; i < max_datapoints; i++) {
                /*DataPoint v1 = new DataPoint(i, data[cursor_float]);
                DataPoint v2 = new DataPoint(i, data[125 + cursor_float]);
                DataPoint v3 = new DataPoint(i, data[250 + cursor_float]);
                DataPoint v4 = new DataPoint(i, data[325 + cursor_float]);
                DataPoint v5 = new DataPoint(i, data[450 + cursor_float]);*/

                DataPoint v = new DataPoint(i, data[cursor_float]);

                if(channel == 1)new_series1[i] = v;
                if(channel == 2)new_series2[i] = v;
                if(channel == 3)new_series3[i] = v;
                if(channel == 4)new_series4[i] = v;
                if(channel == 5)new_series5[i] = v;

                cursor_float += 1;

            }
        }

        raw_eeg_series1.resetData(new_series1);
        raw_eeg_series2.resetData(new_series2);
        raw_eeg_series3.resetData(new_series3);
        raw_eeg_series4.resetData(new_series4);
        raw_eeg_series5.resetData(new_series5);

        //Log.e("Timestamp3",Long.toString(System.currentTimeMillis() - BluetoothService.time1));


    }


    private static double mean(double[] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }



}
