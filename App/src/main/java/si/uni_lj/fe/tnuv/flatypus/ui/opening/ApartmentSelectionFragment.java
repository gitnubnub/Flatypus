package si.uni_lj.fe.tnuv.flatypus.ui.opening;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.security.SecureRandom;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentApartmentSelectionBinding;
import si.uni_lj.fe.tnuv.flatypus.ui.notifications.NotificationsViewModel;

public class ApartmentSelectionFragment extends Fragment {

    private FragmentApartmentSelectionBinding binding;
    private UserViewModel viewModel;
    private NotificationsViewModel notifViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentApartmentSelectionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        notifViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        EditText apartmentCodeInput = binding.apartmentCodeInput;

        binding.joinApartmentButton.setOnClickListener(v -> {
            String apartmentCode = apartmentCodeInput.getText().toString().trim();

            if (apartmentCode.isEmpty()) {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Please fill the field")
                        .setPositiveButton("OK", (d2, which) -> d2.dismiss())
                        .show();
                return;
            }

            viewModel.fetchApartment(apartmentCode);

            viewModel.getRoommates(apartmentCode).observe(getViewLifecycleOwner(), roommates -> {
                if (roommates != null && !roommates.isEmpty()) {
                    for (UserViewModel.User roommate : roommates) {
                        notifViewModel.addNotification(apartmentCode, roommate.getEmail(), "A new flatypus joined your apartment!");
                    }
                }
            });
        });

        binding.createApartmentButton.setOnClickListener(v -> showCreateApartmentDialog());

        return root;
    }

    private void showCreateApartmentDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.create_apartment_layout, null);

        EditText apartmentNameInput = dialogView.findViewById(R.id.apartment_name_input);
        Button createApartmentButton = dialogView.findViewById(R.id.create_apartment_confirm_button);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Create Apartment")
                .setView(dialogView)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();
        dialog.show();

        createApartmentButton.setOnClickListener(v -> {
            String apartmentName = apartmentNameInput.getText().toString().trim();
            String apartmentCode = generateRandomString();

            if (!apartmentName.isEmpty()) {
                viewModel.addApartment(apartmentCode, apartmentName);
                dialog.dismiss();
            } else {
                apartmentNameInput.setError("Item name cannot be empty");
            }
        });
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
