package com.example.reubenpuketapu.apscanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by reubenpuketapu on 3/04/17.
 */

public class AccessPoint {

    private double x;
    private double y;
    private int z;
    private String desc;
    public double averageDistance = 10000;

    public Map<String, BSSID> bssids;

    public AccessPoint(Map<String, BSSID> bssids, int z, double x, double y, String desc) {
        this.bssids = bssids;
        this.x = x;
        this.y = y;
        this.z = z;
        this.desc = desc;

        Timer timer = new Timer();
        timer.schedule(new RemoveTask(), 2000);
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

    // Auto implemented
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessPoint that = (AccessPoint) o;

        if (Double.compare(that.x, x) != 0) return false;
        if (Double.compare(that.y, y) != 0) return false;
        if (z != that.z) return false;
        return desc != null ? desc.equals(that.desc) : that.desc == null;

    }

    // Auto implemented
    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + z;
        result = 31 * result + (desc != null ? desc.hashCode() : 0);
        return result;
    }

    private class RemoveTask extends TimerTask {

        @Override
        public void run() {

            Iterator<BSSID> i = bssids.values().iterator();
            while (i.hasNext()) {
                BSSID s = i.next();
                if (s.timeout >= 5) {
                    bssids.remove(s);
                }
            }

        }
    }
}
