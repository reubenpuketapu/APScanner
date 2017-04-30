package com.example.reubenpuketapu.apscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by reubenpuketapu on 1/05/17.
 */

public class CalibrateActivity extends Activity{

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button done;

    private SensorManager sensorManager;
    private Sensor pressSensor;

    private ArrayList<Double> currentValues = new ArrayList<>();

    private ArrayList<Float> averageValues= new ArrayList<>();

    private Double currentValue = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibrate);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        sensorManager.registerListener(pressureListener, pressSensor, SensorManager.SENSOR_DELAY_FASTEST);

        button1 = (Button) findViewById(R.id.button_1);
        button2 = (Button) findViewById(R.id.button_2);
        button3 = (Button) findViewById(R.id.button_3);
        button4 = (Button) findViewById(R.id.button_4);
        button5 = (Button) findViewById(R.id.button_5);
        done = (Button) findViewById(R.id.done);


        button1.setOnClickListener(oneListener);
        button2.setOnClickListener(twoListener);
        button3.setOnClickListener(threeListener);
        button4.setOnClickListener(fourListener);
        button5.setOnClickListener(fiveListener);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity();
            }
        });

        averageValues.add(0.0f);
        averageValues.add(0.0f);
        averageValues.add(0.0f);
        averageValues.add(0.0f);
        averageValues.add(0.0f);

    }

    private void startActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private SensorEventListener pressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            // TODO: need to calculate the values for each level and map them to each
            // TODO: set to z!

            // WORKS FOR HEIGHT!!!!!!!
            currentValue = (double)event.values[0];
            currentValues.add((double)event.values[0]);

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private View.OnClickListener oneListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            List<Double> list = currentValues.subList(currentValues.size()-11, currentValues.size()-1);

            float average = 0;

            for (int i = 0; i < 10; i++){
                average+= list.get(i);
            }

            averageValues.set(0, ((average/list.size())));

            System.out.println("value: " + averageValues.get(0));
            System.out.println("cvalue: " + currentValue);

            SharedPreferences.Editor sp = getSharedPreferences("calibration", MODE_APPEND).edit();

            sp.putFloat("one", average);

        }
    };

    private View.OnClickListener twoListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            List<Double> list = currentValues.subList(currentValues.size()-11, currentValues.size()-1);

            float average = 0;

            for (int i = 0; i < 10; i++){
                average+= list.get(i);
            }

            averageValues.set(1, average/list.size());

            System.out.println("value: " + averageValues.get(1));
            System.out.println("cvalue: " + currentValue);

            SharedPreferences.Editor sp = getSharedPreferences("calibration", MODE_APPEND).edit();

            sp.putFloat("four", average);


        }
    };
    private View.OnClickListener threeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            List<Double> list = currentValues.subList(currentValues.size()-11, currentValues.size()-1);

            float average = 0;

            for (int i = 0; i < 10; i++){
                average+= list.get(i);
            }

            averageValues.set(2, average/list.size());

            System.out.println("value: " + averageValues.get(2));
            System.out.println("cvalue: " + currentValue);

            SharedPreferences.Editor sp = getSharedPreferences("calibration", MODE_APPEND).edit();

            sp.putFloat("three", average);


        }
    };
    private View.OnClickListener fourListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            List<Double> list = currentValues.subList(currentValues.size()-11, currentValues.size()-1);

            float average = 0;

            for (int i = 0; i < 10; i++){
                average+= list.get(i);
            }

            averageValues.set(3, average/list.size());

            System.out.println("value: " + averageValues.get(3));
            System.out.println("cvalue: " + currentValue);

            SharedPreferences.Editor sp = getSharedPreferences("calibration", MODE_APPEND).edit();

            sp.putFloat("four", average);


        }
    };
    private View.OnClickListener fiveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            List<Double> list = currentValues.subList(currentValues.size()-11, currentValues.size()-1);

            float average = 0;

            for (int i = 0; i < 10; i++){
                average+= list.get(i);
            }

            averageValues.set(4, average/list.size());

            System.out.println("value: " + averageValues.get(4));
            System.out.println("cvalue: " + currentValue);

            SharedPreferences.Editor sp = getSharedPreferences("calibration", MODE_APPEND).edit();

            sp.putFloat("five", average);


        }
    };






}
