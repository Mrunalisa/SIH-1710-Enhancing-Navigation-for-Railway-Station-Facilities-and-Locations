package com.example.manzil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NavigationSetupActivity extends AppCompatActivity {

    private EditText stationInput, destinationInput;
    private Button scanButton, goButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_setup);

        stationInput = findViewById(R.id.stationInput);
        destinationInput = findViewById(R.id.destinationInput);
        scanButton = findViewById(R.id.scanButton);
        goButton = findViewById(R.id.goButton);

        scanButton.setOnClickListener(view -> {
            // Navigate to AR landmark scanning
            startActivity(new Intent(this, NavigationActivity.class));
        });

        goButton.setOnClickListener(view -> {
            String station = stationInput.getText().toString().trim();
            String destination = destinationInput.getText().toString().trim();

            if (station.isEmpty() || destination.isEmpty()) {
                Toast.makeText(this, "Please enter both station and destination", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, NavigationActivity.class);
                intent.putExtra("station", station);
                intent.putExtra("destination", destination);
                startActivity(intent);
            }
        });
    }
}
