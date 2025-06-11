package si.uni_lj.fe.tnuv.flatypus.ui.notifications;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsViewModel extends ViewModel {

    public static class Notification {
        private String id;
        private String apartment;
        private String user;
        private String text;
        private boolean seen;

        public Notification() {}

        public Notification(String id, String apartment, String user, String text, boolean seen) {
            this.id = id;
            this.apartment = apartment;
            this.user = user;
            this.text = text;
            this.seen = seen;
        }

        public String getId() {
            return id;
        }
        public String getApartment() {
            return apartment;
        }
        public String getUser() {
            return user;
        }
        public String getText() {
            return text;
        }
        public boolean isSeen() {
            return seen;
        }
        public void setId(String id) {
            this.id = id;
        }
        public void setSeen(boolean seen) {
            this.seen = seen;
        }
    }

    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>(new ArrayList<>());
    private final LiveData<Integer> unseen = Transformations.map(notifications, list -> {
        int counter = 0;
        if (list != null) {
            for (Notification notif : list) {
                if (!notif.isSeen()) {
                    counter++;
                }
            }
        }
        return counter;
    });
    private DatabaseReference databaseReference;

    public NotificationsViewModel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://flatypus-fde01-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = database.getReference("notifications");
    }

    public LiveData<Integer> unseen() {
        return unseen; // Return the reactive LiveData
    }

    public LiveData<List<Notification>> getNotifications(String apartment, String user) {
        databaseReference.orderByChild("apartment").equalTo(apartment)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Notification> userNotifications = new ArrayList<>();
                        for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                            Notification notification = notificationSnapshot.getValue(Notification.class);
                            if (notification != null) {
                                if (user.equals(notification.user)) {
                                    userNotifications.add(notification);
                                }
                            }
                        }

                        notifications.setValue(userNotifications);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        notifications.setValue(Collections.emptyList());
                        Log.e("Notifications", "Database error: " + error.getMessage());
                    }
                });

        return notifications;
    }

    public void addNotification(String apartment, String user, String text) {
        Notification newNotification = new Notification("", apartment, user, text, false);
        String notificationId = databaseReference.push().getKey();
        if (notificationId != null) {
            newNotification.setId(notificationId);
            databaseReference.child(notificationId).setValue(newNotification)
                    .addOnSuccessListener(aVoid ->
                            Log.d("Notifications", "New notification created.")
                    ).addOnFailureListener(e ->
                            Log.e("Notifications", "Failed to create new notification: " + e.getMessage())
                    );
        }
    }

    public void updateStatus() {
        List<Notification> currentNotifications = notifications.getValue();

        for (Notification updatee : currentNotifications) {
            if (!updatee.isSeen()) {
                String notificationId = updatee.getId();

                if (notificationId != null && !notificationId.isEmpty()) {
                    databaseReference.child(notificationId).child("seen").setValue(true)
                            .addOnSuccessListener(aVoid ->
                                    Log.d("Notifications", "Status updated in Firebase.")
                            ).addOnFailureListener(e ->
                                    Log.e("Notifications", "Failed to update status: " + e.getMessage())
                            );
                } else {
                    Log.e("Notifications", "Notification ID missing. Cannot update Firebase.");
                }
            }
        }
    }
}