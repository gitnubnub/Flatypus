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
        private String id;
        private String apartment;
        private float amount;
        private String owes;
        private String isOwed;
        private boolean settled;

        public Expense() {}

        public Expense(String id, String apartment, float amount, String owes, String isOwed, boolean settled) {
            this.id = id;
            this.apartment = apartment;
            this.amount = amount;
            this.owes = owes;
            this.isOwed = isOwed;
            this.settled = settled;
        }

        public String getId() { return id; }
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
        public void setId(String id) { this.id = id; }
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
                                if (user.equals(expense.owes) || user.equals(expense.isOwed)) {
                                    filteredExpenses.add(expense);
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
        databaseReference.orderByChild("apartment").equalTo(apartment)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean found = false;

                        for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                            Expense existingExpense = expenseSnapshot.getValue(Expense.class);

                            if (existingExpense == null || existingExpense.isSettled()) continue;

                            boolean directMatch = existingExpense.getOwes().equals(owes) &&
                                    existingExpense.getIsOwed().equals(isOwed);

                            boolean reverseMatch = existingExpense.getOwes().equals(isOwed) &&
                                    existingExpense.getIsOwed().equals(owes);

                            if (directMatch || reverseMatch) {
                                float newAmount;

                                if (directMatch) {
                                    newAmount = existingExpense.getAmount() + amount;
                                    expenseSnapshot.getRef().child("amount").setValue(newAmount)
                                            .addOnSuccessListener(aVoid ->
                                                    Log.d("Expenses", "Direct match: expense updated.")
                                            ).addOnFailureListener(e ->
                                                    Log.e("Expenses", "Failed to update: " + e.getMessage())
                                            );
                                } else { // reverseMatch
                                    newAmount = existingExpense.getAmount() - amount;

                                    if (newAmount > 0) {
                                        expenseSnapshot.getRef().child("amount").setValue(newAmount);
                                        Log.d("Expenses", "Reverse match: amount reduced.");
                                    } else if (newAmount < 0) {
                                        // Flip the direction and set positive amount
                                        expenseSnapshot.getRef().child("owes").setValue(owes);
                                        expenseSnapshot.getRef().child("isOwed").setValue(isOwed);
                                        expenseSnapshot.getRef().child("amount").setValue(-newAmount);
                                        Log.d("Expenses", "Reverse match: flipped and updated.");
                                    } else {
                                        expenseSnapshot.getRef().removeValue()
                                                .addOnSuccessListener(aVoid ->
                                                        Log.d("Expenses", "Expense fully settled and removed.")
                                                );
                                    }
                                }

                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            Expense newExpense = new Expense("", apartment, amount, owes, isOwed, false);
                            String expenseId = databaseReference.push().getKey();
                            if (expenseId != null) {
                                newExpense.setId(expenseId);
                                databaseReference.child(expenseId).setValue(newExpense)
                                        .addOnSuccessListener(aVoid ->
                                                Log.d("Expenses", "New expense created.")
                                        ).addOnFailureListener(e ->
                                                Log.e("Expenses", "Failed to create new expense: " + e.getMessage())
                                        );
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Expenses", "Database error: " + error.getMessage());
                    }
                });
    }

    public void updateStatus(int position) {
        List<Expense> currentExpenses = expenses.getValue();
        if (currentExpenses != null && position >= 0 && position < currentExpenses.size()) {
            Expense expense = currentExpenses.get(position);

            boolean newStatus = !expense.isSettled();
            expense.setSettled(newStatus);
            expenses.setValue(currentExpenses);

            String expenseId = expense.getId();
            if (expenseId != null && !expenseId.isEmpty()) {
                databaseReference.child(expenseId).child("settled").setValue(newStatus)
                        .addOnSuccessListener(aVoid ->
                                Log.d("Expenses", "Status updated in Firebase.")
                        ).addOnFailureListener(e ->
                                Log.e("Expenses", "Failed to update status: " + e.getMessage())
                        );
            } else {
                Log.e("Expenses", "Expense ID missing. Cannot update Firebase.");
            }
        }
    }

    public void cleanupSettledExpenses(String apartment) {
        databaseReference.orderByChild("apartment").equalTo(apartment)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                            Expense expense = expenseSnapshot.getValue(Expense.class);
                            if (expense != null && expense.isSettled()) {
                                expenseSnapshot.getRef().removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Expenses", "Cleanup failed: " + error.getMessage());
                    }
                });
    }
}