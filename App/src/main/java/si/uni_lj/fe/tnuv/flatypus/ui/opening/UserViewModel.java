package si.uni_lj.fe.tnuv.flatypus.ui.opening;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {

    public static class User {
        private String username;
        private String email;
        private String password;
        private String profilePicture;
        private String[] apartments;

        public User(String username, String email, String password, String profilePicture, String[] apartments) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.profilePicture = profilePicture;
            this.apartments = apartments;
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
        public String getProfilePicture() {
            return profilePicture;
        }
        public String[] getApartments() {
            return apartments;
        }
    }

    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);

    public UserViewModel() {
        String[] defaultApartments = {"XDSYI"};
        User defaultUser = new User("Eva", "eva@example.com", "password123", "red_fluffy", defaultApartments);
        currentUser.setValue(defaultUser);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> isLoggedIn() {
        return isLoggedIn;
    }

    public void login(String email, String password) {
        if ("eva@example.com".equals(email) && "password123".equals(password)) {
            String[] apartments = {"XDSYI"};
            User user = new User("Eva", email, password, "red_fluffy", apartments);
            currentUser.setValue(user);
            isLoggedIn.setValue(true);
        } else {
            isLoggedIn.setValue(false);
        }
    }

    public void register(String username, String email, String password, String profilePicture) {
        String[] apartments = {};
        User newUser = new User(username, email, password, profilePicture, apartments);
        currentUser.setValue(newUser);
    }

    public void logout() {
        currentUser.setValue(null);
        isLoggedIn.setValue(false);
    }

    public void addApartment(String apartmentCode) {
        User current = currentUser.getValue();
        if (current != null) {
            String[] currentApartments = current.getApartments();
            String[] newApartments = new String[currentApartments.length + 1];
            System.arraycopy(currentApartments, 0, newApartments, 0, currentApartments.length);
            newApartments[currentApartments.length] = apartmentCode;
            current.apartments = newApartments;
            currentUser.setValue(current);
        }
    }
}
