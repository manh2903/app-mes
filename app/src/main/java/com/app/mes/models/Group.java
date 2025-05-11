package com.app.mes.models;

import java.util.List;

public class Group {
    private String groupId;
    private String groupName;
    private String groupAvatar;
    private List<String> memberIds;
    private String lastMessage;
    private long lastMessageTime;

    public Group() {}

    public Group(String groupId, String groupName, String groupAvatar, List<String> memberIds) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupAvatar = groupAvatar;
        this.memberIds = memberIds;
        this.lastMessage = "";
        this.lastMessageTime = 0;
    }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupAvatar() { return groupAvatar; }
    public void setGroupAvatar(String groupAvatar) { this.groupAvatar = groupAvatar; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }
} 