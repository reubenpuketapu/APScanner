package com.example.reubenpuketapu.apscanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
    private TextView desc;
    private TextView level;
    private TextView dist;
    private ImageView image;

    private Canvas canvas;
    private ImageView ivOverlay;
    private ImageView ivBackground;

    private WifiManager wifiManager;
    private SensorManager sensorManager;

    private List<ScanResult> scanResults;
    private Database db;

    private Sensor gyroSensor;
    private Sensor accelSensor;

    private Bitmap bitmap;

    private List<AccessPoint> currentAPs = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_main);

        db = new Database();

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(clickListener);

        ivBackground = (ImageView)findViewById(R.id.iv_background);
        ivOverlay = (ImageView)findViewById(R.id.iv_overlay);
        ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.two, null));

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//


    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            //wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            //registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            //wifiManager.startScan();

            //ivBackground.setImageDrawable(getResources().getDrawable(R.drawable.one, null));


        }
    };

    private void drawLocation(int x, int y, int z){

        if (z == 1){
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

        bitmap = Bitmap.createBitmap(ivBackground.getWidth(), ivBackground.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawCircle(x, y, 10, paint);

        ivOverlay.setImageBitmap(bitmap);

    }

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //wifiManager.setTdlsEnabledWithMacAddress();
            System.out.println(wifiManager.getConnectionInfo());
            scanResults = wifiManager.getScanResults();
//            desc.setText("");
//            level.setText("");

            //clear visiting APs

            for (ScanResult sr : scanResults) {

                if (sr.SSID.contains("victoria") && !db.getAccessPoints().containsKey(sr.BSSID) && sr.frequency < 4000) {
                    //desc.append(sr.BSSID + "\n");
                    //level.append(sr.level + "\n");
                }

                if (db.getAccessPoints().containsKey(sr.BSSID)) {
                    System.out.println("HESY");
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
                }

            }
            // need 3 APs
            if (currentAPs.size() >=3) {

                Location location = calculateLocation();

                dist.setText(location.x + " " + location.y + " " + location.z + "\n");

                //draw the location
                //drawLocation((int)location.x, (int)location.y, (int)location.z);


            }

            drawLocation((int)(200*1.125), (int)(200*1.125), 1);

        }
    };

    private Location calculateLocation() {

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

        double[][] positions = new double[][] { {currentAPs.get(0).getX(), currentAPs.get(0).getY()}, {currentAPs.get(1).getY(), currentAPs.get(1).getY()}, {currentAPs.get(2).getY(), currentAPs.get(2).getY()} };
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

        double RSSI = averageLevel +  34;

        double total = Math.pow(10, (RSSI / -35));

        //double distance = Math.pow(10, (-px - 20* ( Math.log((4*Math.PI)/0.125 )) )/40 );
        return total;
    }




}
