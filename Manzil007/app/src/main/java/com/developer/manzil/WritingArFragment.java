package com.developer.manzil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;

import com.google.ar.sceneform.ux.ArFragment;

public class WritingArFragment extends ArFragment {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    
    @Override
    public String[] getAdditionalPermissions() {
        String[] additionalPermissions = super.getAdditionalPermissions();
        int permissionLength = additionalPermissions != null ? additionalPermissions.length : 0;
        String[] permissions = new String[permissionLength + 2];
        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        permissions[1] = Manifest.permission.CAMERA;
        if (permissionLength > 0) {
            System.arraycopy(additionalPermissions, 0, permissions, 2, additionalPermissions.length);
        }
        return permissions;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Request camera permission if not granted
        if (getActivity() != null && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getActivity(), 
                new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }
}
