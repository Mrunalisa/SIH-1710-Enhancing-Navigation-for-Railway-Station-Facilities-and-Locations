package com.example.manzil03.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.manzil03.model.NavigationNode;
import com.example.manzil03.model.NavigationEdge;
import java.util.List;

@Dao
public interface NavigationDao {
    @Query("SELECT * FROM navigation_nodes")
    LiveData<List<NavigationNode>> getAllNodes();

    @Query("SELECT * FROM navigation_edges")
    LiveData<List<NavigationEdge>> getAllEdges();

    @Query("SELECT * FROM navigation_nodes WHERE id = :nodeId")
    NavigationNode getNodeById(long nodeId);

    @Query("SELECT * FROM navigation_nodes WHERE classroomNumber = :number AND isClassroom = 1")
    NavigationNode findNodeByClassroomNumber(String number);

    @Insert
    long insertNode(NavigationNode node);

    @Update
    void updateNode(NavigationNode node);

    @Delete
    void deleteNode(NavigationNode node);

    @Insert
    void insertEdge(NavigationEdge edge);

    @Delete
    void deleteEdge(NavigationEdge edge);

    @Query("DELETE FROM navigation_edges WHERE sourceNodeId = :nodeId OR targetNodeId = :nodeId")
    void deleteEdgesForNode(long nodeId);

    @Transaction
    default void deleteNodeAndConnections(NavigationNode node) {
        deleteEdgesForNode(node.getId());
        deleteNode(node);
    }

    @Query("SELECT * FROM navigation_edges WHERE sourceNodeId = :nodeId OR targetNodeId = :nodeId")
    List<NavigationEdge> getEdgesForNode(long nodeId);
}