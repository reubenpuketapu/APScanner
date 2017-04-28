package com.example.reubenpuketapu.apscanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by reubenpuketapu on 3/04/17.
 */

public class AccessPoint {

    private double x;
    private double y;
    private int z;
    private String desc;
    public double averageDistance;

    public List<BSSID> bssids;

    public int timeout = 0;

    public AccessPoint(List<BSSID> bssids, int z, double x, double y, String desc) {
        this.bssids = bssids;
        this.x = x;
        this.y = y;
        this.z = z;
        this.desc = desc;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getDesc() {
        return desc;
    }

}
