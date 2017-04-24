package com.example.reubenpuketapu.apscanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by reubenpuketapu on 3/04/17.
 */

public class AccessPoint {

    private String bssid;
    private int x;
    private int y;
    private int z;
    private String desc;
    public double distance;

    public List<Integer> readings = new ArrayList<>();

    public AccessPoint(String bssid, int z, int x, int y, String desc) {
        this.bssid = bssid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.desc = desc;
    }

    public String getBssid() {
        return bssid;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getDesc() {
        return desc;
    }

}
