package com.developer.manzil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput, dobInput, passwordInput, confirmPasswordInput;
    private Spinner genderSpinner;
    private Button createAccountButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Firebase init
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI references
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        dobInput = findViewById(R.id.dobInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        genderSpinner = findViewById(R.id.genderSpinner);
        createAccountButton = findViewById(R.id.createAccountButton);
        
        // Setup gender spinner with custom adapter for white text
        setupGenderSpinner();

        createAccountButton.setOnClickListener(v -> {
            if (validateInputs()) {
                registerUser();
            }
        });
    }

    private void setupGenderSpinner() {
        // Get gender array from resources
        String[] genderArray = getResources().getStringArray(R.array.gender_array);
        
        // Create custom adapter with white text
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item_white,
                genderArray
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(0xFFFFFFFF); // White color
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(0xFFFFFFFF); // White color
                textView.setBackgroundColor(0xFF000000); // Black background
                return view;
            }
        };
        
        // Set dropdown resource
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white);
        
        // Apply adapter to spinner
        if (genderSpinner != null) {
            genderSpinner.setAdapter(adapter);
            genderSpinner.setVisibility(View.VISIBLE);
        }
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String dob = dobInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        // Validate gender spinner
        if (genderSpinner == null || genderSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }
        String gender = genderSpinner.getSelectedItem().toString();

        // Show loading indicator
        createAccountButton.setEnabled(false);
        createAccountButton.setText("Creating Account...");

        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();

                            // Save extra user info in Firestore
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("email", email);
                            user.put("phone", phone);  // ✅ still stored
                            user.put("dob", dob);
                            user.put("gender", gender);

                            db.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignupActivity.this,
                                                "Account created successfully",
                                                Toast.LENGTH_SHORT).show();

                                        // ✅ Directly navigate to MainActivity (no OTP step)
                                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Re-enable button on failure
                                        createAccountButton.setEnabled(true);
                                        createAccountButton.setText("Create Account");
                                        
                                        Toast.makeText(SignupActivity.this,
                                                "Firestore error: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                        
                                        // Delete the auth user if Firestore save failed
                                        if (mAuth.getCurrentUser() != null) {
                                            mAuth.getCurrentUser().delete();
                                        }
                                    });

                        } else {
                            // Re-enable button on failure
                            createAccountButton.setEnabled(true);
                            createAccountButton.setText("Create Account");
                            
                            String errorMessage = "Signup failed";
                            if (task.getException() != null) {
                                errorMessage = task.getException().getMessage();
                            }
                            Toast.makeText(SignupActivity.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private boolean validateInputs() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String dob = dobInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Check if all fields are filled
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Phone number is required");
            phoneInput.requestFocus();
            return false;
        }

        // Validate phone number (basic check - at least 10 digits)
        if (phone.length() < 10) {
            phoneInput.setError("Enter a valid phone number");
            phoneInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(dob)) {
            dobInput.setError("Date of birth is required");
            dobInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return false;
        }

        // Validate password length (Firebase requires at least 6 characters)
        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInput.setError("Please confirm your password");
            confirmPasswordInput.requestFocus();
            return false;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return false;
        }

        // Validate gender spinner
        if (genderSpinner == null || genderSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
