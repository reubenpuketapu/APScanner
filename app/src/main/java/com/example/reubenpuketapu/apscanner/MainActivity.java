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

import com.example.reubenpuketapu.apscanner.db.Database;
import com.example.reubenpuketapu.apscanner.trilateration.NonLinearLeastSquaresSolver;
import com.example.reubenpuketapu.apscanner.trilateration.TrilaterationFunction;
import com.example.reubenpuketapu.apscanner.wrappers.AccessPoint;
import com.example.reubenpuketapu.apscanner.wrappers.BSSID;
import com.example.reubenpuketapu.apscanner.wrappers.Location;
import com.example.reubenpuketapu.apscanner.wrappers.Reading;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;


public class MainActivity extends AppCompatActivity {

    private Button button;
    private Button reset;
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

    private Database db;

    private TreeSet<AccessPoint> currentAPs;
    private Set<String> currentBSSIDs = new HashSet<>();
    private ArrayList<CharSequence> cs = new ArrayList<>();

    // TODO: find deg offset
    private static final double DEG_OFFSET = -80;
    private static final double STRIDE_LENGTH = 0.70625;

    private static final double DRAW_RATIO = 11.75;

    private static final double SAME_FLOOR_EXP = 3.8;
    private static final double DIFF_FLOOR_EXP = 4.3;
    private static final double BASE_LEVEL = 34;
    private static final double TIMEOUT = 20000;
    private static final int VISIT_APS = 4;

    private float[] orientation = new float[3];
    private float[] r = new float[9];
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];

    // x y location in metres
    private Location wifiLocation = new Location(30, 30, 2);
    private Location stepLocation = new Location(0, 0, 2);

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

        reset = (Button) findViewById(R.id.reset_button);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });


        dist = (TextView) findViewById(R.id.distanceText);

        ivBackground = (ImageView) findViewById(R.id.iv_background);
        ivOverlay = (ImageView) findViewById(R.id.iv_overlay);
        ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.one, null));
        ivBackground.setOnTouchListener(imageListener);

        // WIFI STUFF
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        db = new Database(null);

        Intent intent = getIntent();
        cs  = intent.getCharSequenceArrayListExtra("calibrate");

        db.calibrationValues = new ArrayList<>();
        db.populateCalibrations(cs);

        // SENSOR STUFF
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        pressSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(pressureListener, pressSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gravListener, gravSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(magListener, magSensor, SensorManager.SENSOR_DELAY_FASTEST);

        // Start task every 2 seconds
        Timer timer = new Timer();
        timer.schedule(new RemoveTask(), 0, 1000);

    }

    private void reset(){

        currentAPs.clear();
        currentBSSIDs.clear();
        db = new Database(cs);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
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

            azimuthInDegrees += DEG_OFFSET;

            if (azimuthInDegrees < 0.0f) {
                azimuthInDegrees += 360.0f;
            }

            azimuthInDegrees -= 180;

            if (azimuthInDegrees < 0.0f){
                azimuthInDegrees += 360.0f;
            }

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

            stepLocation.x += dx;
            stepLocation.y -= dy;

            drawLocation();

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private SensorEventListener pressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            double pressure = event.values[0];

            double diff = 100000;
            int index = 0;
            for (int i = 0; i < db.calibrationValues.size(); i ++){

                if (Math.abs(pressure - db.calibrationValues.get(i)) < diff){
                    diff = Math.abs(pressure - db.calibrationValues.get(i) );
                    index = i;
                }
            }

            wifiLocation.z = index+1;
            stepLocation.z = index+1;

            dist.setText(event.values[0]+ " \n");

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
            stepLocation.x = (x - xy[0]) / DRAW_RATIO;
            stepLocation.y = y / DRAW_RATIO;

            drawLocation();

            return false;
        }
    };

    public void drawLocation() {

        if (wifiLocation.z == 1.0) {
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.one, null));
        } else if (wifiLocation.z == 3.0) {
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.three, null));
        } else if (wifiLocation.z == 4.0) {
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.four, null));
        } else if (wifiLocation.z == 5.0) {
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.five, null));
        } else {
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.two, null));
        }

        bitmap = Bitmap.createBitmap(ivBackground.getWidth(), ivBackground.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        Iterator<AccessPoint> iterator = currentAPs.descendingIterator();
        int i = 0;
        while (iterator.hasNext() && i < VISIT_APS){

            AccessPoint ap = iterator.next();

            if(ap.getZ() == wifiLocation.z){
                Paint green = new Paint();
                green.setColor(Color.GREEN);
                canvas.drawCircle((float) (ap.getX() * DRAW_RATIO), (float) (ap.getY() * DRAW_RATIO), 10, green);
                green.setStyle(Paint.Style.STROKE);
                canvas.drawCircle((float) (ap.getX() * DRAW_RATIO), (float) (ap.getY() * DRAW_RATIO), (float) (ap.averageDistance *DRAW_RATIO), green);
                green.setStyle(Paint.Style.FILL);
            }
            else{
                Paint blue = new Paint();
                blue.setColor(Color.MAGENTA);
                canvas.drawCircle((float) (ap.getX() * DRAW_RATIO), (float) (ap.getY() * DRAW_RATIO), 10, blue);
                blue.setStyle(Paint.Style.STROKE);
                canvas.drawCircle((float) (ap.getX() * DRAW_RATIO), (float) (ap.getY() * DRAW_RATIO), (float) (ap.averageDistance * DRAW_RATIO) , blue);
                blue.setStyle(Paint.Style.FILL);

            }

            i++;
        }

        Paint paint = new Paint();
        paint.setColor(Color.RED);

        // draw the circle with 117.5 x-y scale ratio
        canvas.drawCircle((float) (wifiLocation.x * DRAW_RATIO), (float) (wifiLocation.y * DRAW_RATIO), 10, paint);

        paint.setColor(Color.BLUE);
        canvas.drawCircle((float) (stepLocation.x * DRAW_RATIO), (float) (stepLocation.y * DRAW_RATIO), 10, paint);


        ivOverlay.setImageBitmap(bitmap);

    }

    // WIFI EVENTS

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //location.z++;
            wifiManager.startScan();
        }
    };

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            List<ScanResult> scanResults = wifiManager.getScanResults();

            for (ScanResult sr : scanResults) {

                if (db.containsBSSID(sr.BSSID)) {
                    AccessPoint ap = db.getAP(sr.BSSID);
                    ap.timeout = System.currentTimeMillis();

                    BSSID bssid = ap.bssids.get(sr.BSSID);

                    bssid.addReading(sr.level);
                    bssid.setFrequency(sr.frequency);

                    ap.averageDistance = calculateDistance(ap);

                    //dist.setText("distance: " + ap.averageDistance);

                    // sort by the *closest* access points, based on their distance
                    currentAPs.add(ap);

                    //if(ap.getZ() == location.z) {
                        //canvas.drawCircle((float) (ap.getX() * DRAW_RATIO), (float) (ap.getY() * DRAW_RATIO), 10, new Paint(Color.BLUE));
                    //}
                    //}

                }

                currentBSSIDs.add(sr.BSSID);

                // need 3 APs
                if (currentAPs.size() > 3) {

                    try {
                        Location tempLocation = calculateWifiLocation();

                        //dist.setText(tempLocation.x + " " + tempLocation.y + " " + tempLocation.z + "\n");

                        // set and draw the location
                        wifiLocation.x = tempLocation.x;
                        wifiLocation.y = tempLocation.y;
                        wifiLocation.z = (int) Math.round(tempLocation.z);

                        drawLocation();
                    }
                    catch (NullPointerException e){

                    }

                }

                // continuously scan for wifi networks
                wifiManager.startScan();

            }
        }
    };

    private Location calculateWifiLocation() {
        try {
            // Uses the up to 5 of the closest Access Points
            int size = Math.min(currentAPs.size(), VISIT_APS);

            double[] distances = new double[size];
            double[][] positions = new double[size][3];

            // only x and y
            Iterator<AccessPoint> iterator = currentAPs.descendingIterator();

            //AccessPoint[] accessPoints = currentAPs.toArray(new AccessPoint[size]);
            for (int i = 0; i < size && iterator.hasNext(); i++) {
                AccessPoint ap = iterator.next();
                distances[i] = ap.averageDistance;
                positions[i][0] = ap.getX();
                positions[i][1] = ap.getY();
                positions[i][2] = ap.getZ();

            }

            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();

            // the answer
            double[] centroid = optimum.getPoint().toArray();

            // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
//        RealVector standardDeviation = optimum.getSigma(0);
            //      RealMatrix covarianceMatrix = optimum.getCovariances(0);

            return new Location(centroid[0], centroid[1], wifiLocation.z);
            //return new Location(200,200);
        }
        catch (TooManyEvaluationsException e){

        }

        return null;

    }

    private double calculateDistance(AccessPoint ap) {

        double averageDistance = 0;

        for (BSSID bssid : ap.bssids.values()) {

            int individualLevel = 0;
            int divideIterations = Math.min(bssid.getReadings().size(), 10);

            if (!bssid.getReadings().isEmpty() && divideIterations >= 2) {

                // get the 3 most recent values if there are more than three
                /*int iterations = Math.min(bssid.getReadings().size(), 10);
                for (int i = bssid.getReadings().size() - 1; i >= 0 && iterations > 0; i--, iterations--) {
                    individualLevel += bssid.getReadings().get(i);
                }*/

                Integer[] readings = bssid.getIntReadings().toArray(new Integer[bssid.getIntReadings().size()]);

                double median;
                if (readings.length % 2 == 0)
                    median = ((double)readings[readings.length/2] + (double)readings[readings.length/2 - 1])/2;
                else
                    median = (double) readings[readings.length/2];

                // average each distance reading for each BSSID on the Access Point
                individualLevel = individualLevel / divideIterations;

                //System.out.println(ap.getDesc() + " same level: " + (ap.getZ() == location.z));

                averageDistance += (convertRssiToM(median, bssid.getWavelength(), ap.getZ() == wifiLocation.z));

                //averageDistance += (convertRssiToM(individualLevel, bssid.getWavelength(), ap.getZ() == location.z));

            }

           // else return 1000;

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

        /*double a = -0.07363796;
        double b = -2.52218124;
        double n = Math.max(2, a * RSSI + b);

        double FSPL = 20 * Math.log(4.0 * Math.PI / wavelength);
        double distance = Math.pow(10, (RSSI - FSPL)/ 10 * n);

        return  distance;*/
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

            List<AccessPoint> accessPoints = new ArrayList<>(currentAPs);
            List<AccessPoint> removeAPs = new ArrayList<>();

            for(AccessPoint ap : accessPoints){

                if (System.currentTimeMillis() - ap.timeout >= TIMEOUT) {
                    removeAPs.add(ap);
                }

                // iterate through each
                for (BSSID bssid : ap.bssids.values()) {

                    Set<Reading> removeReadings = new HashSet<>();

                    // concurrent modification
                    List<Reading> readings = new ArrayList<>(bssid.getReadings());

                    for (Reading reading : readings) {
                        //System.out.println("DIFF: " + (System.currentTimeMillis() - reading.timestamp));

                        if (System.currentTimeMillis() - reading.timestamp >= TIMEOUT) {
                            removeReadings.add(reading);
                        }
                    }

                    bssid.getReadings().removeAll(removeReadings);

                }



                // remove this access point because it has no current readings
                boolean remove = false;
                for (BSSID bssid : ap.bssids.values()){
                    if(bssid.getReadings().isEmpty()){
                        remove = true;
                    }
                }

                /*if (remove){
                    removeAPs.add(ap);
                    System.out.println("removed: " + ap.getDesc());
                }*/
            }

            currentAPs.removeAll(removeAPs);
        }
    }

}



