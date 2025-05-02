package com.example.manzil03.data;

import com.example.manzil03.model.NavigationNode;
import com.example.manzil03.model.NavigationEdge;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleDataset {
    public static void populateDatabase(NavigationDao navigationDao) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Create sample nodes
            List<NavigationNode> nodes = new ArrayList<>();
            
            // Classrooms
            nodes.add(createNode("Room 101", true, "101", 0, 0, 0));
            nodes.add(createNode("Room 102", true, "102", 5, 0, 0));
            nodes.add(createNode("Room 103", true, "103", 10, 0, 0));
            nodes.add(createNode("Room 201", true, "201", 0, 0, 5));
            nodes.add(createNode("Room 202", true, "202", 5, 0, 5));
            
            // Navigation points
            nodes.add(createNode("Hallway 1", false, "", 2.5f, 0, 0));
            nodes.add(createNode("Hallway 2", false, "", 7.5f, 0, 0));
            nodes.add(createNode("Hallway 3", false, "", 2.5f, 0, 5));
            nodes.add(createNode("Hallway 4", false, "", 7.5f, 0, 5));
            nodes.add(createNode("Central Point", false, "", 5, 0, 2.5f));

            // Insert nodes and get their IDs
            List<Long> nodeIds = new ArrayList<>();
            for (NavigationNode node : nodes) {
                long id = navigationDao.insertNode(node);
                nodeIds.add(id);
            }

            // Create connections between nodes
            List<NavigationEdge> edges = new ArrayList<>();
            
            // Connect hallway points
            addEdge(edges, nodeIds.get(5), nodeIds.get(6), calculateDistance(nodes.get(5), nodes.get(6))); // Hallway 1 - Hallway 2
            addEdge(edges, nodeIds.get(7), nodeIds.get(8), calculateDistance(nodes.get(7), nodes.get(8))); // Hallway 3 - Hallway 4
            addEdge(edges, nodeIds.get(5), nodeIds.get(7), calculateDistance(nodes.get(5), nodes.get(7))); // Hallway 1 - Hallway 3
            addEdge(edges, nodeIds.get(6), nodeIds.get(8), calculateDistance(nodes.get(6), nodes.get(8))); // Hallway 2 - Hallway 4
            
            // Connect rooms to nearest hallway points
            addEdge(edges, nodeIds.get(0), nodeIds.get(5), calculateDistance(nodes.get(0), nodes.get(5))); // Room 101 - Hallway 1
            addEdge(edges, nodeIds.get(1), nodeIds.get(6), calculateDistance(nodes.get(1), nodes.get(6))); // Room 102 - Hallway 2
            addEdge(edges, nodeIds.get(2), nodeIds.get(6), calculateDistance(nodes.get(2), nodes.get(6))); // Room 103 - Hallway 2
            addEdge(edges, nodeIds.get(3), nodeIds.get(7), calculateDistance(nodes.get(3), nodes.get(7))); // Room 201 - Hallway 3
            addEdge(edges, nodeIds.get(4), nodeIds.get(8), calculateDistance(nodes.get(4), nodes.get(8))); // Room 202 - Hallway 4

            // Connect to central point
            addEdge(edges, nodeIds.get(5), nodeIds.get(9), calculateDistance(nodes.get(5), nodes.get(9))); // Hallway 1 - Central
            addEdge(edges, nodeIds.get(6), nodeIds.get(9), calculateDistance(nodes.get(6), nodes.get(9))); // Hallway 2 - Central
            addEdge(edges, nodeIds.get(7), nodeIds.get(9), calculateDistance(nodes.get(7), nodes.get(9))); // Hallway 3 - Central
            addEdge(edges, nodeIds.get(8), nodeIds.get(9), calculateDistance(nodes.get(8), nodes.get(9))); // Hallway 4 - Central

            // Insert all edges
            for (NavigationEdge edge : edges) {
                navigationDao.insertEdge(edge);
            }
        });
        executor.shutdown();
    }

    private static NavigationNode createNode(String label, boolean isClassroom, String classroomNumber, 
                                           float x, float y, float z) {
        return new NavigationNode(x, y, z, label, isClassroom, classroomNumber);
    }

    private static void addEdge(List<NavigationEdge> edges, long sourceId, long targetId, float weight) {
        edges.add(new NavigationEdge(sourceId, targetId, weight));
    }

    private static float calculateDistance(NavigationNode a, NavigationNode b) {
        float dx = a.getX() - b.getX();
        float dy = a.getY() - b.getY();
        float dz = a.getZ() - b.getZ();
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}