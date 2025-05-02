package com.example.manzil03.navigation;

import com.example.manzil03.model.NavigationNode;
import com.example.manzil03.model.NavigationEdge;
import java.util.*;

public class PathFinder {
    private static class NodeInfo implements Comparable<NodeInfo> {
        NavigationNode node;
        float gScore;
        float fScore;
        NodeInfo parent;

        NodeInfo(NavigationNode node, float gScore, float fScore, NodeInfo parent) {
            this.node = node;
            this.gScore = gScore;
            this.fScore = fScore;
            this.parent = parent;
        }

        @Override
        public int compareTo(NodeInfo other) {
            return Float.compare(this.fScore, other.fScore);
        }
    }

    public static List<NavigationNode> findPath(
            NavigationNode start,
            NavigationNode goal,
            List<NavigationNode> allNodes,
            List<NavigationEdge> allEdges) {
        
        PriorityQueue<NodeInfo> openSet = new PriorityQueue<>();
        Map<Long, NodeInfo> nodeMap = new HashMap<>();
        Set<Long> closedSet = new HashSet<>();

        NodeInfo startInfo = new NodeInfo(start, 0, heuristic(start, goal), null);
        openSet.add(startInfo);
        nodeMap.put(start.getId(), startInfo);

        while (!openSet.isEmpty()) {
            NodeInfo current = openSet.poll();

            if (current.node.getId() == goal.getId()) {
                return reconstructPath(current);
            }

            closedSet.add(current.node.getId());

            // Get neighbors from edges
            for (NavigationEdge edge : allEdges) {
                if (edge.getSourceNodeId() == current.node.getId() || 
                    edge.getTargetNodeId() == current.node.getId()) {
                    
                    long neighborId = edge.getSourceNodeId() == current.node.getId() 
                        ? edge.getTargetNodeId() 
                        : edge.getSourceNodeId();

                    if (closedSet.contains(neighborId)) continue;

                    NavigationNode neighbor = findNodeById(allNodes, neighborId);
                    if (neighbor == null) continue;

                    float tentativeGScore = current.gScore + edge.getWeight();

                    NodeInfo neighborInfo = nodeMap.get(neighborId);
                    if (neighborInfo == null) {
                        neighborInfo = new NodeInfo(
                            neighbor,
                            tentativeGScore,
                            tentativeGScore + heuristic(neighbor, goal),
                            current
                        );
                        nodeMap.put(neighborId, neighborInfo);
                        openSet.add(neighborInfo);
                    } else if (tentativeGScore < neighborInfo.gScore) {
                        neighborInfo.parent = current;
                        neighborInfo.gScore = tentativeGScore;
                        neighborInfo.fScore = tentativeGScore + heuristic(neighbor, goal);
                        // Refresh position in priority queue
                        openSet.remove(neighborInfo);
                        openSet.add(neighborInfo);
                    }
                }
            }
        }

        return Collections.emptyList(); // No path found
    }

    private static float heuristic(NavigationNode a, NavigationNode b) {
        float dx = a.getX() - b.getX();
        float dy = a.getY() - b.getY();
        float dz = a.getZ() - b.getZ();
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static List<NavigationNode> reconstructPath(NodeInfo goal) {
        List<NavigationNode> path = new ArrayList<>();
        NodeInfo current = goal;
        while (current != null) {
            path.add(0, current.node);
            current = current.parent;
        }
        return path;
    }

    private static NavigationNode findNodeById(List<NavigationNode> nodes, long id) {
        for (NavigationNode node : nodes) {
            if (node.getId() == id) return node;
        }
        return null;
    }
}