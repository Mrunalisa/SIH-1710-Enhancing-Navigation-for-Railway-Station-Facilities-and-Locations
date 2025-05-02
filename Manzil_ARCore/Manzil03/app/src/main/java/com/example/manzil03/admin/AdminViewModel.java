package com.example.manzil03.admin;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.manzil03.data.AppDatabase;
import com.example.manzil03.data.NavigationDao;
import com.example.manzil03.model.NavigationNode;
import com.example.manzil03.model.NavigationEdge;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminViewModel extends AndroidViewModel {
    private final NavigationDao navigationDao;
    private final ExecutorService executorService;
    private NavigationNode selectedNode;

    public AdminViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        navigationDao = db.navigationDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<NavigationNode>> getAllNodes() {
        return navigationDao.getAllNodes();
    }

    public LiveData<List<NavigationEdge>> getAllEdges() {
        return navigationDao.getAllEdges();
    }

    public void addNode(NavigationNode node) {
        executorService.execute(() -> {
            navigationDao.insertNode(node);
        });
    }

    public void updateNode(NavigationNode node) {
        executorService.execute(() -> {
            navigationDao.updateNode(node);
        });
    }

    public void deleteNode(NavigationNode node) {
        executorService.execute(() -> {
            navigationDao.deleteNodeAndConnections(node);
        });
    }

    public void createConnection(NavigationNode targetNode) {
        if (selectedNode == null) return;
        
        executorService.execute(() -> {
            // Calculate weight (distance) between nodes
            float dx = targetNode.getX() - selectedNode.getX();
            float dy = targetNode.getY() - selectedNode.getY();
            float dz = targetNode.getZ() - selectedNode.getZ();
            float weight = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

            NavigationEdge edge = new NavigationEdge(
                selectedNode.getId(),
                targetNode.getId(),
                weight
            );
            navigationDao.insertEdge(edge);
        });
    }

    public void setSelectedNode(NavigationNode node) {
        this.selectedNode = node;
    }

    public NavigationNode getSelectedNode() {
        return selectedNode;
    }

    public void deleteSelectedNode() {
        if (selectedNode != null) {
            deleteNode(selectedNode);
            selectedNode = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}