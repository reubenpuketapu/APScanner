package com.example.reubenpuketapu.apscanner;

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

    private List<AccessPoint> accessPoints = new ArrayList<>();

    public Database(){
        accessPoints.add(new AccessPoint(createSet("08:17:35:9c:e1:c0","08:17:35:9c:e1:cd","08:17:35:9c:e1:cf"), 1, 76.1, 36,"Outside CO105"));//A
        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:32:d0","08:17:35:9d:32:dd","08:17:35:9d:32:df"), 1, 65, 20.5,"CO118"));//B
        accessPoints.add(new AccessPoint(createSet("c8:f9:f9:be:0d:20","c8:f9:f9:be:0d:2d","c8:f9:f9:be:0d:2f"), 1, 65.2, 8.5,"Outside Fuji Xerox"));//C
        accessPoints.add(new AccessPoint(createSet("08:17:35:62:ef:a0","08:17:35:62:ef:2d","08:17:35:62:ef:2f"), 1, 54.9, 40.8,"Outside CO127"));//D
        accessPoints.add(new AccessPoint(createSet("08:17:35:9c:e9:a0","08:17:35:9c:e9:ad","08:17:35:9c:e9:af"), 1, 46, 32.8,"Outside CO128"));//E
        accessPoints.add(new AccessPoint(createSet("f4:cf:e2:b4:e5:e0","f4:cf:e2:b4:e5:ed","f4:cf:e2:b4:e5:ef"), 1, 32.7, 19.3,"Inside CO167"));//H
        accessPoints.add(new AccessPoint(createSet("08:17:35:82:6e:40","08:17:35:82:6e:4d","08:17:35:82:6e:4f"), 1, 27.3, 8.7,"Outside CO143"));//I
        accessPoints.add(new AccessPoint(createSet("70:10:5c:83:a2:70","70:10:5c:83:a2:7d","70:10:5c:83:a2:7f"), 1, 20.8, 20.8,"Arthur's Lab Front"));//J
        accessPoints.add(new AccessPoint(createSet("e8:65:49:40:0c:10","e8:65:49:40:0c:1d","e8:65:49:40:0c:1f"), 1, 49.7, 24,"Inside CO122 Left Side"));//F
        accessPoints.add(new AccessPoint(createSet("54:a2:74:bf:a7:00","54:a2:74:bf:a7:0d","54:a2:74:bf:a7:0f"), 1, 49.6, 17.5,"Inside CO122 Right Side"));//G
        accessPoints.add(new AccessPoint(createSet("08:17:35:9c:dd:50","08:17:35:9c:dd:5d","08:17:35:9c:e9:af"), 1, 16.7, 39.3,"Outside CO148"));//M
       // accessPoints.add(new AccessPoint(createSet("08:17:35:62:ef:a0","08:17:35:62:ef:2d","08:17:35:62:ef:2f"), 1, 54.9, 40.8,"Outside CO127"));//D
        accessPoints.add(new AccessPoint(createSet("c8:f9:f9:a1:b1:f0","c8:f9:f9:a1:b1:f2"), 1, 19.8, 34.2,"Awhina Room"));//L
        // K

        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:10:c2","08:17:35:9d:10:cd","08:17:35:9d:10:cf"), 2, 10.9, 9.1,"Inside CO246"));
        accessPoints.add(new AccessPoint(createSet("2c:3f:38:30:43:a0","2c:3f:38:30:43:ad","2c:3f:38:30:43:af"), 2, 7.6, 39.2,"Outside CO258"));
        accessPoints.add(new AccessPoint(createSet("38:1c:1a:9a:5c:20","38:1c:1a:9a:5c:2d","38:1c:1a:9a:5c:2f"), 2, 16, 40,"Outside CO262"));
        accessPoints.add(new AccessPoint(createSet("08:17:35:9c:8a:70","08:17:35:9c:8a:7d","08:17:35:9c:8a:7f"), 2, 17, 15,"Outside CO242A"));
        accessPoints.add(new AccessPoint(createSet("50:1c:bf:b5:43:30","50:1c:bf:b5:43:3d","50:1c:bf:b5:43:3f"), 2, 23.5, 39.2,"Outside CO253"));
        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:30:20","08:17:35:9d:30:2d","08:17:35:9d:30:2f"), 2, 36.2, 38.3,"Outside CO228"));
        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:26:90","08:17:35:9d:26:9d","08:17:35:9d:26:9f"), 2, 43.8, 17.7,"Outside CO217"));
        accessPoints.add(new AccessPoint(createSet("64:e9:50:b8:41:40","64:e9:50:b8:41:4d","64:e9:50:b8:41:4f"), 2, 35, 18,"Inside CO219"));
        accessPoints.add(new AccessPoint(createSet("08:17:35:9d:27:02"), 2, 62.2, 36.9, "Outside CO201"));

        //accessPoints.put(new AccessPoint("08:17:35:9c:8a:70", 2, 21, 36,"Outside CO250"));


        accessPoints.put(new AccessPoint("2c:3f:38:2a:d9:60", 3, 29, 40,"Outside CO338"));
        accessPoints.put(new AccessPoint("00:1e:7a:27:f2:60", 3, 11, 28,"Outside CO353"));
        accessPoints.put(new AccessPoint("08:17:35:9d:26:20", 3, 29, 27,"Outside CO329"));
        accessPoints.put(new AccessPoint("00:23:33:20:eb:20", 3, 31, 9,"Inside CO322"));
        accessPoints.put(new AccessPoint("08:17:35:9d:34:b0", 3, 75, 11,"Outside CO304"));

/*



        accessPoints.put("00:1e:4a:55:73:20",new AccessPoint("00:1e:4a:55:73:20", 4, 11, 10,"Outside CO435"));
        accessPoints.put("08:17:35:82:72:60",new AccessPoint("08:17:35:82:72:60", 4, 28, 12,"Outside CO427"));
        accessPoints.put("00:1e:7a:28:0a:30",new AccessPoint("00:1e:7a:28:0a:30", 4, 51, 10,"Outside CO419"));
        accessPoints.put("00:23:04:5c:9b:70",new AccessPoint("00:23:04:5c:9b:70", 4, 74, 7,"Outside CO406"));
        accessPoints.put("00:23:04:5c:b1:90",new AccessPoint("00:23:04:5c:b1:90", 5, 11, 10,"Outside CO533"));
        accessPoints.put("00:23:33:20:f2:a0",new AccessPoint("00:23:33:20:f2:a0", 5, 34, 7,"Outside CO525"));
        accessPoints.put("00:3a:98:04:af:90",new AccessPoint("00:3a:98:04:af:90", 5, 48, 7,"Outside CO519"));
        accessPoints.put("08:17:35:9c:f7:50",new AccessPoint("08:17:35:9c:f7:50", 5, 57, 7,"Outside CO515"));
        accessPoints.put("00:23:33:20:fd:40",new AccessPoint("00:23:33:20:fd:40", 5, 73, 10,"Inside CO508"));
        */
    }

    public List<AccessPoint> getAccessPoints(){
        return accessPoints;
    }

    private List<BSSID> createSet(String... elements){
        List<BSSID> set = new ArrayList<>();
        for (String bssid : elements){
            set.add(new BSSID(bssid));
        }
        return set;
    }

}
