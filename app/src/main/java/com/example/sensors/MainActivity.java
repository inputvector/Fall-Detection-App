package com.example.sensors;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.StrictMode;
import android.view.View;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.sensors.ml.ModelTf;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.location.Location;
import android.location.LocationManager;


public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    ModelTf model;

    //Sensor
    TextView accX;
    TextView accY;
    TextView accZ;
    TextView gyroX;
    TextView gyroY;
    TextView gyroZ;

    //Location
    TextView longitude_textView;
    TextView latitude_textView;
    LocationManager locationManager;
    String latitude_str, longitude_str;

    FloatingActionButton dataViewbtn;

    FloatingActionButton smsViewbtn;

    HashMap<String, String> DataToSend = new HashMap<String, String>();
    //Button buttonSend;

    TextView result_text;
    float[] new_data = new float[6];
    String current_activity = "";
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");


    DBHelperClass dbHelper;
    public static final String PASSED_DATA ="com.example.passdata_betweenactivities.PASSED_DATA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (android.os.Build.VERSION.SDK_INT > 27) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accX= findViewById(R.id.AccX);
        accY= findViewById(R.id.AccY);
        accZ= findViewById(R.id.AccZ);

        gyroX= findViewById(R.id.GyroX);
        gyroY= findViewById(R.id.GyroY);
        gyroZ= findViewById(R.id.GyroZ);

        dataViewbtn = findViewById(R.id.dbview);
        smsViewbtn = findViewById(R.id.smsview);

        //buttonSend = findViewById(R.id.buttonSend);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        int accelerometer = Sensor.TYPE_ACCELEROMETER;
        int gyroscope = Sensor.TYPE_GYROSCOPE;
        sensorManager.registerListener(sensorListener,
                sensorManager.getDefaultSensor(accelerometer),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener,
                sensorManager.getDefaultSensor(gyroscope),
                SensorManager.SENSOR_DELAY_NORMAL);


        //////////////// ML

        result_text = findViewById(R.id.Prediction);
            try {
                model = ModelTf.newInstance(getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
            }

        //////////////// DB
        dbHelper = new DBHelperClass(this);

        dataViewbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDataActivity();
            }
        });

        smsViewbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openSmsActivity();
            }
        });




        //////////////// LOCATION
        getLocation();



    }

    public void sendSensorData(String dataToSend){
        Socket socket = null;
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
        try {
            socket = new Socket("192.168.86.46", 8888);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream.writeUTF(dataToSend);
            Toast.makeText(MainActivity.this,dataInputStream.readUTF(), Toast.LENGTH_LONG).show();


        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally{
            if (socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }}

        if (dataOutputStream != null){
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (dataInputStream != null){
            try {
                dataInputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public SensorEventListener sensorListener = new SensorEventListener(){
        public void onSensorChanged(SensorEvent sensorEvent){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];
                accX.setText(String.valueOf(X_lateral));
                accY.setText(String.valueOf(Y_longitudinal));
                accZ.setText(String.valueOf(Z_vertical));
                new_data[0] = X_lateral;
                new_data[1] = Y_longitudinal;
                new_data[2] = Z_vertical;

            }else if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE){

                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];
                gyroX.setText(String.valueOf(X_lateral));
                gyroY.setText(String.valueOf(Y_longitudinal));
                gyroZ.setText(String.valueOf(Z_vertical));
                new_data[3] = X_lateral;
                new_data[4] = Y_longitudinal;
                new_data[5] = Z_vertical;

            }

            /////////////////// DB
            save_to_db(new_data);

            //////////////////// ML
            String res = make_predictions(new_data);
            result_text.setText(res);
            if(res.equals("FALL") & !res.equals(current_activity)){
                current_activity = res;
                Date date = new Date();
                sendSensorData( "Time: "+formatter.format(date)+ " Alert: FALL");
                Toast.makeText(MainActivity.this,"Results are being send to the server", Toast.LENGTH_LONG).show();
            }else{
                current_activity = res;
            }
        }
        public void onAccuracyChanged(Sensor sensor , int accuracy){
        }
    };

    @Override
    public void onResume(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }
    @Override
    public void onPause(){
        sensorManager.unregisterListener(sensorListener);
        super.onPause();
    }

    public String make_predictions(float[] input){
        float result = 0f;
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 6}, DataType.FLOAT32);
        ByteBuffer byteBuffer = ByteBuffer.allocate(6);
        inputFeature0.loadArray(input);
        // Runs model inference and gets result.
        ModelTf.Outputs outputs = model.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
        result= outputFeature0.getFloatValue(0) ;

        // Releases model resources if no longer used.
        //model.close();

        if (result < 0.5) {
            return "WALKING";
        }else{
            return "FALL";
        }

    }

    public void save_to_db(float[] input){

        dbHelper.addData(input);
    }

    public void  openDataActivity(){

        Intent intent = new Intent(this, DBActivity.class);
        startActivity(intent);


    }

    public void  openSmsActivity(){

        String sms_message = "Action : " +  make_predictions(new_data) +",  "+
                             "Longitude : " + longitude_textView.getText().toString() +",  "+
                             "Latitude : " + latitude_textView.getText().toString();

        Intent intent = new Intent(this, SmsActivity.class);
        intent.putExtra( PASSED_DATA, sms_message);


        startActivity(intent);


    }

   public void getLocation() {
        latitude_textView = (TextView) findViewById(R.id.Latitude);
        longitude_textView = (TextView) findViewById(R.id.Longitude);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new
                    String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new
                LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        longitude_str = String.format("%.6f",location.getLongitude());
                        latitude_str  = String.format("%.6f",location.getLatitude());
                        latitude_textView.setText(latitude_str);
                        longitude_textView.setText(longitude_str);
                    }
                });
    }
}
