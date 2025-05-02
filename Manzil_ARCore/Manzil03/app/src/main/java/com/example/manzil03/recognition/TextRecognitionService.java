package com.example.manzil03.recognition;

import android.media.Image;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextRecognitionService {
    private final TextRecognizer recognizer;
    private final Pattern numberPattern;
    private long lastScanTime = 0;
    private long scanDelay = 500; // Default delay of 500ms between scans

    public TextRecognitionService() {
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        numberPattern = Pattern.compile("^\\d+$"); // Match only numbers
    }

    public void setScanDelay(long milliseconds) {
        this.scanDelay = milliseconds;
    }

    public interface RecognitionCallback {
        void onNumbersDetected(List<String> numbers);
        void onError(Exception e);
    }

    public void processImage(@NonNull ImageProxy imageProxy, RecognitionCallback callback) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime < scanDelay) {
            imageProxy.close();
            return;
        }

        lastScanTime = currentTime;
        Image mediaImage = imageProxy.getImage();
        
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(
                mediaImage, 
                imageProxy.getImageInfo().getRotationDegrees()
            );

            recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    List<String> detectedNumbers = extractVerticalNumbers(visionText);
                    callback.onNumbersDetected(detectedNumbers);
                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                    imageProxy.close();
                });
        } else {
            imageProxy.close();
        }
    }

    private List<String> extractVerticalNumbers(Text visionText) {
        List<String> numbers = new ArrayList<>();
        
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            // Check if block is roughly vertical (height > width)
            float blockAspectRatio = (float) block.getBoundingBox().height() / 
                                   block.getBoundingBox().width();
                                   
            if (blockAspectRatio > 1.2f) { // Consider it vertical if height is 20% more than width
                for (Text.Line line : block.getLines()) {
                    String text = line.getText().trim();
                    if (numberPattern.matcher(text).matches()) {
                        numbers.add(text);
                    }
                }
            }
        }
        
        return numbers;
    }
}