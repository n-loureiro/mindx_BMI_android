package com.joaoaraujo.mindX_android;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Activity_Menu extends AppCompatActivity implements View.OnClickListener {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    Button settingsButton;

    public static double[] thresholds = {3.9548, 2.2470, 1.0005, -0.9172, -2.1637, -3.8715};
    public static double attractor_weight = .4;
    public static double voltage_limit = .1;

    public static final int BTLE_SERVICES = 2;
    public static String address_headset, name_headset;


    public double getVoltageLimit(){
        return voltage_limit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // get the listview
        expListView = findViewById(R.id.expandable_menu);

        // preparing list data
        populateMenu();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        expListView.setGroupIndicator(null);
        expListView.setDividerHeight(0);

        // On parent click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,int  groupPosition, long id) {

                if(groupPosition == 0){
                   return parent.expandGroup(groupPosition);
                }

                if(groupPosition == 1){
                    Intent intent = new Intent(getApplicationContext(), com.joaoaraujo.mindX_android.Activity_Impedance.class);
                    startActivity(intent);
                    return true;
                }

                // Raw signals
                if(groupPosition == 2)
                {
                    Intent intent = new Intent(getApplicationContext(), com.joaoaraujo.mindX_android.Activity_Raw.class);
                    startActivity(intent);
                    return true;
                }

                else return true;
            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                // Games
                if(groupPosition == 0){
                    //Utils.toast(getApplicationContext(), "Starting BMI game!");
                    Intent intent = new Intent(getApplicationContext(), com.joaoaraujo.mindX_android.Activity_Games.class);
                    intent.putExtra(Activity_Games.EXTRA_NAME, name_headset);
                    intent.putExtra(Activity_Games.EXTRA_ADDRESS, address_headset);
                    intent.putExtra(Activity_Games.EXTRA_THRESHOLDS, thresholds);
                    intent.putExtra(Activity_Games.EXTRA_ATTRACTORWEIGHT, attractor_weight);
                    intent.putExtra(Activity_Games.EXTRA_GAMEID, childPosition);
                    startActivityForResult(intent, BTLE_SERVICES);
                    return false;
                }

                Toast.makeText(
                        getApplicationContext(),
                        listDataHeader.get(groupPosition)
                                + " : "
                                + listDataChild.get(
                                listDataHeader.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        });

        settingsButton = findViewById(R.id.settings_button);

        settingsButton.setOnClickListener(this);

    }

    private void populateMenu() {

        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        // Adding child data
        listDataHeader.add("Games");
        listDataHeader.add("Impedance Check");
        listDataHeader.add("Raw Signals");
        listDataHeader.add("My Statistics");

        // Adding child data
        List<String> games = new ArrayList<String>();
        games.add("Cursor");
        games.add("Heading");
        games.add("Space Portals");

        listDataChild.put(listDataHeader.get(0), games); // Header, Child data
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.settings_button){
            Intent intent = new Intent(this, com.joaoaraujo.mindX_android.Activity_Settings.class);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
