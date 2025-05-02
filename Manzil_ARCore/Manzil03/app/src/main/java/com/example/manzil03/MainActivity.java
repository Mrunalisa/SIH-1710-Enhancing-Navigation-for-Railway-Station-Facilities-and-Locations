package com.example.manzil03;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.example.manzil03.admin.AdminActivity;
import com.example.manzil03.databinding.ActivityMainBinding;
import com.example.manzil03.navigation.NavigationActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private SharedPreferences securePreferences;
    private static final String ADMIN_PASSWORD_KEY = "admin_password";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123"; // Change in production

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupSecurePreferences();
        setupUI();
    }

    private void setupSecurePreferences() {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

            securePreferences = EncryptedSharedPreferences.create(
                this,
                "secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // Only set admin password if not already set
            if (!securePreferences.contains(ADMIN_PASSWORD_KEY)) {
                securePreferences.edit()
                    .putString(ADMIN_PASSWORD_KEY, DEFAULT_ADMIN_PASSWORD)
                    .apply();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing security", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupUI() {
        binding.adminButton.setOnClickListener(v -> startAdminMode());
        binding.userButton.setOnClickListener(v -> startUserMode());
    }

    private void startAdminMode() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }

    private void startUserMode() {
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}