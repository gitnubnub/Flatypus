package si.uni_lj.fe.tnuv.flatypus.ui.expenses;

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

import java.util.List;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentExpensesBinding;

public class ExpensesFragment extends Fragment {

    private FragmentExpensesBinding binding;
    private ExpensesViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExpensesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(ExpensesViewModel.class);

        String currentUser = viewModel.getCurrentUser();

        // expenses you owe
        LinearLayout expensesContainer1 = binding.expensesContainer1;
        viewModel.getExpenses().observe(getViewLifecycleOwner(), expenses -> {
            expensesContainer1.removeAllViews();

            for (int i = 0; i < expenses.size(); i++) {
                ExpensesViewModel.Expense expense = expenses.get(i);
                if (expense.getOwes().equals(currentUser)) {
                    View expenseView = inflater.inflate(R.layout.expense_layout, expensesContainer1, false);

                    ImageView assigneeProfilePicture = expenseView.findViewById(R.id.assignee_profile_picture);
                    TextView username = expenseView.findViewById(R.id.user_name);
                    TextView description = expenseView.findViewById(R.id.expense_description);
                    TextView amount = expenseView.findViewById(R.id.amount_text);
                    Button settle = expenseView.findViewById(R.id.action_button);

                    assigneeProfilePicture.setImageResource(R.drawable.platypus);
                    username.setText(expense.getIsOwed());
                    description.setText("you owe");
                    amount.setText(String.valueOf(expense.getAmount()) + " €");
                    settle.setText("Settled");

                    final int position = i;
                    settle.setOnClickListener(v -> viewModel.updateStatus(position, true));

                    expensesContainer1.addView(expenseView);
                }
            }
        });

        // expenses you are owed
        LinearLayout expensesContainer2 = binding.expensesContainer2;
        viewModel.getExpenses().observe(getViewLifecycleOwner(), expenses -> {
            expensesContainer2.removeAllViews();

            for (int i = 0; i < expenses.size(); i++) {
                ExpensesViewModel.Expense expense = expenses.get(i);
                if (expense.getIsOwed().equals(currentUser)) {
                    View expenseView = inflater.inflate(R.layout.expense_layout, expensesContainer2, false);

                    ImageView assigneeProfilePicture = expenseView.findViewById(R.id.assignee_profile_picture);
                    TextView username = expenseView.findViewById(R.id.user_name);
                    TextView description = expenseView.findViewById(R.id.expense_description);
                    TextView amount = expenseView.findViewById(R.id.amount_text);
                    Button settle = expenseView.findViewById(R.id.action_button);

                    assigneeProfilePicture.setImageResource(R.drawable.platypus);
                    username.setText(expense.getOwes());
                    description.setText("owes you");
                    amount.setText(String.valueOf(expense.getAmount()) + " €");
                    settle.setText("Received");

                    final int position = i;
                    settle.setOnClickListener(v -> viewModel.updateStatus(position, true));

                    expensesContainer2.addView(expenseView);
                }
            }
        });

        binding.addExpenseButton.setOnClickListener(v -> showAddExpenseDialog());

        return root;
    }

    private void showAddExpenseDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_expense_layout, null);
        viewModel.resetRoommateSelections();

        EditText amountInput = dialogView.findViewById(R.id.amount_input);
        LinearLayout roommateContainer = dialogView.findViewById(R.id.roommate_container);
        Button submitButton = dialogView.findViewById(R.id.add_expense_confirm_button);
        roommateContainer.removeAllViews();

        viewModel.getRoommates().observe(getViewLifecycleOwner(), roommates -> {
            for (ExpensesViewModel.Roommate roommate : roommates) {
                if (roommate.getName().equals(viewModel.getCurrentUser())) {
                    continue;
                }

                CheckBox checkBox = new CheckBox(requireContext());
                checkBox.setText(roommate.getName());
                checkBox.setTextColor(getResources().getColor(R.color.dark_red));
                checkBox.setChecked(roommate.isSelected());
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    roommate.setSelected(isChecked);
                });

                roommateContainer.addView(checkBox);
            }
        });

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

            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                amountInput.setError("Invalid amount");
                return;
            }

            List<ExpensesViewModel.Roommate> allRoommates = viewModel.getRoommates().getValue();
            boolean atLeastOneSelected = false;
            if (allRoommates != null) {
                int selectedRoommates = 0;
                for (ExpensesViewModel.Roommate roommate : allRoommates) {
                    if (roommate.isSelected()) {
                        selectedRoommates++;
                        atLeastOneSelected = true;
                    }
                }
                for (ExpensesViewModel.Roommate roommate : allRoommates) {
                    if (roommate.isSelected()) {
                        viewModel.addExpense(amount / (selectedRoommates + 1), roommate.getName());
                    }
                }
            }

            if (!atLeastOneSelected) {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Please select at least one roommate")
                        .setPositiveButton("OK", (d2, which) -> d2.dismiss())
                        .show();
                return;
            }

            viewModel.resetRoommateSelections();
            dialog.dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}