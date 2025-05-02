package com.example.manzil03.navigation;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.manzil03.data.AppDatabase;
import com.example.manzil03.data.NavigationDao;
import com.example.manzil03.model.NavigationNode;
import com.example.manzil03.model.NavigationEdge;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NavigationViewModel extends AndroidViewModel {
    private final NavigationDao navigationDao;
    private final ExecutorService executorService;
    private final MutableLiveData<List<PathSmoother.Point3D>> currentPath;
    private NavigationNode currentLocation;
    private String destinationNumber;

    public NavigationViewModel(Application application) {
        super(application);
        navigationDao = AppDatabase.getDatabase(application).navigationDao();
        executorService = Executors.newSingleThreadExecutor();
        currentPath = new MutableLiveData<>();
    }

    public void updateCurrentLocation(String classroomNumber) {
        executorService.execute(() -> {
            NavigationNode node = navigationDao.findNodeByClassroomNumber(classroomNumber);
            if (node != null && (currentLocation == null || currentLocation.getId() != node.getId())) {
                currentLocation = node;
                updatePath();
            }
        });
    }

    public void setDestination(String classroomNumber) {
        this.destinationNumber = classroomNumber;
        updatePath();
    }

    private void updatePath() {
        if (currentLocation == null || destinationNumber == null) return;

        executorService.execute(() -> {
            NavigationNode destination = navigationDao.findNodeByClassroomNumber(destinationNumber);
            if (destination == null) return;

            // Get all nodes and edges for pathfinding
            List<NavigationNode> allNodes = navigationDao.getAllNodes().getValue();
            List<NavigationEdge> allEdges = navigationDao.getAllEdges().getValue();
            
            if (allNodes == null || allEdges == null) return;

            // Find path using A* algorithm
            List<NavigationNode> path = PathFinder.findPath(
                currentLocation,
                destination,
                allNodes,
                allEdges
            );

            if (!path.isEmpty()) {
                // Smooth the path using Bezier curves
                List<PathSmoother.Point3D> smoothedPath = PathSmoother.smoothPath(path);
                currentPath.postValue(smoothedPath);
            }
        });
    }

    public LiveData<List<PathSmoother.Point3D>> getCurrentPath() {
        return currentPath;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}