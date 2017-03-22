package lab2_201_01.uwaterloo.ca.lab4_201_01;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Timer;

import static lab2_201_01.uwaterloo.ca.lab4_201_01.GameLoopTask.TheScore;


public class Lab4_201_01 extends AppCompatActivity {

    public static TextView[] Movement = new TextView[1];//Textview to display Movement Pattern
    //Static object for handing Accelerometer values
    public static SensorSuper Values = new SensorSuper(20);
    public static Timer gameTimer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab4_201_01);

        //Create an instance of Relative layout
        final RelativeLayout MainLayout = (RelativeLayout) findViewById(R.id.RelativeLayout);

        //SetBackground of game to be the 2048 main board and tweaking Relative layout params
        MainLayout.setBackgroundResource(R.drawable.testboard);
        final int width = 1080;
        final int height = 1623;
        MainLayout.getLayoutParams().width = width;
        MainLayout.getLayoutParams().height = height;

        //Set Textview To show direction of movement to screen
        DisplayText(Movement, MainLayout, "");
        Movement[0].setGravity(Gravity.CENTER);
        Movement[0].setTextSize(50);
        Movement[0].setTextColor(Color.GREEN);
        Movement[0].setVisibility(View.INVISIBLE);
        //Start sensor
        StartSensors();

        //Create Button handler to trigger restart game
        Button Restart = new Button(getApplicationContext());
        Restart.setX(700);
        Restart.setY(1450);
        String temp = " Restart Game ";
        Restart.setText(temp);
        Restart.setTextColor(Color.WHITE);
        Restart.setBackgroundColor(Color.GRAY);
        MainLayout.addView(Restart);
        Restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                Lab4_201_01.this.finish();
                startActivity(intent);
                Game(MainLayout);
                TheScore = 0;

            }
        });
        //Begin Game handler
        Game(MainLayout);
    }

    protected void Game(RelativeLayout MainLayout){
        //Create GameLoop handler that handles actions based on FSM input
        //Set FrameRate Refresh rate
        int FrameRate = 10; //~100 frames a second
        //Create instance of Main Class to create new UI thread handled by the GameLoopTask
        Lab4_201_01 lab4_201_01 = new Lab4_201_01();
        GameLoopTask gameLoopTask = new GameLoopTask(lab4_201_01, getApplicationContext(), MainLayout);
        //Create a periodic timer that will execute the method run un the gameLoopTask class
        gameTimer.schedule(gameLoopTask, FrameRate, FrameRate);
    }

    //If the app stops kill the loop to factory reset the entire game
    @Override
    protected void onRestart(){
        super.onRestart();
    }

    //This method takes an array of Textviews and displays it to the MainLayout Screen
    public void DisplayText(TextView[] ScreenText, RelativeLayout MainLayout, String Text){
        for (int i = 0; i < ScreenText.length; i++){
            ScreenText[i] = new TextView(this);
            ScreenText[i].setTextColor(Color.WHITE);
            MainLayout.addView(ScreenText[i]);
            MainLayout.bringChildToFront(ScreenText[i]);

            if (Text != null){
                ScreenText[i].setText(Text);
                ScreenText[i].setGravity(Gravity.CENTER);
            }
        }
    }
    //Setup sensor
    protected void StartSensors(){
        //Setting up the accelerometer Sensor
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        SensorEventListener A = new AccelerometerSensorEventListener(Movement);
        sensorManager.registerListener(A, Accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

}
