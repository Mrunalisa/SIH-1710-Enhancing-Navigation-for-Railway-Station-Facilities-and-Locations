package com.example.manzil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StationSelectionActivity extends AppCompatActivity {

    private Spinner stationSpinner;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_selection);

        stationSpinner = findViewById(R.id.stationSpinner);
        confirmButton = findViewById(R.id.confirmButton);

        // Example station names
        String[] stations = {"Select Station", "Station A", "Station B", "Station C", "Station D"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, stations);
        stationSpinner.setAdapter(adapter);

        confirmButton.setOnClickListener(v -> {
            String selectedStation = stationSpinner.getSelectedItem().toString();

            if (selectedStation.equals("Select Station")) {
                Toast.makeText(this, "Please select a valid station", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Selected: " + selectedStation, Toast.LENGTH_SHORT).show();
                // Proceed to NavigationActivity or next step
                Intent intent = new Intent(this, NavigationActivity.class);
                intent.putExtra("stationName", selectedStation);
                startActivity(intent);
            }
        });
    }
}
