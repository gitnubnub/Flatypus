package si.uni_lj.fe.tnuv.flatypus.ui;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private String currentUsername = "Eva"; // Default username (replace with dynamic source if needed)
    private String currentApartmentCode = "XDSYI"; // Default apartment code (replace with dynamic source if needed)

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Back Button
        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // Username Input
        EditText usernameInput = binding.usernameInput;
        usernameInput.setText(currentUsername);
        usernameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                currentUsername = usernameInput.getText().toString();
                // Save username (implement your save logic here)
            }
        });

        // Profile Picture
        ImageView profilePicture = binding.profilePicture;
        ImageButton changePictureButton = binding.changePictureButton;
        changePictureButton.setOnClickListener(v -> {
            // Implement image picker logic here (e.g., using Intent to open gallery)
            Toast.makeText(requireContext(), "Change picture clicked", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "Apartments list clicked", Toast.LENGTH_SHORT).show();
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

        return root;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}