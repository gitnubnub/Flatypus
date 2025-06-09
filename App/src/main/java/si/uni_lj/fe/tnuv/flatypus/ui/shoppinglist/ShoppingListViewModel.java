package si.uni_lj.fe.tnuv.flatypus.ui.shoppinglist;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShoppingListViewModel extends ViewModel {
    private final MutableLiveData<List<ShoppingItem>> shoppingItems = new MutableLiveData<>(new ArrayList<>());
    private DatabaseReference databaseReference;

    public ShoppingListViewModel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://flatypus-fde01-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = database.getReference("shoppingItems");
    }

    public LiveData<List<ShoppingItem>> getShoppingItems(String apartment) {
        databaseReference.orderByChild("apartment").equalTo(apartment)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<ShoppingItem> itemList = new ArrayList<>();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        ShoppingItem item = itemSnapshot.getValue(ShoppingItem.class);
                        if (item != null) {
                            if (item.isChecked()) {
                                itemSnapshot.getRef().removeValue();
                            } else {
                                itemList.add(item);
                            }
                        }
                    }
                    shoppingItems.setValue(itemList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    shoppingItems.setValue(Collections.emptyList());
                    Log.e("Shopping", "Database error: " + error.getMessage());
                }
            });

        return shoppingItems;
    }

    public void addItem(String itemName, String apartmentCode) {
        ShoppingItem newItem = new ShoppingItem(itemName, apartmentCode, false);
        String itemId = databaseReference.push().getKey();
        if (itemId != null) {
            databaseReference.child(itemId).setValue(newItem)
                    .addOnSuccessListener(aVoid ->
                            Log.d("Shopping", "Item added successfully")
                    ).addOnFailureListener(e ->
                            Log.e("Shopping", "Failed to add item: " + e.getMessage())
                    );
        }
    }

    public void toggleItem(int position) {
        List<ShoppingItem> currentList = shoppingItems.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            ShoppingItem item = currentList.get(position);
            item.setChecked(!item.isChecked());
            shoppingItems.setValue(currentList);
        }
    }

    public static class ShoppingItem {
        private String name;
        private String apartment;
        private boolean isChecked;

        public ShoppingItem() {}

        public ShoppingItem(String name, String apartment, boolean isChecked) {
            this.name = name;
            this.apartment = apartment;
            this.isChecked = isChecked;
        }

        public String getName() { return name; }
        public String getApartment() { return apartment; }
        public boolean isChecked() { return isChecked; }
        public void setChecked(boolean checked) { this.isChecked = checked; }
    }
}