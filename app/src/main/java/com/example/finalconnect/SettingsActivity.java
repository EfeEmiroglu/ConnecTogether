package com.example.finalconnect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    Button buttonSave;
    Switch statusSwitch, statusSwitch2;
    String statusText;
    TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        statusText = intent.getStringExtra("statusText");

        initialWork();

        if(statusText.equals("Left")){
            statusSwitch.setChecked(true);
            statusView.setText("Left");
        }else if(statusText.equals("Right")){
            statusSwitch2.setChecked(true);
            statusView.setText("Right");
        }

        statusSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusSwitch2.setChecked(false);
                statusView.setText("Left");
            }
        });

        statusSwitch2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusSwitch.setChecked(false);
                statusView.setText("Right");
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(statusSwitch.isChecked()){
                    Intent returnIntent = new Intent();
                    statusSwitch2.setChecked(false);
                    returnIntent.putExtra("statusText", statusView.getText());
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }
                if(statusSwitch2.isChecked()){
                    Intent returnIntent = new Intent();
                    statusSwitch.setChecked(false);
                    returnIntent.putExtra("statusText", statusView.getText());
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }
            }
        });
    }

    private void initialWork() {
        buttonSave = findViewById(R.id.buttonSave);
        statusSwitch = findViewById(R.id.statusSwitch);
        statusSwitch2 = findViewById(R.id.statusSwitch2);
        statusView = findViewById(R.id.statusView);
    }
}
