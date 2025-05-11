package com.app.mes.models;

public class Chat {
    private String chatId;
    private String userId;
    private String friendId;
    private String name;
    private String avatar;
    private String lastMessage;
    private long lastMessageTime;
    private boolean isOnline;

    public Chat() {
        // Required empty constructor for Firebase
    }

    public Chat(String chatId, String userId, String friendId, String name, String avatar) {
        this.chatId = chatId;
        this.userId = userId;
        this.friendId = friendId;
        this.name = name;
        this.avatar = avatar;
        this.lastMessage = "";
        this.lastMessageTime = 0;
        this.isOnline = false;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
} 