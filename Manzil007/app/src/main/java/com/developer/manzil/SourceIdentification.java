package com.developer.manzil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class SourceIdentification extends AppCompatActivity {

    Uri uri;
    String picpath="";
    Button mCapture,mDetect,mGallery;
    ImageView mImageView;
    TextView mSourceText;
    Bitmap mBitmap;
    int mCapturedFlag = 0, mGallerySelectFlag = 0, mSourceDetectedFlag = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE = 7;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_navigation_prev) {
                //mTextMessage.setText(R.string.title_home);
                Intent mPrevIntent = new Intent(SourceIdentification.this, Destination.class);
                startActivity(mPrevIntent);
                finish();
                return true;
            } else if (itemId == R.id.bottom_navigation_steps) {
                //mTextMessage.setText(R.string.title_dashboard);
                Intent mGuideIntent = new Intent(SourceIdentification.this, MainActivity.class);
                startActivity(mGuideIntent);
                finish();
                return true;
            } else if (itemId == R.id.bottom_navigation_next) {
                // Check if source has been detected before proceeding
                SharedPreferences sd = getSharedPreferences("data", Context.MODE_PRIVATE);
                String detectedSource = sd.getString("sdSrc", "");
                String selectedDest = sd.getString("sdDest", "");
                
                if (detectedSource.isEmpty()) {
                    Toast.makeText(SourceIdentification.this, "Please detect your current location first", Toast.LENGTH_LONG).show();
                    return false;
                }
                
                if (selectedDest.isEmpty()) {
                    Toast.makeText(SourceIdentification.this, "No destination selected", Toast.LENGTH_LONG).show();
                    return false;
                }
                
                // Navigate directly to AR Navigation
                Intent mNextIntent = new Intent(SourceIdentification.this, ARNavigation.class);
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
        setContentView(R.layout.activity_source_identification);

        //mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //Camera Permissions
        if (ContextCompat.checkSelfPermission(SourceIdentification.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SourceIdentification.this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        }

        mImageView = (ImageView) findViewById(R.id.imageViewid);
        mCapture = (Button) findViewById(R.id.capturebtnid);
        mDetect = (Button) findViewById(R.id.detectbtnid);
        mGallery = (Button) findViewById(R.id.selectbtnid);
        mSourceText = (TextView) findViewById(R.id.textView2);

        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkPermissionREAD_EXTERNAL_STORAGE(SourceIdentification.this)) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                }
            }
        });

        mDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCapturedFlag == 1 || mGallerySelectFlag == 1)
                    detectText();
                else
                    Toast.makeText(getApplicationContext(), "No Image Captured", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(mBitmap);
            mCapturedFlag = 1;
        }

        if (resultCode == RESULT_OK && requestCode == 7) {
            try {
                Context applicationContext = getApplicationContext();
                uri = data.getData();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.Source source = ImageDecoder.createSource(applicationContext.getContentResolver(), uri);
                    mBitmap = ImageDecoder.decodeBitmap(source);
                } else {
                    mBitmap = MediaStore.Images.Media.getBitmap(applicationContext.getContentResolver(), uri);
                }
                mImageView.setImageBitmap(mBitmap);
                picpath = uri.toString();
                mGallerySelectFlag = 1;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void detectText() {
        if (mBitmap == null) {
            Toast.makeText(this, "No image to process", Toast.LENGTH_SHORT).show();
            return;
        }
        
        InputImage image = InputImage.fromBitmap(mBitmap, 0);
        TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        textRecognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text result) {
                        Toast.makeText(SourceIdentification.this, "Text recognition started", Toast.LENGTH_SHORT).show();
                        processText(result);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SourceIdentification.this, "Text recognition failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        });
    }

    private void processText(Text mVisionText) {
        List<Text.TextBlock> mBlocks = mVisionText.getTextBlocks();
        SharedPreferences sd=getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed=sd.edit();

        String mText;
        if (mBlocks.size() == 0) {
            Toast.makeText(SourceIdentification.this, "No Text Found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder allText = new StringBuilder();
        for (Text.TextBlock mBlock_i : mVisionText.getTextBlocks()) {
            mText = mBlock_i.getText();
            allText.append(mText).append(" ");
            Toast.makeText(SourceIdentification.this, "Detected: " + mText, Toast.LENGTH_SHORT).show();
        }
        
        String fullText = allText.toString().trim();
        Toast.makeText(SourceIdentification.this, "Full text: " + fullText, Toast.LENGTH_LONG).show();
        
        mText = fullText.replace("\n", " ");
        String detectedSource = detectSource(mText);
        
        if (mSourceDetectedFlag == 1) {
            mSourceText.setTextSize(20);
            mSourceText.setText(detectedSource);
            ed.putString("sdSrc", detectedSource);
            ed.commit();
            Toast.makeText(SourceIdentification.this, "Location detected: " + detectedSource, Toast.LENGTH_LONG).show();
            
            // Auto-start AR navigation after successful detection
            String selectedDest = sd.getString("sdDest", "");
            if (!selectedDest.isEmpty()) {
                Toast.makeText(SourceIdentification.this, "Starting AR Navigation...", Toast.LENGTH_SHORT).show();
                Intent arIntent = new Intent(SourceIdentification.this, ARNavigation.class);
                startActivity(arIntent);
                finish();
            } else {
                Toast.makeText(SourceIdentification.this, "Please select a destination first", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(SourceIdentification.this, "No matching landmark found. Try capturing a room number or sign.", Toast.LENGTH_LONG).show();
        }
    }

    private String detectSource(String mText) {
        String mSource = "";
        int mCnt = 0;
        mSourceDetectedFlag = 0; // Reset flag
        
        String[] tags = getResources().getStringArray(R.array.boards);
        Toast.makeText(this, "Checking " + tags.length + " landmarks", Toast.LENGTH_SHORT).show();
        
        for (String tag : tags) {
            String[] pair = tag.split(":");
            if (pair.length >= 2) {
                String key = pair[0];
                String value = pair[1];
                
                // More flexible matching - check for numbers and keywords
                if (mText.toLowerCase().contains(value.toLowerCase()) || 
                    value.toLowerCase().contains(mText.toLowerCase()) ||
                    mText.contains(value) || value.contains(mText)) {
                    mSource = key;
                    mCnt++;
                    Toast.makeText(this, "Match found: " + key + " for " + value, Toast.LENGTH_SHORT).show();
                }
            }
        }
        
        // Allow detection even with multiple matches, take the first one
        if (mCnt >= 1) {
            mSourceDetectedFlag = 1;
            Toast.makeText(this, "Detection successful: " + mSource, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No matches found in text: '" + mText + "'", Toast.LENGTH_LONG).show();
        }
        
        return mSource;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(SourceIdentification.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{permission},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}
