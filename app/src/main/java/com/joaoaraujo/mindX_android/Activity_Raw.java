package com.joaoaraujo.mindX_android;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Iterator;

import static java.lang.Math.max;
import static java.lang.Math.random;


public class Activity_Raw extends AppCompatActivity {

    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    GraphView raw_eeg_graph1;
    LineGraphSeries<DataPoint> raw_eeg_series;
    private final int max_datapoints = 2500; // maximum datapoints = 5 seconds
    private final int Fs = 500; // sampling freq

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
        raw_eeg_graph1.getViewport().setMinY(-1);
        raw_eeg_graph1.getViewport().setMaxY(1);
        raw_eeg_graph1.setTitle("EEG channel 1");


        raw_eeg_series = new LineGraphSeries<>();

        for(int i = 0; i < max_datapoints; i++) {
            DataPoint v = new DataPoint(i,0);
            raw_eeg_series.appendData(v, false, max_datapoints, true);
        }

        raw_eeg_series.setColor(Color.BLUE);

        raw_eeg_graph1.addSeries(raw_eeg_series);

    }

    @Override
    protected void onResume(){
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                generateData(125);

                mHandler.postDelayed(this, 500);
            }
        };
        mHandler.postDelayed(mTimer1, 500);
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        super.onPause();
    }

    private void generateData(int datapoints) {

        Iterator series_itr = raw_eeg_series.getValues(0,  max_datapoints);
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
        raw_eeg_series = new LineGraphSeries<>(new_series);
        raw_eeg_series.setColor(Color.BLUE);
        raw_eeg_graph1.removeAllSeries();
        raw_eeg_graph1.addSeries(raw_eeg_series);

    }



}
