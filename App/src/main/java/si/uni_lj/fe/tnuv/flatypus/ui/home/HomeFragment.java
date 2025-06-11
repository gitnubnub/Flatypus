package si.uni_lj.fe.tnuv.flatypus.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentHomeBinding;
import si.uni_lj.fe.tnuv.flatypus.ui.expenses.ExpensesViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.shoppinglist.ShoppingListViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.to_do.ToDoViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        ShoppingListViewModel shoppingViewModel = new ViewModelProvider(requireActivity()).get(ShoppingListViewModel.class);
        ToDoViewModel toDoViewModel = new ViewModelProvider(this).get(ToDoViewModel.class);
        ExpensesViewModel expensesViewModel = new ViewModelProvider(this).get(ExpensesViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Settings Icon Navigation
        binding.settingsIcon.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_home_to_nav_settings);
        });

        // Notification Icon Navigation
        binding.notificationIcon.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_home_to_nav_notifications);
        });

        // Initialize heart calculation with user and shopping data
        homeViewModel.initHeartCalculation(requireContext(), userViewModel, shoppingViewModel, toDoViewModel, expensesViewModel);

        // Hearts in an arc
        ConstraintLayout layout = (ConstraintLayout) root;
        homeViewModel.getHeartCount().observe(getViewLifecycleOwner(), count -> {
            if (layout.getWidth() == 0) {
                layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        positionHearts(layout, count);
                    }
                });
            } else {
                positionHearts(layout, count);
            }
        });

        // Notification Count
        TextView notificationCount = binding.notificationCount;
        homeViewModel.getNotificationCount().observe(getViewLifecycleOwner(), count -> {
            notificationCount.setText(count > 99 ? "99+" : String.valueOf(count));
        });

        // Platypus (Observe LiveData to handle null safely)
        ImageView platypus = binding.characterImage;
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                int currentPfp = user.getProfilePicture();
                Log.d("Home Fragment: ", "Profile picture value: " + currentPfp);
                if (currentPfp == R.drawable.pfp_red) {
                    platypus.setImageResource(R.drawable.platypus_red);
                } else if (currentPfp == R.drawable.pfp_green) {
                    platypus.setImageResource(R.drawable.platypus_green);
                } else if (currentPfp == R.drawable.pfp_purple) {
                    platypus.setImageResource(R.drawable.platypus_purple);
                } else {
                    Log.w("Home Fragment: ", "Unknown profile picture value: " + currentPfp + ", defaulting to platypus_red");
                    platypus.setImageResource(R.drawable.platypus_red);
                }
            } else {
                Log.w("Home Fragment: ", "User is null, defaulting to platypus_red");
                platypus.setImageResource(R.drawable.platypus_red);
            }
        });

        return root;
    }

    private void positionHearts(ConstraintLayout layout, Integer count) {
        int MAX_HEARTS = 5;
        // Remove existing hearts
        for (int i = layout.getChildCount() - 1; i >= 0; i--) {
            View child = layout.getChildAt(i);
            if (child.getTag() != null && child.getTag().equals("heart")) {
                layout.removeView(child);
            }
        }

        // Add new hearts in an arc
        if (count == null) count = 0; // Handle null case
        float totalWidth = layout.getWidth();
        float effectiveWidth = totalWidth * 0.3f;
        float heartSize = 40f; // Size of each heart in dp
        float spacing = (effectiveWidth - MAX_HEARTS * heartSize) / (MAX_HEARTS + 1); // Space between hearts
        float arcHeight = 50f; // Height of the arc in dp (adjust as needed)

        float heartSizePx = heartSize * getResources().getDisplayMetrics().density;
        float spacingPx = spacing * getResources().getDisplayMetrics().density;
        float arcHeightPx = arcHeight * getResources().getDisplayMetrics().density;

        int middleIndex = (MAX_HEARTS - 1) / 2;
        float middle = totalWidth / 2;

        // Draw all hearts in a single loop
        for (int i = 0; i < MAX_HEARTS; i++) {
            ImageView heart = new ImageView(requireContext());
            heart.setTag("heart");

            // Set heart type based on count
            if (i < count) {
                heart.setImageResource(R.drawable.heart_full);
            } else {
                heart.setImageResource(R.drawable.heart_empty);
            }

            // Calculate position with consistent arc
            float x = middle + (i - middleIndex) * (heartSizePx + spacingPx);
            float normalizedPosition = (float) (i - middleIndex) / middleIndex; // Normalize from -1 to 1
            float y = arcHeightPx * normalizedPosition * normalizedPosition; // Parabolic arc

            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    (int) heartSizePx, (int) heartSizePx);
            params.startToStart = R.id.hearts_anchor;
            params.topToTop = R.id.hearts_anchor;
            params.leftMargin = (int) (x - heartSizePx / 2);
            params.topMargin = (int) y;
            heart.setLayoutParams(params);

            layout.addView(heart);
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}