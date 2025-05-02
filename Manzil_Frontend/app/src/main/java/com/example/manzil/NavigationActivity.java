package com.example.manzil;

import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NavigationActivity extends AppCompatActivity {

    private SurfaceView arSurfaceView;
    private TextView navigationTitle, stepInstruction;
    private ProgressBar navigationProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Bind views
        arSurfaceView = findViewById(R.id.arSurfaceView);
        navigationTitle = findViewById(R.id.navigationTitle);
        stepInstruction = findViewById(R.id.stepInstruction);
        navigationProgress = findViewById(R.id.navigationProgress);

        // Example data (replace with live AR or navigation updates)
        navigationTitle.setText("Navigating to Station B");
        stepInstruction.setText("Walk straight for 20 meters");
        navigationProgress.setProgress(30);  // 30% done

        // TODO: Integrate ARCore / Sceneform rendering and location updates here
    }

    // Optional: override lifecycle methods if AR requires it
    @Override
    protected void onResume() {
        super.onResume();
        // Start camera or AR session
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause camera or AR session
    }
}
