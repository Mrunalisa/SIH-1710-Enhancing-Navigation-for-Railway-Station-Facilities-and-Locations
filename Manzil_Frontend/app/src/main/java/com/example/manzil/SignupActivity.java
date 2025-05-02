package com.example.manzil;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput, dobInput, passwordInput, confirmPasswordInput;
    private Spinner genderSpinner;
    private Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        dobInput = findViewById(R.id.dobInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        genderSpinner = findViewById(R.id.genderSpinner);
        createAccountButton = findViewById(R.id.createAccountButton);

        createAccountButton.setOnClickListener(v -> {
            if (validateInputs()) {
                // Here you could connect to a backend or navigate
                startActivity(new Intent(SignupActivity.this, OTPActivity.class));
            }
        });
    }

    private boolean validateInputs() {
        if (nameInput.getText().toString().isEmpty() ||
                emailInput.getText().toString().isEmpty() ||
                phoneInput.getText().toString().isEmpty() ||
                dobInput.getText().toString().isEmpty() ||
                passwordInput.getText().toString().isEmpty() ||
                confirmPasswordInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!passwordInput.getText().toString().equals(confirmPasswordInput.getText().toString())) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
