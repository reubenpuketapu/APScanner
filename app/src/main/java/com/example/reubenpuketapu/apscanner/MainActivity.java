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

    private static double DEG_OFFSET = 8;
    private static double STRIDE_LENGTH = 0.72625;

    private float[] orientation = new float[3];
    private float[] r = new float[9];
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];

    private double xLocation = 30;
    private double yLocation = 22;

    private double dx;
    private double dy;
    private double z;

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
            xLocation+= dx;
            yLocation-= dy;

            drawLocation(xLocation * 10, yLocation * 10, 1);

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

            xLocation = (x - xy[0])/10;
            yLocation = y/10 ;//+ xy[1];

            drawLocation(xLocation*10, yLocation*10, 1);

            return false;
        }
    };

    public void drawLocation(double x, double y, double z){
        if (z == oldz){
            // Don't change the background because it is the same as last iteration
        }
        else if (z == 0){
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.omaha, null));
        }
        else if (z == 1){
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.one, null));
        }
        else if (z == 3){
            ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.three, null));
        }
        else if (z == 4){
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
        canvas.drawCircle((int)x, (int)y, 10, paint);

        ivOverlay.setImageBitmap(bitmap);

        oldz = z;

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

            for (ScanResult sr : scanResults) {

                if (sr.SSID.contains("victoria") && !db.getAccessPoints().containsKey(sr.BSSID) && sr.frequency < 4000) {
                    //desc.append(sr.BSSID + "\n");
                    //level.append(sr.level + "\n");
                }

                /*if (db.getAccessPoints().containsKey(sr.BSSID)) {
                    AccessPoint ap = db.getAccessPoints().get(sr.BSSID);
                    ap.readings.add(sr.level);
                    ap.distance = calculateDistance(ap);

                    //desc.append(ap.getDesc() + "\n");
                    //level.append(ap.distance + "\n");

                    currentAPs.add(ap);
                    Collections.sort(currentAPs, new Comparator<AccessPoint>() {
                        @Override
                        public int compare(AccessPoint lhs, AccessPoint rhs) {
                            if(lhs.distance > rhs.distance) return -1;
                            else if (lhs.distance < rhs.distance) return 1;
                            else return 0;
                        }
                    });
                }*/

                System.out.println(sr.SSID + " " + convertRssiToM(sr.level));

            }

            // need 3 APs
            if (currentAPs.size() >=3) {

                Location location = calculateWifiLocation();

                dist.setText(location.x + " " + location.y + " " + location.z + "\n");

                //draw the location
                //drawLocation((int)location.x, (int)location.y, (int)location.z);

            }


            // z for omaha
            drawLocation((int)(200*1.125), (int)(200*1.125), 0);

        }
    };

    private Location calculateWifiLocation() {

        // Uses the three / 4 closest Access Points

        double d0 = currentAPs.get(0).distance;
        double d1 = currentAPs.get(1).distance;
        double d2 = currentAPs.get(2).distance;
        double d3 = currentAPs.get(3).distance;
        /*
        double rhs0 = Math.sqrt((d0 -Math.pow(currentAPs.get(0).getX(), 2) - Math.pow(currentAPs.get(0).getY(), 2) - Math.pow(currentAPs.get(0).getZ(), 2)));
        double rhs1 = Math.sqrt((d1 -Math.pow(currentAPs.get(1).getX(), 2) - Math.pow(currentAPs.get(1).getY(), 2) - Math.pow(currentAPs.get(1).getZ(), 2)));
        double rhs2 = Math.sqrt((d2 -Math.pow(currentAPs.get(2).getX(), 2) - Math.pow(currentAPs.get(2).getY(), 2) - Math.pow(currentAPs.get(2).getZ(), 2)));

        System.out.println("d:" + rhs0 + " " + rhs1 + " " + rhs2);

        double[][] lhs = {{-1,-1,-1}, {-1,-1,-1}, {-1,-1,-1}};
        double[] rhs = {rhs0, rhs1, rhs2};

        Matrix left = new Matrix(lhs);
        Matrix right = new Matrix(rhs, 3);

        Matrix ans = left.solve(right);*/

        double[][] positions = new double[][] { {currentAPs.get(0).getX(), currentAPs.get(0).getY()}, {currentAPs.get(1).getX(), currentAPs.get(1).getY()}, {currentAPs.get(2).getX(), currentAPs.get(2).getY()} };
        double[] distances = new double[] { d0, d1, d2 };

        NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
        LeastSquaresOptimizer.Optimum optimum = solver.solve();

        // the answer
        double[] centroid = optimum.getPoint().toArray();

        // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
        RealVector standardDeviation = optimum.getSigma(0);
        RealMatrix covarianceMatrix = optimum.getCovariances(0);

//        double x = (1 - Math.pow(d2, 2) + Math.pow(d0, 2) )/2;
//        double y = (1 - Math.pow(d1, 2) + Math.pow(d0, 2) )/2;
//        double z = 2*x + 2*y;

        //return new Location(centroid[0], centroid[1]);
        return new Location(200,200);

    }

    // -34dbm right next to co228

    private double calculateDistance(AccessPoint ap){

        int averageLevel = 0;

        for(Integer d : ap.readings){
            averageLevel += d;
        }

        averageLevel = averageLevel / ap.readings.size();

        //double distance = Math.pow(10, (-px - 20* ( Math.log((4*Math.PI)/0.125 )) )/40 );
        return convertRssiToM(averageLevel);
    }

    private double convertRssiToM(double RSSI){

        return Math.pow(10, ((RSSI+34) / -35));

    }

}
