package si.uni_lj.fe.tnuv.flatypus.ui.shoppinglist;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentShoppingListBinding;
import si.uni_lj.fe.tnuv.flatypus.ui.expenses.ExpensesViewModel;

public class ShoppingListFragment extends Fragment {

    private FragmentShoppingListBinding binding;
    private ShoppingListViewModel viewModel;
    private ExpensesViewModel expViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ShoppingListViewModel.class);
        expViewModel = new ViewModelProvider(requireActivity()).get(ExpensesViewModel.class);

        binding = FragmentShoppingListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LinearLayout shoppingListContainer = binding.shoppingListContainer;
        viewModel.getShoppingItems().observe(getViewLifecycleOwner(), shoppingItems -> {
            shoppingListContainer.removeAllViews();
            for (int i = 0; i < shoppingItems.size(); i++) {
                ShoppingListViewModel.ShoppingItem shoppingItem = shoppingItems.get(i);
                View itemView = inflater.inflate(R.layout.item_shopping_list, shoppingListContainer, false);

                // set up shopping item
                CheckBox itemCheckbox = itemView.findViewById(R.id.item_checkbox);
                TextView itemName = itemView.findViewById(R.id.item_text);

                itemCheckbox.setChecked(shoppingItem.isChecked());
                itemName.setText(shoppingItem.getName());

                // checkbox interaction
                final int position = i;
                itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    String currentUser = viewModel.getCurrentUser();
                    showPriceInputDialog(position, currentUser);
                    itemCheckbox.setChecked(false);
                });

                shoppingListContainer.addView(itemView);
            }
        });

        binding.addItemButton.setOnClickListener(v -> showAddItemDialog());

        return root;
    }

    private void showPriceInputDialog(int position, String currentUser) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.input_price_layout, null);

        EditText itemPriceInput = dialogView.findViewById(R.id.item_price_input);
        Button addExpenseButton = dialogView.findViewById(R.id.input_price_confirm_button);

        // Show dialog
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add expense")
                .setView(dialogView)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();
        dialog.show();

        // Handle add item button
        addExpenseButton.setOnClickListener(v -> {
            String itemName = itemPriceInput.getText().toString();

            if (!itemName.isEmpty()) {
                float price;
                try {
                    price = Float.parseFloat(itemName);

                    List<ExpensesViewModel.Roommate> roommates = expViewModel.getRoommates().getValue();
                    for (ExpensesViewModel.Roommate roommate : roommates) {
                        if (roommate.getName() != currentUser) {
                            expViewModel.addExpense(price / roommates.size(), roommate.getName());
                        }
                    }

                    viewModel.toggleItem(position);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    itemPriceInput.setError("Invalid amount");
                    return;
                }
            } else {
                itemPriceInput.setError("Item price cannot be empty");
            }
        });
    }

    private void showAddItemDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_item_layout, null);

        EditText itemNameInput = dialogView.findViewById(R.id.item_name_input);
        Button addItemButton = dialogView.findViewById(R.id.add_item_confirm_button);

        // Show dialog
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add Item")
                .setView(dialogView)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();
        dialog.show();

        // Handle add item button
        addItemButton.setOnClickListener(v -> {
            String itemName = itemNameInput.getText().toString().trim();

            if (!itemName.isEmpty()) {
                viewModel.addItem(itemName);
                dialog.dismiss();
            } else {
                itemNameInput.setError("Item name cannot be empty");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}