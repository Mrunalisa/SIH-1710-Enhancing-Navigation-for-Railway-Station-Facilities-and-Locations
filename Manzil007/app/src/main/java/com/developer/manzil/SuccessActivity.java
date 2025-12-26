package com.developer.manzil;

import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class SuccessActivity extends AppCompatActivity {
    TextView mSuccessMsg,mSuccessInst;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_navigation_prev) {
                //mTextMessage.setText(R.string.title_home);
                Intent mPrevIntent = new Intent(SuccessActivity.this,MainActivity.class);
                startActivity(mPrevIntent);
                finish();
                return true;
            } else if (itemId == R.id.bottom_navigation_steps) {
                //mTextMessage.setText(R.string.title_dashboard);
                Intent mGuideIntent = new Intent(SuccessActivity.this,MainActivity.class);
                startActivity(mGuideIntent);
                finish();
                return true;
            } else if (itemId == R.id.bottom_navigation_next) {
                //mTextMessage.setText(R.string.title_notifications);
                Intent mNextIntent = new Intent(SuccessActivity.this,Destination.class);
                startActivity(mNextIntent);
                finish();
                return true;
            }
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        mSuccessMsg = findViewById(R.id.textsuccessid);
        mSuccessMsg.setTextSize(20);
        mSuccessMsg.setTextColor(getColor(R.color.colorAccent));

        mSuccessInst = findViewById(R.id.textNextInstrid);
        mSuccessInst.setTextSize(20);
        mSuccessInst.setTextColor(getColor(R.color.colorPrimary));

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
}
