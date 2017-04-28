package com.example.reubenpuketapu.apscanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by reubenpuketapu on 28/04/17.
 */

public class BSSID {

    private String bssid;
    private double freq;

    public  double distance;
    public List<Integer> readings = new ArrayList<>();


    public BSSID(String bssid) {
        this.bssid = bssid;
        this.freq = freq;
    }

    public String getBssid() {
        return bssid;
    }

    public double getFreq() {
        return freq;
    }
}
