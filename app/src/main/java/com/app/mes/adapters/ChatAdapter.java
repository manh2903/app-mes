package com.app.mes.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mes.databinding.ItemChatBinding;
import com.app.mes.models.Chat;
import com.app.mes.models.ChatListItem;
import com.app.mes.models.Group;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private Context context;
    private List<ChatListItem> itemList;
    private OnChatClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnChatClickListener {
        void onChatClick(ChatListItem item);
    }

    public ChatAdapter(Context context, List<ChatListItem> itemList, OnChatClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatBinding binding = ItemChatBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ChatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatListItem item = itemList.get(position);
        if (item.getType() == ChatListItem.TYPE_CHAT) {
            Chat chat = item.getChat();
            holder.binding.username.setText(chat.getName());
            Log.d("ChatAdapter", "Last message: " + chat.getLastMessage().equals(""));
            holder.binding.lastMessage.setText(chat.getLastMessage().equals("") ? "Hình ảnh" : chat.getLastMessage());
            holder.binding.messageTime.setText(dateFormat.format(new Date(chat.getLastMessageTime())));
            if (chat.getAvatar() != null && !chat.getAvatar().isEmpty()) {
                Glide.with(context)
                        .load(chat.getAvatar())
                        .into(holder.binding.profileImage);
            } else {
                holder.binding.profileImage.setImageResource(com.app.mes.R.drawable.default_avatar);
            }
        } else if (item.getType() == ChatListItem.TYPE_GROUP) {
            Group group = item.getGroup();
            holder.binding.username.setText(group.getGroupName());
            holder.binding.lastMessage.setText(group.getLastMessage().equals("") ? "Hình ảnh" : group.getLastMessage());
            holder.binding.messageTime.setText(dateFormat.format(new Date(group.getLastMessageTime())));
            if (group.getGroupAvatar() != null && !group.getGroupAvatar().isEmpty() && !group.getGroupAvatar().equals("default")) {
                Glide.with(context)
                        .load(group.getGroupAvatar())
                        .into(holder.binding.profileImage);
            } else {
                holder.binding.profileImage.setImageResource(com.app.mes.R.drawable.default_avatar);
            }
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateItems(List<ChatListItem> items) {
        this.itemList = items;
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ItemChatBinding binding;

        ChatViewHolder(ItemChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
} 