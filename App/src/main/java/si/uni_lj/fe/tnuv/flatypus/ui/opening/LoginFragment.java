package si.uni_lj.fe.tnuv.flatypus.ui.opening;

import android.app.AlertDialog;
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
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private UserViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        EditText emailInput = binding.emailInput;
        EditText passwordInput = binding.passwordInput;

        binding.loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Please fill all fields")
                        .setPositiveButton("OK", (d2, which) -> d2.dismiss())
                        .show();
                return;
            }

            viewModel.login(email, password);
            viewModel.correctLoginInput().observe(getViewLifecycleOwner(), correctLoginInput -> {

                if (!correctLoginInput) {
                    new AlertDialog.Builder(requireContext())
                            .setMessage("Incorrect email or password")
                            .setPositiveButton("OK", (d2, which) -> d2.dismiss())
                            .show();
                    viewModel.setCorrectLoginInput(true);
                    return;
                }
            });
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
