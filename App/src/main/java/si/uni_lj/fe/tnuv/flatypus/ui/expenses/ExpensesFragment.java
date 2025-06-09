package si.uni_lj.fe.tnuv.flatypus.ui.expenses;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentExpensesBinding;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;

public class ExpensesFragment extends Fragment {

    private FragmentExpensesBinding binding;
    private ExpensesViewModel viewModel;
    private UserViewModel userViewModel;
    private String currentUser;
    private String currentApartmentCode;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExpensesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(requireActivity()).get(ExpensesViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user.getEmail();
                currentApartmentCode = user.getCurrentApartment();

                // expenses you owe
                LinearLayout expensesContainer1 = binding.expensesContainer1;
                viewModel.getExpenses(currentApartmentCode, currentUser).observe(getViewLifecycleOwner(), expenses -> {
                    expensesContainer1.removeAllViews();

                    for (int i = 0; i < expenses.size(); i++) {
                        ExpensesViewModel.Expense expense = expenses.get(i);
                        if (expense.getOwes().equals(currentUser)) {
                            View expenseView = inflater.inflate(R.layout.expense_layout, expensesContainer1, false);

                            int finalI = i;
                            userViewModel.getUserByMail(expense.getIsOwed()).observe(getViewLifecycleOwner(), otherUser -> {
                                ImageView assigneeProfilePicture = expenseView.findViewById(R.id.assignee_profile_picture);
                                TextView username = expenseView.findViewById(R.id.user_name);
                                TextView description = expenseView.findViewById(R.id.expense_description);
                                TextView amount = expenseView.findViewById(R.id.amount_text);
                                Button settle = expenseView.findViewById(R.id.action_button);

                                assigneeProfilePicture.setImageResource(otherUser.getProfilePicture());
                                username.setText(otherUser.getUsername());
                                description.setText("you owe");
                                amount.setText(String.format("%.2f", expense.getAmount()) + " €");

                                if (expense.isSettled()) {
                                    settle.setText("Settled ✓");
                                } else {
                                    settle.setText("Settled");
                                }
                                settle.setVisibility(View.VISIBLE);

                                final int position = finalI;
                                settle.setOnClickListener(v -> {
                                    viewModel.updateStatus(position);
                                });
                            });

                            expensesContainer1.addView(expenseView);
                        }
                    }
                });

                // expenses you are owed
                LinearLayout expensesContainer2 = binding.expensesContainer2;
                viewModel.getExpenses(currentApartmentCode, currentUser).observe(getViewLifecycleOwner(), expenses -> {
                    expensesContainer2.removeAllViews();

                    for (int i = 0; i < expenses.size(); i++) {
                        ExpensesViewModel.Expense expense = expenses.get(i);
                        if (expense.getIsOwed().equals(currentUser)) {
                            View expenseView = inflater.inflate(R.layout.expense_layout, expensesContainer2, false);

                            int finalI = i;
                            userViewModel.getUserByMail(expense.getOwes()).observe(getViewLifecycleOwner(), otherUser -> {
                                ImageView assigneeProfilePicture = expenseView.findViewById(R.id.assignee_profile_picture);
                                TextView username = expenseView.findViewById(R.id.user_name);
                                TextView description = expenseView.findViewById(R.id.expense_description);
                                TextView amount = expenseView.findViewById(R.id.amount_text);
                                Button settle = expenseView.findViewById(R.id.action_button);

                                assigneeProfilePicture.setImageResource(otherUser.getProfilePicture());
                                username.setText(otherUser.getUsername());
                                description.setText("owes you");
                                amount.setText(String.format("%.2f", expense.getAmount()) + " €");

                                if (expense.isSettled()) {
                                    settle.setText("Received ✓");
                                } else {
                                    settle.setText("Received");
                                }
                                settle.setVisibility(View.VISIBLE);

                                final int position = finalI;
                                settle.setOnClickListener(v -> {
                                    viewModel.updateStatus(position);
                                });
                            });

                            expensesContainer2.addView(expenseView);
                        }
                    }
                });
            }
        });

        binding.addExpenseButton.setOnClickListener(v -> {
            userViewModel.getRoommates(currentApartmentCode).observe(getViewLifecycleOwner(), roommates -> {
                if (roommates != null && !roommates.isEmpty()) {
                    showAddExpenseDialog(roommates);
                }
            });
        });

        return root;
    }

    private void showAddExpenseDialog(List<UserViewModel.User> roommates) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_expense_layout, null);

        EditText amountInput = dialogView.findViewById(R.id.amount_input);
        LinearLayout roommateContainer = dialogView.findViewById(R.id.roommate_container);
        Button submitButton = dialogView.findViewById(R.id.add_expense_confirm_button);
        roommateContainer.removeAllViews();

        List<UserViewModel.User> chosenRoommates = new ArrayList<>();

        for (UserViewModel.User roommate : roommates) {
            if (roommate.getEmail().equals(currentUser)) {
                continue;
            }

            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(roommate.getUsername());
            checkBox.setTextColor(getResources().getColor(R.color.dark_red));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (chosenRoommates.contains(roommate)) {
                    chosenRoommates.remove(roommate);
                } else {
                    chosenRoommates.add(roommate);
                }
            });

            roommateContainer.addView(checkBox);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add Expense")
                .setView(dialogView)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();
        dialog.show();

        submitButton.setOnClickListener(v -> {
            String amountText = amountInput.getText().toString();
            if (amountText.isEmpty()) {
                amountInput.setError("Amount is required");
                return;
            }

            float amount;
            try {
                amount = Float.parseFloat(amountText);
            } catch (NumberFormatException e) {
                amountInput.setError("Invalid amount");
                return;
            }

            for (UserViewModel.User roommate : chosenRoommates) {
                viewModel.addExpense(currentApartmentCode, amount / (chosenRoommates.size() + 1), roommate.getEmail(), currentUser);
            }

            if (chosenRoommates.isEmpty()) {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Please select at least one roommate")
                        .setPositiveButton("OK", (d2, which) -> d2.dismiss())
                        .show();
                return;
            }

            dialog.dismiss();
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.cleanupSettledExpenses(currentApartmentCode);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}