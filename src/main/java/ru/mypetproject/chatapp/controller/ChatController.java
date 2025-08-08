package ru.mypetproject.chatapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import ru.mypetproject.chatapp.dto.ChatMessage;
import ru.mypetproject.chatapp.model.ChatRoom;
import ru.mypetproject.chatapp.model.Message;
import ru.mypetproject.chatapp.model.User;
import ru.mypetproject.chatapp.repository.ChatRoomRepository;
import ru.mypetproject.chatapp.repository.MessageRepository;
import ru.mypetproject.chatapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    private static final String CHAT_ROOM_NAME = "Public";

    // Клиент отправляет сообщение в /app/chat.sendMessage
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        System.out.println("✅ Получено: " + chatMessage);
        // 1. Устанавливаем время
        chatMessage.setTimestamp(LocalDateTime.now().format(formatter));
        // 2. Находим пользователя
        User user = userRepository.findByUsername(chatMessage.getSender())
                .orElseThrow(() -> new RuntimeException("User not found " + chatMessage.getSender()));
        // 3. Находим комнату
        ChatRoom chatRoom = chatRoomRepository.findByName(chatMessage.getRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        // 4. Сохраняем сообщение в БД
        Message message = Message.builder()
                .content(chatMessage.getContent())
                .timestamp(LocalDateTime.now())
                .user(user)
                .chatRoom(chatRoom)
                .build();
        messageRepository.save(message);
        // 5. Рассылаем всем
        messagingTemplate.convertAndSend("/topic/chat/" + chatMessage.getRoomId(), chatMessage);
    }

    // Клиент присоединяется к чату: /app/chat.addUser
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setTimestamp(LocalDateTime.now().format(formatter));

        // Сохраняем имя в сессии
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

        // Находим комнату
        String roomId = chatMessage.getRoomId();
        ChatRoom chatRoom = chatRoomRepository.findByName(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        // Загружаем историю сообщений
        List<Message> messageHistory = messageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoom.getId());

        // Отправляем историю только этому пользователю
        List<ChatMessage> history = messageHistory.stream().map(m -> ChatMessage.builder()
                .sender(m.getUser().getUsername())
                .content(m.getContent())
                .timestamp(m.getTimestamp().format(formatter))
                .type(ChatMessage.MessageType.CHAT)
                .roomId(roomId)
                .build()).toList();

        // Отправляем историю только новому пользователю
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSender(),  // получатель
                "/queue/history",         // его личная очередь
                history
        );

        // Оповещаем всех, что пользователь присоединился
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, chatMessage);

    }
}
