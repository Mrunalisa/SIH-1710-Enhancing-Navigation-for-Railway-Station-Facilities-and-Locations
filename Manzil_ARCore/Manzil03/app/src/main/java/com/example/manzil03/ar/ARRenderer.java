package com.example.manzil03.ar;

import android.content.Context;
import com.example.manzil03.navigation.PathSmoother.Point3D;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import io.github.sceneview.ar.ArSceneView;
import io.github.sceneview.ar.node.ArNode;
import io.github.sceneview.node.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.math.Vector3;
import dev.romainguy.kotlin.math.Float3;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.ArrayList;
import java.util.List;

public class ARRenderer {
    private final ArSceneView sceneView;
    private final List<ArNode> pathNodes = new ArrayList<>();
    private float pathRenderDistance = 5.0f; // Default render distance in meters

    public ARRenderer(ArSceneView sceneView) {
        this.sceneView = sceneView;
    }

    public void setPathRenderDistance(float distance) {
        this.pathRenderDistance = distance;
    }

    public void renderPath(List<Point3D> smoothedPath) throws CameraNotAvailableException {
        clearPath();
        if (smoothedPath.size() < 2) return;

        // Get the current AR frame and camera pose
        com.google.ar.core.Frame frame = sceneView.getArSession().update();
        Pose cameraPose = frame.getCamera().getPose();

        // Convert path points to world space positions
        List<float[]> positions = new ArrayList<>();
        for (Point3D point : smoothedPath) {
            float[] point4 = {point.x, point.y, point.z, 1};
            // For simplicity, just use the point as is (no transform)
            positions.add(new float[]{point.x, point.y, point.z});
        }

        // Render the path as a series of small spheres between points
        for (float[] pos : positions) {
            ArNode sphereNode = createPathSphereNode(pos);
            sceneView.addChild(sphereNode);
            pathNodes.add(sphereNode);
        }

        // Add distance markers
        addDistanceMarkers(positions);
    }

    private void addDistanceMarkers(List<float[]> positions) {
        float accumulatedDistance = 0;
        float[] lastPosition = positions.get(0);
        for (int i = 1; i < positions.size(); i++) {
            float[] currentPosition = positions.get(i);
            float distance = distanceBetween(lastPosition, currentPosition);
            accumulatedDistance += distance;
            if (accumulatedDistance >= pathRenderDistance) {
                ArNode markerNode = createDistanceMarker(currentPosition);
                pathNodes.add(markerNode);
                sceneView.addChild(markerNode);
                accumulatedDistance = 0;
            }
            lastPosition = currentPosition;
        }
    }

    private ArNode createDistanceMarker(float[] position) {
        ArNode markerNode = new ArNode(sceneView.getEngine());
        markerNode.setPosition(new Float3(position[0], position[1], position[2]));
        // If you have a model, load and set it here:
        // markerNode.setModel(yourLoadedModel);
        return markerNode;
    }

    public void clearPath() {
        for (ArNode node : pathNodes) {
            sceneView.removeChild(node);
            node.getAnchor().detach();
        }
        pathNodes.clear();
    }

    public void onDestroy() {
        clearPath();
    }

    private float distanceBetween(float[] a, float[] b) {
        float dx = a[0] - b[0];
        float dy = a[1] - b[1];
        float dz = a[2] - b[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private ArNode createPathSphereNode(float[] pos) {
        ArNode node = new ArNode(sceneView.getEngine());
        node.setPosition(new Float3(pos[0], pos[1], pos[2]));
        // If you have a model, load and set it here:
        // node.setModel(yourLoadedModel);
        return node;
    }
}