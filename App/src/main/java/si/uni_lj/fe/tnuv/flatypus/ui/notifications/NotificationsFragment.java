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

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationsViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        LinearLayout newNotifsContainer = binding.newNotificationsContainer;
        newNotifsContainer.removeAllViews();
        View newNotifView = inflater.inflate(R.layout.notification_layout, newNotifsContainer, false);

        //placeholder examples
        TextView notificationText1 = newNotifView.findViewById(R.id.notification_text);
        notificationText1.setText("preview unread notification");
        notificationText1.setBackgroundResource(R.drawable.received_message_bckg);
        notificationText1.setTextColor(getResources().getColor(R.color.dark_red));

        newNotifsContainer.addView(newNotifView);

        LinearLayout oldNotifsContainer = binding.oldNotificationsContainer;
        oldNotifsContainer.removeAllViews();
        View oldNotifView = inflater.inflate(R.layout.notification_layout, oldNotifsContainer, false);

        //placeholder examples
        TextView notificationText2 = oldNotifView.findViewById(R.id.notification_text);
        notificationText2.setText("preview unread notification");
        notificationText2.setBackgroundResource(R.drawable.sent_message_bckg);
        notificationText2.setTextColor(getResources().getColor(R.color.wheat));

        oldNotifsContainer.addView(oldNotifView);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}