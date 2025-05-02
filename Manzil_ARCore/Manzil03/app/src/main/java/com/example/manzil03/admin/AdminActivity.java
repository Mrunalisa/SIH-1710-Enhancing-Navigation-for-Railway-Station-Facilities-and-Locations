package com.example.manzil03.admin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manzil03.R;
import com.example.manzil03.databinding.ActivityAdminBinding;
import com.example.manzil03.model.NavigationNode;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ar.core.Pose;
import io.github.sceneview.ar.ArSceneView;
import io.github.sceneview.ar.node.ArNode;
import io.github.sceneview.node.Node;
import java.util.ArrayList;
import java.util.List;
import dev.romainguy.kotlin.math.Float3;
import android.util.Log;
import android.app.AlertDialog;

public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding binding;
    private AdminViewModel viewModel;
    private ArSceneView arSceneView;
    private NodeListAdapter adapter;
    private ArNode selectedNode;
    private static final int REQUEST_CODE_PERMISSIONS = 20;
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AdminActivity", "onCreate: starting");
        try {
            binding = ActivityAdminBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            if (!allPermissionsGranted()) {
                Log.d("AdminActivity", "Requesting permissions");
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                return;
            }

            viewModel = new ViewModelProvider(this).get(AdminViewModel.class);
            if (binding.arSceneView == null) {
                Log.e("AdminActivity", "arSceneView is null!");
                showFatalError("AR view could not be initialized. Please check your layout.");
                return;
            }
            setupArView();
            setupNodeList();
            setupFabs();

            // Observe nodes
            viewModel.getAllNodes().observe(this, nodes -> {
                adapter.submitList(nodes);
                updateArNodes(nodes);
            });
            Log.d("AdminActivity", "onCreate: initialization complete");
        } catch (Exception e) {
            Log.e("AdminActivity", "onCreate: initialization failed", e);
            showFatalError("Failed to initialize Admin Mode: " + e.getMessage());
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Camera permission required for admin features", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setupArView() {
        arSceneView = binding.arSceneView;
        if (arSceneView == null) {
            Log.e("AdminActivity", "setupArView: arSceneView is null!");
            showFatalError("AR view could not be initialized. Please check your layout.");
            return;
        }
        // Remove setOnTapArPlaneListener, or use a generic touch listener if needed
        // arSceneView.setOnTouchListener(...);
    }

    private void setupNodeList() {
        RecyclerView nodeList = binding.nodeList;
        adapter = new NodeListAdapter(node -> {
            // Node selected from list
            highlightNode(node);
            showNodeOptionsDialog(node);
        });
        nodeList.setAdapter(adapter);
        nodeList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupFabs() {
        binding.addConnectionFab.setOnClickListener(v -> {
            if (selectedNode != null) {
                showConnectionDialog();
            } else {
                Toast.makeText(this, "Select a node first", Toast.LENGTH_SHORT).show();
            }
        });

        binding.deleteFab.setOnClickListener(v -> {
            if (selectedNode != null) {
                try {
                    viewModel.deleteSelectedNode();
                    selectedNode = null;
                    Toast.makeText(this, "Node deleted", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error deleting node", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showAddNodeDialog(Pose hitPose) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_node, null);
        new MaterialAlertDialogBuilder(this)
            .setTitle("Add Navigation Node")
            .setView(dialogView)
            .setPositiveButton("Add", (dialog, which) -> {
                try {
                    String label = ((android.widget.EditText) dialogView.findViewById(R.id.labelInput)).getText().toString();
                    boolean isClassroom = ((android.widget.CheckBox) dialogView.findViewById(R.id.isClassroomCheck)).isChecked();
                    String classroomNumber = ((android.widget.EditText) dialogView.findViewById(R.id.classroomNumberInput)).getText().toString();

                    NavigationNode newNode = new NavigationNode(
                        hitPose.tx(),
                        hitPose.ty(),
                        hitPose.tz(),
                        label,
                        isClassroom,
                        isClassroom ? classroomNumber : ""
                    );
                    viewModel.addNode(newNode);
                    Toast.makeText(this, "Node added", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error adding node", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showConnectionDialog() {
        ArrayList<NavigationNode> possibleTargets = new ArrayList<>(adapter.getCurrentList());
        possibleTargets.remove(viewModel.getSelectedNode());

        String[] nodeLabels = possibleTargets.stream()
            .map(NavigationNode::getLabel)
            .toArray(String[]::new);

        new MaterialAlertDialogBuilder(this)
            .setTitle("Connect to Node")
            .setItems(nodeLabels, (dialog, which) -> {
                try {
                    NavigationNode targetNode = possibleTargets.get(which);
                    viewModel.createConnection(targetNode);
                    Toast.makeText(this, "Nodes connected", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error connecting nodes", Toast.LENGTH_SHORT).show();
                }
            })
            .show();
    }

    private void showNodeOptionsDialog(NavigationNode node) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Node Options")
            .setItems(new String[]{"Edit", "Delete", "Connect"}, (dialog, which) -> {
                switch (which) {
                    case 0: // Edit
                        showEditNodeDialog(node);
                        break;
                    case 1: // Delete
                        viewModel.deleteNode(node);
                        break;
                    case 2: // Connect
                        viewModel.setSelectedNode(node);
                        showConnectionDialog();
                        break;
                }
            })
            .show();
    }

    private void showEditNodeDialog(NavigationNode node) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_node, null);
        ((android.widget.EditText) dialogView.findViewById(R.id.labelInput)).setText(node.getLabel());
        ((android.widget.CheckBox) dialogView.findViewById(R.id.isClassroomCheck)).setChecked(node.isClassroom());
        ((android.widget.EditText) dialogView.findViewById(R.id.classroomNumberInput)).setText(node.getClassroomNumber());

        new MaterialAlertDialogBuilder(this)
            .setTitle("Edit Node")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                try {
                    String label = ((android.widget.EditText) dialogView.findViewById(R.id.labelInput)).getText().toString();
                    boolean isClassroom = ((android.widget.CheckBox) dialogView.findViewById(R.id.isClassroomCheck)).isChecked();
                    String classroomNumber = ((android.widget.EditText) dialogView.findViewById(R.id.classroomNumberInput)).getText().toString();

                    node.setLabel(label);
                    node.setClassroom(isClassroom);
                    node.setClassroomNumber(classroomNumber);
                    viewModel.updateNode(node);
                    Toast.makeText(this, "Node updated", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error updating node", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void highlightNode(NavigationNode node) {
        if (selectedNode != null) {
            // Reset previous selection
            selectedNode.setModelScale(new Float3(1.0f, 1.0f, 1.0f));
        }
        // Find and highlight the new selection
        for (Node arNode : arSceneView.getChildren()) {
            Float3 pos = arNode.getPosition();
            if (pos.get(0) == node.getX() && pos.get(1) == node.getY() && pos.get(2) == node.getZ()) {
                if (arNode instanceof ArNode) {
                    selectedNode = (ArNode) arNode;
                    selectedNode.setModelScale(new Float3(1.5f, 1.5f, 1.5f)); // Scale up to highlight
                }
                break;
            }
        }
    }

    private void updateArNodes(List<NavigationNode> nodes) {
        // Clear existing nodes
        for (Node child : arSceneView.getChildren()) {
            arSceneView.removeChild(child);
        }
        // Add new nodes
        for (NavigationNode node : nodes) {
            ArNode arNode = new ArNode(arSceneView.getEngine());
            arNode.setPosition(new Float3(node.getX(), node.getY(), node.getZ()));
            // If you have a model, load and set it here:
            // if (node.isClassroom()) {
            //     arNode.setModel(yourClassroomModel);
            // } else {
            //     arNode.setModel(yourRegularNodeModel);
            // }
            arSceneView.addChild(arNode);
        }
    }

    private void showFatalError(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Close", (dialog, which) -> finish())
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}