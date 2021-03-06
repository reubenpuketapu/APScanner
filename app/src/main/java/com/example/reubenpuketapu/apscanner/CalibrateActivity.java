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
import android.widget.EditText;
import android.widget.TextView;

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
    private TextView text;
    private EditText manual;

    private SensorManager sensorManager;
    private Sensor pressSensor;

    private ArrayList<Double> currentValues = new ArrayList<>();

    private ArrayList<String> averageValues= new ArrayList<>();

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

        text = (TextView) findViewById(R.id.calibrateText);
        manual = (EditText) findViewById(R.id.editText);

        averageValues.add("");
        averageValues.add("");
        averageValues.add("");
        averageValues.add("");
        averageValues.add("");

    }

    private void startActivity(){
        ArrayList<CharSequence> cs = convertStringArray();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putCharSequenceArrayListExtra("calibrate", cs);
        startActivity(intent);
    }

    private ArrayList<CharSequence> convertStringArray() {

        ArrayList<CharSequence> cs = new ArrayList<>();

        for (String s : averageValues){
            cs.add(s);
        }

        return cs;

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

    // For all of these collect the most recent 10 values of pressure and set it to that index of the arrayList

    private View.OnClickListener oneListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            List<Double> list = currentValues.subList(currentValues.size()-11, currentValues.size()-1);

            Double average = 0.0;

            for (int i = 0; i < 10; i++){
                average+= list.get(i);
            }
            average /= list.size();

            if (manual.getText().length() != 0){
                averageValues.set(0, manual.getText().toString());
            }else{
                averageValues.set(0, average.toString());
            }

            System.out.println("value: " + averageValues.get(0));
            System.out.println("cvalue: " + currentValue);

        }
    };

    private View.OnClickListener twoListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            List<Double> list = currentValues.subList(currentValues.size()-11, currentValues.size()-1);

            Double average = 0.0;

            for (int i = 0; i < 10; i++){
                average+= list.get(i);
            }
            average /= list.size();

            if (manual.getText().length() != 0){
                averageValues.set(1, manual.getText().toString());
            }else{
                averageValues.set(1, average.toString());
            }

            System.out.println("value: " + averageValues.get(1));
            System.out.println("cvalue: " + currentValue);


        }
    };
    private View.OnClickListener threeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            List<Double> list = currentValues.subList(currentValues.size()-11, currentValues.size()-1);

            Double average = 0.0;

            for (int i = 0; i < 10; i++){
                average+= list.get(i);
            }
            average /= list.size();

            if (manual.getText().length() != 0){
                averageValues.set(2, manual.getText().toString());
            }else{
                averageValues.set(2, average.toString());
            }

            System.out.println("value: " + averageValues.get(2));
            System.out.println("cvalue: " + currentValue);


        }
    };
    private View.OnClickListener fourListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            List<Double> list = currentValues.subList(currentValues.size()-11, currentValues.size()-1);

            Double average = 0.0;

            for (int i = 0; i < 10; i++){
                average+= list.get(i);
            }
            average /= list.size();

            if (manual.getText().length() != 0){
                averageValues.set(3, manual.getText().toString());
            }else{
                averageValues.set(3, average.toString());
            }

            System.out.println("value: " + averageValues.get(3));
            System.out.println("cvalue: " + currentValue);


        }
    };
    private View.OnClickListener fiveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            List<Double> list = currentValues.subList(currentValues.size()-11, currentValues.size()-1);

            Double average = 0.0;

            for (int i = 0; i < 10; i++){
                average+= list.get(i);
            }
            average /= list.size();

            if (manual.getText().length() != 0){
                averageValues.set(4, manual.getText().toString());
            }else{
                averageValues.set(4, average.toString());
            }

            System.out.println("value: " + averageValues.get(4));
            System.out.println("cvalue: " + currentValue);

            text.setText(averageValues.toString());


        }
    };








}
