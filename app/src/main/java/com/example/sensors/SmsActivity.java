package com.example.sensors;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;

public class SmsActivity extends AppCompatActivity {

    //SMS
    EditText phoneNumber_EditText;
    EditText patientID_EditText;
    Button sendButton;
    SmsManager smsManager;
    String sms_message;
    FloatingActionButton mainViewbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        phoneNumber_EditText  = (EditText) findViewById(R.id.phone_number);
        patientID_EditText = (EditText) findViewById(R.id.patient_id);
        sendButton = (Button) findViewById(R.id.send_sms);
        Intent intent = getIntent();
        sms_message = intent.getStringExtra(MainActivity.PASSED_DATA);
        mainViewbtn = (FloatingActionButton)findViewById(R.id.mainView);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

             //   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ }
                if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){

                    sendSMS();
                }else{

                    requestPermissions(new String[] {Manifest.permission.SEND_SMS},0);
                }


            }
        });

        mainViewbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity();
            }
        });

    }

    void sendSMS() {
        String patient_ID = patientID_EditText.getText().toString();
        String phone = phoneNumber_EditText.getText().toString();

        String final_message = "Patient : "+ patient_ID + ",  " + sms_message;

        try {
            smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, final_message, null, null);

            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.",
                    Toast.LENGTH_LONG).show();
        }


    }

    public void  openMainActivity(){

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);


    }

}