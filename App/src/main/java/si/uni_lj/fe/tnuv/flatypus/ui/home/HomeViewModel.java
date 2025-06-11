package si.uni_lj.fe.tnuv.flatypus.ui.home;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import java.util.List;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.ui.expenses.ExpensesViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.shoppinglist.ShoppingListViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.to_do.ToDoViewModel;
import si.uni_lj.fe.tnuv.flatypus.widget.WidgetProvider;

public class HomeViewModel extends ViewModel {
    public static final String ACTION_HEART_COUNT_UPDATED = WidgetProvider.ACTION_HEART_COUNT_UPDATED;
    private static final MutableLiveData<Integer> heartCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> mNotificationCount = new MutableLiveData<>(0);

    private final MutableLiveData<Integer> shoppingItemCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> incompleteTaskCount = new MutableLiveData<>(0);
    private final MutableLiveData<Float> owedExpenseSum = new MutableLiveData<>(0f);
    private Context context;
    private UserViewModel userViewModel;
    private int lastHeartCount = -1;
    private int lastPlatypusResourceId = -1;

    public HomeViewModel() {
        heartCount.setValue(5);
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

    public static int getCurrentHeartCount() {
        Integer value = heartCount.getValue();
        return value != null ? value : 0;
    }

    public void updateHeartCount(int newCount, int platypusResourceId) {
        if (newCount != lastHeartCount || platypusResourceId != lastPlatypusResourceId) {
            heartCount.setValue(newCount);
            if (context != null) {
                Log.d("HomeViewModel", "Broadcasting heartCount: " + newCount + ", platypusResourceId: " + platypusResourceId + ", Context: " + context);
                Intent intent = new Intent(ACTION_HEART_COUNT_UPDATED);
                intent.putExtra("heartCount", newCount);
                intent.putExtra("platypusResourceId", platypusResourceId);
                context.getApplicationContext().sendBroadcast(intent);
                WidgetProvider.updateWidgets(context.getApplicationContext());
            } else {
                Log.d("HomeViewModel", "No context available, broadcast not sent");
            }
            lastHeartCount = newCount;
            lastPlatypusResourceId = platypusResourceId;
        }
    }

    public void initHeartCalculation(Context context, UserViewModel userViewModel, ShoppingListViewModel shoppingViewModel,
                                     ToDoViewModel toDoViewModel, ExpensesViewModel expensesViewModel) {
        this.context = context.getApplicationContext();
        this.userViewModel = userViewModel;
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
                        int profilePicture = user.getProfilePicture();
                        int platypusResource = mapToPlatypusResource(profilePicture);
                        calculateHeartCount(0, 0f, 0, platypusResource);
                    }
                } else {
                    calculateHeartCount(0, 0f, 0, R.drawable.platypus_red);
                }
            }
        });
    }

    void observeShoppingItems(ShoppingListViewModel shoppingViewModel, String currentApartment) {
        shoppingViewModel.getShoppingItems(currentApartment).observeForever(new Observer<List<ShoppingListViewModel.ShoppingItem>>() {
            @Override
            public void onChanged(List<ShoppingListViewModel.ShoppingItem> items) {
                int count = (items != null) ? items.size() : 0;
                shoppingItemCount.setValue(count);
                combineCounts();
            }
        });
    }

    void observeTasks(ToDoViewModel toDoViewModel, String currentUserEmail, String currentApartment) {
        toDoViewModel.getTasks().observeForever(new Observer<List<ToDoViewModel.Task>>() {
            @Override
            public void onChanged(List<ToDoViewModel.Task> tasks) {
                int count = toDoViewModel.getIncompleteTasksForUser(currentUserEmail, currentApartment);
                incompleteTaskCount.setValue(count);
                combineCounts();
            }
        });
    }

    void observeOwedExpenses(ExpensesViewModel expensesViewModel, String currentApartment, String currentUserEmail) {
        expensesViewModel.getOwedExpenses(currentApartment, currentUserEmail).observeForever(new Observer<List<ExpensesViewModel.Expense>>() {
            @Override
            public void onChanged(List<ExpensesViewModel.Expense> expenses) {
                float sum = 0f;
                if (expenses != null) {
                    for (ExpensesViewModel.Expense expense : expenses) {
                        sum += expense.getAmount();
                    }
                }
                owedExpenseSum.setValue(sum);
                combineCounts();
            }
        });
    }

    void combineCounts() {
        Integer shoppingCount = shoppingItemCount.getValue() != null ? shoppingItemCount.getValue() : 0;
        Integer taskCount = incompleteTaskCount.getValue() != null ? incompleteTaskCount.getValue() : 0;
        Float expenseSum = owedExpenseSum.getValue() != null ? owedExpenseSum.getValue() : 0f;
        UserViewModel.User user = userViewModel.getCurrentUser().getValue();
        int profilePictureId = (user != null) ? user.getProfilePicture() : R.drawable.platypus_red;
        Log.d("HomeViewModel", "Combining counts with profilePictureId: " + profilePictureId);
        int platypusResourceId = mapToPlatypusResource(profilePictureId);
        calculateHeartCount(shoppingCount, expenseSum, taskCount, platypusResourceId);
    }

    private int mapToPlatypusResource(int profilePictureId) {
        Log.d("HomeViewModel", "Mapping profilePictureId: " + profilePictureId + " to platypus resource");
        if (profilePictureId == R.drawable.pfp_red) {
            return R.drawable.platypus_red;
        } else if (profilePictureId == R.drawable.pfp_green) {
            return R.drawable.platypus_green;
        } else if (profilePictureId == R.drawable.pfp_purple) {
            return R.drawable.platypus_purple;
        } else {
            Log.w("HomeViewModel", "Unknown profile picture value: " + profilePictureId + ", defaulting to platypus_red");
            return R.drawable.platypus_red;
        }
    }

    void calculateHeartCount(int shoppingCount, float expenseCount, int taskCount, int platypusResource) {
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
        updateHeartCount(finalHearts, platypusResource);
    }
}