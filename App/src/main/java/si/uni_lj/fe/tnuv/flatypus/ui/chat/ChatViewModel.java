package si.uni_lj.fe.tnuv.flatypus.ui.chat;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel;
import si.uni_lj.fe.tnuv.flatypus.ui.opening.UserViewModel.User;

public class ChatViewModel extends ViewModel {

    public static class Chat {
        private String apartment;
        private String message;
        private String sender;
        private String timestamp;

        public Chat() {}

        private Chat(String apartment, String message, String sender, String timestamp) {
            this.apartment = apartment;
            this.message = message;
            this.sender = sender;
            this.timestamp = timestamp;
        }

        public String getApartment() { return apartment; }
        public String getMessage() {
            return message;
        }
        public String getSender() {
            return sender;
        }
        public String getTimestamp() {
            return timestamp;
        }
    }

    private final MutableLiveData<List<Chat>> chats = new MutableLiveData<>(new ArrayList<>());
    private DatabaseReference databaseReference;

    public ChatViewModel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://flatypus-fde01-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = database.getReference("chats");
    }

    public LiveData<List<Chat>> getChats(String apartment) {
        databaseReference.orderByChild("apartment").equalTo(apartment)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Chat> chatList = new ArrayList<>();
                        for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                            Chat chat = chatSnapshot.getValue(Chat.class);
                            if (chat != null) {
                                chatList.add(chat);
                            }
                        }
                        chats.setValue(chatList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        chats.setValue(Collections.emptyList());
                        Log.e("Chat", "Database error: " + error.getMessage());
                    }
                });

        return chats;
    }

    public void addChat(String apartment, String message, String sender) {
        Chat newChat = new Chat(apartment, message, sender, Instant.now().toString());
        String chatId = databaseReference.push().getKey();

        if (chatId != null) {
            databaseReference.child(chatId).setValue(newChat)
                    .addOnSuccessListener(aVoid ->
                            Log.d("Chat", "Chat added successfully")
                    ).addOnFailureListener(e ->
                            Log.e("Chat", "Failed to add chat: " + e.getMessage())
                    );
        }
    }
}