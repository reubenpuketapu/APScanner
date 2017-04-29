package com.example.reubenpuketapu.apscanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.reubenpuketapu.apscanner.trilateration.NonLinearLeastSquaresSolver;
import com.example.reubenpuketapu.apscanner.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;


public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView dist;

    private Canvas canvas;
    private ImageView ivOverlay;
    private ImageView ivBackground;
    private Bitmap bitmap;

    private WifiManager wifiManager;
    private SensorManager sensorManager;

    private Sensor stepSensor;
    private Sensor pressSensor;
    private Sensor gravSensor;
    private Sensor magSensor;

    private List<ScanResult> scanResults;
    private Database db;

    private TreeSet<AccessPoint> currentAPs;
    private Set<String> currentBSSIDs = new HashSet<>();

    // TODO: find deg offset
    private static final double DEG_OFFSET = 8;
    private static final double STRIDE_LENGTH = 0.72625;
    private static final double DRAW_RATIO = 11.75;
    private static final double SAME_FLOOR_EXP = 3.5;
    private static final double DIFF_FLOOR_EXP = 5.0;
    private static final double BASE_LEVEL = 34;

    private float[] orientation = new float[3];
    private float[] r = new float[9];
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];

    // x y location in metres
    private Location location = new Location(30, 30, 2);

    // dx dy in metres
    private double dx;
    private double dy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_main);

        currentAPs = new TreeSet<>(accessPointComparator);

        // UI STUFF
        button = (Button) findViewById(R.id.scan_button);
        button.setOnClickListener(clickListener);

        dist = (TextView) findViewById(R.id.distanceText);

        ivBackground = (ImageView) findViewById(R.id.iv_background);
        ivOverlay = (ImageView) findViewById(R.id.iv_overlay);
        ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.one, null));
        ivBackground.setOnTouchListener(imageListener);

        // WIFI STUFF
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        db = new Database();

        // SENSOR STUFF
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        pressSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(pressureListener, pressSensor, SensorManager.SENSOR_DELAY_NORMAL); // slow delays allgood
        sensorManager.registerListener(gravListener, gravSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(magListener, magSensor, SensorManager.SENSOR_DELAY_FASTEST);

        // Start task every 2 seconds
        Timer timer = new Timer();
        timer.schedule(new RemoveTask(), 0, 1000);

    }

    // SENSOR EVENTS

    private SensorEventListener gravListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            gravity[0] = event.values[0];
            gravity[1] = event.values[1];
            gravity[2] = event.values[2];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private SensorEventListener magListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            geomagnetic[0] = event.values[0];
            geomagnetic[1] = event.values[1];
            geomagnetic[2] = event.values[2];

            float[] temp = new float[3];

            sensorManager.getRotationMatrix(r, null, gravity, geomagnetic);
            sensorManager.getOrientation(r, temp);

            float azimuthInRadians = temp[0];
            float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);
            if (azimuthInDegrees < 0.0f) {
                azimuthInDegrees += 360.0f;
            }
            azimuthInDegrees += DEG_OFFSET;
            orientation[0] = (float) Math.toRadians(azimuthInDegrees);

            dx = STRIDE_LENGTH * Math.sin((orientation[0]));
            dy = STRIDE_LENGTH * Math.cos((orientation[0]));

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    private SensorEventListener stepListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {

            location.x += dx;
            location.y -= dy;

            //drawLocation(xLocation * 10, yLocation * 10, 1);

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private SensorEventListener pressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            // TODO: need to calculate the values for each level and map them to each
            // TODO: set to z!

            // WORKS FOR HEIGHT!!!!!!!
            //dist.setText(event.values[0]+ " \n");

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    // UI EVENTS

    private View.OnTouchListener imageListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            int[] xy = new int[2];
            v.getLocationOnScreen(xy);

            // off set the ratio
            location.x = (x - xy[0]) / DRAW_RATIO;
            location.y = y / DRAW_RATIO;

            drawLocation(location.x, location.y, location.z);

            return false;
        }
    };

    public void drawLocation(double x, double y, double floor) {

        if (floor == 1) {
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.one, null));
        } else if (floor == 3) {
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.three, null));
        } else if (floor == 4) {
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.four, null));
        } else if (floor == 5) {
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.five, null));
        } else {
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.two, null));
        }

        bitmap = Bitmap.createBitmap(ivBackground.getWidth(), ivBackground.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.RED);

        // draw the circle with 117.5 x-y scale ratio
        canvas.drawCircle((float) (x * DRAW_RATIO), (float) (y * DRAW_RATIO), 10, paint);

        ivOverlay.setImageBitmap(bitmap);

    }

    // WIFI EVENTS

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            wifiManager.startScan();
        }
    };

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            scanResults = wifiManager.getScanResults();

            for (ScanResult sr : scanResults) {

                if (db.containsBSSID(sr.BSSID)) {
                    AccessPoint ap = db.getAP(sr.BSSID);

                    BSSID bssid = ap.bssids.get(sr.BSSID);
                    bssid.timestamp = System.currentTimeMillis();
                    bssid.addReading(sr.level);
                    bssid.setFrequency(sr.frequency);

                    ap.averageDistance = calculateDistance(ap);
                    System.out.println("home distance: " + ap.averageDistance);

                    dist.setText("distance: " + ap.averageDistance);

                    // sort by the *closest* access points, based on their distance
                    currentAPs.add(ap);

                    if(ap.getZ() == location.z) {
                        canvas.drawCircle((float) (ap.getX() * DRAW_RATIO), (float) (ap.getY() * DRAW_RATIO), 10, new Paint(Color.BLUE));
                    }

                }

                currentBSSIDs.add(sr.BSSID);

                // need 3 APs
                if (currentAPs.size() >= 4) {

                    Location tempLocation = calculateWifiLocation();

                    dist.setText(tempLocation.x + " " + tempLocation.y + " " + tempLocation.z + "\n");

                    // set and draw the location
                    location.x = tempLocation.x;
                    location.y = tempLocation.y;
                    location.z = tempLocation.z;

                    drawLocation(location.x, location.y, location.z);

                }

                // continuously scan for wifi networks
                wifiManager.startScan();

            }
        }
    };

    private Location calculateWifiLocation() {

        // Uses the up to 5 of the closest Access Points
        int size = Math.min(currentAPs.size(), 5);

        double[] distances = new double[size];
        double[][] positions = new double[size][3];

        // only x and y
        int i = 0;
        for(AccessPoint ap : currentAPs){
            distances[i] = ap.averageDistance;
            positions[i][0] = ap.getX();
            positions[i][1] = ap.getY();
            positions[i][2] = ap.getZ();

            if (i++ >= size){
                break;
            }
        }

        NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
        LeastSquaresOptimizer.Optimum optimum = solver.solve();

        // the answer
        double[] centroid = optimum.getPoint().toArray();

        // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
        RealVector standardDeviation = optimum.getSigma(0);
        RealMatrix covarianceMatrix = optimum.getCovariances(0);

        return new Location(centroid[0], centroid[1], centroid[2]);
        //return new Location(200,200);

    }

    private double calculateDistance(AccessPoint ap) {

        double averageDistance = 0;

        for (BSSID bssid : ap.bssids.values()) {

            int individualLevel = 0;
            int divideIterations = Math.min(bssid.getReadings().size(), 3);

            if (!bssid.getReadings().isEmpty()) {

                // get the 3 most recent values if there are more than three
                int iterations = Math.min(bssid.getReadings().size(), 3);
                for (int i = bssid.getReadings().size() - 1; i >= 0 && iterations > 0; i--, iterations--) {
                    individualLevel += bssid.getReadings().get(i);
                }

                // average each distance reading for each BSSID on the Access Point
                individualLevel = individualLevel / divideIterations;
                averageDistance += (convertRssiToM(individualLevel, bssid.getWavelength(), true));

                //averageDistance += (convertRssiToM(individualLevel, bssid.getWavelength(), ap.getZ() == location.z));

            }

        }

        return averageDistance / ap.bssids.size();
    }

    private double convertRssiToM(double RSSI, double wavelength, boolean sameFloor) {

        //return Math.pow(10, ((RSSI+34) / -35));

        // taking into account APs on different floors will have an increased path loss exponent
        if (sameFloor) {
            return Math.pow(10, (RSSI + BASE_LEVEL) / (-10 * SAME_FLOOR_EXP));
            //return Math.pow(10, ((RSSI + (20 * (Math.log10((4 * Math.PI)/wavelength)))) / (-10 * SAME_FLOOR_EXP)));
        } else {
            return Math.pow(10, (RSSI + BASE_LEVEL) / (-10 * DIFF_FLOOR_EXP));
            //return Math.pow(10, ((RSSI + (20 * (Math.log10((4 * Math.PI)/wavelength))) / (-10 * DIFF_FLOOR_EXP))));
        }
    }

    private int calculateWifiZ() {

        int[] floorCount = {0, 0, 0, 0, 0};

        // get the closest three APs
        int i = 0;
        for (AccessPoint ap : currentAPs){
            floorCount[ap.getZ() - 1]++;

            if(i++ >= 3) break;
        }

        int maxFloor = 0;
        int floor = 0;
        for (i = 0; i < floorCount.length; i++) {
            if (floorCount[i] > maxFloor) {
                maxFloor = floorCount[i];
                floor = i;
            }
        }

        return floor+1;

    }


    private Comparator<AccessPoint> accessPointComparator = new Comparator<AccessPoint>() {
        @Override
        public int compare(AccessPoint lhs, AccessPoint rhs) {
            if (lhs.averageDistance > rhs.averageDistance) return -1;
            else if (lhs.averageDistance < rhs.averageDistance) return 1;
            else return 0;
        }

    };

    /**
     * Background task to remove Access Points that haven't been seen in 5 seconds
     */
    private class RemoveTask extends TimerTask{

        @Override
        public void run() {

            Set<AccessPoint> removeAPs = new HashSet<>();

            for(AccessPoint ap : currentAPs){

                // iterate through each
                for (BSSID bssid : ap.bssids.values()) {

                    System.out.println("DIFF: " + (System.currentTimeMillis()- bssid.timestamp));

                    if (System.currentTimeMillis() - bssid.timestamp >= 5000) {
                        bssid.getReadings().clear();
                    }
                }

                // remove this access point because it has no current readings
                boolean remove = false;
                for (BSSID bssid : ap.bssids.values()){
                    if(bssid.getReadings().isEmpty()){
                        remove = true;
                    }
                }

                if (remove){
                    removeAPs.add(ap);
                    System.out.println("removed: " + ap.getDesc());
                }
            }

            currentAPs.removeAll(removeAPs);
        }
    }

}



