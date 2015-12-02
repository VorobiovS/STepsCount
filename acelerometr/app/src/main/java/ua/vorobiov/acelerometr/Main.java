package ua.vorobiov.acelerometr;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.util.Arrays;

public class Main extends AppCompatActivity {

    TextView tvText;
    SensorManager sensorManager;
    Sensor sensorAccel;
    StringBuilder sb = new StringBuilder();
    int steps=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tvText = (TextView) findViewById(R.id.tvText);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listener);
    }

    String format(float values, float values1, float values2) {
        return String.format("%1$.1f\t\t%2$.1f\t\t%3$.1f", values, values1, values2);
    }

    int i=0;

    float[] valuesMotionX=new float[301];
    float[] valuesMotionY=new float[301];
    float[] valuesMotionZ=new float[301];
    float[] valuesVector=new float[301];
    float[] sortVector=new float[301];

    void showInfo() {
        //tvText.setText(Integer.toString(i));
        valuesMotionX[i]=valuesAccelMotion[0];
        valuesMotionY[i]=valuesAccelMotion[1];
        valuesMotionZ[i]=valuesAccelMotion[2];
        if(i>0)
        valuesVector[i]=(float)Math.sqrt((valuesMotionX[i]-valuesMotionX[i-1])*(valuesMotionX[i]-valuesMotionX[i-1])
                +(valuesMotionY[i]-valuesMotionY[i-1])*(valuesMotionY[i]-valuesMotionY[i-1])+(valuesMotionZ[i]-valuesMotionZ[i-1])*(valuesMotionZ[i]-valuesMotionZ[i-1]));
        if(i==299) {
            steps+=stepsCount();
            sb.setLength(0);
            sb.append(Integer.toString(steps));
            tvText.setText(sb);
            i=0;
        }
        i++;
    }
    //stdev
    public float stdev(float arr[]) {
        float powerSum1 = 0;
        float powerSum2 = 0;
        float sttdev = 0;

        for (int i = 1; i < arr.length; i++) {
            powerSum1 += arr[i];
            powerSum2 += Math.pow(arr[i], 2);
            sttdev = (float)Math.sqrt(i * powerSum2 - Math.pow(powerSum1, 2)) / i;
        }
        return sttdev;
    }

    boolean[] boolArr = new boolean[301];


    public void setBoolArr(){
        float std=stdev(valuesVector);
        if(std<=1)
            std=100;
        for(int i=0; i<valuesVector.length; i++) {
            boolArr[i] = valuesVector[i] > std;
        }
    }

    public int stepsCount(){
        setBoolArr();
        int steps=0;
        for(int i=0; i<boolArr.length; i++){
            //steps+=(boolArr[i] && i<boolArr.length - 1 && !boolArr[i + 1]?(i < boolArr.length - 2?(!boolArr[i + 2]?1:0):1):0);
            if(boolArr[i] && i<boolArr.length-1 && !boolArr[i+1]) {
                if (i < boolArr.length - 2){
                    if (!boolArr[i + 2]) {
                        steps++;
                    }
                }else steps++;}
        }
        return steps;
    }

    float[] valuesAccel = new float[3];
    float[] valuesAccelMotion = new float[3];
    float[] valuesAccelGravity = new float[3];

    SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
                    for (int i = 0; i < 3; i++) {
                        valuesAccel[i] = event.values[i];
                        valuesAccelGravity[i] = (float) (0.1 * event.values[i] + 0.9 * valuesAccelGravity[i]);
                        valuesAccelMotion[i] = event.values[i] - valuesAccelGravity[i];
                    }
            if(i<=299)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showInfo();
                }
            });
        }
    };
    int pos;
    public void nextValues(View view){
        onPause();
    }
}