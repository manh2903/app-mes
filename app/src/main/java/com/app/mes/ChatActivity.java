package com.app.mes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.mes.adapters.MessageAdapter;
import com.app.mes.databinding.ActivityChatBinding;
import com.app.mes.models.Chat;
import com.app.mes.models.Message;
import com.app.mes.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String receiverId;
    private String receiverName;
    private String receiverAvatar;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get receiver info from intent
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("userName");
        receiverAvatar = getIntent().getStringExtra("userAvatar");

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(receiverName);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("ChatImages");

        setupRecyclerView();
        messageAdapter.setReceiverAvatarUrl(receiverAvatar);
        setupImagePicker();
        readMessages();

        binding.sendButton.setOnClickListener(v -> sendMessage());
        binding.btnImage.setOnClickListener(v -> openImagePicker());
    }

    private void setupRecyclerView() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        binding.recyclerView.setAdapter(messageAdapter);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadImage(imageUri);
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadImage(Uri imageUri) {
        if (auth.getCurrentUser() != null) {
            String messageId = UUID.randomUUID().toString();
            StorageReference fileReference = storageReference.child(messageId + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            sendImageMessage(imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void sendImageMessage(String imageUrl) {
        if (auth.getCurrentUser() != null) {
            String messageId = UUID.randomUUID().toString();
            Message message = new Message(
                    messageId,
                    auth.getCurrentUser().getUid(),
                    receiverId,
                    "",
                    imageUrl
            );

            databaseReference.child("Messages")
                    .child(messageId)
                    .setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        // Update last message in chat
                        updateChat(message);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void sendMessage() {
        String messageText = binding.messageInput.getText().toString().trim();
        if (!messageText.isEmpty() && auth.getCurrentUser() != null) {
            String messageId = UUID.randomUUID().toString();
            Message message = new Message(
                    messageId,
                    auth.getCurrentUser().getUid(),
                    receiverId,
                    messageText,
                    null
            );

            databaseReference.child("Messages")
                    .child(messageId)
                    .setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        binding.messageInput.setText("");
                        // Update last message in chat
                        updateChat(message);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateChat(Message message) {
        String chatId = getChatId(auth.getCurrentUser().getUid(), receiverId);
        
        // Lấy thông tin người dùng
        databaseReference.child("Users").child(receiverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User receiver = snapshot.getValue(User.class);
                        if (receiver != null) {
                            // Tạo chat cho người gửi
                            Chat senderChat = new Chat();
                            senderChat.setChatId(chatId);
                            senderChat.setUserId(receiverId);
                            senderChat.setFriendId(auth.getCurrentUser().getUid());
                            senderChat.setName(receiver.getName());
                            senderChat.setAvatar(receiver.getAvatar());
                            senderChat.setLastMessage(message.getMessage());
                            senderChat.setLastMessageTime(message.getTimestamp());

                            // Lưu chat cho người gửi
                            databaseReference.child("Chats")
                                    .child(auth.getCurrentUser().getUid())
                                    .child(chatId)
                                    .setValue(senderChat);

                            // Lấy thông tin người gửi
                            databaseReference.child("Users").child(auth.getCurrentUser().getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot senderSnapshot) {
                                            User sender = senderSnapshot.getValue(User.class);
                                            if (sender != null) {
                                                // Tạo chat cho người nhận
                                                Chat receiverChat = new Chat();
                                                receiverChat.setChatId(chatId);
                                                receiverChat.setUserId(auth.getCurrentUser().getUid());
                                                receiverChat.setFriendId(receiverId);
                                                receiverChat.setName(sender.getName());
                                                receiverChat.setAvatar(sender.getAvatar());
                                                receiverChat.setLastMessage(message.getMessage());
                                                receiverChat.setLastMessageTime(message.getTimestamp());

                                                // Lưu chat cho người nhận
                                                databaseReference.child("Chats")
                                                        .child(receiverId)
                                                        .child(chatId)
                                                        .setValue(receiverChat);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private String getChatId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    private void readMessages() {
        if (auth.getCurrentUser() != null) {
            databaseReference.child("Messages")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            messageList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Message message = dataSnapshot.getValue(Message.class);
                                if (message != null && 
                                    ((message.getSenderId().equals(auth.getCurrentUser().getUid()) && 
                                      message.getReceiverId().equals(receiverId)) ||
                                     (message.getSenderId().equals(receiverId) && 
                                      message.getReceiverId().equals(auth.getCurrentUser().getUid())))) {
                                    messageList.add(message);
                                }
                            }
                            messageAdapter.updateMessages(messageList);
                            if (!messageList.isEmpty()) {
                                binding.recyclerView.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}