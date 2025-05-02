package com.example.manzil03.navigation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.manzil03.ar.ARRenderer;
import com.example.manzil03.databinding.ActivityNavigationBinding;
import com.example.manzil03.model.NavigationNode;
import com.example.manzil03.recognition.TextRecognitionService;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NavigationActivity extends AppCompatActivity {
    private ActivityNavigationBinding binding;
    private NavigationViewModel viewModel;
    private ARRenderer arRenderer;
    private TextRecognitionService textRecognitionService;
    private ExecutorService cameraExecutor;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NavigationViewModel.class);
        arRenderer = new ARRenderer(binding.arSceneView);
        textRecognitionService = new TextRecognitionService();
        cameraExecutor = Executors.newSingleThreadExecutor();

        setupUI();
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        // Observe navigation path updates
        viewModel.getCurrentPath().observe(this, smoothedPath -> {
            if (smoothedPath != null && !smoothedPath.isEmpty()) {
                try {
                    arRenderer.renderPath(smoothedPath);
                } catch (com.google.ar.core.exceptions.CameraNotAvailableException e) {
                    // Handle the exception (e.g., log or show a message)
                    e.printStackTrace();
                }
            }
        });

        // Set up settings
        binding.pathDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float distance = progress / 10f; // Convert to meters (0.1m to 10m)
                arRenderer.setPathRenderDistance(distance);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.scanDelaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long delay = progress * 100; // Convert to milliseconds (100ms to 2000ms)
                textRecognitionService.setScanDelay(delay);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupUI() {
        binding.destinationInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String destination = binding.destinationInput.getText().toString();
                viewModel.setDestination(destination);
                return true;
            }
            return false;
        });

        binding.clearButton.setOnClickListener(v -> {
            arRenderer.clearPath();
            binding.destinationInput.setText("");
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    textRecognitionService.processImage(image, new TextRecognitionService.RecognitionCallback() {
                        @Override
                        public void onNumbersDetected(List<String> numbers) {
                            for (String number : numbers) {
                                viewModel.updateCurrentLocation(number);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> 
                                Toast.makeText(NavigationActivity.this, 
                                    "Error detecting text: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != 
                PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permissions required for navigation", 
                    Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        binding = null;
    }
}