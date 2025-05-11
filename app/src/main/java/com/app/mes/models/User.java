package com.app.mes.models;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String uid;
    private String name;
    private String email;
    private String avatar;
    private String status;
    private boolean isOnline;
    private Map<String, Boolean> friends;
    private Map<String, Boolean> friendRequests;

    public User() {
        // Required empty constructor for Firebase
        this.friends = new HashMap<>();
        this.friendRequests = new HashMap<>();
    }

    public User(String uid, String name, String email, String avatar) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.status = "Xin chào! Tôi đang sử dụng MES";
        this.isOnline = false;
        this.friends = new HashMap<>();
        this.friendRequests = new HashMap<>();
    }

    public User(String uid, String name, String email, String avatar, String status, boolean isOnline) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.status = status;
        this.isOnline = isOnline;
        this.friends = new HashMap<>();
        this.friendRequests = new HashMap<>();
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public Map<String, Boolean> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Boolean> friends) {
        this.friends = friends;
    }

    public Map<String, Boolean> getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(Map<String, Boolean> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public void addFriend(String friendId) {
        if (friends == null) {
            friends = new HashMap<>();
        }
        friends.put(friendId, true);
    }

    public void removeFriend(String friendId) {
        if (friends != null) {
            friends.remove(friendId);
        }
    }

    public boolean isFriend(String friendId) {
        return friends != null && friends.containsKey(friendId);
    }

    public void addFriendRequest(String userId) {
        if (friendRequests == null) {
            friendRequests = new HashMap<>();
        }
        friendRequests.put(userId, true);
    }

    public void removeFriendRequest(String userId) {
        if (friendRequests != null) {
            friendRequests.remove(userId);
        }
    }

    public boolean hasFriendRequest(String userId) {
        return friendRequests != null && friendRequests.containsKey(userId);
    }
} 