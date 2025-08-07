package ru.mypetproject.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    private String content;
    private String sender;
    private String timestamp;
    private MessageType type;
    private String roomId;

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
}
