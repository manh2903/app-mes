package com.app.mes;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mes.adapters.UserSelectAdapter;
import com.app.mes.models.Group;
import com.app.mes.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateGroupActivity extends AppCompatActivity {
    private EditText edtGroupName;
    private RecyclerView recyclerViewUsers;
    private Button btnCreateGroup;
    private UserSelectAdapter userSelectAdapter;
    private List<User> userList;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        edtGroupName = findViewById(R.id.edtGroupName);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userList = new ArrayList<>();

        userSelectAdapter = new UserSelectAdapter(this, userList);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userSelectAdapter);

        loadFriends();

        btnCreateGroup.setOnClickListener(v -> createGroup());
    }

    private void loadFriends() {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();

        // Lấy danh sách bạn bè
        databaseReference.child("Users")
                .child(currentUserId)
                .child("friends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot friendsSnapshot) {
                        userList.clear();
                        for (DataSnapshot friendSnapshot : friendsSnapshot.getChildren()) {
                            String friendId = friendSnapshot.getKey();
                            // Lấy thông tin chi tiết của bạn bè
                            databaseReference.child("Users")
                                    .child(friendId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                            User user = userSnapshot.getValue(User.class);
                                            if (user != null) {
                                                userList.add(user);
                                                userSelectAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(CreateGroupActivity.this,
                                                "Lỗi: " + error.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CreateGroupActivity.this,
                            "Lỗi: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createGroup() {
        String groupName = edtGroupName.getText().toString().trim();
        List<String> selectedIds = userSelectAdapter.getSelectedUserIds();
        selectedIds.add(auth.getCurrentUser().getUid()); // Thêm chính mình vào nhóm
        if (groupName.isEmpty() || selectedIds.size() < 3) {
            Toast.makeText(this, "Nhập tên nhóm và chọn ít nhất 2 thành viên!", Toast.LENGTH_SHORT).show();
            return;
        }
        String groupId = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();
        Group group = new Group(groupId, groupName, "default", selectedIds, currentTime);
        databaseReference.child("Groups")
                .child(groupId)
                .setValue(group)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tạo nhóm thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}