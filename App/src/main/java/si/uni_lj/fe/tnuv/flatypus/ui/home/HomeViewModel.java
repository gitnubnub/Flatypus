package si.uni_lj.fe.tnuv.flatypus.ui.home;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import java.util.List;

import si.uni_lj.fe.tnuv.flatypus.ui.expenses.ExpensesViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.shoppinglist.ShoppingListViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.to_do.ToDoViewModel;
import si.uni_lj.fe.tnuv.flatypus.widget.WidgetProvider;

public class HomeViewModel extends ViewModel {
    public static final String ACTION_HEART_COUNT_UPDATED = WidgetProvider.ACTION_HEART_COUNT_UPDATED;
    private static final MutableLiveData<Integer> heartCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> mNotificationCount = new MutableLiveData<>(0);

    private final MutableLiveData<Integer> shoppingItemCount = new MutableLiveData<>(0); // To store shopping count
    private final MutableLiveData<Integer> incompleteTaskCount = new MutableLiveData<>(0); // To store task count
    private final MutableLiveData<Float> owedExpenseSum = new MutableLiveData<>(0f);
    private Context context;

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

    // Static method to get the current heart count
    public static int getCurrentHeartCount() {
        Integer value = heartCount.getValue();
        return value != null ? value : 0;
    }

    public void updateHeartCount(int newCount) {
        heartCount.setValue(newCount);
        if (context != null) {
            Log.d("HomeViewModel", "Broadcasting heartCount: " + newCount + ", Context: " + context);
            Intent intent = new Intent(ACTION_HEART_COUNT_UPDATED);
            intent.putExtra("heartCount", newCount);
            context.getApplicationContext().sendBroadcast(intent); // Use application context
            // Fallback: Force widget update
            WidgetProvider.updateWidgets(context.getApplicationContext());
        } else {
            Log.d("HomeViewModel", "No context available, broadcast not sent");
        }
    }

    // Initialize heart calculation with user and shopping data
// Initialize heart calculation with user, shopping, and task data
// Initialize heart calculation with user, shopping, and task data

    public void initHeartCalculation(Context context, UserViewModel userViewModel, ShoppingListViewModel shoppingViewModel,
                                     ToDoViewModel toDoViewModel, ExpensesViewModel expensesViewModel) {
        this.context = context; // Store the context
        userViewModel.getCurrentUser().observeForever(new Observer<UserViewModel.User>() {
            @Override
            public void onChanged(UserViewModel.User user) {
                if (user != null) {
                    String currentApartment = user.getCurrentApartment();
                    String currentUserEmail = user.getEmail();
                    if (currentApartment != null && !currentApartment.isEmpty()) {
                        toDoViewModel.fetchTasks(currentApartment);
                        observeShoppingItems(shoppingViewModel, currentApartment);
                        observeTasks(toDoViewModel, currentUserEmail, currentApartment);
                        observeOwedExpenses(expensesViewModel, currentApartment, currentUserEmail);
                    } else {
                        calculateHeartCount(0, 0f, 0);
                    }
                } else {
                    calculateHeartCount(0, 0f, 0);
                }
            }
        });
    }
    // Function to observe and return the number of shopping items
    void observeShoppingItems(ShoppingListViewModel shoppingViewModel, String currentApartment) {
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
    void observeTasks(ToDoViewModel toDoViewModel, String currentUserEmail, String currentApartment) {
        toDoViewModel.getTasks().observeForever(new Observer<List<ToDoViewModel.Task>>() {
            @Override
            public void onChanged(List<ToDoViewModel.Task> tasks) {
                int count = toDoViewModel.getIncompleteTasksForUser(currentUserEmail, currentApartment); // Use the new method
                incompleteTaskCount.setValue(count); // Update the incomplete task count
                combineCounts(); // Recalculate when task count changes
            }
        });
    }

    // Function to observe and return the sum of owed expenses
    void observeOwedExpenses(ExpensesViewModel expensesViewModel, String currentApartment, String currentUserEmail) {
        expensesViewModel.getOwedExpenses(currentApartment, currentUserEmail).observeForever(new Observer<List<ExpensesViewModel.Expense>>() {
            @Override
            public void onChanged(List<ExpensesViewModel.Expense> expenses) {
                float sum = 0f;
                if (expenses != null) {
                    for (ExpensesViewModel.Expense expense : expenses) {
                        sum += expense.getAmount(); // Assuming Expense has getAmount()
                    }
                }
                owedExpenseSum.setValue(sum); // Update the owed expense sum
                combineCounts(); // Recalculate when expense sum changes
            }
        });
    }



    // Combine counts and calculate heart count
    void combineCounts() {
        Integer shoppingCount = shoppingItemCount.getValue() != null ? shoppingItemCount.getValue() : 0;
        Integer taskCount = incompleteTaskCount.getValue() != null ? incompleteTaskCount.getValue() : 0;
        Float expenseSum = owedExpenseSum.getValue() != null ? owedExpenseSum.getValue() : 0f;
        calculateHeartCount(shoppingCount, expenseSum, taskCount); // Use stored counts
    }
    void calculateHeartCount(int shoppingCount, float expenseCount, int taskCount) {
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

        int finalHearts = Math.max(0, hearts);
        //heartCount.setValue(finalHearts);
        updateHeartCount(finalHearts); // Use stored context
    }
}