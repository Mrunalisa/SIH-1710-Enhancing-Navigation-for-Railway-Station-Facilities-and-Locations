package com.example.manzil03.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.manzil03.model.NavigationNode;
import com.example.manzil03.model.NavigationEdge;

@Database(
    entities = {NavigationNode.class, NavigationEdge.class}, 
    version = 1,
    exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract NavigationDao navigationDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "navigation_database"
                    )
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            // Populate with sample data when database is created
                            SampleDataset.populateDatabase(INSTANCE.navigationDao());
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}