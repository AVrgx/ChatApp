package ru.mypetproject.chatapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import ru.mypetproject.chatapp.dto.ChatMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    // Клиент отправляет сообщение в /app/chat.sendMessage
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage message) {
        message.setTimestamp(LocalDateTime.now().format(formatter));

        messagingTemplate.convertAndSend("/topic/chat", message);
    }

    // Клиент присоединяется к чату: /app/chat.addUser
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        message.setType(ChatMessage.MessageType.JOIN);
        message.setTimestamp(LocalDateTime.now().format(formatter));
        messagingTemplate.convertAndSend("/topic/public", message);
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", message.getSender());
    }
}
