package si.uni_lj.fe.tnuv.flatypus.ui.chat;

import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.databinding.FragmentChatBinding;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private ChatViewModel viewModel;
    private UserViewModel userViewModel;
    private String currentUser;
    private String currentApartmentCode;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user.getEmail();
                currentApartmentCode = user.getCurrentApartment();

                ScrollView messagesContainer = binding.messagesContainer;
                viewModel.getChats(currentApartmentCode).observe(getViewLifecycleOwner(), chats -> {
                    messagesContainer.removeAllViews();

                    for (int i = 0; i < chats.size(); i++) {
                        ChatViewModel.Chat chat = chats.get(i);
                        View chatView = inflater.inflate(R.layout.message_layout, messagesContainer, false);

                        if (!chat.getSender().equals(currentUser)) {
                            userViewModel.getUserByMail(chat.getSender()).observe(getViewLifecycleOwner(), otherUser -> {
                                TextView message = chatView.findViewById(R.id.message_text);
                                ImageView senderProfilePicture = chatView.findViewById(R.id.sender_picture);
                                TextView senderName = chatView.findViewById(R.id.sender_name);

                                message.setText(chat.getMessage());
                                message.setBackgroundResource(R.drawable.received_message_bckg);
                                message.setTextColor(getResources().getColor(R.color.dark_red));
                                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) message.getLayoutParams();
                                params.gravity = Gravity.START;
                                message.setLayoutParams(params);

                                senderProfilePicture.setImageResource(otherUser.getProfilePicture());
                                senderName.setText(otherUser.getUsername());
                            });
                        } else {
                            TextView message = chatView.findViewById(R.id.message_text);
                            message.setText(chat.getMessage());
                            message.setBackgroundResource(R.drawable.sent_message_bckg);
                            message.setTextColor(getResources().getColor(R.color.wheat));
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) message.getLayoutParams();
                            params.gravity = Gravity.END;
                            message.setLayoutParams(params);
                        }
                    }
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