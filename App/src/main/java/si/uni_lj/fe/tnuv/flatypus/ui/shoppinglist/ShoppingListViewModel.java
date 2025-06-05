package si.uni_lj.fe.tnuv.flatypus.ui.shoppinglist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListViewModel extends ViewModel {
    private final MutableLiveData<List<ShoppingItem>> shoppingItems = new MutableLiveData<>(new ArrayList<>());
    private final String currentUser = "Eva"; // hard-coded example

    public ShoppingListViewModel() {
        List<ShoppingItem> initialItems = new ArrayList<>();

        initialItems.add(new ShoppingItem("eggs", false));
        initialItems.add(new ShoppingItem("milk", false));
        initialItems.add(new ShoppingItem("trash bags", false));
        initialItems.add(new ShoppingItem("ice tea", false));

        shoppingItems.setValue(initialItems);
    }

    public LiveData<List<ShoppingItem>> getShoppingItems() {
        return shoppingItems;
    }

    public String getCurrentUser() { return currentUser; }

    public void addItem(String itemName) {
        List<ShoppingItem> currentItems = shoppingItems.getValue();
        if (currentItems == null) {
            currentItems = new ArrayList<>();
        }

        currentItems.add(new ShoppingItem(itemName, false));
        shoppingItems.setValue(currentItems);
    }

    public void toggleItem(int position) {
        List<ShoppingItem> currentItems = shoppingItems.getValue();
        if (currentItems != null && position >= 0 && position < currentItems.size()) {
            currentItems.remove(position);
            shoppingItems.setValue(currentItems);
        }
    }

    // Simple data class for shopping items
    public static class ShoppingItem {
        private String name;
        private boolean isChecked;

        public ShoppingItem(String name, boolean isChecked) {
            this.name = name;
            this.isChecked = isChecked;
        }

        public String getName() { return name; }
        public boolean isChecked() { return isChecked; }
        public void setChecked(boolean checked) { this.isChecked = checked; }
    }
}