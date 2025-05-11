package com.app.mes.models;

public class ChatListItem {
    public static final int TYPE_CHAT = 0;
    public static final int TYPE_GROUP = 1;

    private int type;
    private Chat chat;
    private Group group;

    public ChatListItem(Chat chat) {
        this.type = TYPE_CHAT;
        this.chat = chat;
    }

    public ChatListItem(Group group) {
        this.type = TYPE_GROUP;
        this.group = group;
    }

    public int getType() { return type; }
    public Chat getChat() { return chat; }
    public Group getGroup() { return group; }
} 