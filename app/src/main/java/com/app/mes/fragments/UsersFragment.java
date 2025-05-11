package com.app.mes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.mes.ChatActivity;
import com.app.mes.adapters.UserAdapter;
import com.app.mes.databinding.FragmentUsersBinding;
import com.app.mes.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {
    private FragmentUsersBinding binding;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private ValueEventListener userListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        userList = new ArrayList<>();

        // Setup RecyclerView
        userAdapter = new UserAdapter(getContext(), userList, user -> {
            // Kiểm tra xem đã là bạn bè chưa
            databaseReference.child(auth.getCurrentUser().getUid())
                    .child("friends")
                    .child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Đã là bạn bè, cho phép nhắn tin
                                String chatId = getChatId(auth.getCurrentUser().getUid(), user.getUid());
                                Intent intent = new Intent(getContext(), ChatActivity.class);
                                intent.putExtra("userId", user.getUid());
                                intent.putExtra("userName", user.getName());
                                intent.putExtra("userAvatar", user.getAvatar());
                                startActivity(intent);
                            } else {
                                // Chưa là bạn bè, hiển thị thông báo
                                Toast.makeText(getContext(), "Bạn cần kết bạn trước khi nhắn tin", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(userAdapter);

        // Setup tìm kiếm
        binding.searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Đọc danh sách người dùng
        readUsers();

        return view;
    }

    private void searchUsers(String query) {
        if (!isAdded() || binding == null) return;

        List<User> searchList = new ArrayList<>();
        for (User user : userList) {
            if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                searchList.add(user);
            }
        }
        userAdapter.updateUsers(searchList);

        // Hiển thị thông báo nếu không tìm thấy
        if (searchList.isEmpty()) {
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void readUsers() {
        if (auth.getCurrentUser() == null) return;

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;

                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && !user.getUid().equals(auth.getCurrentUser().getUid())) {
                        userList.add(user);
                    }
                }
                userAdapter.updateUsers(userList);

                // Hiển thị thông báo nếu không có người dùng nào
                if (userList.isEmpty()) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                } else {
                    binding.emptyView.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        databaseReference.addValueEventListener(userListener);
    }

    private String getChatId(String uid1, String uid2) {
        // Tạo chatId duy nhất cho mỗi cặp người dùng
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) {
            databaseReference.removeEventListener(userListener);
        }
        binding = null;
    }
}