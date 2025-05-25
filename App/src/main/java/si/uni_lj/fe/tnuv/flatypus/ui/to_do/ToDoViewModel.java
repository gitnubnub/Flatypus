package si.uni_lj.fe.tnuv.flatypus.ui.todo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ToDoViewModel extends ViewModel {

    // Task data class
    public static class Task {
        private String name;
        private String assignee;
        private String profilePicture; // Resource name or URL (e.g., "platypus")
        private boolean isCompleted;
        private String repeat; // "none", "weekly", "monthly"

        public Task(String name, String assignee, String profilePicture, boolean isCompleted, String repeat) {
            this.name = name;
            this.assignee = assignee;
            this.profilePicture = profilePicture;
            this.isCompleted = isCompleted;
            this.repeat = repeat;
        }

        public String getName() { return name; }
        public String getAssignee() { return assignee; }
        public String getProfilePicture() { return profilePicture; }
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { this.isCompleted = completed; }
        public String getRepeat() { return repeat; }
    }

    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> notificationCount = new MutableLiveData<>(0);
    private final List<String> roommates = new ArrayList<>(); // List of roommates
    private final String currentUser = "Eva"; // Example current user (replace with actual logic)

    public ToDoViewModel() {
        // Initialize roommates (replace with actual data source)
        roommates.add("Eva");
        roommates.add("John");
        roommates.add("Alice");

        // Initialize with some sample tasks
        List<Task> initialTasks = new ArrayList<>();
        initialTasks.add(new Task("Clean the kitchen", "Eva", "platypus", false, "weekly"));
        initialTasks.add(new Task("Buy groceries", "John", "red_fluffy", false, "none"));
        tasks.setValue(initialTasks);
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public LiveData<Integer> getNotificationCount() {
        return notificationCount;
    }

    public List<String> getRoommates() {
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
        // If assignee is "random", pick a random roommate
        if ("random".equalsIgnoreCase(assignee)) {
            Random random = new Random();
            assignee = roommates.get(random.nextInt(roommates.size()));
        }
        // Assign a default profile picture based on assignee (replace with actual logic)
        String profilePicture = assignee.equals("Eva") ? "platypus" : "red_fluffy";
        currentTasks.add(new Task(name, assignee, profilePicture, false, repeat));
        tasks.setValue(currentTasks);
    }

    public void updateTaskCompletion(int position, boolean isCompleted) {
        List<Task> currentTasks = tasks.getValue();
        if (currentTasks != null && position >= 0 && position < currentTasks.size()) {
            Task task = currentTasks.get(position);
            task.setCompleted(isCompleted);
            tasks.setValue(currentTasks);
        }
    }
}
