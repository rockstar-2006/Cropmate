package com.example.cropmate;

public class ChatMessage {
    private String text;
    private boolean isUser;

    public ChatMessage(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    public String getText() { return text; }
    public boolean isUser() { return isUser; }

    public static ChatMessage fromAI(String text) {
        return new ChatMessage(text, false);
    }
}
