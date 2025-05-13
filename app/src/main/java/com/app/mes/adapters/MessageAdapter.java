package com.app.mes.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mes.databinding.ItemMessageBinding;
import com.app.mes.models.Message;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private FirebaseAuth auth;
    private SimpleDateFormat dateFormat;
    private String receiverAvatarUrl;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
        this.auth = FirebaseAuth.getInstance();
        this.dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    // Thêm setter để truyền avatar của người nhận từ ChatActivity
    public void setReceiverAvatarUrl(String avatarUrl) {
        this.receiverAvatarUrl = avatarUrl;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMessageBinding binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        boolean isCurrentUser = message.getSenderId().equals(auth.getCurrentUser().getUid());

        // Hiển thị thời gian
        String time = dateFormat.format(new Date(message.getTimestamp()));

        // Hiển thị tin nhắn dựa trên người gửi
        if (isCurrentUser) {
            // Tin nhắn của người dùng hiện tại
            holder.binding.messageContainer.setVisibility(ViewGroup.VISIBLE);
            holder.binding.messageContainer.setBackgroundResource(android.R.color.holo_blue_light);
            holder.binding.messageText.setTextColor(android.graphics.Color.WHITE);
            holder.binding.messageText.setText(message.getMessage());
            holder.binding.messageTime.setText(time);
            
            // Ẩn container tin nhắn nhận
            holder.binding.receivedMessageContainer.setVisibility(ViewGroup.GONE);
            holder.binding.receivedAvatar.setVisibility(ViewGroup.GONE);
        } else {
            // Tin nhắn nhận được
            holder.binding.receivedMessageContainer.setVisibility(ViewGroup.VISIBLE);
            holder.binding.receivedMessageContainer.setBackgroundResource(android.R.color.darker_gray);
            holder.binding.receivedMessageText.setTextColor(android.graphics.Color.WHITE);
            holder.binding.receivedMessageText.setText(message.getMessage());
            holder.binding.receivedMessageTime.setText(time);
            holder.binding.receivedAvatar.setVisibility(ViewGroup.VISIBLE);

            // Hiển thị avatar người gửi
            if (receiverAvatarUrl != null && !receiverAvatarUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(receiverAvatarUrl)
                        .into(holder.binding.receivedAvatar);
            } else if (message.getSenderAvatar() != null && !message.getSenderAvatar().equals("default") && !message.getSenderAvatar().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(message.getSenderAvatar())
                        .into(holder.binding.receivedAvatar);
            } else {
                holder.binding.receivedAvatar.setImageResource(com.app.mes.R.drawable.default_avatar);
            }

            // Hiển thị tên người gửi
            holder.binding.senderName.setVisibility(ViewGroup.VISIBLE);
            holder.binding.senderName.setText(message.getSenderName());

            // Ẩn container tin nhắn gửi
            holder.binding.messageContainer.setVisibility(ViewGroup.GONE);
        }

        // Hiển thị hình ảnh nếu có
        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            if (isCurrentUser) {
                holder.binding.messageImage.setVisibility(ViewGroup.VISIBLE);
                Glide.with(holder.itemView.getContext())
                        .load(message.getImageUrl())
                        .into(holder.binding.messageImage);
                holder.binding.messageText.setVisibility(ViewGroup.GONE);
            } else {
                holder.binding.receivedMessageImage.setVisibility(ViewGroup.VISIBLE);
                Glide.with(holder.itemView.getContext())
                        .load(message.getImageUrl())
                        .into(holder.binding.receivedMessageImage);
                holder.binding.receivedMessageText.setVisibility(ViewGroup.GONE);
            }
        } else {
            if (isCurrentUser) {
                holder.binding.messageImage.setVisibility(ViewGroup.GONE);
                holder.binding.messageText.setVisibility(ViewGroup.VISIBLE);
            } else {
                holder.binding.receivedMessageImage.setVisibility(ViewGroup.GONE);
                holder.binding.receivedMessageText.setVisibility(ViewGroup.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void updateMessages(List<Message> messages) {
        // Sắp xếp tin nhắn theo thời gian tăng dần
        Collections.sort(messages, (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
        this.messageList = messages;
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        ItemMessageBinding binding;

        MessageViewHolder(ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
} 