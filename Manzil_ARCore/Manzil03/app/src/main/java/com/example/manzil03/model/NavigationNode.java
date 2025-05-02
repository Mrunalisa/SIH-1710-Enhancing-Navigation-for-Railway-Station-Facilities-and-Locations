package com.example.manzil03.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "navigation_nodes")
public class NavigationNode {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private float x;
    private float y;
    private float z;
    private String label;
    private boolean isClassroom;
    private String classroomNumber;

    public NavigationNode(float x, float y, float z, String label, boolean isClassroom, String classroomNumber) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.label = label;
        this.isClassroom = isClassroom;
        this.classroomNumber = classroomNumber;
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getZ() { return z; }
    public void setZ(float z) { this.z = z; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isClassroom() { return isClassroom; }
    public void setClassroom(boolean classroom) { isClassroom = classroom; }
    public String getClassroomNumber() { return classroomNumber; }
    public void setClassroomNumber(String classroomNumber) { this.classroomNumber = classroomNumber; }
}