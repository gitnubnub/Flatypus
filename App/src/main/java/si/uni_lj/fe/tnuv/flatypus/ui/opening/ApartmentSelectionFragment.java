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
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentApartmentSelectionBinding;

public class ApartmentSelectionFragment extends Fragment {

    private FragmentApartmentSelectionBinding binding;
    private UserViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentApartmentSelectionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        EditText apartmentCodeInput = binding.apartmentCodeInput;

        binding.createApartmentButton.setOnClickListener(v -> {
            String apartmentCode = apartmentCodeInput.getText().toString().trim();

            if (apartmentCode.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill the field", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.addApartment(apartmentCode);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
