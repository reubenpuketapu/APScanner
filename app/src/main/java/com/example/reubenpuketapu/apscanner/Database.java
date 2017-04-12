package com.example.reubenpuketapu.apscanner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by reubenpuketapu on 3/04/17.
 */

public class Database {

    private Map<String, AccessPoint> accessPoints = new HashMap<>();

    public Database(){
        accessPoints.put("2c:3f:38:2a:d9:60",new AccessPoint("2c:3f:38:2a:d9:60", 3, 29, 40,"Outside CO338"));
        accessPoints.put("00:1e:7a:27:f2:60",new AccessPoint("00:1e:7a:27:f2:60", 3, 11, 28,"Outside CO353"));
        accessPoints.put("08:17:35:9d:26:20",new AccessPoint("08:17:35:9d:26:20", 3, 29, 27,"Outside CO329"));
        accessPoints.put("00:23:33:20:eb:20",new AccessPoint("00:23:33:20:eb:20", 3, 31, 9,"Inside CO322"));
        accessPoints.put("08:17:35:9d:34:b0",new AccessPoint("08:17:35:9d:34:b0", 3, 75, 11,"Outside CO304"));

        accessPoints.put("08:17:35:9d:10:c0",new AccessPoint("08:17:35:9d:10:c0", 2, 13, 43,"Inside CO246"));
        accessPoints.put("08:17:35:9c:8a:70",new AccessPoint("08:17:35:9c:8a:70", 2, 21, 36,"Outside CO250"));
        accessPoints.put("00:23:33:20:fb:70",new AccessPoint("00:23:33:20:fb:70", 2, 6, 8,"Outside CO258"));
        accessPoints.put("08:17:35:9d:26:90",new AccessPoint("08:17:35:9d:26:90", 2, 60, 31,"Outside CO216"));
        accessPoints.put("08:17:35:9d:30:20",new AccessPoint("08:17:35:9d:30:20", 2, 47, 9,"Outside CO228"));
        accessPoints.put("08:17:35:9d:27:00",new AccessPoint("08:17:35:9d:27:00", 2, 87, 10, "Outside CO201"));
        accessPoints.put("08:17:35:9d:2a:c0",new AccessPoint("08:17:35:9d:2a:c0", 1, 37, -8,"Wishbone"));
        accessPoints.put("08:17:35:82:6e:40",new AccessPoint("08:17:35:82:6e:40", 1, 22, 41,"Outside Fuji Xerox"));
        accessPoints.put("c8:f9:f9:be:0d:20",new AccessPoint("c8:f9:f9:be:0d:20", 1, 69, 41,"Outside Tardis"));
        accessPoints.put("08:17:35:9d:32:d0",new AccessPoint("08:17:35:9d:32:d0", 1, 69, 27,"CO118"));
        accessPoints.put("08:17:35:62:ef:20",new AccessPoint("08:17:35:62:ef:20", 1, 48, 9,"CO124"));
        accessPoints.put("08:17:35:9c:dd:50",new AccessPoint("08:17:35:9c:dd:50", 1, 8, 7,"Outside CO148"));
        accessPoints.put("08:17:35:9c:e9:a0",new AccessPoint("08:17:35:9c:e9:a0", 1, 50, 5,"Outside CO126"));
        accessPoints.put("08:17:35:9c:e1:c0",new AccessPoint("08:17:35:9c:e1:c0", 1, 79, 10,"Outside CO105"));
        accessPoints.put("00:1e:4a:55:73:20",new AccessPoint("00:1e:4a:55:73:20", 4, 11, 10,"Outside CO435"));
        accessPoints.put("08:17:35:82:72:60",new AccessPoint("08:17:35:82:72:60", 4, 28, 12,"Outside CO427"));
        accessPoints.put("00:1e:7a:28:0a:30",new AccessPoint("00:1e:7a:28:0a:30", 4, 51, 10,"Outside CO419"));
        accessPoints.put("00:23:04:5c:9b:70",new AccessPoint("00:23:04:5c:9b:70", 4, 74, 7,"Outside CO406"));
        accessPoints.put("00:23:04:5c:b1:90",new AccessPoint("00:23:04:5c:b1:90", 5, 11, 10,"Outside CO533"));
        accessPoints.put("00:23:33:20:f2:a0",new AccessPoint("00:23:33:20:f2:a0", 5, 34, 7,"Outside CO525"));
        accessPoints.put("00:3a:98:04:af:90",new AccessPoint("00:3a:98:04:af:90", 5, 48, 7,"Outside CO519"));
        accessPoints.put("08:17:35:9c:f7:50",new AccessPoint("08:17:35:9c:f7:50", 5, 57, 7,"Outside CO515"));
        accessPoints.put("00:23:33:20:fd:40",new AccessPoint("00:23:33:20:fd:40", 5, 73, 10,"Inside CO508"));
        // my one
        accessPoints.put("50:1c:bf:b5:43:30",new AccessPoint("50:1c:bf:b5:43:30", 2, 35, 6,"Outside CO232"));

    }

    public Map<String, AccessPoint> getAccessPoints(){
        return accessPoints;
    }

}
