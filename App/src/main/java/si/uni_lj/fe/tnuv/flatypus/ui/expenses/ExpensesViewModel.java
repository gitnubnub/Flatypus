package si.uni_lj.fe.tnuv.flatypus.ui.expenses;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ExpensesViewModel extends ViewModel {

    public static class Expense {
        private float amount;
        private String owes;
        private String isOwed;
        private String profilePicture;

        public Expense(float amount, String owes, String isOwed, String profilePicture) {
            this.amount = amount;
            this.owes = owes;
            this.isOwed = isOwed;
            this.profilePicture = profilePicture;
        }

        public float getAmount() {
            return amount;
        }
        public String getIsOwed() {
            return isOwed;
        }
        public String getOwes() {
            return owes;
        }
        public String getProfilePicture() {
            return profilePicture;
        }
    }

    public static class Roommate {
        private String name;
        private boolean isSelected;

        public Roommate(String name) {
            this.name = name;
            this.isSelected = false;
        }

        public String getName() { return name; }
        public boolean isSelected() { return isSelected; }
        public void setSelected(boolean selected) { isSelected = selected; }
    }

    private final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Roommate>> roommates = new MutableLiveData<>(new ArrayList<>());
    private final String currentUser = "Eva";

    public ExpensesViewModel() {
        List<Roommate> initialRoommates = new ArrayList<>();
        initialRoommates.add(new Roommate("Eva"));
        initialRoommates.add(new Roommate("John"));
        initialRoommates.add(new Roommate("Alice"));
        initialRoommates.add(new Roommate("Ari"));
        initialRoommates.add(new Roommate("Urška"));
        roommates.setValue(initialRoommates);

        List<Expense> initialExpenses = new ArrayList<>();
        initialExpenses.add(new Expense(15.46F, "Eva", "Urška", "platypus"));
        initialExpenses.add(new Expense(25.30F, "Ari", "Eva", "red_fluffy"));
    }

    public LiveData<List<Expense>> getExpenses() {
        return expenses;
    }
    public LiveData<List<Roommate>> getRoommates() {
        return roommates;
    }
    public String getCurrentUser() {
        return currentUser;
    }

    public void addExpense(float amount, String owes) {
        List<Expense> currentExpenses = expenses.getValue();
        String isOwed = getCurrentUser();
        String profilePicture = isOwed.equals("Eva") ? "platypus" : "red_fluffy";

        if (currentExpenses == null) {
            currentExpenses = new ArrayList<>();
        }

        boolean alreadyInDebt = false;
        for (Expense debt : currentExpenses) {
            if (debt.owes == owes && debt.isOwed == isOwed) {
                alreadyInDebt = true;
                debt.amount += amount;
            }
        }

        if (!alreadyInDebt) {
            currentExpenses.add(new Expense(amount, owes, isOwed, profilePicture));
        }

        expenses.setValue(currentExpenses);
    }

    public void updateStatus(int position) {
        List<Expense> currentExpenses = expenses.getValue();
        if (currentExpenses != null && position >= 0 && position < currentExpenses.size()) {
            currentExpenses.remove(position);
            expenses.setValue(currentExpenses);
        }
    }

    public void resetRoommateSelections() {
        List<Roommate> currentRoommates = roommates.getValue();
        if (currentRoommates != null) {
            for (Roommate roommate : currentRoommates) {
                roommate.setSelected(false);
            }
            roommates.setValue(currentRoommates);
        }
    }
}