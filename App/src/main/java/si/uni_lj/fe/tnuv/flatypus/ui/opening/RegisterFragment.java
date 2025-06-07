package si.uni_lj.fe.tnuv.flatypus.ui.opening;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentRegisterBinding;

public class RegisterFragment  extends Fragment {

    private FragmentRegisterBinding binding;
    private UserViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        EditText usernameInput = binding.usernameInput;
        EditText emailInput = binding.emailInput;
        EditText passwordInput = binding.passwordInput;

        binding.registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.register(username, email, password, "red_fluffy");
            if (!viewModel.isLoggedIn().getValue()) {
                Toast.makeText(requireContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
            } else {
                Navigation.findNavController(root).navigate(R.id.action_nav_register_to_nav_apartment);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
