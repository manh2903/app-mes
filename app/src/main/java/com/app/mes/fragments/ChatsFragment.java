package com.app.mes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.mes.ChatActivity;
import com.app.mes.adapters.ChatAdapter;
import com.app.mes.databinding.FragmentChatsBinding;
import com.app.mes.models.Chat;
import com.app.mes.models.ChatListItem;
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
import java.util.Collections;
import java.util.List;

public class ChatsFragment extends Fragment {
    private FragmentChatsBinding binding;
    private ChatAdapter chatAdapter;
    private List<ChatListItem> itemList;
    private FirebaseAuth auth;
    private DatabaseReference chatsRef;
    private DatabaseReference groupsRef;
    private ValueEventListener chatListener;
    private ValueEventListener groupListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        auth = FirebaseAuth.getInstance();
        chatsRef = FirebaseDatabase.getInstance().getReference("Chats");
        groupsRef = FirebaseDatabase.getInstance().getReference("Groups");
        itemList = new ArrayList<>();

        chatAdapter = new ChatAdapter(getContext(), itemList, item -> {
            if (item.getType() == ChatListItem.TYPE_CHAT) {
                Chat chat = item.getChat();
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("userId", chat.getUserId());
                intent.putExtra("userName", chat.getName());
                intent.putExtra("userAvatar", chat.getAvatar());
                startActivity(intent);
            } else if (item.getType() == ChatListItem.TYPE_GROUP) {
                Group group = item.getGroup();
                // TODO: Mở GroupChatActivity
                Toast.makeText(getContext(), "Mở chat nhóm: " + group.getGroupName(), Toast.LENGTH_SHORT).show();
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(chatAdapter);

        readAllChatsAndGroups();

        return view;
    }

    private void readAllChatsAndGroups() {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();
        itemList.clear();

        // Đọc chat cá nhân
        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                DataSnapshot userChatsSnapshot = snapshot.child(currentUserId);
                for (DataSnapshot chatSnapshot : userChatsSnapshot.getChildren()) {
                    Chat chat = chatSnapshot.getValue(Chat.class);
                    if (chat != null) {
                        itemList.add(new ChatListItem(chat));
                    }
                }
                // Đọc group sau khi đã có chat cá nhân
                readGroupsAndUpdate();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        chatsRef.addValueEventListener(chatListener);
    }

    private void readGroupsAndUpdate() {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();
        groupListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    Group group = groupSnapshot.getValue(Group.class);
                    if (group != null && group.getMemberIds() != null && group.getMemberIds().contains(currentUserId)) {
                        itemList.add(new ChatListItem(group));
                    }
                }
                // Sắp xếp theo thời gian mới nhất
                Collections.sort(itemList, (a, b) -> {
                    long t1 = a.getType() == ChatListItem.TYPE_CHAT ? a.getChat().getLastMessageTime() : a.getGroup().getLastMessageTime();
                    long t2 = b.getType() == ChatListItem.TYPE_CHAT ? b.getChat().getLastMessageTime() : b.getGroup().getLastMessageTime();
                    return Long.compare(t2, t1);
                });
                chatAdapter.updateItems(itemList);
                if (binding != null) {
                    if (itemList.isEmpty()) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                    } else {
                        binding.emptyView.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        groupsRef.addListenerForSingleValueEvent(groupListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatListener != null) {
            chatsRef.removeEventListener(chatListener);
        }
        if (groupListener != null) {
            groupsRef.removeEventListener(groupListener);
        }
        binding = null;
    }
}
