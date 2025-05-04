package com.example.manzil;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OTPActivity extends AppCompatActivity {

    private EditText otpInput;
    private Button verifyOtpButton;
    private TextView resendOtpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);

        otpInput = findViewById(R.id.otpInput);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        resendOtpText = findViewById(R.id.resendOtpText);

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = otpInput.getText().toString().trim();
                if (otp.length() == 6) {
                    // TODO: Verify OTP with backend or Firebase
                    Toast.makeText(OTPActivity.this, "Verifying OTP...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OTPActivity.this, "Please enter a valid 6-digit OTP.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        resendOtpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Trigger OTP resend
                Toast.makeText(OTPActivity.this, "OTP Resent.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
