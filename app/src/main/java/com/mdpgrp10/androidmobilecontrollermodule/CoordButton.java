package com.mdpgrp10.androidmobilecontrollermodule;

/**
 * Created by Glambert on 25/2/2016.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.EditText;



public class CoordButton extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coord_button);
        setResult(Activity.RESULT_CANCELED);

        Bundle bundle = getIntent().getExtras();

        EditText robot_x_text = (EditText) findViewById(R.id.tb_robot_x);
        EditText robot_y_text = (EditText) findViewById(R.id.tb_robot_y);
        if (bundle.getString("robot_x_text") != null) {
            robot_x_text.setText(bundle.getString("robot_x_text"));
        }
        if (bundle.getString("robot_y_text") != null) {
            robot_y_text.setText(bundle.getString("robot_y_text"));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.coord_button, menu);
        return true;
    }

    public void button_save(View v) {
        setResult(-1, new Intent().putExtra("robot_x_text", ((EditText) findViewById(R.id.tb_robot_x)).getText().toString()).putExtra("robot_y_text", ((EditText) findViewById(R.id.tb_robot_y)).getText().toString()));
        finish();
    }

    public void button_cancel(View v) {
        finish();
    }
}
