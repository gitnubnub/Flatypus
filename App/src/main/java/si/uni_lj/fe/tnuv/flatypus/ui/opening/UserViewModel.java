package si.uni_lj.fe.tnuv.flatypus.ui.opening;

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

public class UserViewModel extends ViewModel {

    public static class User {
        private String username;
        private String email;
        private String password;
        private int profilePicture;
        private boolean notifications;
        private List<String> apartments;
        private String currentApartment;

        public User() {}

        public User(String username, String email, String password, int profilePicture, boolean notifications, List<String> apartments, String currentApartment) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.profilePicture = profilePicture;
            this.notifications = notifications;
            this.apartments = apartments != null ? apartments : new ArrayList<>();
            this.currentApartment = currentApartment;
        }

        public String getUsername() {
            return username;
        }
        public String getEmail() {
            return email;
        }
        public String getPassword() {
            return password;
        }
        public int getProfilePicture() {
            return profilePicture;
        }
        public boolean getNotifications() {
            return notifications;
        }
        public List<String> getApartments() {
            return apartments != null ? apartments : new ArrayList<>();
        }
        public String getCurrentApartment() {
            return currentApartment;
        }
    }

    public static class Apartment {
        private String name;
        private String code;

        public Apartment() {}

        public Apartment(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }
    }

    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);
    private static DatabaseReference databaseReference;
    private static DatabaseReference apartmentsReference;
    private MutableLiveData<String> apartmentFetchResult = new MutableLiveData<>();

    public UserViewModel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://flatypus-fde01-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = database.getReference("users");
        apartmentsReference = database.getReference("apartments");
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> isLoggedIn() {
        return isLoggedIn;
    }

    public static LiveData<User> getUserByMail(String email) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();

        databaseReference.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                userLiveData.setValue(user);
                                return;
                            }
                        }
                        userLiveData.setValue(null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("UserLookup", "Database error: " + error.getMessage());
                        userLiveData.setValue(null);
                    }
                });

        return userLiveData;
    }

    public LiveData<List<User>> getRoommates(String apartment) {
        MutableLiveData<List<User>> roommates = new MutableLiveData<>(new ArrayList<>());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> result = new ArrayList<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && user.getApartments() != null && user.getApartments().contains(apartment)) {
                        result.add(user);
                    }
                }

                roommates.setValue(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Roommates", "Database error: " + error.getMessage());
                roommates.setValue(Collections.emptyList());
            }
        });

        return roommates;
    }

    public LiveData<String> getApartmentFetchResult() {
        return apartmentFetchResult;
    }

    public void login(String email, String password) {
        databaseReference.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User candidate = userSnapshot.getValue(User.class);
                    if (candidate != null && candidate.password.equals(password)) {
                        currentUser.setValue(candidate);
                        isLoggedIn.setValue(true);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    isLoggedIn.setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Login", "Database error: " + error.getMessage());
                isLoggedIn.setValue(false);
            }
        });
    }

    public void register(String username, String email, String password, int profilePicture) {
        List<String> apartments = new ArrayList<>();
        User newUser = new User(username, email, password, profilePicture, true, apartments, "");

        databaseReference.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // User with this email already exists
                            Log.e("Register", "Email already in use.");
                            isLoggedIn.setValue(false);
                        } else {
                            // Email is unique – proceed with registration
                            String userId = databaseReference.push().getKey();
                            if (userId != null) {
                                databaseReference.child(userId).setValue(newUser)
                                        .addOnSuccessListener(aVoid -> {
                                            currentUser.setValue(newUser);
                                            Log.d("Register", "User successfully registered.");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Register", "Failed to register user: " + e.getMessage());
                                            isLoggedIn.setValue(false);
                                        });
                            } else {
                                Log.e("Register", "Failed to generate user ID.");
                                isLoggedIn.setValue(false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Register", "Database error: " + error.getMessage());
                        isLoggedIn.setValue(false);
                    }
                });
    }

    public void logout() {
        currentUser.setValue(null);
        isLoggedIn.setValue(false);
    }

    public void addApartment(String apartmentCode, String apartmentName) {
        User current = currentUser.getValue();
        if (current != null) {
            List<String> currentApartments = current.getApartments();
            currentApartments.add(apartmentCode);
            current.currentApartment = apartmentCode;

            currentUser.setValue(current);

            // Create a new apartment object
            String apartmentId = apartmentsReference.push().getKey();
            if (apartmentId != null) {
                Apartment newApartment = new Apartment(apartmentName, apartmentCode);
                apartmentsReference.child(apartmentId).setValue(newApartment)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("AddApartment", "Apartment created with ID: " + apartmentId);
                            // Update user with the apartment reference
                            databaseReference.orderByChild("email").equalTo(current.getEmail())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                                userSnapshot.getRef().child("apartments").setValue(currentApartments)
                                                        .addOnSuccessListener(aVoid2 -> Log.d("AddApartment", "User apartments updated"))
                                                        .addOnFailureListener(e -> Log.e("AddApartment", "Failed to update user apartments: " + e.getMessage()));
                                                userSnapshot.getRef().child("currentApartment").setValue(apartmentCode)
                                                        .addOnSuccessListener(aVoid2 -> Log.d("AddApartment", "Current apartment updated"))
                                                        .addOnFailureListener(e -> Log.e("AddApartment", "Failed to update current apartment: " + e.getMessage()));
                                                break;
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e("AddApartment", "Database error: " + error.getMessage());
                                        }
                                    });
                        })
                        .addOnFailureListener(e -> Log.e("AddApartment", "Failed to create apartment: " + e.getMessage()));
            } else {
                Log.e("AddApartment", "Failed to generate apartment ID.");
            }

            isLoggedIn.setValue(true);
        }
    }

    public void fetchApartment(String apartmentCode) {
        User current = currentUser.getValue();
        if (current != null) {
            apartmentsReference.orderByChild("code").equalTo(apartmentCode)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean found = false;
                            for (DataSnapshot apartmentSnapshot : snapshot.getChildren()) {
                                Apartment apartment = apartmentSnapshot.getValue(Apartment.class);
                                if (apartment != null) {
                                    found = true;
                                    List<String> currentApartments = current.getApartments();
                                    if (!currentApartments.contains(apartmentCode)) {
                                        currentApartments.add(apartmentCode);
                                        current.currentApartment = apartmentCode;
                                        currentUser.setValue(current);
                                        databaseReference.orderByChild("email").equalTo(current.getEmail())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                        for (DataSnapshot userData : userSnapshot.getChildren()) {
                                                            userData.getRef().child("apartments").setValue(currentApartments)
                                                                    .addOnSuccessListener(aVoid -> {
                                                                        userData.getRef().child("currentApartment").setValue(apartmentCode)
                                                                                .addOnSuccessListener(aVoid2 -> {
                                                                                    apartmentFetchResult.setValue(apartmentCode); // Signal success
                                                                                })
                                                                                .addOnFailureListener(e -> apartmentFetchResult.setValue(null));
                                                                    })
                                                                    .addOnFailureListener(e -> apartmentFetchResult.setValue(null));
                                                            break;
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        apartmentFetchResult.setValue(null);
                                                    }
                                                });
                                    } else {
                                        apartmentFetchResult.setValue(apartmentCode); // Already joined
                                    }
                                    break;
                                }
                            }
                            if (!found) {
                                apartmentFetchResult.setValue(null);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            apartmentFetchResult.setValue(null);
                        }
                    });

            isLoggedIn.setValue(true);
        }
    }
    // New method to get apartment name by code
    public LiveData<String> getApartmentNameByCode(String apartmentCode) {
        MutableLiveData<String> apartmentNameLiveData = new MutableLiveData<>("");

        apartmentsReference.orderByChild("code").equalTo(apartmentCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot apartmentSnapshot : snapshot.getChildren()) {
                            Apartment apartment = apartmentSnapshot.getValue(Apartment.class);
                            if (apartment != null) {
                                apartmentNameLiveData.setValue(apartment.getName());
                                return;
                            }
                        }
                        apartmentNameLiveData.setValue(""); // Return empty string if not found
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("GetApartmentName", "Database error: " + error.getMessage());
                        apartmentNameLiveData.setValue("");
                    }
                });

        return apartmentNameLiveData;
    }
    // New method to remove an apartment by code
    public void removeApartment(String apartmentCode) {
        User current = currentUser.getValue();
        if (current != null) {
            List<String> currentApartments = current.getApartments();
            if (currentApartments.contains(apartmentCode)) {
                currentApartments.remove(apartmentCode);
                String newCurrentApartment = "";

                // If the removed apartment was the current one, set the next available apartment
                if (current.currentApartment != null && current.currentApartment.equals(apartmentCode)) {
                    if (!currentApartments.isEmpty()) {
                        // Set the first remaining apartment as the new currentApartment
                        newCurrentApartment = currentApartments.get(0);
                    }
                    current.currentApartment = newCurrentApartment;
                }

                currentUser.setValue(current);

                // Update user in database
                String finalNewCurrentApartment = newCurrentApartment;
                databaseReference.orderByChild("email").equalTo(current.getEmail())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                    userSnapshot.getRef().child("apartments").setValue(currentApartments)
                                            .addOnSuccessListener(aVoid -> Log.d("RemoveApartment", "User apartments updated"))
                                            .addOnFailureListener(e -> Log.e("RemoveApartment", "Failed to update user apartments: " + e.getMessage()));
                                    userSnapshot.getRef().child("currentApartment").setValue(finalNewCurrentApartment)
                                            .addOnSuccessListener(aVoid -> Log.d("RemoveApartment", "Current apartment updated"))
                                            .addOnFailureListener(e -> Log.e("RemoveApartment", "Failed to update current apartment: " + e.getMessage()));
                                    break;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("RemoveApartment", "Database error: " + error.getMessage());
                            }
                        });
            } else {
                Log.w("RemoveApartment", "Apartment code " + apartmentCode + " not found in user's apartments");
            }
        }
    }
    public void changeProfilePicture(int newProfilePicture) {
        User current = currentUser.getValue();
        if (current != null) {
            current.profilePicture = newProfilePicture;
            currentUser.setValue(current);

            databaseReference.orderByChild("email").equalTo(current.getEmail())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                userSnapshot.getRef().child("profilePicture").setValue(newProfilePicture)
                                        .addOnSuccessListener(aVoid -> Log.d("ChangeProfilePicture", "Profile picture updated"))
                                        .addOnFailureListener(e -> Log.e("ChangeProfilePicture", "Failed to update profile picture: " + e.getMessage()));
                                break;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("ChangeProfilePicture", "Database error: " + error.getMessage());
                        }
                    });
        }
    }

    public void changeUsername(String newUsername) {
        User current = currentUser.getValue();
        if (current != null) {
            current.username = newUsername;
            currentUser.setValue(current);

            databaseReference.orderByChild("email").equalTo(current.getEmail())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                userSnapshot.getRef().child("username").setValue(newUsername)
                                        .addOnSuccessListener(aVoid -> Log.d("ChangeUsername", "Username updated"))
                                        .addOnFailureListener(e -> Log.e("ChangeUsername", "Failed to update username: " + e.getMessage()));
                                break;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("ChangeUsername", "Database error: " + error.getMessage());
                        }
                    });
        }
    }

    public void toggleNotifications(boolean isEnabled) {
        User current = currentUser.getValue();
        if (current != null) {
            current.notifications = isEnabled;
            currentUser.setValue(current);

            databaseReference.orderByChild("email").equalTo(current.getEmail())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                userSnapshot.getRef().child("notifications").setValue(isEnabled)
                                        .addOnSuccessListener(aVoid -> Log.d("ToggleNotifications", "Notifications updated to " + isEnabled))
                                        .addOnFailureListener(e -> Log.e("ToggleNotifications", "Failed to update notifications: " + e.getMessage()));
                                break;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("ToggleNotifications", "Database error: " + error.getMessage());
                        }
                    });
        }
    }

    public boolean isNotificationsEnabled() {
        User current = currentUser.getValue();
        if (current != null) {
            return current.notifications;
        }
        return false; // Default to false if no user is logged in
    }
}
