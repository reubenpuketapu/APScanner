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
import java.util.List;


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

    private List<AccessPoint> currentAPs = new ArrayList<>();

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
    private Location location = new Location(30,30);

    // dx dy in metres
    private double dx;
    private double dy;

    private double oldz = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_main);

        // UI STUFF
        button = (Button) findViewById(R.id.scan_button);
        button.setOnClickListener(clickListener);

        dist = (TextView) findViewById(R.id.distanceText);

        ivBackground = (ImageView)findViewById(R.id.iv_background);
        ivOverlay = (ImageView)findViewById(R.id.iv_overlay);
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
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
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
            float azimuthInDegrees = (float)Math.toDegrees(azimuthInRadians);
            if (azimuthInDegrees < 0.0f) {
                azimuthInDegrees += 360.0f;
            }
            azimuthInDegrees += DEG_OFFSET;
            orientation[0] = (float)Math.toRadians(azimuthInDegrees);

            dx = STRIDE_LENGTH * Math.sin((orientation[0]));
            dy = STRIDE_LENGTH * Math.cos((orientation[0]));

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy){}
    };


    private SensorEventListener stepListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {

            location.x += dx;
            location.y -= dy;

            //drawLocation(xLocation * 10, yLocation * 10, 1);

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
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

            int x = (int)event.getX();
            int y = (int)event.getY();

            int[] xy = new int[2];
            v.getLocationOnScreen(xy);

            // off set the ratio
            location.x = (x - xy[0])/DRAW_RATIO;
            location.y = y/DRAW_RATIO ;

            drawLocation(location.x, location.y, 2);

            return false;
        }
    };

    public void drawLocation(double x, double y, double floor){

        if (floor == 0){
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.omaha, null));
        }
        else if (floor == 1){
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.one, null));
        }
        else if (floor == 3){
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.three, null));
        }
        else if (floor == 4){
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.four, null));
        }
        else  {
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.two, null));
        }
        // Don't need to change canvas
        bitmap = Bitmap.createBitmap(ivBackground.getWidth(), ivBackground.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.RED);

        // draw the circle with 117.5 scale ratio

        for (AccessPoint ap : db.getAccessPoints()) {
            if(ap.getZ() == floor) canvas.drawCircle((int)(ap.getX()*DRAW_RATIO), (int)(ap.getY()*DRAW_RATIO), 10, paint);
        }


        ivOverlay.setImageBitmap(bitmap);

        //oldz = z;

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
            //wifiManager.setTdlsEnabledWithMacAddress();

            scanResults = wifiManager.getScanResults();

            //clear visiting APs

            System.out.println("HELLO");

            /*List<String> seenBSSIDs = new ArrayList<>();
            for (ScanResult sr : scanResults) {

                if (sr.SSID.contains("victoria") && !db.getAccessPoints().containsKey(sr.BSSID) && sr.frequency < 4000) {
                    //desc.append(sr.BSSID + "\n");
                    //level.append(sr.level + "\n");
                }

                if(db.getAccessPoints().containsKey(sr.BSSID)) {
                    AccessPoint ap = db.getAccessPoints().get(sr.BSSID);
                    ap.readings.add(sr.level);
                    ap.distance = calculateDistance(ap);
                    ap.timeout = 0;

                    // sort by the *closest* access points, based on their distance
                    currentAPs.add(ap);
                    Collections.sort(currentAPs, new Comparator<AccessPoint>() {
                        @Override
                        public int compare(AccessPoint lhs, AccessPoint rhs) {
                            if(lhs.distance > rhs.distance) return -1;
                            else if (lhs.distance < rhs.distance) return 1;
                            else return 0;
                        }
                    });
                }

                System.out.println(sr.SSID + " " + convertRssiToM(sr.level));


                // remove an access point if it hasnt been seen in the last 3 scans
                seenBSSIDs.add(sr.BSSID);
                for (int i = 0; i < currentAPs.size(); i++){

                    if(!seenBSSIDs.contains(currentAPs.get(i).getBssid()) && currentAPs.contains(sr.BSSID)){
                        currentAPs.get(i).timeout += 1;

                        if(currentAPs.get(i).timeout == 3){
                            currentAPs.remove(i);
                        }
                    }
                }
            }*/



            // need 3 APs
            if (currentAPs.size() >=3) {

                Location tempLocation = calculateWifiLocation();

                dist.setText(location.x + " " + location.y + " " + location.z + "\n");

                // set and draw the location
                location.x = tempLocation.x;
                location.y = tempLocation.y;

                drawLocation(location.x, location.y, location.z);

            }


            // z for omaha
            //drawLocation(4, 4, 1);

            //wifiManager.startScan();

        }
    };

    private Location calculateWifiLocation() {

        // Uses the up to 5 of the closest Access Points
        int size = Math.min(currentAPs.size(), 5);

        double[] distances = new double[size];
        double[][] positions = new double[size][2];

        // only x and y
        for (int i = 0; i < size; i++){
            distances[i] = currentAPs.get(i).averageDistance;
            positions[i][0] = currentAPs.get(i).getX();
            positions[i][1] = currentAPs.get(i).getY();
            //positions[i][2] = currentAPs.get(i).getZ();
        }

        NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
        LeastSquaresOptimizer.Optimum optimum = solver.solve();

        // the answer
        double[] centroid = optimum.getPoint().toArray();

        // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
        RealVector standardDeviation = optimum.getSigma(0);
        RealMatrix covarianceMatrix = optimum.getCovariances(0);

        return new Location(centroid[0], centroid[1]);
        //return new Location(200,200);

    }

    // -34dbm right next to co228

    /*private double calculateDistance(AccessPoint ap){

        // get the 3 most recent values if there are more than three

        int averageLevel = 0;
        int count = 3;
        for(int i = ap.readings.size()-1; i > 0; i--){
            if (count == 0) break;
            averageLevel += ap.readings.get(i);
            count--;
        }

        averageLevel = averageLevel / ap.readings.size();

        return convertRssiToM(averageLevel, ap.getZ() == location.z);
    }*/

    private double convertRssiToM(double RSSI, boolean sameFloor){
        if(sameFloor) {
            return Math.pow(10, ((RSSI+BASE_LEVEL) / - 10 * SAME_FLOOR_EXP));
        }
        else {
            return Math.pow(10, ((RSSI+BASE_LEVEL) / - 10 * DIFF_FLOOR_EXP));

        }
    }

    private int calculateWifiZ(){

        int[] floorCount = {0,0,0,0,0};

        // get the closest three APs
        for(int i = 0; i < currentAPs.size() && i < 3; i++){
            floorCount[currentAPs.get(i).getZ()-1]++;
        }

        int maxFloor = 0;

        for (int i = 0; i < floorCount.length; i++){
            if(floorCount[i] > maxFloor){
                maxFloor = floorCount[i];
            }
        }

        return maxFloor+1;

    }

}

