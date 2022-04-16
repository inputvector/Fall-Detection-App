package com.example.sensors;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class DBActivity extends AppCompatActivity {

    DBHelperClass dbHelper;
    TableLayout tableLayout;
    ListView lw;
    String [] dbData = {};
    FloatingActionButton mainViewbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbactivity);

        setContentView(R.layout.activity_dbactivity);
      //  tableLayout = (TableLayout)findViewById(R.id.tableLayout);
        lw = (ListView)findViewById(R.id.list);
        mainViewbtn = (FloatingActionButton)findViewById(R.id.mainView);


        dbHelper = new DBHelperClass(this);
        populateTable();


        mainViewbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity();
            }
        });

    }

    private void populateTable(){

        Cursor data = dbHelper.getData();
        if(data.getCount() == 0){

            Log.e("DB", "DB is empty");
        }else{
            data.moveToFirst();
            //ArrayList<ArrayList<String>> listData = new ArrayList<ArrayList<String>>();
            ArrayList<String> listData = new ArrayList<String>();
            //listData.add("\t\t\t\t\t\t\t\t\t  SENSOR DATABASE \t\t\t");
            listData.add("SENSOR DATABASE");//String.format("%-" + 100 +"s","SENSOR DATABASE"));
            listData.add("ACC_X \t ACC_Y \t ACC_Z \t GYRO_X \t GYRO_Y \t GYRO_Z");
            while (data.moveToNext()) {

                String temp =
                        Float.toString(data.getFloat(1)) + " \t\t " +
                        Float.toString(data.getFloat(2)) + " \t\t " +
                        Float.toString(data.getFloat(3)) + " \t\t " +
                        Float.toString(data.getFloat(4)) + " \t\t " +
                        Float.toString(data.getFloat(5)) + " \t\t " +
                        Float.toString(data.getFloat(6))  ;
                listData.add(temp);

            }
            data.close();
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    listData );

            lw.setAdapter(arrayAdapter);


        }


    }

    public void  openMainActivity(){

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);


    }

}