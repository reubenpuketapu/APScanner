package com.example.reubenpuketapu.apscanner.db;

import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.example.reubenpuketapu.apscanner.wrappers.AccessPoint;
import com.example.reubenpuketapu.apscanner.wrappers.BSSID;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by reubenpuketapu on 3/04/17.
 */

public class Database {

    public final List<AccessPoint> accessPoints = new ArrayList<>();
    public ArrayList<Double> calibrationValues;
    private final Set<String> allBSSIDS = new HashSet<>();

    public Database (ArrayList<CharSequence> cs){

        accessPoints.add(new AccessPoint(createSet("08:17:35:9c:e1:c0","08:17:35:9c:e1:cd","08:17:35:9c:e1:cf"), 3, 76.1, 36,"Outside CO105"));//A
        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:32:d0","08:17:35:9d:32:dd","08:17:35:9d:32:df"), 3, 65, 20.5,"CO118"));//B
        accessPoints.add(new AccessPoint(createSet("c8:f9:f9:be:0d:20","c8:f9:f9:be:0d:2d","c8:f9:f9:be:0d:2f"), 3, 65.2, 8.5,"Outside Fuji Xerox"));//C
        accessPoints.add(new AccessPoint(createSet("08:17:35:62:ef:a0","08:17:35:62:ef:2d","08:17:35:62:ef:2f"), 3, 50.9, 40.8,"Outside CO127"));//D
        accessPoints.add(new AccessPoint(createSet("08:17:35:9c:e9:a0","08:17:35:9c:e9:ad","08:17:35:9c:e9:af"), 3, 51, 32.8,"Outside CO128"));//E
        accessPoints.add(new AccessPoint(createSet("f4:cf:e2:bf:e5:e0","f4:cf:e2:bf:e5:ed","f4:cf:e2:bf:e5:ef"), 3, 32.7, 19.3,"Inside CO167"));//H
        accessPoints.add(new AccessPoint(createSet("08:17:35:82:6e:40","08:17:35:82:6e:4d","08:17:35:82:6e:4f"), 3, 27.3, 8.7,"Outside CO143"));//I
        accessPoints.add(new AccessPoint(createSet("70:10:5c:83:a2:70","70:10:5c:83:a2:7d","70:10:5c:83:a2:7f"), 3, 20.8, 20.8,"Inside CO145"));//J
        accessPoints.add(new AccessPoint(createSet("08:17:35:9c:e6:80"), 3, 12.1, 25.3,"Inside CO145 Back"));//K
        accessPoints.add(new AccessPoint(createSet("e8:65:49:40:0c:10","e8:65:49:40:0c:1d","e8:65:49:40:0c:1f"), 3, 49.7, 24,"Inside CO122 Left Side"));//F
        accessPoints.add(new AccessPoint(createSet("54:a2:74:bf:a7:00","54:a2:74:bf:a7:0d","54:a2:74:bf:a7:0f"), 3, 49.6, 17.5,"Inside CO122 Right Side"));//G
        accessPoints.add(new AccessPoint(createSet("08:17:35:9c:dd:50","08:17:35:9c:dd:5d","08:17:35:9c:e9:af"), 3, 16.7, 39.3,"Outside CO148"));//M
        accessPoints.add(new AccessPoint(createSet("08:17:35:62:ef:a0","08:17:35:62:ef:2d","08:17:35:62:ef:2f"), 3, 34.7, 40.6,"Outside CO132"));//N
        accessPoints.add(new AccessPoint(createSet("c8:f9:f9:a1:b1:f0","c8:f9:f9:a1:b1:f2"), 3, 19.8, 34.2,"Inside CO145A"));//L

        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:10:c2","08:17:35:9d:10:cd","08:17:35:9d:10:cf"), 8, 10.9, 9.1,"Inside CO246"));
        accessPoints.add(new AccessPoint(createSet("2c:3f:38:30:43:a0","2c:3f:38:30:43:ad","2c:3f:38:30:43:af"), 8, 7.6, 39.2,"Outside CO258"));
        accessPoints.add(new AccessPoint(createSet("38:1c:1a:9a:5c:20","38:1c:1a:9a:5c:2d","38:1c:1a:9a:5c:2f"), 8, 16, 40,"Outside CO262"));
        accessPoints.add(new AccessPoint(createSet("08:17:35:9c:8a:70","08:17:35:9c:8a:7d","08:17:35:9c:8a:7f"), 8, 17, 15,"Outside CO242A"));
        accessPoints.add(new AccessPoint(createSet("50:1c:bf:b5:43:30","50:1c:bf:b5:43:3d","50:1c:bf:b5:43:3f"), 8, 28.5, 39.2,"Outside CO253"));
        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:30:20","08:17:35:9d:30:2d","08:17:35:9d:30:2f"), 8, 36.2, 38.3,"Outside CO228"));
        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:26:90","08:17:35:9d:26:9d","08:17:35:9d:26:9f"), 8, 43.8, 17.7,"Outside CO217"));
        accessPoints.add(new AccessPoint(createSet("64:e9:50:b8:41:40","64:e9:50:b8:41:4d","64:e9:50:b8:41:4f"), 8, 35, 18,"Inside CO219"));
        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:27:02"), 8, 62.2, 36.9, "Outside CO201"));

        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:24:40","08:17:35:9d:24:4d","08:17:35:9d:24:4f"), 13, 25.3, 36.2,"Outside CO358"));
        accessPoints.add(new AccessPoint(createSet("2c:3f:38:30:6a:10","2c:3f:38:30:6a:1d","2c:3f:38:30:6a:1f","2c:3f:38:30:6a:12"), 13, 24.5, 21.4,"Outside CO347"));
        accessPoints.add(new AccessPoint(createSet("2c:3f:38:2a:d9:60","2c:3f:38:2a:d9:62"), 13, 31, 14.5,"Outside CO347"));
        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:26:20","08:17:35:9d:26:2d","08:17:35:9d:26:2f"), 13, 39.4, 20.4,"Outside C0330"));
        accessPoints.add(new AccessPoint(createSet("54:78:1a:43:98:50","54:78:1a:43:98:5d","54:78:1a:43:98:5f"), 13, 39, 35.6,"Outside CO322"));
        accessPoints.add(new AccessPoint(createSet("70:10:5c:f5:25:10","70:10:5c:f5:25:1d","70:10:5c:f5:25:1f", "70:10:5c:f5:25:12"), 13, 46, 37.1,"Outside CO318"));
        accessPoints.add(new AccessPoint(createSet("e8:65:49:10:00:d0","e8:65:49:10:00:dd","e8:65:49:10:00:df", "e8:65:49:10:00:d2"), 13, 56.5, 33.5,"Outside CO313"));
        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:34:b0","08:17:35:9d:34:b2"), 13, 66.1, 33.4,"Outside CO304"));

        accessPoints.add(new AccessPoint(createSet("54:78:1a:43:95:50","54:78:1a:43:95:5d","54:78:1a:43:95:5f"), 18, 7.7, 8.2,"Outside CO425"));
        accessPoints.add(new AccessPoint(createSet("08:17:35:82:72:60","08:17:35:82:72:6d","08:17:35:82:72:6f"), 18, 19.6, 6.7,"Outside CO427"));
        accessPoints.add(new AccessPoint(createSet("50:06:04:c3:a5:a0","50:06:04:c3:a5:ad","50:06:04:c3:a5:af"), 18, 29, 11.5,"Outside CO418"));
        accessPoints.add(new AccessPoint(createSet("2c:3f:38:2a:67:00","2c:3f:38:2a:67:0d","2c:3f:38:2a:67:0f"), 18, 41.8, 11.5,"Outside CO408"));

        accessPoints.add(new AccessPoint(createSet("08:17:35:62:cc:a0","08:17:35:62:cc:ad","08:17:35:62:cc:af"), 23, 16.5, 4.4,"Outside CO564"));
        accessPoints.add(new AccessPoint(createSet("e8:65:49:33:04:60","e8:65:49:33:04:6d","e8:65:49:33:04:6f"), 23, 28.6, 7.3,"Outside CO532"));
        accessPoints.add(new AccessPoint(createSet("f4:cf:e2:c6:a2:c0","f4:cf:e2:c6:a2:cd","f4:cf:e2:c6:a2:cf"), 23, 40.3, 7.2,"Outside CO530"));
        accessPoints.add(new AccessPoint(createSet("f4:cf:e2:b2:b9:00","f4:cf:e2:b2:b9:0d","f4:cf:e2:b2:b9:0f"), 23, 42.3, 3.7,"Inside CO501"));
        accessPoints.add(new AccessPoint(createSet("f4:cf:e2:be:7b:80","f4:cf:e2:be:7b:8d","f4:cf:e2:be:7b:8f"), 23, 53.3, 4.3,"Outside CO504"));
        accessPoints.add(new AccessPoint(createSet("f4:cf:e2:ae:92:50","f4:cf:e2:ae:92:5d","f4:cf:e2:ae:92:5f"), 23, 57, 7.2,"Outside CO525"));
        accessPoints.add(new AccessPoint(createSet("f4:cf:e2:b4:dd:f0","f4:cf:e2:b4:dd:fd","f4:cf:e2:b4:dd:ff"), 23, 63.3, 4.3,"Outside CO506"));
        accessPoints.add(new AccessPoint(createSet("f4:cf:e2:b3:08:70","f4:cf:e2:b3:08:7d","f4:cf:e2:b3:08:7f"), 23, 65.2, 72,"Outside CO524"));
        accessPoints.add(new AccessPoint(createSet("f4:cf:e2:be:78:f0","f4:cf:e2:be:78:fd","f4:cf:e2:be:78:ff"), 23, 69.5, 7.2,"Outside CO522"));
        accessPoints.add(new AccessPoint(createSet("f4:cf:e2:b4:dc:c0","f4:cf:e2:b4:dc:cd","f4:cf:e2:b4:dc:cf"), 23, 74.9, 4.4,"Outside CO510"));
        accessPoints.add(new AccessPoint(createSet("f4:cf:e2:b3:a8:10","f4:cf:e2:b3:a8:1d","f4:cf:e2:b3:a8:1f"), 23, 82.8, 7.2,"Outside CO519"));
        accessPoints.add(new AccessPoint(createSet("f4:cf:e2:b3:08:80","f4:cf:e2:b3:08:8d","f4:cf:e2:b3:08:8f"), 23, 83.1, 4.4,"Outside CO513"));
        addBSSIDSToSet();

        if (cs != null && !cs.isEmpty()){
            populateCalibrations(cs);

        }
    }

    public void populateCalibrations(ArrayList<CharSequence> cs) {

        try {

            calibrationValues = new ArrayList<>();

            calibrationValues.add(Double.parseDouble(cs.get(0).toString()));
            calibrationValues.add(Double.parseDouble(cs.get(1).toString()));
            calibrationValues.add(Double.parseDouble(cs.get(2).toString()));
            calibrationValues.add(Double.parseDouble(cs.get(3).toString()));
            calibrationValues.add(Double.parseDouble(cs.get(4).toString()));

            System.out.println("calibration values: " + calibrationValues);
        }
        catch (NullPointerException e){

        }
        catch (NumberFormatException e){

        }

    }

    private void addBSSIDSToSet() {

        for (AccessPoint ap : accessPoints) {
            for (BSSID bssid : ap.bssids.values())
                allBSSIDS.add(bssid.getBssid());
        }
    }

    private Map<String, BSSID> createSet(String... elements){
        Map<String, BSSID> set = new HashMap<>();
        for (String bssid : elements){
            set.put(bssid, new BSSID(bssid));
        }
        return set;
    }

    public AccessPoint getAP(String BSSID){

        for (AccessPoint ap : accessPoints){
            if(ap.bssids.containsKey(BSSID)){
                return ap;
            }
        }

        return null;
    }

    public boolean containsBSSID(String bssid) {
        return allBSSIDS.contains(bssid);
    }




}
