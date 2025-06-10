package si.uni_lj.fe.tnuv.flatypus.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import java.util.List;

import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.shoppinglist.ShoppingListViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.to_do.ToDoViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Integer> heartCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> mNotificationCount = new MutableLiveData<>(0);

    private final MutableLiveData<Integer> shoppingItemCount = new MutableLiveData<>(0); // To store shopping count
    private final MutableLiveData<Integer> incompleteTaskCount = new MutableLiveData<>(0); // To store task count
    public HomeViewModel() {
        heartCount.setValue(5); // Default value
        mNotificationCount.setValue(0);
    }

    public LiveData<Integer> getHeartCount() {
        return heartCount;
    }

    public LiveData<Integer> getNotificationCount() {
        return mNotificationCount;
    }

    public void incrementNotificationCount() {
        Integer current = mNotificationCount.getValue();
        mNotificationCount.setValue(current != null ? current + 1 : 1);
    }

    public LiveData<Object> getCharacter() {
        return null;
    }

    // Methods to update the state
    public void updateHeartCount(int newCount) {
        heartCount.setValue(newCount);
    }

    // Initialize heart calculation with user and shopping data
// Initialize heart calculation with user, shopping, and task data
// Initialize heart calculation with user, shopping, and task data
    public void initHeartCalculation(UserViewModel userViewModel, ShoppingListViewModel shoppingViewModel, ToDoViewModel toDoViewModel) {
        userViewModel.getCurrentUser().observeForever(new Observer<UserViewModel.User>() {
            @Override
            public void onChanged(UserViewModel.User user) {
                if (user != null) {
                    String currentApartment = user.getCurrentApartment();
                    String currentUserEmail = user.getEmail();
                    if (currentApartment != null && !currentApartment.isEmpty()) {
                        // Trigger task fetch to ensure data is available
                        toDoViewModel.fetchTasks(currentApartment);
                        // Set up observations
                        observeShoppingItems(shoppingViewModel, currentApartment);
                        observeTasks(toDoViewModel, currentUserEmail, currentApartment);
                        // Combine counts when both are available
                        combineCounts();
                    } else {
                        calculateHeartCount(0, 100f, 0); // Default to 0 shopping items if no apartment
                    }
                } else {
                    calculateHeartCount(0, 100f, 0); // Default if no user
                }
            }
        });
    }

    // Function to observe and return the number of shopping items
    private void observeShoppingItems(ShoppingListViewModel shoppingViewModel, String currentApartment) {
        shoppingViewModel.getShoppingItems(currentApartment).observeForever(new Observer<List<ShoppingListViewModel.ShoppingItem>>() {
            @Override
            public void onChanged(List<ShoppingListViewModel.ShoppingItem> items) {
                int count = (items != null) ? items.size() : 0;
                shoppingItemCount.setValue(count); // Update the shopping item count
                combineCounts(); // Recalculate when shopping count changes
            }
        });
    }

    // Function to observe and return the number of incomplete user tasks
// Function to observe and return the number of incomplete user tasks
    private void observeTasks(ToDoViewModel toDoViewModel, String currentUserEmail, String currentApartment) {
        toDoViewModel.getTasks().observeForever(new Observer<List<ToDoViewModel.Task>>() {
            @Override
            public void onChanged(List<ToDoViewModel.Task> tasks) {
                int count = toDoViewModel.getIncompleteTasksForUser(currentUserEmail, currentApartment); // Use the new method
                incompleteTaskCount.setValue(count); // Update the incomplete task count
                combineCounts(); // Recalculate when task count changes
            }
        });
    }

    // Combine counts and calculate heart count
    private void combineCounts() {
        Integer shoppingCount = shoppingItemCount.getValue() != null ? shoppingItemCount.getValue() : 0;
        Integer taskCount = incompleteTaskCount.getValue() != null ? incompleteTaskCount.getValue() : 0;
        calculateHeartCount(shoppingCount, 0, taskCount); // Use stored counts
    }
    private void calculateHeartCount(int shoppingCount, float expenseCount, int taskCount) {
        int hearts = 5;

        if (shoppingCount >= 5) {
            hearts--;
        }
        if (expenseCount >= 10 && expenseCount < 50) {
            hearts--;
        } else if (expenseCount >= 50) {
            hearts -= 2;
        }

        if (taskCount >= 2 && taskCount < 4) {
            hearts--;
        } else if (taskCount >= 4) {
            hearts -= 2;
        }

        heartCount.setValue(Math.max(0, hearts)); // Ensure non-negative hearts
    }
}