package com.example.manzil03.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "navigation_edges",
        primaryKeys = {"sourceNodeId", "targetNodeId"},
        foreignKeys = {
            @ForeignKey(entity = NavigationNode.class,
                    parentColumns = "id",
                    childColumns = "sourceNodeId"),
            @ForeignKey(entity = NavigationNode.class,
                    parentColumns = "id",
                    childColumns = "targetNodeId")
        },
        indices = {
            @Index("sourceNodeId"),
            @Index("targetNodeId")
        })
public class NavigationEdge {
    private long sourceNodeId;
    private long targetNodeId;
    private float weight; // Distance between nodes

    public NavigationEdge(long sourceNodeId, long targetNodeId, float weight) {
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.weight = weight;
    }

    // Getters and setters
    public long getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(long sourceNodeId) { this.sourceNodeId = sourceNodeId; }
    public long getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(long targetNodeId) { this.targetNodeId = targetNodeId; }
    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }
}