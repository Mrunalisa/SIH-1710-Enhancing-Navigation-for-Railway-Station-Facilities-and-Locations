package com.developer.manzil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Destination extends AppCompatActivity {

    private TextView mTextMessage;
    //ArrayAdapter<String> mAdapter;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_navigation_prev) {
                //mTextMessage.setText(R.string.title_home);
                Intent mPrevIntent = new Intent(Destination.this, MainActivity.class);
                startActivity(mPrevIntent);
                finish();
                return true;
            } else if (itemId == R.id.bottom_navigation_steps) {
                //mTextMessage.setText(R.string.title_dashboard);
                return true;
            } else if (itemId == R.id.bottom_navigation_next) {
                //mTextMessage.setText(R.string.title_notifications);
                Intent mGuideIntent = new Intent(Destination.this,SourceIdentification.class);
                startActivity(mGuideIntent);
                finish();
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);

        //mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation_dest);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //mAdapter = ArrayAdapter.createFromResource(this,R.array.destinations,);

        ListView listView = (ListView) findViewById(R.id.destination_list);
        
        // Set up adapter for destinations
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.destinations, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sd=getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed=sd.edit();
                String mText = (String)((TextView) view).getText();
                Toast.makeText(getApplicationContext(),"Selected: " + mText,Toast.LENGTH_SHORT).show();
                ed.putString("sdDest",mText);
                ed.commit();
                
                // Navigate to next screen after selection
                Intent nextIntent = new Intent(Destination.this, SourceIdentification.class);
                startActivity(nextIntent);
                finish();
            }
        });
        //listView.setAdapter(mAdapter);

    }

}
