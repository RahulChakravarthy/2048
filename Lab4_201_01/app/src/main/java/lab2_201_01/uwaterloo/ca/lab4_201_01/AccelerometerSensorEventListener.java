package lab2_201_01.uwaterloo.ca.lab4_201_01;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

class AccelerometerSensorEventListener extends Lab4_201_01 implements SensorEventListener{

    TextView[] Movement = new TextView[1];

    public AccelerometerSensorEventListener(TextView[] Movement) {
        this.Movement = Movement;
    }
    public void onAccuracyChanged(Sensor s, int i) {}
    public void onSensorChanged(SensorEvent se) {
        //Remove Values that surpass the appropriate index
        Values.removeLast();
        //Store data after applying LPF
        //Store All accelerometer component values into a local list (true = LPF, false = ~LPF)
        Values.addFirst(se.values[0], se.values[1], se.values[2], true);
        //FSM part (Change text on screen based on hand gestures
        this.Movement[0].setText(Values.ReturnMovement());
    }
}
