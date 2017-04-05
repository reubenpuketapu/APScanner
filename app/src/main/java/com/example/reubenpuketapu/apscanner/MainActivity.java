package com.example.reubenpuketapu.apscanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView desc;
    private TextView level;
    private TextView dist;

    private WifiManager wifiManager;
    private List<ScanResult> scanResults;
    private Database db;

    private double averageLevel;
    private ArrayList<Double> levels = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new Database();

        button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(clickListener);

        desc = (TextView) findViewById(R.id.descText);
        level = (TextView) findViewById(R.id.levelText);
        dist = (TextView) findViewById(R.id.distanceText);

    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            levels.clear();
            averageLevel = 0;

            wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            wifiManager.startScan();

        }
    };

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //wifiManager.setTdlsEnabledWithMacAddress();
            WifiConfiguration wc = new WifiConfiguration();
            System.out.println(wifiManager.getConnectionInfo());
            scanResults = wifiManager.getScanResults();
            desc.setText("");
            level.setText("");
            for (ScanResult sr : scanResults) {

                desc.append(sr.SSID + "\n");
                level.append(sr.level + "\n");

                //for (AccessPoint ap : db.getAccessPoints()) {
                //if (sr.BSSID.equals(ap.getBssid())){
                //        desc.append(ap.getDesc() + "\n");
                //        level.append(String.format("%.5g%n", calculateDistance(sr.level)) + "\n");

                if (sr.SSID.contains("bah" ) && sr.frequency < 4000 ){
                    calculateDistance(sr.level);
                }

                //}
            }
        }
    };

    // -34dbm right next to co228

    private double calculateDistance(double level){
        averageLevel = 0;

        levels.add(level);
        for(Double d : levels){
            averageLevel += d;
        }
        averageLevel = averageLevel / levels.size();

        double RSSI = averageLevel +  30;

        double total = Math.pow(10, (RSSI / -30));

        //double distance = Math.pow(10, (-px - 20* ( Math.log((4*Math.PI)/0.125 )) )/40 );
        System.out.println(total+"m");
        dist.setText("Distance: " + total);
        return total;
    }
}
