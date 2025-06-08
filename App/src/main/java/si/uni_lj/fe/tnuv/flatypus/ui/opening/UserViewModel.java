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
            this.apartments = apartments;
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
            return apartments;
        }
        public String getCurrentApartment() {
            return currentApartment;
        }
    }

    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);
    private DatabaseReference databaseReference;

    public UserViewModel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://flatypus-fde01-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = database.getReference("users");
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> isLoggedIn() {
        return isLoggedIn;
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

    public void addApartment(String apartmentCode) {
        User current = currentUser.getValue();
        if (current != null) {
            List<String> currentApartments = current.getApartments();
            currentApartments.add(apartmentCode);
            currentUser.setValue(current);
            isLoggedIn.setValue(true);
        }
    }
}
