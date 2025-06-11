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
import si.uni_lj.fe.tnuv.flatypus.ui.notifications.NotificationsViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;

public class ToDoViewModel extends ViewModel {

    // Task data class
    public static class Task {
        private String name;
        private String assignee; // Now stores email
        private boolean isCompleted;
        private String repeat;
        public String apartmentCode;
        private String taskId;

        public Task() {}

        public Task(String name, String assignee, boolean isCompleted, String repeat, String apartmentCode, String taskId) {
            this.name = name;
            this.assignee = assignee; // Email
            this.isCompleted = isCompleted;
            this.repeat = repeat;
            this.apartmentCode = apartmentCode;
            this.taskId = taskId;
        }

        public String getName() { return name; }
        public String getAssignee() { return assignee; }
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { this.isCompleted = completed; }
        public String getRepeat() { return repeat; }
        public String getApartmentCode() { return apartmentCode; }
        public String getTaskId() { return taskId; }
    }

    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> notificationCount = new MutableLiveData<>(0);
    private final MutableLiveData<List<String>> roommates = new MutableLiveData<>(new ArrayList<>());
    private String currentUserEmail; // Changed to email
    private DatabaseReference databaseReference;
    private String currentApartmentCode = ""; // Will be set from UserViewModel
    private UserViewModel userViewModel;
    private NotificationsViewModel notifViewModel;

    public ToDoViewModel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://flatypus-fde01-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = database.getReference();
    }

    public void initializeWithUserViewModel(UserViewModel userViewModel, NotificationsViewModel notifViewModel) {
        this.notifViewModel = notifViewModel;
        this.userViewModel = userViewModel;
        userViewModel.getCurrentUser().observeForever(user -> {
            if (user != null) {
                currentUserEmail = user.getEmail(); // Use email
                currentApartmentCode = user.getCurrentApartment(); // Update apartment code
                Log.d("ToDoViewModel", "Current user email: " + currentUserEmail + ", apartment: " + currentApartmentCode);
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

        UserViewModel.User initialUser = userViewModel.getCurrentUser().getValue();
        if (initialUser != null) {
            currentUserEmail = initialUser.getEmail();
            currentApartmentCode = initialUser.getCurrentApartment();
            Log.d("ToDoViewModel", "Initial user email: " + currentUserEmail + ", apartment: " + currentApartmentCode);
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
        return currentUserEmail; // Return email
    }

    public void addTask(String name, String assigneeEmail, String repeat) {
        List<Task> currentTasks = tasks.getValue();
        if (currentTasks == null) {
            currentTasks = new ArrayList<>();
        }
        if ("random".equalsIgnoreCase(assigneeEmail)) {
            List<String> currentRoommates = roommates.getValue();
            if (currentRoommates != null && !currentRoommates.isEmpty()) {
                Random random = new Random();
                String randomUsername = currentRoommates.get(random.nextInt(currentRoommates.size()));
                // Use a final local variable to store the updated email
                final String[] updatedAssigneeEmail = new String[1]; // Array to allow modification in lambda
                userViewModel.getRoommates(currentApartmentCode).observeForever(users -> {
                    for (UserViewModel.User user : users) {
                        if (user.getUsername().equals(randomUsername)) {
                            updatedAssigneeEmail[0] = user.getEmail();
                            addTaskToListAndDatabase(name, updatedAssigneeEmail[0], repeat);
                            notifViewModel.addNotification(currentApartmentCode, updatedAssigneeEmail[0], "You have a new task: " + name);
                            break;
                        }
                    }
                });
            } else {
                assigneeEmail = currentUserEmail; // Fallback
                addTaskToListAndDatabase(name, assigneeEmail, repeat);
            }
        } else {
            addTaskToListAndDatabase(name, assigneeEmail, repeat);
        }
    }

    private void addTaskToListAndDatabase(String name, String assigneeEmail, String repeat) {
        List<Task> currentTasks = tasks.getValue();
        if (currentTasks == null) currentTasks = new ArrayList<>();

        String taskId = databaseReference.child("tasks").push().getKey();
        Task newTask = new Task(name, assigneeEmail, false, repeat, currentApartmentCode, taskId);
        currentTasks.add(newTask);
        tasks.setValue(currentTasks);

        if (taskId != null) {
            databaseReference.child("tasks").child(taskId).setValue(newTask)
                    .addOnSuccessListener(aVoid -> Log.d("AddTask", "Task added to database with ID: " + taskId))
                    .addOnFailureListener(e -> Log.e("AddTask", "Failed to add task: " + e.getMessage()));
        }
    }

    public void updateTaskCompletion(int position, boolean isChecked) {
        List<Task> currentTasks = tasks.getValue();
        if (currentTasks != null && position >= 0 && position < currentTasks.size()) {
            Task task = currentTasks.get(position);
            boolean newCompletedStatus = !task.isCompleted();
            task.setCompleted(newCompletedStatus);
            currentTasks.set(position, task);
            tasks.setValue(currentTasks);

            String taskId = task.getTaskId();
            if (taskId != null) {
                databaseReference.child("tasks").child(taskId).child("isCompleted").setValue(newCompletedStatus)
                        .addOnSuccessListener(aVoid -> Log.d("UpdateTask", "Task completion toggled to " + newCompletedStatus + " for ID: " + taskId))
                        .addOnFailureListener(e -> Log.e("UpdateTask", "Failed to update task: " + e.getMessage()));
            } else {
                Log.w("UpdateTask", "Task ID is null, cannot update database");
            }
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

    public void fetchTasks() {
        if (currentApartmentCode.isEmpty()) {
            Log.w("FetchTasks", "currentApartmentCode is empty, skipping fetch");
            return;
        }

        databaseReference.child("tasks").orderByChild("apartmentCode").equalTo(currentApartmentCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Task> taskList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Task task = dataSnapshot.getValue(Task.class);
                            if (task != null) {
                                task.taskId = dataSnapshot.getKey();
                                Boolean isCompletedFromDb = dataSnapshot.child("isCompleted").getValue(Boolean.class);
                                if (isCompletedFromDb != null && isCompletedFromDb) {
                                    dataSnapshot.getRef().removeValue()
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("FetchTasks", "Deleted completed task with ID: " + task.getTaskId());
                                                fetchTasks();
                                            })
                                            .addOnFailureListener(e -> Log.e("FetchTasks", "Failed to delete task: " + e.getMessage()));
                                } else {
                                    taskList.add(task);
                                }
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
    public void fetchTasks(String currentApartmentCode) {
        if (currentApartmentCode.isEmpty()) {
            Log.w("FetchTasks", "currentApartmentCode is empty, skipping fetch");
            return;
        }

        databaseReference.child("tasks").orderByChild("apartmentCode").equalTo(currentApartmentCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Task> taskList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Task task = dataSnapshot.getValue(Task.class);
                            if (task != null) {
                                task.taskId = dataSnapshot.getKey();
                                Boolean isCompletedFromDb = dataSnapshot.child("isCompleted").getValue(Boolean.class);
                                if (isCompletedFromDb != null && isCompletedFromDb) {
                                    dataSnapshot.getRef().removeValue()
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("FetchTasks", "Deleted completed task with ID: " + task.getTaskId());
                                                fetchTasks();
                                            })
                                            .addOnFailureListener(e -> Log.e("FetchTasks", "Failed to delete task: " + e.getMessage()));
                                } else {
                                    taskList.add(task);
                                }
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

    // New method to count incomplete tasks for a specific user email
    public int getIncompleteTasksForUser(String userEmail, String currentApartmentCode) {
        List<Task> currentTasks = tasks.getValue();
        int incompleteCount = 0;

        if (currentTasks != null) {
            for (Task task : currentTasks) {
                Log.d("GetIncompleteTasks", "Evaluating task: Assignee=" + task.getAssignee() +
                        ", UserEmail=" + userEmail + ", isCompleted=" + task.isCompleted() +
                        ", Apartment=" + task.getApartmentCode() + ", CurrentApartment=" + currentApartmentCode);
                if (task.getAssignee() != null && userEmail != null &&
                        task.getAssignee().equalsIgnoreCase(userEmail) &&
                        !task.isCompleted() &&
                        task.getApartmentCode().equals(currentApartmentCode)) {
                    incompleteCount++;
                    Log.d("GetIncompleteTasks", "Match found for task: " + task.getName());
                }
            }
        } else {
            Log.w("GetIncompleteTasks", "Current tasks list is null");
        }

        Log.d("GetIncompleteTasks", "User: " + userEmail + ", Incomplete Count: " + incompleteCount + ", Apartment: " + currentApartmentCode);
        return incompleteCount;
    }
}