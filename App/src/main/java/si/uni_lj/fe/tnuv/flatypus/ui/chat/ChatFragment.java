package si.uni_lj.fe.tnuv.flatypus.ui.chat;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentChatBinding;
import si.uni_lj.fe.tnuv.flatypus.ui.notifications.NotificationsViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private ChatViewModel viewModel;
    private UserViewModel userViewModel;
    private NotificationsViewModel notifViewModel;
    private String currentUser;
    private String currentApartmentCode;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        notifViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.backButton.setOnClickListener(v -> {
            Navigation.findNavController(root).navigate(R.id.action_nav_chat_to_nav_home);
            AppCompatActivity activity = (AppCompatActivity) requireActivity();
            BottomNavigationView navView = activity.findViewById(R.id.nav_view);
            if (navView != null) {
                navView.setSelectedItemId(R.id.navigation_home);
            }
        });

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user.getEmail();
                currentApartmentCode = user.getCurrentApartment();

                LinearLayout messagesLayout = binding.messagesLayout;
                viewModel.getChats(currentApartmentCode).observe(getViewLifecycleOwner(), chats -> {
                    messagesLayout.removeAllViews();
                    Collections.sort(chats, Comparator.comparing(c -> Instant.parse(c.getTimestamp())));

                    // Map to cache user data
                    Map<String, UserViewModel.User> userCache = new HashMap<>();

                    for (ChatViewModel.Chat chat : chats) {
                        View chatView = inflater.inflate(R.layout.message_layout, messagesLayout, false);
                        TextView message = chatView.findViewById(R.id.message_text);
                        LinearLayout senderInfo = chatView.findViewById(R.id.sender_info);
                        ImageView senderProfilePicture = chatView.findViewById(R.id.sender_picture);
                        TextView senderName = chatView.findViewById(R.id.sender_name);

                        message.setText(chat.getMessage());

                        if (chat.getSender().equals(currentUser)) {
                            // Sent message
                            message.setBackgroundResource(R.drawable.sent_message_bckg);
                            message.setTextColor(getResources().getColor(R.color.wheat));
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) message.getLayoutParams();
                            params.gravity = Gravity.END;
                            message.setLayoutParams(params);
                            senderInfo.setVisibility(View.GONE);
                        } else {
                            // Received message
                            message.setBackgroundResource(R.drawable.received_message_bckg);
                            message.setTextColor(getResources().getColor(R.color.dark_red));
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) message.getLayoutParams();
                            params.gravity = Gravity.START;
                            message.setLayoutParams(params);
                            senderInfo.setVisibility(View.VISIBLE);

                            // Fetch user data if not cached
                            if (!userCache.containsKey(chat.getSender())) {
                                userViewModel.getUserByMail(chat.getSender()).observe(getViewLifecycleOwner(), otherUser -> {
                                    userCache.put(chat.getSender(), otherUser);
                                    senderProfilePicture.setImageResource(otherUser.getProfilePicture());
                                    senderName.setText(otherUser.getUsername());
                                });
                            } else {
                                UserViewModel.User cachedUser = userCache.get(chat.getSender());
                                senderProfilePicture.setImageResource(cachedUser.getProfilePicture());
                                senderName.setText(cachedUser.getUsername());
                            }
                        }

                        messagesLayout.addView(chatView);
                    }

                    // Scroll to the bottom after rendering
                    messagesLayout.post(() -> {
                        ScrollView scrollView = binding.messagesContainer;
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    });
                });

                EditText messageInput = binding.messageInput;
                ImageButton sendButton = binding.sendButton;

                sendButton.setOnClickListener(v -> {
                    String messageText = messageInput.getText().toString();
                    if (messageText.isEmpty()) {
                        messageInput.setError("Text is required");
                        return;
                    }

                    viewModel.addChat(currentApartmentCode, messageText, currentUser);
                    messageInput.setText(""); // Clear input after sending

                    userViewModel.getRoommates(currentApartmentCode).observe(getViewLifecycleOwner(), roommates -> {
                        if (roommates != null && !roommates.isEmpty()) {
                            for (UserViewModel.User roommate : roommates) {
                                if (!user.getEmail().equals(roommate.getEmail())) {
                                    notifViewModel.addNotification(currentApartmentCode, roommate.getEmail(), "You got a new message from " + user.getUsername() + ".");
                                }
                            }
                        }
                    });
                });
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