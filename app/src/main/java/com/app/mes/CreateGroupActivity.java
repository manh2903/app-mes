package com.app.mes;

import android.os.Bundle;
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
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        userList = new ArrayList<>();

        userSelectAdapter = new UserSelectAdapter(this, userList);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userSelectAdapter);

        loadUsers();

        btnCreateGroup.setOnClickListener(v -> createGroup());
    }

    private void loadUsers() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && !user.getUid().equals(auth.getCurrentUser().getUid())) {
                        userList.add(user);
                    }
                }
                userSelectAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateGroupActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createGroup() {
        String groupName = edtGroupName.getText().toString().trim();
        List<String> selectedIds = userSelectAdapter.getSelectedUserIds();
        selectedIds.add(auth.getCurrentUser().getUid()); // Thêm chính mình vào nhóm
        if (groupName.isEmpty() || selectedIds.size() < 3) {
            Toast.makeText(this, "Nhập tên nhóm và chọn ít nhất 3 thành viên!", Toast.LENGTH_SHORT).show();
            return;
        }
        String groupId = UUID.randomUUID().toString();
        Group group = new Group(groupId, groupName, "default", selectedIds);
        FirebaseDatabase.getInstance().getReference("Groups")
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