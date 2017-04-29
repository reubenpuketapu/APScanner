package com.example.reubenpuketapu.apscanner;

import java.util.ArrayList;
import java.util.List;

public class BSSID {


    public static final double C = 3e8;

    private String bssid;
    private double frequency;

    public long timestamp = 0;
    private List<Integer> readings = new ArrayList<>();


    public BSSID(String bssid) {
        this.bssid = bssid;
    }

    public String getBssid() {
        return bssid;
    }

    public double getWavelength() {
        return C/(frequency*1000000);
    }

    public void setFrequency(double frequency){
        this.frequency = frequency;
    }

    public void addReading(int level) {
        readings.add(level);
    }

    public List<Integer> getReadings(){

        //int maxIndex = Math.min(readings.size(), 3);

        if (readings.size() >= 10){
            readings.remove(0);
        }

        return readings;
    }

    // Auto generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BSSID bssid1 = (BSSID) o;

        return bssid != null ? bssid.equals(bssid1.bssid) : bssid1.bssid == null;

    }

    // Auto generated
    @Override
    public int hashCode() {
        return bssid != null ? bssid.hashCode() : 0;
    }
}
