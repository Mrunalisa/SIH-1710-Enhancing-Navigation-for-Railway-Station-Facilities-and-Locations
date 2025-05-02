package com.example.manzil03.navigation;

import com.example.manzil03.model.NavigationNode;
import java.util.ArrayList;
import java.util.List;

public class PathSmoother {
    private static final int SEGMENTS_PER_CURVE = 20;

    public static List<Point3D> smoothPath(List<NavigationNode> path) {
        if (path.size() < 2) {
            return convertToPoints(path);
        }

        List<Point3D> controlPoints = generateControlPoints(path);
        return generateBezierPath(controlPoints);
    }

    private static List<Point3D> convertToPoints(List<NavigationNode> nodes) {
        List<Point3D> points = new ArrayList<>();
        for (NavigationNode node : nodes) {
            points.add(new Point3D(node.getX(), node.getY(), node.getZ()));
        }
        return points;
    }

    private static List<Point3D> generateControlPoints(List<NavigationNode> path) {
        List<Point3D> controlPoints = new ArrayList<>();
        
        // First point
        controlPoints.add(new Point3D(path.get(0).getX(), path.get(0).getY(), path.get(0).getZ()));

        // Generate control points between nodes
        for (int i = 0; i < path.size() - 1; i++) {
            NavigationNode current = path.get(i);
            NavigationNode next = path.get(i + 1);

            // Calculate direction vector
            float dx = next.getX() - current.getX();
            float dy = next.getY() - current.getY();
            float dz = next.getZ() - current.getZ();
            float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

            // Create control points at 1/3 and 2/3 of the distance
            Point3D p1 = new Point3D(
                current.getX() + dx * 0.33f,
                current.getY() + dy * 0.33f,
                current.getZ() + dz * 0.33f
            );
            Point3D p2 = new Point3D(
                current.getX() + dx * 0.67f,
                current.getY() + dy * 0.67f,
                current.getZ() + dz * 0.67f
            );

            controlPoints.add(p1);
            controlPoints.add(p2);
        }

        // Last point
        NavigationNode last = path.get(path.size() - 1);
        controlPoints.add(new Point3D(last.getX(), last.getY(), last.getZ()));

        return controlPoints;
    }

    private static List<Point3D> generateBezierPath(List<Point3D> controlPoints) {
        List<Point3D> smoothPath = new ArrayList<>();
        
        for (int i = 0; i < controlPoints.size() - 3; i += 3) {
            Point3D p0 = controlPoints.get(i);
            Point3D p1 = controlPoints.get(i + 1);
            Point3D p2 = controlPoints.get(i + 2);
            Point3D p3 = controlPoints.get(i + 3);

            for (int j = 0; j <= SEGMENTS_PER_CURVE; j++) {
                float t = j / (float) SEGMENTS_PER_CURVE;
                smoothPath.add(calculateBezierPoint(t, p0, p1, p2, p3));
            }
        }

        return smoothPath;
    }

    private static Point3D calculateBezierPoint(float t, Point3D p0, Point3D p1, Point3D p2, Point3D p3) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        float x = uuu * p0.x + 3 * uu * t * p1.x + 3 * u * tt * p2.x + ttt * p3.x;
        float y = uuu * p0.y + 3 * uu * t * p1.y + 3 * u * tt * p2.y + ttt * p3.y;
        float z = uuu * p0.z + 3 * uu * t * p1.z + 3 * u * tt * p2.z + ttt * p3.z;

        return new Point3D(x, y, z);
    }

    public static class Point3D {
        public final float x, y, z;

        public Point3D(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}