package si.uni_lj.fe.tnuv.flatypus.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel.User;

public class ChatViewModel extends ViewModel {

    public static class Chat {
        private String message;
        private User sender;
        private Instant timestamp;

        private Chat(String message, User sender, Instant timestamp) {
            this.message = message;
            this.sender = sender;
            this.timestamp = timestamp;
        }

        public String getMessage() {
            return message;
        }
        public User getSender() {
            return sender;
        }
        public Instant getTimestamp() {
            return timestamp;
        }
    }

    private final MutableLiveData<List<Chat>> chats = new MutableLiveData<>(new ArrayList<>());

    public ChatViewModel() {
        List<Chat> initialChats = new ArrayList<>();
    }
}