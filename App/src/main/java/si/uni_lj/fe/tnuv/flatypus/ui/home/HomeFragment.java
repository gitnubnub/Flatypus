package si.uni_lj.fe.tnuv.flatypus.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Character Image
        /*ImageView characterImage = binding.characterImage;
        homeViewModel.getCharacter().observe(getViewLifecycleOwner(), character -> {
            if ("red_fluffy".equals(character)) {
                characterImage.setImageResource(R.drawable.red_fluffy);
            }
        });*/

        // Hearts in an arc
        ConstraintLayout layout = (ConstraintLayout) root;
        homeViewModel.getHeartCount().observe(getViewLifecycleOwner(), count -> {
            if (layout.getWidth() == 0) {
                // Wait for layout to be measured
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
        if (count != null && count > 0) {
            float totalWidth = layout.getWidth();
            float effectiveWidth = totalWidth * 0.3f;
            float heartSize = 40f; // Size of each heart in dp
            float spacing = (effectiveWidth - MAX_HEARTS * heartSize) / (MAX_HEARTS + 1); // Space between hearts
            float arcHeight = 50f; // Height of the arc in dp (adjust as needed)

            float heartSizePx = heartSize * getResources().getDisplayMetrics().density;
            float spacingPx = spacing * getResources().getDisplayMetrics().density;
            float arcHeightPx = arcHeight * getResources().getDisplayMetrics().density;

            int middleIndex = (MAX_HEARTS - 1) / 2;

            for (int i = 0; i < count; i++) {
                ImageView heart = new ImageView(requireContext());
                heart.setImageResource(R.drawable.heart_full);
                heart.setTag("heart"); // Tag to identify hearts for removal

                float middle = totalWidth / 2;
                // Calculate position
                float x = middle + (i - middleIndex) * (heartSizePx + spacingPx);

                float normalizedPosition = (float) ((float) (i - (MAX_HEARTS - 1) / 2.0) / ((MAX_HEARTS - 1) / 2.0));

                if (count == 1) {
                    normalizedPosition = 0;
                }
                float y = arcHeightPx * normalizedPosition * normalizedPosition;

                // Set layout params
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                        (int) heartSizePx, (int) heartSizePx);
                params.startToStart = R.id.hearts_anchor;
                params.topToTop = R.id.hearts_anchor;
                params.leftMargin = (int) (x - heartSizePx / 2);
                params.topMargin = (int) y;
                heart.setLayoutParams(params);

                layout.addView(heart);
            }

            for (int i = count; i < 5; i++) {
                ImageView heart = new ImageView(requireContext());
                heart.setImageResource(R.drawable.heart_empty);
                heart.setTag("heart"); // Tag to identify hearts for removal

                float middle = totalWidth / 2;
                // Calculate position
                float x = middle + (i - middleIndex) * (heartSizePx + spacingPx);

                float normalizedPosition = (float) ((float) (i - (MAX_HEARTS - 1) / 2.0) / ((MAX_HEARTS - 1) / 2.0));

                if (count == 1) {
                    normalizedPosition = 0;
                }
                float y = arcHeightPx * normalizedPosition * normalizedPosition;

                // Set layout params
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}