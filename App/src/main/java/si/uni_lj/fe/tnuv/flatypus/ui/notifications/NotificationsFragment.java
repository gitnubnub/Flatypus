package si.uni_lj.fe.tnuv.flatypus.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentNotificationsBinding;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationsViewModel viewModel;
    private UserViewModel userViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                LinearLayout newNotifsContainer = binding.newNotificationsContainer;
                viewModel.getNotifications(user.getCurrentApartment(), user.getEmail()).observe(getViewLifecycleOwner(), notifications -> {
                    newNotifsContainer.removeAllViews();

                    for (NotificationsViewModel.Notification notification : notifications) {
                        if (!notification.isSeen()) {
                            View newNotifView = inflater.inflate(R.layout.notification_layout, newNotifsContainer, false);

                            TextView notificationText1 = newNotifView.findViewById(R.id.notification_text);
                            notificationText1.setText(notification.getText());
                            notificationText1.setBackgroundResource(R.drawable.received_message_bckg);
                            notificationText1.setTextColor(getResources().getColor(R.color.dark_red));

                            newNotifsContainer.addView(newNotifView);
                        }
                    }
                });

                LinearLayout oldNotifsContainer = binding.oldNotificationsContainer;
                viewModel.getNotifications(user.getCurrentApartment(), user.getEmail()).observe(getViewLifecycleOwner(), notifications -> {
                    oldNotifsContainer.removeAllViews();

                    for (NotificationsViewModel.Notification notification : notifications) {
                        if (notification.isSeen()) {
                            View oldNotifView = inflater.inflate(R.layout.notification_layout, oldNotifsContainer, false);

                            TextView notificationText2 = oldNotifView.findViewById(R.id.notification_text);
                            notificationText2.setText(notification.getText());
                            notificationText2.setBackgroundResource(R.drawable.sent_message_bckg);
                            notificationText2.setTextColor(getResources().getColor(R.color.wheat));

                            oldNotifsContainer.addView(oldNotifView);
                        }
                    }
                });
            }
        });

        return root;
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.updateStatus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}