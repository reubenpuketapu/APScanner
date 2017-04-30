package com.example.reubenpuketapu.apscanner.wrappers;

/**
 * Created by reubenpuketapu on 30/04/17.
 */

public class Reading {

    public int value;
    public long timestamp = 0;

    public Reading(int value) {
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }



}
