package si.uni_lj.fe.tnuv.flatypus.ui.settings;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.data.Apartment;
import si.uni_lj.fe.tnuv.flatypus.data.Chat;
import si.uni_lj.fe.tnuv.flatypus.data.Expense;
import si.uni_lj.fe.tnuv.flatypus.data.Task;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private DatabaseReference databaseReference;
    private FragmentSettingsBinding binding;
    private String currentUsername = "Eva"; // Pull from database Default username (replace with dynamic source if needed)
    private String currentApartmentCode = "XDSYI"; // Pull from database Default apartment code (replace with dynamic source if needed)

    private int currentProfilePicture = R.drawable.pfp_red; // Pull from database (if empty, set to red)

    private final int[] profilePictures = {
            R.drawable.pfp_red,
            R.drawable.pfp_green,
            R.drawable.pfp_purple
    };

    private List<Apartment> userApartments;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://flatypus-fde01-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = database.getReference();

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize user apartments (replace with dynamic data source if needed)
        userApartments = new ArrayList<>(); //pull from database
        userApartments.add(new Apartment("Apartment 1", "XDSYI", new ArrayList<String>(), new ArrayList<Expense>(), new ArrayList<Task>(), new ArrayList<Chat>()));
        userApartments.add(new Apartment("Apartment 2", "ABCDE", new ArrayList<String>(), new ArrayList<Expense>(), new ArrayList<Task>(), new ArrayList<Chat>()));

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
        notificationsToggle.setChecked(true); // Default state (replace with dynamic source if needed)
        notificationsToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle notifications toggle (implement your logic here)
            Toast.makeText(requireContext(), "Notifications " + (isChecked ? "on" : "off"), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // Leave Apartment Button
        binding.leaveApartmentButton.setOnClickListener(v -> showLeaveConfirmationDialog());

        // Leave Apartment Button
        binding.logOutButton.setOnClickListener(v -> showLogOutConfirmationDialog());

        return root;
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
                    currentUsername = usernameEditText.getText().toString().trim();
                    binding.usernameInput.setText(currentUsername);
                    Toast.makeText(requireContext(), "Username saved", Toast.LENGTH_SHORT).show();
                    // Implement your save logic here (e.g., save to database or preferences)
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
                    Toast.makeText(requireContext(), "Apartment left", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogOutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Are you sure?")
                .setMessage("Do you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Implement leave apartment logic here
                    Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAvailableProfilePictures() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Profile Picture");

        // Inflate the dialog layout
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_profile_pictures, null);
        GridView gridView = view.findViewById(R.id.profile_pictures_grid);

        // Set up the adapter
        ProfilePictureAdapter adapter = new ProfilePictureAdapter(requireContext(), profilePictures);
        gridView.setAdapter(adapter);

        // Find the current profile picture's position in the array
        for (int i = 0; i < profilePictures.length; i++) {
            if (profilePictures[i] == currentProfilePicture) {
                adapter.setSelectedPosition(i);
                break;
            }
        }

        // Handle item clicks
        gridView.setOnItemClickListener((parent, v, position, id) -> {
            adapter.setSelectedPosition(position);
        });

        builder.setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    int selectedPosition = adapter.getSelectedPosition();
                    if (selectedPosition != -1) {
                        currentProfilePicture = profilePictures[selectedPosition];
                        binding.profilePicture.setImageResource(currentProfilePicture);
                        Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                        // Implement your save logic here (e.g., save to database or preferences)
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

        // Set up the adapter
        ApartmentAdapter adapter = new ApartmentAdapter(requireContext(), userApartments);
        listView.setAdapter(adapter);

        // Handle "Add Apartment" button click
        addApartmentButton.setOnClickListener(v -> showAddApartmentDialog(adapter));

        builder.setView(view)
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAddApartmentDialog(ApartmentAdapter adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Apartment");

        // Inflate the dialog layout
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_apartment, null);
        EditText codeInput = view.findViewById(R.id.apartment_code_input);

        builder.setView(view)
                .setPositiveButton("Join", (dialog, which) -> {
                    String code = codeInput.getText().toString().trim().toUpperCase();
                    if (code.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter a code", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Simulate joining an apartment (replace with actual logic)
                    String apartmentName = "Apartment " + (userApartments.size() + 1); // Placeholder name
                    Apartment newApartment = new Apartment(apartmentName, code, new ArrayList<String>(), new ArrayList<Expense>(), new ArrayList<Task>(), new ArrayList<Chat>());
                    adapter.addApartment(newApartment);
                    Toast.makeText(requireContext(), "Joined " + apartmentName, Toast.LENGTH_SHORT).show();
                    // Implement your join apartment logic here (e.g., API call)
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}