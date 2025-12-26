package com.developer.manzil;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Collection;

public class ARNavigation extends AppCompatActivity implements SensorEventListener, StepListener{

    //AR variables
    ArFragment fragment;
    private PointerDrawable pointer = new PointerDrawable();
    private boolean isTracking;
    private boolean isHitting;

    //Sensor variables
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer,magnetometer;
    private static int numSteps=0;
    boolean magSensor = false;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    int mAbsoluteDir;
    int mCross=0;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    //Instruction List variables
    Path[] mAllInstructionList = new Path[10];
    static int mInstructionNum=0;
    private int mInstructionCnt=0;

    //Navigation Logic Variables - Updated for rooms 401-416
    int mDestNum=0,mSrcNum=0;
    int mDestGroup=0,mSrcGroup=0;
    int mDir=0;
    // Step counts based on floor plan: 401→402(1), 401/402→403/404(12), 403→404(1), 404→405(20), etc.
    int mStepsRightSide[]={1,12,1,20,1};     // 401→402→403/404→405
    int mStepsBottomSide[]={12,20,1};        // 405/406→407/408→409  
    int mStepsLeftSide[]={1,12,1,20,1};      // 409→410→411/412→413
    int mStepsTopSide[]={12,20};             // 413/414→415/416→401
    int mStepsCross=7;
    int mAryPtrSrc,mAryPtrDest;

    //UI variables
    Button mGallery;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_navigation_prev) {
                //mTextMessage.setText(R.string.title_home);
                Intent mPrevIntent = new Intent(ARNavigation.this, SourceDetection.class);
                startActivity(mPrevIntent);
                finish();
                return true;
            } else if (itemId == R.id.bottom_navigation_steps) {
                //mTextMessage.setText(R.string.title_dashboard);
                Intent mGuideIntent = new Intent(ARNavigation.this, MainActivity.class);
                startActivity(mGuideIntent);
                finish();
                return true;
            } else if (itemId == R.id.bottom_navigation_next) {
                //mTextMessage.setText(R.string.title_notifications);
                Intent mNextIntent = new Intent(ARNavigation.this, MainActivity.class);
                startActivity(mNextIntent);
                finish();
                return true;
            }
            return false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.content_navigate);
            
            // Initialize AR with proper error handling and delays
            initializeARWithDelay();
            
            // Set up navigation with delay to ensure AR is ready
            new android.os.Handler().postDelayed(() -> {
                try {
                    startNavigation();
                } catch (Exception e) {
                    // Silent error handling for production
                }
            }, 1000);

            BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
            if (navigation != null) {
                navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
            }
            
        } catch (Exception e) {
            handleCriticalError(e);
        }
    }
    
    private void initializeARWithDelay() {
        new android.os.Handler().postDelayed(() -> {
            try {
                initializeARFragment();
            } catch (Exception e) {
                retryARInitialization();
            }
        }, 500);
    }
    
    private void initializeARFragment() {
        try {
            fragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.cam_fragment);
            if (fragment == null) {
                retryARInitialization();
                return;
            }
            
            // Wait for fragment to be fully initialized
            new android.os.Handler().postDelayed(() -> {
                setupARFragment();
            }, 1000);
            
        } catch (Exception e) {
            retryARInitialization();
        }
    }
    
    private void setupARFragment() {
        try {
            if (fragment == null || fragment.getArSceneView() == null) {
                retryARInitialization();
                return;
            }
            
            // Set up scene update listener with continuous arrow placement
            fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
                try {
                    if (fragment != null) {
                        fragment.onUpdate(frameTime);
                        onUpdate();
                        
                        // Continuously place arrows based on magnetometer and navigation
                        updateArrowsBasedOnDirection();
                    }
                } catch (Exception e) {
                    // Silently handle update errors to prevent crashes
                }
            });
            
            // Set up session initialization with comprehensive error handling
            fragment.setOnSessionInitializationListener(session -> {
                try {
                    if (session != null) {
                        com.google.ar.core.Config config = session.getConfig();
                        config.setPlaneFindingMode(com.google.ar.core.Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL);
                        config.setLightEstimationMode(com.google.ar.core.Config.LightEstimationMode.ENVIRONMENTAL_HDR);
                        session.configure(config);
                        
                        // Immediately show first arrow when AR is ready
                        new android.os.Handler().postDelayed(() -> {
                            try {
                                showInitialArrow();
                                // Start continuous arrow updates
                                startContinuousArrowUpdates();
                            } catch (Exception e) {
                                // Silent error handling
                            }
                        }, 1000);
                    }
                } catch (Exception e) {
                    // Silent error handling
                }
            });
            
            // Remove tap listener for production - arrows placed automatically during navigation
            
            
        } catch (Exception e) {
            retryARInitialization();
        }
    }
    
    private void retryARInitialization() {
        new android.os.Handler().postDelayed(() -> {
            try {
                initializeARFragment();
            } catch (Exception e) {
                enableFallbackMode();
            }
        }, 2000);
    }
    
    private void handleCriticalError(Exception e) {
        try {
            // Log error and provide user feedback
            
            // Enable fallback mode
            enableFallbackMode();
            
        } catch (Exception fallbackError) {
            // Last resort - finish activity gracefully
            finish();
        }
    }
    
    private void enableFallbackMode() {
        try {
            
            // Set up basic navigation without AR
            new android.os.Handler().postDelayed(() -> {
                try {
                    startNavigation();
                } catch (Exception e) {
                    // Silent error handling
                }
            }, 1000);
            
        } catch (Exception e) {
            // Silent error handling
        }
    }
    
    // Show initial arrow immediately when AR is ready
    private void showInitialArrow() {
        try {
            if (isARReady()) {
                // Force place initial arrow immediately
                forcePlaceArrow(Uri.parse("Arrow_straight_Zneg.sfb"));
                
                // Update status to show arrow is placed
                runOnUiThread(() -> {
                    Button statusButton = findViewById(R.id.selectbtnid);
                    if (statusButton != null) {
                        statusButton.setText("🎯 AR Navigation Active");
                    }
                });
            }
        } catch (Exception e) {
            // Silent error handling
        }
    }
    
    // Continuous arrow updates based on magnetometer direction
    private void updateArrowsBasedOnDirection() {
        try {
            if (!isARReady() || mInstructionNum >= mInstructionCnt) {
                return;
            }
            
            // Only update every few frames to avoid performance issues
            if (System.currentTimeMillis() - lastArrowUpdate < 1000) {
                return;
            }
            lastArrowUpdate = System.currentTimeMillis();
            
            // Get current direction and target direction
            int targetDirection = getTargetDirection();
            int currentRange = getRange(mAbsoluteDir);
            
            if (targetDirection != -1) {
                // Clear previous arrows
                clearPreviousArrows();
                
                // Determine which arrow to show
                String arrowModel = getArrowModelForDirection(currentRange, targetDirection);
                
                // Force place arrow (don't wait for plane detection)
                forcePlaceArrow(Uri.parse(arrowModel));
            }
            
        } catch (Exception e) {
            // Silent error handling
        }
    }
    
    // Start continuous arrow updates
    private void startContinuousArrowUpdates() {
        try {
            // Update arrows every 2 seconds
            android.os.Handler handler = new android.os.Handler();
            Runnable arrowUpdater = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isARReady() && mInstructionNum < mInstructionCnt) {
                            updateArrowsBasedOnDirection();
                            handler.postDelayed(this, 2000);
                        }
                    } catch (Exception e) {
                        // Silent error handling
                    }
                }
            };
            handler.postDelayed(arrowUpdater, 2000);
        } catch (Exception e) {
            // Silent error handling
        }
    }
    
    // Get appropriate arrow model based on direction
    private String getArrowModelForDirection(int currentRange, int targetRange) {
        int turnDirection = calculateTurnDirection(currentRange, targetRange);
        
        switch (turnDirection) {
            case 1: return "Arrow_Right_Zneg.sfb";     // Turn right
            case -1: return "Arrow_Left_Zneg.sfb";     // Turn left
            case 2: return "Arrow_straight_Zpos.sfb";  // Turn around
            default: return "Arrow_straight_Zneg.sfb"; // Go straight
        }
    }
    
    private long lastArrowUpdate = 0;
    
    // Get direction text for user feedback
    private String getDirectionText(int currentRange, int targetRange) {
        int turnDirection = calculateTurnDirection(currentRange, targetRange);
        
        switch (turnDirection) {
            case 1: return "Turn Right ➡️";
            case -1: return "Turn Left ⬅️";
            case 2: return "Turn Around 🔄";
            default: return "Go Straight ⬆️";
        }
    }

    //--------------------------Pedometer Navigation logic------------------------------------------

    public void startNavigation() {
        try {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            
            if (accelerometer == null || magnetometer == null) {
                return;
            }
            
            simpleStepDetector = new StepDetector();
            simpleStepDetector.registerListener(this);
            numSteps = 0;
            
            sensorManager.registerListener(ARNavigation.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(ARNavigation.this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            
        } catch (Exception e) {
            return;
        }

        // Initialize navigation path immediately
        generateNavigationFromPreferences();

        //initialise a new string array
        //String[] mEachInstruction = new String[]{};
        // Create a List from String Array elements
        //mAllInstructionList = new ArrayList<String>(Arrays.asList(mEachInstruction));
        //mAllInstructionList = new Path[10];

        String mSavedSrc="Hello 000",mSavedDest="Hello 000";
        try {
            SharedPreferences sd=getSharedPreferences("data",Context.MODE_PRIVATE);

            mSavedSrc = sd.getString("sdSrc","");
            mSavedDest = sd.getString("sdDest","");

            if (mSavedSrc.isEmpty() || mSavedDest.isEmpty()) {
                return;
            }

            if (mSavedDest.equals("Toilet (East)") || mSavedDest.equals("Toilet East"))
            {   mDestNum = 450; mDestGroup=1;   } // Right side toilet (near 404)
            else if (mSavedDest.equals("Toilet (North)") || mSavedDest.equals("Toilet North"))
            {   mDestNum = 451; mDestGroup=2;   } // Top side toilet (near 408)  
            else if (mSavedDest.equals("Toilet (South)") || mSavedDest.equals("Toilet South"))
            {   mDestNum = 452; mDestGroup=3;   } // Left side toilet (near 412)
            else if (mSavedDest.equals("Entrance"))
            {   mDestNum = 460; mDestGroup=5;   } // Entrance between 401 and 416
            else
            {
                try {
                    if (mSavedDest.length() >= 3) {
                        mDestNum = Integer.parseInt(mSavedDest.substring(mSavedDest.length() - 3));
                        // Group assignment based on corrected floor plan
                        if (mDestNum >= 401 && mDestNum <= 404)
                            mDestGroup = 1; // Right side
                        else if (mDestNum >= 405 && mDestNum <= 408)
                            mDestGroup = 2; // Top side
                        else if (mDestNum >= 409 && mDestNum <= 412)
                            mDestGroup = 3; // Left side  
                        else if (mDestNum >= 413 && mDestNum <= 416)
                            mDestGroup = 4; // Bottom side
                        else {
                            return;
                        }
                    } else {
                        return;
                    }
                } catch (NumberFormatException e) {
                    return;
                }
            }
        } catch (Exception e) {
            return;
        }

        try {
            if (mSavedSrc.equals("Toilet (East)") || mSavedSrc.equals("Toilet East"))
            {   mSrcNum = 450; mSrcGroup=1;   } // Right side toilet (near 404)
            else if (mSavedSrc.equals("Toilet (North)") || mSavedSrc.equals("Toilet North"))
            {   mSrcNum = 451; mSrcGroup=2;   } // Top side toilet (near 408)  
            else if (mSavedSrc.equals("Toilet (South)") || mSavedSrc.equals("Toilet South"))
            {   mSrcNum = 452; mSrcGroup=3;   } // Left side toilet (near 412)
            else if (mSavedSrc.equals("Entrance"))
            {   mSrcNum = 460; mSrcGroup=5;   } // Entrance between 401 and 416
            else
            {
                if (mSavedSrc.length() >= 3) {
                    mSrcNum = Integer.parseInt(mSavedSrc.substring(mSavedSrc.length() - 3));
                    // Group assignment based on corrected floor plan
                    if (mSrcNum >= 401 && mSrcNum <= 404)
                        mSrcGroup = 1; // Right side
                    else if (mSrcNum >= 405 && mSrcNum <= 408)
                        mSrcGroup = 2; // Top side
                    else if (mSrcNum >= 409 && mSrcNum <= 412)
                        mSrcGroup = 3; // Left side  
                    else if (mSrcNum >= 413 && mSrcNum <= 416)
                        mSrcGroup = 4; // Bottom side
                    else {
                        return;
                    }
                } else {
                    return;
                }
            }
        } catch (NumberFormatException e) {
            return;
        } catch (Exception e) {
            return;
        }

        // New navigation logic for corridor-only room layout 401-416
        generateNavigationPath(mSrcNum, mDestNum, mSrcGroup, mDestGroup);
        
        // Validate navigation was generated successfully
        if (mInstructionCnt == 0) {
            return;
        }
    }

    private void generateNavigationPath(int srcNum, int destNum, int srcGroup, int destGroup) {
        // Handle navigation within the corridor-only layout
        if (srcNum == destNum) {
            return;
        }

        // Corrected room sequence: Right(401-404), Top(405-408), Left(409-412), Bottom(413-416)
        // Entrance between 401 and 416
        int[] roomSequence = {401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416};
        int srcIndex = -1, destIndex = -1;
        
        // Find source and destination indices in room sequence
        for (int i = 0; i < roomSequence.length; i++) {
            if (roomSequence[i] == srcNum) srcIndex = i;
            if (roomSequence[i] == destNum) destIndex = i;
        }
        
        // Handle special cases (toilets, entrance)
        if (srcNum >= 450 && srcNum <= 460) srcIndex = getSpecialLocationIndex(srcNum);
        if (destNum >= 450 && destNum <= 460) destIndex = getSpecialLocationIndex(destNum);
        
        if (srcIndex == -1 || destIndex == -1) {
            return;
        }

        // Calculate clockwise and counter-clockwise distances through corridor only
        int clockwiseSteps = calculateCorridorSteps(srcIndex, destIndex, true);
        int counterClockwiseSteps = calculateCorridorSteps(srcIndex, destIndex, false);
        
        // Choose shorter path
        boolean useClockwise = clockwiseSteps <= counterClockwiseSteps;
        
        if (useClockwise) {
            generateCorridorPath(srcIndex, destIndex, true);
        } else {
            generateCorridorPath(srcIndex, destIndex, false);
        }
    }

    private int getSpecialLocationIndex(int specialNum) {
        switch (specialNum) {
            case 450: return 3;  // Toilet East (near 404)
            case 451: return 7;  // Toilet North (near 408)
            case 452: return 11; // Toilet South (near 412)
            case 460: return -1; // Entrance (between 401 and 416) - special handling
            default: return -1;
        }
    }

    private int calculateCorridorSteps(int srcIndex, int destIndex, boolean clockwise) {
        int totalSteps = 0;
        int current = srcIndex;
        
        while (current != destIndex) {
            totalSteps += getCorridorStepsBetweenRooms(current, clockwise);
            current = clockwise ? (current + 1) % 16 : (current - 1 + 16) % 16;
        }
        return totalSteps;
    }

    private int getCorridorStepsBetweenRooms(int roomIndex, boolean clockwise) {
        // Updated for corrected layout: Right(401-404), Top(405-408), Left(409-412), Bottom(413-416)
        // Only corridor movement allowed - no central space access
        switch (roomIndex) {
            case 0: return clockwise ? 1 : 20;    // 401→402 or 401←416 (entrance)
            case 1: return clockwise ? 12 : 1;    // 402→403/404 or 402←401
            case 2: return clockwise ? 1 : 12;    // 403→404 or 403←402
            case 3: return clockwise ? 20 : 1;    // 404→405 or 404←403 (toilet access)
            case 4: return clockwise ? 1 : 20;    // 405→406 or 405←404
            case 5: return clockwise ? 12 : 1;    // 406→407/408 or 406←405
            case 6: return clockwise ? 20 : 12;   // 407→408 or 407←406
            case 7: return clockwise ? 1 : 20;    // 408→409 or 408←407 (toilet access)
            case 8: return clockwise ? 1 : 1;     // 409→410 or 409←408
            case 9: return clockwise ? 12 : 1;    // 410→411/412 or 410←409
            case 10: return clockwise ? 1 : 12;   // 411→412 or 411←410
            case 11: return clockwise ? 20 : 1;   // 412→413 or 412←411 (toilet access)
            case 12: return clockwise ? 1 : 20;   // 413→414 or 413←412
            case 13: return clockwise ? 12 : 1;   // 414→415/416 or 414←413
            case 14: return clockwise ? 20 : 12;  // 415→416 or 415←414
            case 15: return clockwise ? 20 : 20;  // 416→401 or 416←415 (entrance access)
            default: return 1;
        }
    }

    private void generateCorridorPath(int srcIndex, int destIndex, boolean clockwise) {
        int current = srcIndex;
        mDir = clockwise ? 1 : -1;
        
        while (current != destIndex) {
            int steps = getCorridorStepsBetweenRooms(current, clockwise);
            mAllInstructionList[mInstructionCnt] = new Path();
            mAllInstructionList[mInstructionCnt].setPath(mDir, steps);
            mInstructionCnt++;
            current = clockwise ? (current + 1) % 16 : (current - 1 + 16) % 16;
        }
    }

    private int correspondLoc(int val) {
        // Updated for new room layout - no longer needed for 401-416 range
        return val;
    }

    //----------------------------------Direction ranges--------------------------------------------

    public int getRange(int degree){
        int mRangeVal=0;
        if (degree>335 || degree <25)
            mRangeVal=1;    //N
        else if (degree>65 && degree <115)
            mRangeVal=2;    //E
        else if (degree>155 && degree <205)
            mRangeVal=3;    //S
        else if (degree>245 && degree <295)
            mRangeVal=4;    //W
        return mRangeVal;
    }

    // Get target direction based on current navigation instruction
    private int getTargetDirection() {
        if (mInstructionNum >= mInstructionCnt || mAllInstructionList[mInstructionNum] == null) {
            return -1;
        }
        
        int direction = mAllInstructionList[mInstructionNum].getDir();
        
        // Updated for corrected floor plan: Right(401-404)=South, Top(405-408)=West, Left(409-412)=North, Bottom(413-416)=East
        if (direction == 1) { // Clockwise
            // Determine target based on current room group and destination
            if (mSrcGroup == 1) return 2; // From Right side, go East
            if (mSrcGroup == 2) return 1; // From Top side, go North  
            if (mSrcGroup == 3) return 4; // From Left side, go West
            if (mSrcGroup == 4) return 3; // From Bottom side, go South
        } else if (direction == -1) { // Counter-clockwise
            if (mSrcGroup == 1) return 4; // From Right side, go West
            if (mSrcGroup == 2) return 3; // From Top side, go South
            if (mSrcGroup == 3) return 2; // From Left side, go East  
            if (mSrcGroup == 4) return 1; // From Bottom side, go North
        }
        
        return -1;
    }
    
    // Calculate which direction to turn (1=right, -1=left, 2=around)
    private int calculateTurnDirection(int currentRange, int targetRange) {
        // Handle range values: 1=North, 2=East, 3=South, 4=West
        int diff = (targetRange - currentRange + 4) % 4;
        
        switch (diff) {
            case 0: return 0;  // Same direction - go straight
            case 1: return 1;  // Turn right (90 degrees clockwise)
            case 2: return 2;  // Turn around (180 degrees) - destination is opposite
            case 3: return -1; // Turn left (90 degrees counter-clockwise)
            default: return 0; // Default to straight
        }
    }
    
    // Generate navigation from preferences immediately
    private void generateNavigationFromPreferences() {
        try {
            SharedPreferences sd = getSharedPreferences("data", Context.MODE_PRIVATE);
            String mSavedSrc = sd.getString("sdSrc", "");
            String mSavedDest = sd.getString("sdDest", "");
            
            if (!mSavedSrc.isEmpty() && !mSavedDest.isEmpty()) {
                generateNavigationFromSavedData(mSavedSrc, mSavedDest);
            } else {
                Toast.makeText(this, "No navigation data found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading navigation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // Generate navigation from saved source and destination data
    private void generateNavigationFromSavedData(String mSavedSrc, String mSavedDest) {
        try {
            Toast.makeText(getApplicationContext(),"Starting AR Navigation\nFrom: "+mSavedSrc+" To: "+mSavedDest,Toast.LENGTH_LONG).show();

            // Parse destination
            int mDestNum = 0, mDestGroup = 0;
            if (mSavedDest.equals("Toilet (East)") || mSavedDest.equals("Toilet East")) {
                mDestNum = 450; mDestGroup = 1;
            } else if (mSavedDest.equals("Toilet (North)") || mSavedDest.equals("Toilet North")) {
                mDestNum = 451; mDestGroup = 2;
            } else if (mSavedDest.equals("Toilet (South)") || mSavedDest.equals("Toilet South")) {
                mDestNum = 452; mDestGroup = 3;
            } else if (mSavedDest.equals("Entrance")) {
                mDestNum = 460; mDestGroup = 5;
            } else {
                if (mSavedDest.length() >= 3) {
                    mDestNum = Integer.parseInt(mSavedDest.substring(mSavedDest.length() - 3));
                    if (mDestNum >= 401 && mDestNum <= 404) mDestGroup = 1;
                    else if (mDestNum >= 405 && mDestNum <= 408) mDestGroup = 2;
                    else if (mDestNum >= 409 && mDestNum <= 412) mDestGroup = 3;
                    else if (mDestNum >= 413 && mDestNum <= 416) mDestGroup = 4;
                }
            }

            // Parse source
            int mSrcNum = 0, mSrcGroup = 0;
            if (mSavedSrc.equals("Toilet (East)") || mSavedSrc.equals("Toilet East")) {
                mSrcNum = 450; mSrcGroup = 1;
            } else if (mSavedSrc.equals("Toilet (North)") || mSavedSrc.equals("Toilet North")) {
                mSrcNum = 451; mSrcGroup = 2;
            } else if (mSavedSrc.equals("Toilet (South)") || mSavedSrc.equals("Toilet South")) {
                mSrcNum = 452; mSrcGroup = 3;
            } else if (mSavedSrc.equals("Entrance")) {
                mSrcNum = 460; mSrcGroup = 5;
            } else {
                if (mSavedSrc.length() >= 3) {
                    mSrcNum = Integer.parseInt(mSavedSrc.substring(mSavedSrc.length() - 3));
                    if (mSrcNum >= 401 && mSrcNum <= 404) mSrcGroup = 1;
                    else if (mSrcNum >= 405 && mSrcNum <= 408) mSrcGroup = 2;
                    else if (mSrcNum >= 409 && mSrcNum <= 412) mSrcGroup = 3;
                    else if (mSrcNum >= 413 && mSrcNum <= 416) mSrcGroup = 4;
                }
            }

            // Store for later use
            this.mSrcNum = mSrcNum;
            this.mDestNum = mDestNum;
            this.mSrcGroup = mSrcGroup;
            this.mDestGroup = mDestGroup;

            // Generate navigation path
            generateNavigationPath(mSrcNum, mDestNum, mSrcGroup, mDestGroup);
            
            if (mInstructionCnt > 0) {
                Toast.makeText(getApplicationContext(), "Navigation ready! " + mInstructionCnt + " steps planned", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Navigation generation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //----------------------------------Sensor Management-------------------------------------------

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccelerometer(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAbsoluteDir = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
            mAbsoluteDir = Math.round(mAbsoluteDir);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void step(long timeNs) {
        // Check if navigation instructions are available
        if (mInstructionCnt == 0 || mAllInstructionList[0] == null) {
            // Try to initialize navigation if not done
            try {
                SharedPreferences sd = getSharedPreferences("data", Context.MODE_PRIVATE);
                String mSavedSrc = sd.getString("sdSrc", "");
                String mSavedDest = sd.getString("sdDest", "");
                if (!mSavedSrc.isEmpty() && !mSavedDest.isEmpty()) {
                    generateNavigationFromSavedData(mSavedSrc, mSavedDest);
                }
            } catch (Exception e) {
                // Silent error handling
            }
            return;
        }
        
        int d=99,dAll=99;
        Snackbar snackbar;
        mGallery = (Button) findViewById(R.id.selectbtnid);

        if (mInstructionNum==1 && mInstructionNum<mInstructionCnt)
            d = mAllInstructionList[1].getDir();
        if (mInstructionNum<mInstructionCnt && mAllInstructionList[mInstructionNum] != null)
            dAll = mAllInstructionList[mInstructionNum].getDir();

        if (mGallery != null) {
            mGallery.setText("Steps:" + numSteps + "/" + (mInstructionCnt > mInstructionNum ? mAllInstructionList[mInstructionNum].getSteps() : 0));
        }

        // Check if current instruction is completed
        if (mInstructionNum < mInstructionCnt && mAllInstructionList[mInstructionNum] != null && 
            numSteps >= mAllInstructionList[mInstructionNum].getSteps()) {
            mInstructionNum++;
            numSteps=0;
            if (mInstructionNum >= mInstructionCnt) {
                sensorManager.unregisterListener(ARNavigation.this);
                Intent mNextIntent = new Intent(ARNavigation.this, SuccessActivity.class);
                startActivity(mNextIntent);
                finish();
                return;
            }
        }
        // Show AR arrows immediately when step is detected
        try {
            // Clear previous arrows to avoid clutter
            clearPreviousArrows();
            
            // Always show arrows for better navigation experience
            // Determine target direction based on current instruction
            int targetDirection = getTargetDirection();
            int currentRange = getRange(mAbsoluteDir);
            
            // Show directional arrows based on where user needs to go
            if (targetDirection != -1) {
                String arrowModel = getArrowModelForDirection(currentRange, targetDirection);
                // Force place arrow immediately
                forcePlaceArrow(Uri.parse(arrowModel));
                
                // Update status with direction info
                runOnUiThread(() -> {
                    Button statusButton = findViewById(R.id.selectbtnid);
                    if (statusButton != null) {
                        String directionText = getDirectionText(currentRange, targetDirection);
                        statusButton.setText("Steps:" + numSteps + " - " + directionText);
                    }
                });
            } else {
                // Fallback - show forward arrow
                forcePlaceArrow(Uri.parse("Arrow_straight_Zneg.sfb"));
            }
        } catch (Exception e) {
            // Always try to show at least a basic arrow
            try {
                forcePlaceArrow(Uri.parse("Arrow_straight_Zneg.sfb"));
            } catch (Exception fallback) {
                // Silent error handling for production
            }
        }
        numSteps++;

    }

    //-----------------------------AR Object placement----------------------------------------------

    private void addObject(Uri model) {
        try {
            // Comprehensive AR readiness check
            if (!isARReady()) {
                // Try to initialize AR and retry
                initializeARWithDelay();
                // Schedule retry
                new android.os.Handler().postDelayed(() -> {
                    if (isARReady()) {
                        addObject(model);
                    } else {
                        forcePlaceArrow(model);
                    }
                }, 2000);
                return;
            }
            
            Frame frame = fragment.getArSceneView().getArFrame();
            if (frame == null) {
                forcePlaceArrow(model);
                return;
            }
            
            // Check tracking state
            TrackingState trackingState = frame.getCamera().getTrackingState();
            if (trackingState != TrackingState.TRACKING) {
                // Still try to place arrow for better user experience
                forcePlaceArrow(model);
                return;
            }
            
            // Try hit test with comprehensive plane detection
            boolean objectPlaced = tryPlaceOnDetectedPlane(frame, model);
            
            if (!objectPlaced) {
                // Fallback to any available plane
                objectPlaced = tryPlaceOnAnyPlane(frame, model);
            }
            
            if (!objectPlaced) {
                // Final fallback - force placement
                forcePlaceArrow(model);
            }
            
        } catch (Exception e) {
            // Always provide fallback
            forcePlaceArrow(model);
        }
    }
    
    private boolean isARReady() {
        try {
            return fragment != null && 
                   fragment.getArSceneView() != null && 
                   fragment.getArSceneView().getSession() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    
    private boolean tryPlaceOnDetectedPlane(Frame frame, Uri model) {
        try {
            android.graphics.Point pt = getScreenCenter();
            List<HitResult> hits = frame.hitTest(pt.x, pt.y);
            
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    Anchor anchor = hit.createAnchor();
                    placeObject(fragment, anchor, model);
                    return true;
                }
            }
        } catch (Exception e) {
            // Silent error handling
        }
        return false;
    }
    
    private boolean tryPlaceOnAnyPlane(Frame frame, Uri model) {
        try {
            Collection<Plane> planes = frame.getUpdatedTrackables(Plane.class);
            for (Plane plane : planes) {
                if (plane.getTrackingState() == TrackingState.TRACKING && 
                    plane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                    Anchor anchor = plane.createAnchor(plane.getCenterPose());
                    placeObject(fragment, anchor, model);
                    return true;
                }
            }
        } catch (Exception e) {
            // Silent error handling
        }
        return false;
    }

    // Force place arrow at camera position when plane detection fails
    private void forcePlaceArrow(Uri model) {
        try {
            if (!isARReady()) {
                return;
            }
            
            Frame frame = fragment.getArSceneView().getArFrame();
            com.google.ar.core.Session session = fragment.getArSceneView().getSession();
            
            if (frame != null && session != null) {
                // Always try to place on detected planes first
                Collection<Plane> planes = frame.getUpdatedTrackables(Plane.class);
                for (Plane plane : planes) {
                    if (plane.getTrackingState() == TrackingState.TRACKING) {
                        // Place arrow on the center of detected plane
                        com.google.ar.core.Pose planePose = plane.getCenterPose();
                        Anchor anchor = plane.createAnchor(planePose);
                        placeObject(fragment, anchor, model);
                        return;
                    }
                }
                
                // If no planes available, use camera position
                if (frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
                    com.google.ar.core.Pose cameraPose = frame.getCamera().getPose();
                    // Place arrow 1.5 meters in front of camera, on ground level
                    com.google.ar.core.Pose anchorPose = cameraPose.compose(
                        com.google.ar.core.Pose.makeTranslation(0, -0.8f, -1.5f)
                    );
                    
                    Anchor anchor = session.createAnchor(anchorPose);
                    placeObject(fragment, anchor, model);
                }
            }
        } catch (Exception e) {
            // Silent error handling
        }
    }

    // Clear previous arrows to avoid clutter
    private void clearPreviousArrows() {
        try {
            if (isARReady() && fragment.getArSceneView().getScene() != null) {
                // Safely remove all anchor nodes from the scene
                fragment.getArSceneView().getScene().getChildren().clear();
                // Toast.makeText(getApplicationContext(), "Previous arrows cleared", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Silently handle clear errors to prevent spam
            // Toast.makeText(getApplicationContext(), "Failed to clear arrows: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        try {
            if (fragment == null || anchor == null || model == null) {
                return;
            }
            
            CompletableFuture<Void> renderableFuture =
                    ModelRenderable.builder()
                            .setSource(fragment.getContext(), model)
                            .build()
                            .thenAccept(renderable -> {
                                try {
                                    if (renderable != null) {
                                        addNodeToScene(fragment, anchor, renderable);
                                        // Show success feedback using existing button
                                        runOnUiThread(() -> {
                                            Button statusButton = findViewById(R.id.selectbtnid);
                                            if (statusButton != null) {
                                                statusButton.setText("🎯 Arrow Active - Follow Direction");
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    // Silent error handling
                                }
                            })
                            .exceptionally((throwable -> {
                                try {
                                    // Show error feedback using existing button
                                    runOnUiThread(() -> {
                                        Button statusButton = findViewById(R.id.selectbtnid);
                                        if (statusButton != null) {
                                            statusButton.setText("⚠️ AR Loading - Move Camera Slowly");
                                        }
                                    });
                                } catch (Exception e) {
                                    // Prevent any additional crashes
                                }
                                return null;
                            }));
        } catch (Exception e) {
            // Silent error handling
        }
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        try {
            if (fragment == null || anchor == null || renderable == null) {
                return;
            }
            
            if (!isARReady()) {
                return;
            }
            
            AnchorNode anchorNode = new AnchorNode(anchor);
            
            // Create a transformable node for AR arrow
            TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
            
            // Set proper scale and rotation for visibility
            node.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f)); // Make arrows larger and more visible
            node.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 0f)); // Face forward
            
            // Disable transformation to prevent user interaction
            node.getRotationController().setEnabled(false);
            node.getScaleController().setEnabled(false);
            node.getTranslationController().setEnabled(false);
            
            node.setRenderable(renderable);
            node.setParent(anchorNode);
            
            // Safely add to scene
            if (fragment.getArSceneView() != null && fragment.getArSceneView().getScene() != null) {
                fragment.getArSceneView().getScene().addChild(anchorNode);
                node.select();
            }
            
        } catch (Exception e) {
            // Silent error handling
        }
    }

    //---------------------------AR green dot center detection methods------------------------------

    private void onUpdate() {
        try {
            boolean trackingChanged = updateTracking();
            View contentView = findViewById(android.R.id.content);
            
            if (contentView != null && trackingChanged) {
                if (isTracking) {
                    contentView.getOverlay().add(pointer);
                } else {
                    contentView.getOverlay().remove(pointer);
                }
                contentView.invalidate();
            }

            if (isTracking && contentView != null) {
                boolean hitTestChanged = updateHitTest();
                if (hitTestChanged) {
                    pointer.setEnabled(isHitting);
                    contentView.invalidate();
                }
            }
        } catch (Exception e) {
            // Silently handle update errors to prevent crashes during AR updates
        }
    }

    private boolean updateTracking() {
        try {
            if (!isARReady()) {
                isTracking = false;
                return false;
            }
            
            Frame frame = fragment.getArSceneView().getArFrame();
            boolean wasTracking = isTracking;
            isTracking = frame != null &&
                    frame.getCamera().getTrackingState() == TrackingState.TRACKING;
            return isTracking != wasTracking;
        } catch (Exception e) {
            isTracking = false;
            return false;
        }
    }

    private boolean updateHitTest() {
        try {
            if (!isARReady()) {
                isHitting = false;
                return false;
            }
            
            Frame frame = fragment.getArSceneView().getArFrame();
            android.graphics.Point pt = getScreenCenter();
            List<HitResult> hits;
            boolean wasHitting = isHitting;
            isHitting = false;
            
            if (frame != null) {
                hits = frame.hitTest(pt.x, pt.y);
                for (HitResult hit : hits) {
                    Trackable trackable = hit.getTrackable();
                    if (trackable instanceof Plane &&
                            ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                        isHitting = true;
                        break;
                    }
                }
            }
            return wasHitting != isHitting;
        } catch (Exception e) {
            isHitting = false;
            return false;
        }
    }

    private android.graphics.Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new android.graphics.Point(vw.getWidth()/2, vw.getHeight()/2);
    }

    //----------------------------------------------------------------------------------------------

    
    // Override lifecycle methods for proper AR management
    @Override
    protected void onResume() {
        super.onResume();
        
        try {
            // Re-register sensors if they were unregistered
            if (sensorManager != null && accelerometer != null && magnetometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            }
            
        } catch (Exception e) {
            // Silent error handling for production
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        try {
            // Unregister sensors to save battery
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
        } catch (Exception e) {
            // Silently handle pause errors
        }
    }
    
    @Override
    protected void onDestroy() {
        try {
            // Clean up resources
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
            
            // Clear AR scene
            if (isARReady()) {
                clearPreviousArrows();
            }
        } catch (Exception e) {
            // Silently handle cleanup errors
        }
        
        super.onDestroy();
    }
}