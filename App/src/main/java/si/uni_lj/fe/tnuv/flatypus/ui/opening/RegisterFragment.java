package si.uni_lj.fe.tnuv.flatypus.ui.opening;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentRegisterBinding;
import si.uni_lj.fe.tnuv.flatypus.ui.settings.ProfilePictureAdapter;

public class RegisterFragment  extends Fragment {

    private FragmentRegisterBinding binding;
    private UserViewModel viewModel;
    private int currentProfilePicture = R.drawable.pfp_red;
    private final int[] profilePictures = {
            R.drawable.pfp_red,
            R.drawable.pfp_green,
            R.drawable.pfp_purple
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        EditText usernameInput = binding.usernameInput;
        EditText emailInput = binding.emailInput;
        EditText passwordInput = binding.passwordInput;

        ImageView profilePicture = binding.profilePicture;
        profilePicture.setImageResource(currentProfilePicture);
        Button changePictureButton = binding.avatarButton;
        changePictureButton.setOnClickListener(v -> {
            showAvailableProfilePictures();
        });

        binding.registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.register(username, email, password, R.drawable.pfp_red);
            Navigation.findNavController(root).navigate(R.id.action_nav_register_to_nav_apartment);
        });

        return root;
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
                        Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                    }
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
