package com.silver.bet365leader;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editEndpoint;
    EditText editUsername;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        btnSave = findViewById(R.id.btnSave);
        editEndpoint = findViewById(R.id.editEndpoint);
        editUsername = findViewById(R.id.editUsername);


        AppSharedInfo appSharedInfo = new AppSharedInfo(this);
        editEndpoint.setText(appSharedInfo.getEndpoint());
        editUsername.setText(appSharedInfo.getUsername());

        btnSave.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnSave) {
            // Save endpoint and username

            String strEndpoint = editEndpoint.getText().toString();
            String strUsername = editUsername.getText().toString();

            AppSharedInfo appSharedInfo = new AppSharedInfo(this);
            appSharedInfo.saveEndpoint(strEndpoint);
            appSharedInfo.saveUsername(strUsername);

            Intent returnIntent = new Intent();
            returnIntent.putExtra("Endpoint", strEndpoint);
            returnIntent.putExtra("Username", strUsername);
            setResult(Activity.RESULT_OK, returnIntent);

            finish();
        }
    }
}