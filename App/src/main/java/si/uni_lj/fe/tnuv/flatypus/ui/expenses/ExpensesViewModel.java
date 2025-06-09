package si.uni_lj.fe.tnuv.flatypus.ui.expenses;

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
import java.util.Collections;
import java.util.List;

public class ExpensesViewModel extends ViewModel {

    public static class Expense {
        private String apartment;
        private float amount;
        private String owes;
        private String isOwed;
        private boolean settled;

        public Expense() {}

        public Expense(String apartment, float amount, String owes, String isOwed, boolean settled) {
            this.apartment = apartment;
            this.amount = amount;
            this.owes = owes;
            this.isOwed = isOwed;
            this.settled = settled;
        }

        public String getApartment() { return apartment; }
        public float getAmount() {
            return amount;
        }
        public String getIsOwed() {
            return isOwed;
        }
        public String getOwes() {
            return owes;
        }
        public boolean isSettled() { return settled; }
        public void setSettled(boolean settled) { this.settled = settled; }
    }

    private final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>(new ArrayList<>());
    private DatabaseReference databaseReference;

    public ExpensesViewModel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://flatypus-fde01-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = database.getReference("expenses");
    }

    public LiveData<List<Expense>> getExpenses(String apartment, String user) {
        databaseReference.orderByChild("apartment").equalTo(apartment)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Expense> filteredExpenses = new ArrayList<>();
                        for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                            Expense expense = expenseSnapshot.getValue(Expense.class);
                            if (expense != null) {
                                if (expense.isSettled()) {
                                    expenseSnapshot.getRef().removeValue();
                                } else {
                                    if (user.equals(expense.owes) || user.equals(expense.isOwed)) {
                                        filteredExpenses.add(expense);
                                    }
                                }
                            }
                        }
                        expenses.setValue(filteredExpenses);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        expenses.setValue(Collections.emptyList());
                        Log.e("Expenses", "Database error: " + error.getMessage());
                    }
                });

        return expenses;
    }

    public void addExpense(String apartment, float amount, String owes, String isOwed) {
        Expense newExpense = new Expense(apartment, amount, owes, isOwed, false);
        String expenseId = databaseReference.push().getKey();
        if (expenseId != null) {
            databaseReference.child(expenseId).setValue(newExpense)
                    .addOnSuccessListener(aVoid ->
                            Log.d("Shopping", "Item added successfully")
                    ).addOnFailureListener(e ->
                            Log.e("Shopping", "Failed to add item: " + e.getMessage())
                    );
        }
    }

    public void updateStatus(int position) {
        List<Expense> currentExpenses = expenses.getValue();
        if (currentExpenses != null && position >= 0 && position < currentExpenses.size()) {
            Expense expense = currentExpenses.get(position);
            expense.setSettled(!expense.isSettled());
            expenses.setValue(currentExpenses);
        }
    }
}