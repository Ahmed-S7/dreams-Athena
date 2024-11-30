package com.example.athena.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.example.athena.Controllers.NotificationController;
import com.example.athena.Views.NotificationView;
import com.example.athena.Models.Notification;
import com.example.athena.Models.detailsForNotification;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * Service class that runs in the background and runs the logic for checking and
 * building notifications
 */
public class NotificationService extends Service {
    // Service to listen for changes in the database
    private NotificationView notificationView;
    private NotificationController notificationController;
    private String deviceId;
    /**
     * Called at the beginning of the service's lifetime, begins the notification checker loop
     * @param intent - contains data needed by the service
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // initialize controller, view, and deviceId
        this.deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        this.notificationView = new NotificationView();
        this.notificationController = new NotificationController(this, deviceId);

        // Start the periodic notification checker
        startNotificationListener();

        // Restarts the service if it is killed at any point
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // not binding to anything
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Starts the service listener checking for notifications and sending them
     */
    private void startNotificationListener() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String refPath = "Users/" + deviceId + "/Events";
        CollectionReference userEventsRef = db.collection(refPath);

        userEventsRef.addSnapshotListener((snapshots, exception) -> {
            if (exception != null) {
                Log.w("notificationListener", "Error in listener", exception);
                return;
            }
            Log.w("notificationListener", "listener is working");
            if (snapshots != null && !snapshots.isEmpty()) {
                for (DocumentChange change : snapshots.getDocumentChanges()) {
                    DocumentSnapshot userEvent = change.getDocument();
                    Boolean isNotified = userEvent.getBoolean("isNotified");

                    if (change.getType() == DocumentChange.Type.MODIFIED && Boolean.FALSE.equals(isNotified)) {
                        // call notification function
                        String status = userEvent.getString("status");
                        String eventId = userEvent.getId();
                        Log.w("notificationListener", "processing eventId: "+eventId);
                        handleNotificationEvent(eventId, status, userEvent.getReference());
                    }
                }
            }
        });
    }

    public void handleNotificationEvent(String eventId, String status, DocumentReference eventRef) {
        notificationView.getNotificationData(eventId, status, new NotificationView.NotificationDataCallback() {
            @Override
            public void onDataRetrieved(detailsForNotification notifDetatils) {
                boolean valid = notificationController.checkValidNotification(notifDetatils);
                if (valid) {

                    Notification notification = notificationController.convertToNotification(notifDetatils);
                    serviceShowNotification(notification);

                    // set isNotified back to true
                    eventRef.update("isNotified", true)
                            .addOnSuccessListener(aVoid -> Log.w("notificationListener", "isNotified reset to false"))
                            .addOnFailureListener(err -> Log.w("notificationListener", "Failed to reset isNotified", err));
                } else {
                    Log.w("notificationListener", "invalid notification");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d("handleNotificationEvent","Error occurred:" + e);
            }
        });
    }

    public void serviceShowNotification(Notification notification) {
        notificationController.showNotification(this, notification);
    }
}
