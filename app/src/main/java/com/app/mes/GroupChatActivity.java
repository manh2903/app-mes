package com.app.mes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.mes.adapters.MessageAdapter;
import com.app.mes.databinding.ActivityGroupChatBinding;
import com.app.mes.helper.UploadHelper;
import com.app.mes.models.Group;
import com.app.mes.models.Message;
import com.app.mes.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroupChatActivity extends AppCompatActivity {
    private ActivityGroupChatBinding binding;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private FirebaseAuth auth;
    private DatabaseReference groupsRef;
    private DatabaseReference messagesRef;
    private DatabaseReference usersRef;
    private String groupId;
    private Group currentGroup;
    private ValueEventListener messageListener;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy groupId từ Intent
        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin nhóm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();
        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");
        messagesRef = FirebaseDatabase.getInstance().getReference("GroupMessages");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Khởi tạo RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(messageAdapter);

        // Thiết lập Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Thiết lập ImagePicker
        setupImagePicker();

        // Xử lý sự kiện gửi tin nhắn và ảnh
        binding.sendButton.setOnClickListener(v -> sendMessage());
        binding.btnImage.setOnClickListener(v -> openImagePicker());

        // Tải thông tin nhóm và tin nhắn
        loadGroupInfo();
        loadMessages();
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
        if (auth.getCurrentUser() == null) return;
        String messageId = messagesRef.child(groupId).push().getKey();
        if (messageId == null) return;

        UploadHelper.uploadImage(imageUri, "group_images", messageId, new UploadHelper.UploadCallbackListener() {
            @Override
            public void onSuccess(String imageUrl) {
                usersRef.child(auth.getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User currentUser = snapshot.getValue(User.class);
                                if (currentUser != null) {
                                    Message message = new Message();
                                    message.setMessageId(messageId);
                                    message.setSenderId(auth.getCurrentUser().getUid());
                                    message.setSenderName(currentUser.getName());
                                    message.setSenderAvatar(currentUser.getAvatar());
                                    message.setMessage(null);
                                    message.setImageUrl(imageUrl);
                                    message.setTimestamp(System.currentTimeMillis());

                                    messagesRef.child(groupId).child(messageId).setValue(message)
                                            .addOnSuccessListener(aVoid -> {
                                                binding.messageInput.setText("");
                                                // Cập nhật thông tin nhóm
                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("lastMessage", "Đã gửi một ảnh");
                                                updates.put("lastMessageTime", System.currentTimeMillis());
                                                groupsRef.child(groupId).updateChildren(updates);
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(GroupChatActivity.this,
                                                    "Lỗi gửi ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(GroupChatActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(GroupChatActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupInfo() {
        showLoading();
        groupsRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentGroup = snapshot.getValue(Group.class);
                if (currentGroup != null) {
                    binding.groupName.setText(currentGroup.getGroupName());
                    binding.memberCount.setText(currentGroup.getMemberIds().size() + " thành viên");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupChatActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        // Lấy thông tin người gửi từ senderId
                        usersRef.child(message.getSenderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                User sender = userSnapshot.getValue(User.class);
                                if (sender != null) {
                                    message.setSenderName(sender.getName());
                                    message.setSenderAvatar(sender.getAvatar());
                                    messageAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(GroupChatActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    binding.recyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
                hideLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoading();
                Toast.makeText(GroupChatActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        messagesRef.child(groupId).addValueEventListener(messageListener);
    }

    private void sendMessage() {
        String messageText = binding.messageInput.getText().toString().trim();
        if (messageText.isEmpty() || auth.getCurrentUser() == null) return;

        String messageId = messagesRef.child(groupId).push().getKey();
        if (messageId == null) return;

        // Lấy thông tin người gửi
        usersRef.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    Message message = new Message();
                    message.setMessageId(messageId);
                    message.setSenderId(auth.getCurrentUser().getUid());
                    message.setSenderName(currentUser.getName());
                    message.setSenderAvatar(currentUser.getAvatar());
                    message.setMessage(messageText);
                    message.setImageUrl(null);
                    message.setTimestamp(System.currentTimeMillis());

                    // Lưu tin nhắn
                    messagesRef.child(groupId).child(messageId).setValue(message)
                            .addOnSuccessListener(aVoid -> {
                                binding.messageInput.setText("");
                                // Cập nhật thông tin nhóm
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("lastMessage", messageText);
                                updates.put("lastMessageTime", System.currentTimeMillis());
                                groupsRef.child(groupId).updateChildren(updates);
                            })
                            .addOnFailureListener(e -> Toast.makeText(GroupChatActivity.this,
                                    "Lỗi gửi tin nhắn: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupChatActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            messagesRef.child(groupId).removeEventListener(messageListener);
        }
        binding = null;
    }
} 