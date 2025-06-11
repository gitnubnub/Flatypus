package si.uni_lj.fe.tnuv.flatypus.ui.settings;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.data.Apartment;
import si.uni_lj.fe.tnuv.flatypus.data.Chat;
import si.uni_lj.fe.tnuv.flatypus.data.Expense;
import si.uni_lj.fe.tnuv.flatypus.data.Task;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentSettingsBinding;
import si.uni_lj.fe.tnuv.flatypus.ui.notifications.NotificationsViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;

public class SettingsFragment extends Fragment {

    private DatabaseReference databaseReference;
    private FragmentSettingsBinding binding;
    private UserViewModel viewModel;
    private NotificationsViewModel notifViewModel;
    private String currentUsername = "Eva"; // Pull from database Default username (replace with dynamic source if needed)
    private String currentApartmentCode = "XDSYI"; // Pull from database Default apartment code (replace with dynamic source if needed)

    private int currentProfilePicture; // Pull from database (if empty, set to red)

    private final int[] profilePictures = {
            R.drawable.pfp_red,
            R.drawable.pfp_green,
            R.drawable.pfp_purple
    };

    private List<Apartment> userApartments = new ArrayList<>(); // will be populated from database

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://flatypus-fde01-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = database.getReference();

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        notifViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);
        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), new Observer<UserViewModel.User>() {
            @Override
            public void onChanged(UserViewModel.User user) {
                if (user != null) {
                    currentUsername = user.getUsername();
                    currentApartmentCode = user.getCurrentApartment();
                    currentProfilePicture = user.getProfilePicture();
                    updateUIWithUserData(user);
                    fetchAndUpdateApartments(user.getApartments());
                }
            }
        });
        // Back Button
        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // Username Input
        EditText usernameInput = binding.usernameInput;
        usernameInput.setText(currentUsername);
        usernameInput.setOnClickListener(v -> showUsernameDialog());
        ImageButton changeNameButton = binding.changeNameButton;
        changeNameButton.setOnClickListener(v -> showUsernameDialog());

        // Profile Picture
        ImageView profilePicture = binding.profilePicture;
        profilePicture.setImageResource(currentProfilePicture);
        ImageButton changePictureButton = binding.changePictureButton;
        changePictureButton.setOnClickListener(v -> {
            // Implement image picker logic here (e.g., using Intent to open gallery)
            showAvailableProfilePictures();
        });

        // Notifications Toggle
        Switch notificationsToggle = binding.notificationsToggle;
        notificationsToggle.setChecked(viewModel.isNotificationsEnabled()); // Default state (replace with dynamic source if needed)
        notificationsToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle notifications toggle (implement your logic here)
            viewModel.toggleNotifications(isChecked);
        });

        // Apartments Button
        binding.apartmentsButton.setOnClickListener(v -> {
            // Implement navigation to apartments list fragment or activity
            showApartmentsDialog();
        });

        // Apartment Code
        binding.apartmentCode.setText(currentApartmentCode);
        ImageButton copyCodeButton = binding.copyCodeButton;
        copyCodeButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Apartment Code", currentApartmentCode);
            clipboard.setPrimaryClip(clip);
            new AlertDialog.Builder(requireContext())
                    .setMessage("Copied to clipboard")
                    .setPositiveButton("OK", (d2, which) -> d2.dismiss())
                    .show();
        });

        // Leave Apartment Button
        binding.leaveApartmentButton.setOnClickListener(v -> showLeaveConfirmationDialog());

        // Log Out Button
        binding.logOutButton.setOnClickListener(v -> showLogOutConfirmationDialog());

        return root;
    }


    // DATABASE
    private void updateUIWithUserData(UserViewModel.User user) {
        binding.usernameInput.setText(user.getUsername());
        binding.profilePicture.setImageResource(user.getProfilePicture());
        binding.apartmentCode.setText(user.getCurrentApartment());
        binding.notificationsToggle.setChecked(user.getNotifications());
    }

    private void fetchAndUpdateApartments(List<String> apartmentCodes) {
        userApartments.clear(); // Clear the list to start fresh
        if (apartmentCodes == null || apartmentCodes.isEmpty()) return;

        for (String code : apartmentCodes) {
            // Use LiveData to fetch the apartment name
            viewModel.getApartmentNameByCode(code).observeForever(name -> {
                if (name != null && !name.isEmpty()) {
                    // Check if an apartment with this code already exists
                    boolean apartmentExists = userApartments.stream().anyMatch(apartment ->
                            apartment.getCode() != null && apartment.getCode().equals(code)
                    );
                    if (!apartmentExists) {
                        // Create and add a new Apartment object only if it doesn't exist
                        userApartments.add(new Apartment(name, code, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
                    }
                }
                // Note: No notifyDataSetChanged here since adapter isn't passed
            });
        }
    }

    private void showUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Username");

        // Inflate a layout with an EditText
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_username, null);
        EditText usernameEditText = view.findViewById(R.id.username_edit_text);
        usernameEditText.setText(currentUsername);

        builder.setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newUsername = usernameEditText.getText().toString().trim();
                    if (!newUsername.isEmpty()) {
                        viewModel.changeUsername(newUsername);
                        currentUsername = newUsername;
                        binding.usernameInput.setText(currentUsername);
                        new AlertDialog.Builder(requireContext())
                                .setMessage("Username saved")
                                .setPositiveButton("OK", (d2, w) -> d2.dismiss())
                                .show();
                    } else {
                        new AlertDialog.Builder(requireContext())
                                .setMessage("Username cannot be empty")
                                .setPositiveButton("OK", (d2, w) -> d2.dismiss())
                                .show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLeaveConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Are you sure?")
                .setMessage("Do you want to leave the apartment?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Implement leave apartment logic here
                    String apartmentToLeave = currentApartmentCode;
                    viewModel.removeApartment(apartmentToLeave);
                    currentApartmentCode = viewModel.getCurrentUser().getValue().getCurrentApartment();
                    binding.apartmentCode.setText(currentApartmentCode);
                    new AlertDialog.Builder(requireContext())
                            .setMessage("Apartment left")
                            .setPositiveButton("OK", (d2, w) -> d2.dismiss())
                            .show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogOutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Are you sure?")
                .setMessage("Do you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    viewModel.logout();
                    new AlertDialog.Builder(requireContext())
                            .setMessage("Logged out")
                            .setPositiveButton("OK", (d2, w) -> d2.dismiss())
                            .show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAvailableProfilePictures() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Profile Picture");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_profile_pictures, null);
        GridView gridView = view.findViewById(R.id.profile_pictures_grid);

        ProfilePictureAdapter adapter = new ProfilePictureAdapter(requireContext(), profilePictures);
        gridView.setAdapter(adapter);

        for (int i = 0; i < profilePictures.length; i++) {
            if (profilePictures[i] == currentProfilePicture) {
                adapter.setSelectedPosition(i);
                break;
            }
        }

        gridView.setOnItemClickListener((parent, v, position, id) -> {
            adapter.setSelectedPosition(position);
        });

        builder.setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    int selectedPosition = adapter.getSelectedPosition();
                    if (selectedPosition != -1) {
                        currentProfilePicture = profilePictures[selectedPosition];
                        binding.profilePicture.setImageResource(currentProfilePicture);
                        viewModel.changeProfilePicture(currentProfilePicture);
                        new AlertDialog.Builder(requireContext())
                                .setMessage("Profile picture updated")
                                .setPositiveButton("OK", (d2, w) -> d2.dismiss())
                                .show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showApartmentsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Your Apartments");

        // Inflate the dialog layout
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_apartments, null);
        ListView listView = view.findViewById(R.id.apartments_list);
        Button addApartmentButton = view.findViewById(R.id.add_apartment_button);
        Button createApartmentButton = view.findViewById(R.id.create_apartment_button);

        // Set up the adapter
        ApartmentAdapter adapter = new ApartmentAdapter(requireContext(), userApartments);
        listView.setAdapter(adapter);

        // Handle apartment item clicks
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Apartment selectedApartment = userApartments.get(position);
            String apartmentCode = selectedApartment.getCode();
            viewModel.switchApartment(apartmentCode); // Switch to the selected apartment

            // Optionally observe currentUser to confirm the switch
            viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null && user.getCurrentApartment().equals(apartmentCode)) {
                    Log.d("ApartmentsDialog", "Successfully switched to apartment: " + selectedApartment.getName());
                    ((AlertDialog) parent.getTag()).dismiss(); // Dismiss dialog on success
                }
            });
        });

        // Handle "Add Apartment" button click
        addApartmentButton.setOnClickListener(v -> showAddApartmentDialog(adapter));
        createApartmentButton.setOnClickListener(v -> showCreateApartmentDialog(adapter));
        builder.setView(view)
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        // Tag the dialog for the item click listener to access it
        listView.setTag(dialog);
    }
    private void showAddApartmentDialog(ApartmentAdapter adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Apartment");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_apartment, null);
        EditText codeInput = view.findViewById(R.id.apartment_code_input);

        builder.setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String code = codeInput.getText().toString();
                    if (code.isEmpty()) {
                        new AlertDialog.Builder(requireContext())
                                .setMessage("Please enter a code")
                                .setPositiveButton("OK", (d2, w) -> d2.dismiss())
                                .show();
                        return;
                    }
                    viewModel.fetchApartment(code, notifViewModel);
                    adapter.notifyDataSetChanged();
                    currentApartmentCode = code; // Update current apartment code
                    binding.apartmentCode.setText(currentApartmentCode);
                    new AlertDialog.Builder(requireContext())
                            .setMessage("Joined apartment with code " + code)
                            .setPositiveButton("OK", (d2, w) -> d2.dismiss())
                            .show();
                    fetchAndUpdateApartments(viewModel.getCurrentUser().getValue().getApartments());
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showCreateApartmentDialog(ApartmentAdapter adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Create Apartment");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_apartment, null);
        EditText nameInput = view.findViewById(R.id.apartment_name_input);

        builder.setView(view)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = nameInput.getText().toString();
                    if (name.isEmpty()) {
                        new AlertDialog.Builder(requireContext())
                                .setMessage("Please enter a name")
                                .setPositiveButton("OK", (d2, w) -> d2.dismiss())
                                .show();
                        return;
                    }
                    String code = generateRandomString();
                    viewModel.addApartment(code, name);
                    fetchAndUpdateApartments(viewModel.getCurrentUser().getValue().getApartments());
                    adapter.notifyDataSetChanged();
                    currentApartmentCode = code; // Update current apartment code
                    binding.apartmentCode.setText(currentApartmentCode);
                    new AlertDialog.Builder(requireContext())
                            .setMessage("Joined apartment with code " + code)
                            .setPositiveButton("OK", (d2, w) -> d2.dismiss())
                            .show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static String generateRandomString() {
        final SecureRandom random = new SecureRandom();
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}