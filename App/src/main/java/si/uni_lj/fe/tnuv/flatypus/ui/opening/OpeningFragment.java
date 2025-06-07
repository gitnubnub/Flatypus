package si.uni_lj.fe.tnuv.flatypus.ui.opening;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentOpeningBinding;

public class OpeningFragment extends Fragment {

    private FragmentOpeningBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOpeningBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.loginButton.setOnClickListener(v -> {
            Navigation.findNavController(root).navigate(R.id.action_opening_to_nav_login);
        });

        binding.registerButton.setOnClickListener(v -> {
            Navigation.findNavController(root).navigate(R.id.action_opening_to_nav_register);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
