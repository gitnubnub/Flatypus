package si.uni_lj.fe.tnuv.flatypus.ui.to_do;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;

public class ToDoViewModel extends ViewModel {

    // Task data class
    public static class Task {
        private String name;
        private String assignee;
        private int profilePicture; // Resource name or URL (e.g., "platypus")
        private boolean isCompleted;
        private String repeat; // "none", "weekly", "monthly"
        public String apartmentCode;

        public Task(){}

        public Task(String name, String assignee, int profilePicture, boolean isCompleted, String repeat, String apartmentCode) {
            this.name = name;
            this.assignee = assignee;
            this.profilePicture = profilePicture;
            this.isCompleted = isCompleted;
            this.repeat = repeat;
            this.apartmentCode = apartmentCode;
        }

        public String getName() { return name; }
        public String getAssignee() { return assignee; }
        public int getProfilePicture() { return profilePicture; }
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { this.isCompleted = completed; }
        public String getRepeat() { return repeat; }
        public String getApartmentCode() { return apartmentCode; }
    }

    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> notificationCount = new MutableLiveData<>(0);
    private final MutableLiveData<List<String>> roommates = new MutableLiveData<>(new ArrayList<>());
    private String currentUser = "Eva"; // Replace with dynamic source if needed
    private DatabaseReference databaseReference;
    private String currentApartmentCode = ""; // Will be set from UserViewModel
    private UserViewModel userViewModel;


    public ToDoViewModel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://flatypus-fde01-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = database.getReference();
    }

    // Setter to initialize with shared UserViewModel
    public void initializeWithUserViewModel(UserViewModel userViewModel) {
        this.userViewModel = userViewModel;
        userViewModel.getCurrentUser().observeForever(user -> {
            if (user != null) {
                currentUser = user.getUsername(); // Update current user
                currentApartmentCode = user.getCurrentApartment(); // Update apartment code
                Log.d("ToDoViewModel", "Current user: " + currentUser + ", apartment: " + currentApartmentCode);
                if (!currentApartmentCode.isEmpty()) {
                    fetchRoommates();
                    fetchTasks();
                } else {
                    Log.w("ToDoViewModel", "currentApartmentCode is empty, skipping fetch");
                }
            } else {
                Log.w("ToDoViewModel", "currentUser is null");
            }
        });

        // Initial check
        UserViewModel.User initialUser = userViewModel.getCurrentUser().getValue();
        if (initialUser != null) {
            currentUser = initialUser.getUsername();
            currentApartmentCode = initialUser.getCurrentApartment();
            Log.d("ToDoViewModel", "Initial user: " + currentUser + ", apartment: " + currentApartmentCode);
            if (!currentApartmentCode.isEmpty()) {
                fetchRoommates();
                fetchTasks();
            }
        }
    }
    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public LiveData<Integer> getNotificationCount() {
        return notificationCount;
    }

    public LiveData<List<String>> getRoommates() {
        return roommates;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void addTask(String name, String assignee, String repeat) {
        List<Task> currentTasks = tasks.getValue();
        if (currentTasks == null) {
            currentTasks = new ArrayList<>();
        }
        if ("random".equalsIgnoreCase(assignee)) {
            List<String> currentRoommates = roommates.getValue();
            if (currentRoommates != null && !currentRoommates.isEmpty()) {
                Random random = new Random();
                assignee = currentRoommates.get(random.nextInt(currentRoommates.size()));
            } else {
                assignee = currentUser; // Fallback if no roommates
            }
        }
        final int[] profilePicture = {R.drawable.pfp_red};
        String finalAssignee = assignee;
        List<Task> finalCurrentTasks = currentTasks;
        databaseReference.child("users").orderByChild("username").equalTo(assignee)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            UserViewModel.User user = userSnapshot.getValue(UserViewModel.User.class);
                            if (user != null) {
                                profilePicture[0] = user.getProfilePicture();
                                Task newTask = new Task(name, finalAssignee, profilePicture[0], false, repeat, currentApartmentCode);
                                finalCurrentTasks.add(newTask);
                                tasks.setValue(finalCurrentTasks);

                                // Save to database
                                String taskId = databaseReference.child("tasks").push().getKey();
                                if (taskId != null) {
                                    databaseReference.child("tasks").child(taskId).setValue(newTask)
                                            .addOnSuccessListener(aVoid -> Log.d("AddTask", "Task added to database"))
                                            .addOnFailureListener(e -> Log.e("AddTask", "Failed to add task: " + e.getMessage()));
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("AddTask", "Database error: " + error.getMessage());
                    }
                });
    }

    public void updateTaskCompletion(int position, boolean isCompleted) {
        List<Task> currentTasks = tasks.getValue();
        if (currentTasks != null && position >= 0 && position < currentTasks.size()) {
            Task task = currentTasks.get(position);
            task.setCompleted(isCompleted);
            tasks.setValue(currentTasks);

            // Update in database
            databaseReference.child("tasks").orderByChild("name").equalTo(task.getName())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                dataSnapshot.getRef().child("isCompleted").setValue(isCompleted)
                                        .addOnSuccessListener(aVoid -> Log.d("UpdateTask", "Task completion updated"))
                                        .addOnFailureListener(e -> Log.e("UpdateTask", "Failed to update task: " + e.getMessage()));
                                break;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("UpdateTask", "Database error: " + error.getMessage());
                        }
                    });
        }
    }

    private void fetchRoommates() {
        if (currentApartmentCode.isEmpty()) {
            Log.w("FetchRoommates", "currentApartmentCode is empty, skipping fetch");
            return;
        }

        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> roommateList = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    UserViewModel.User user = userSnapshot.getValue(UserViewModel.User.class);
                    if (user != null && user.getApartments().contains(currentApartmentCode)) {
                        roommateList.add(user.getUsername());
                    }
                }
                roommates.setValue(roommateList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FetchRoommates", "Database error: " + error.getMessage());
            }
        });
    }
    private void fetchTasks() {
        if (currentApartmentCode.isEmpty()) {
            Log.w("FetchTasks", "currentApartmentCode is empty, skipping fetch");
            return;
        }

        databaseReference.child("tasks").orderByChild("apartmentCode").equalTo(currentApartmentCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Task> taskList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Task task = dataSnapshot.getValue(Task.class);
                            if (task != null) {
                                taskList.add(task);
                            }
                        }
                        tasks.setValue(taskList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FetchTasks", "Database error: " + error.getMessage());
                    }
                });
    }
}
