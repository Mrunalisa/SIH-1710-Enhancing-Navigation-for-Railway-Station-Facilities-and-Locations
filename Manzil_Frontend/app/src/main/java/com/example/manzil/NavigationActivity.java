package com.example.manzil;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private SurfaceView arSurfaceView;
    private TextView navigationTitle, stepInstruction;
    private ProgressBar navigationProgress;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind views
        arSurfaceView = findViewById(R.id.arSurfaceView);
        navigationTitle = findViewById(R.id.navigationTitle);
        stepInstruction = findViewById(R.id.stepInstruction);
        navigationProgress = findViewById(R.id.navigationProgress);

        // Example Data
        navigationTitle.setText("Navigating to Station B");
        stepInstruction.setText("Walk straight for 20 meters");
        navigationProgress.setProgress(30);  // Example progress

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Navigate to Profile Page
            startActivity(new Intent(NavigationActivity.this, ProfileActivity.class));

        } else if (id == R.id.nav_select_station) {
            // Navigate to Select Station Page
            startActivity(new Intent(NavigationActivity.this, StationSelectionActivity.class));

        }

//        else if (id == R.id.nav_logout) {
//            // Logout user and return to Welcome screen
//            mAuth.signOut();
//            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
//
//            Intent intent = new Intent(NavigationActivity.this, WelcomeActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
//        }
        else {
            Toast.makeText(this, "Feature Coming Soon!", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Optional: AR Lifecycle
    @Override
    protected void onResume() {
        super.onResume();
        // Start AR session (if required)
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause AR session (if required)
    }
}
