package com.app.mes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mes.databinding.ItemUserBinding;
import com.app.mes.models.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;
    private OnUserClickListener listener;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(Context context, List<User> userList, OnUserClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
        this.auth = FirebaseAuth.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users");
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.binding.username.setText(user.getName());
        holder.binding.status.setText(user.getStatus());

        // Load avatar using Glide
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Glide.with(context)
                    .load(user.getAvatar())
                    .into(holder.binding.profileImage);
        }

        // Ẩn trạng thái online mặc định
        holder.binding.onlineStatus.setVisibility(ViewGroup.GONE);

        // Set friend button state
        if (auth.getCurrentUser() != null && !user.getUid().equals(auth.getCurrentUser().getUid())) {
            holder.binding.btnFriend.setVisibility(android.view.View.VISIBLE);

            // Kiểm tra trạng thái kết bạn
            databaseReference.child(auth.getCurrentUser().getUid())
                    .child("friends")
                    .child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Đã là bạn
                                holder.binding.btnFriend.setText("Xóa bạn");
                                holder.binding.btnFriend.setEnabled(true);
                                holder.binding.btnFriend.setOnClickListener(v -> removeFriend(user));
                                // Hiển thị chấm tròn online/offline
                                holder.binding.onlineStatus.setVisibility(ViewGroup.VISIBLE);
                                if (user.isOnline()) {
                                    holder.binding.onlineStatus.setBackgroundResource(com.app.mes.R.drawable.bg_online_status);
                                } else {
                                    holder.binding.onlineStatus.setBackgroundResource(com.app.mes.R.drawable.bg_offline_status);
                                }
                            } else {
                                // Kiểm tra nếu bạn là người nhận lời mời
                                databaseReference.child(auth.getCurrentUser().getUid())
                                        .child("friendRequests")
                                        .child(user.getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    // Bạn là người nhận lời mời
                                                    holder.binding.btnFriend.setText("Chấp nhận");
                                                    holder.binding.btnFriend.setEnabled(true);
                                                    holder.binding.btnFriend.setOnClickListener(v -> acceptFriendRequest(user));
                                                    holder.binding.onlineStatus.setVisibility(ViewGroup.GONE);
                                                } else {
                                                    // Kiểm tra nếu bạn là người gửi lời mời
                                                    databaseReference.child(user.getUid())
                                                            .child("friendRequests")
                                                            .child(auth.getCurrentUser().getUid())
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (snapshot.exists()) {
                                                                        // Bạn là người gửi lời mời
                                                                        holder.binding.btnFriend.setText("Hủy lời mời");
                                                                        holder.binding.btnFriend.setEnabled(true);
                                                                        holder.binding.btnFriend.setOnClickListener(v -> cancelFriendRequest(user));
                                                                        holder.binding.onlineStatus.setVisibility(ViewGroup.GONE);
                                                                    } else {
                                                                        // Chưa gửi lời mời
                                                                        holder.binding.btnFriend.setText("Kết bạn");
                                                                        holder.binding.btnFriend.setEnabled(true);
                                                                        holder.binding.btnFriend.setOnClickListener(v -> sendFriendRequest(user));
                                                                        holder.binding.onlineStatus.setVisibility(ViewGroup.GONE);
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    Toast.makeText(context, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(context, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            holder.binding.btnFriend.setVisibility(android.view.View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    private void sendFriendRequest(User user) {
        if (auth.getCurrentUser() != null) {
            String currentUserId = auth.getCurrentUser().getUid();
            
            // Add friend request to user
            databaseReference.child(user.getUid())
                    .child("friendRequests")
                    .child(currentUserId)
                    .setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Đã gửi lời mời kết bạn đến " + user.getName(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void acceptFriendRequest(User user) {
        if (auth.getCurrentUser() != null) {
            String currentUserId = auth.getCurrentUser().getUid();
            
            // Remove friend request
            databaseReference.child(currentUserId)
                    .child("friendRequests")
                    .child(user.getUid())
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Add friend to both users
                        databaseReference.child(currentUserId)
                                .child("friends")
                                .child(user.getUid())
                                .setValue(true)
                                .addOnSuccessListener(aVoid1 -> {
                                    databaseReference.child(user.getUid())
                                            .child("friends")
                                            .child(currentUserId)
                                            .setValue(true)
                                            .addOnSuccessListener(aVoid2 -> {
                                                Toast.makeText(context, "Đã kết bạn với " + user.getName(), Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void removeFriend(User user) {
        if (auth.getCurrentUser() != null) {
            String currentUserId = auth.getCurrentUser().getUid();
            
            // Remove friend from current user
            databaseReference.child(currentUserId)
                    .child("friends")
                    .child(user.getUid())
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Remove current user from friend's list
                        databaseReference.child(user.getUid())
                                .child("friends")
                                .child(currentUserId)
                                .removeValue()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(context, "Đã xóa " + user.getName() + " khỏi danh sách bạn bè", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void cancelFriendRequest(User user) {
        if (auth.getCurrentUser() != null) {
            String currentUserId = auth.getCurrentUser().getUid();
            databaseReference.child(currentUserId)
                    .child("friendRequests")
                    .child(user.getUid())
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Đã hủy lời mời kết bạn với " + user.getName(), Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;

        UserViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
} 