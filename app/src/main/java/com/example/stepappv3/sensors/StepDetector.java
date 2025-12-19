package com.example.stepappv3.sensors;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

public class StepDetector implements SensorEventListener {
    public interface OnStepDetectedListener{
        void onStepDetected();
    }

    private final SensorManager sensorManager;
    private final Sensor stepDetectorSensor;
    private final Sensor accelerometerSensor;
    private OnStepDetectedListener listener;
    private static final int STEP_DELAY_NS = 250_000_000;
    private static final float STEP_THRESHOLD = 12f;
    private long lastStepTimeNs = 0;
    private float lastMagnitude = 0;
    private boolean isPeak = false;

    public StepDetector(Context context){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void setOnStepDetectedListener(OnStepDetectedListener listener) {
        this.listener = listener;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (listener != null) {
                listener.onStepDetected();
            }
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTimeNs = event.timestamp;

            if (currentTimeNs - lastStepTimeNs < STEP_DELAY_NS) {
                return;
            }

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float currentMagnitude = (float) Math.sqrt(x * x + y * y + z * z);


            if (lastMagnitude > STEP_THRESHOLD && currentMagnitude < STEP_THRESHOLD) {

                if (listener != null) {
                    lastStepTimeNs = currentTimeNs;
                    listener.onStepDetected();
                }
            }
            lastMagnitude = currentMagnitude;
        }
    }


    public void start(){
        if (stepDetectorSensor != null){
            sensorManager.registerListener(this,stepDetectorSensor,SensorManager.SENSOR_DELAY_NORMAL);
        } else if (accelerometerSensor != null){
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }
    public void stop(){
        if (stepDetectorSensor != null){
            sensorManager.unregisterListener(this);
        }
    }

}
