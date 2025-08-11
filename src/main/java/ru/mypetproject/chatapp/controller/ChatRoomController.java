package ru.mypetproject.chatapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.mypetproject.chatapp.dto.ChatMessage;
import ru.mypetproject.chatapp.dto.ChatRoomDto;
import ru.mypetproject.chatapp.model.ChatRoom;
import ru.mypetproject.chatapp.model.Message;
import ru.mypetproject.chatapp.model.User;
import ru.mypetproject.chatapp.repository.ChatRoomRepository;
import ru.mypetproject.chatapp.repository.MessageRepository;
import ru.mypetproject.chatapp.repository.UserRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatRoomController {
    private static final Logger logger = Logger.getLogger(ChatRoomController.class.getName());

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        List<ChatRoom> rooms = chatRoomRepository.findAll();
        logger.info("Загружены комнаты: " + rooms.size());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomName}/users")
    public ResponseEntity<List<String>> getRoomUsers(@PathVariable String roomName) {
        ChatRoom room = chatRoomRepository.findByName(roomName)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        List<String> usernames = room.getUsers().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usernames);
    }

    @GetMapping("/{roomName}/messages")
    public ResponseEntity<List<ChatMessage>> getRoomMessages(@PathVariable String roomName) {
        ChatRoom room = chatRoomRepository.findByName(roomName)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        List<Message> messages = messageRepository.findByChatRoomIdOrderByTimestampAsc(room.getId());
        List<ChatMessage> chatMessages = messages.stream().map(m -> ChatMessage.builder()
                .sender(m.getUser().getUsername())
                .content(m.getContent())
                .timestamp(m.getTimestamp().format(formatter))
                .type(ChatMessage.MessageType.CHAT)
                .roomId(roomName)
                .build()).toList();
        logger.info("Загружена история сообщений для комнаты " + roomName + ": " + chatMessages.size() + " сообщений");
        return ResponseEntity.ok(chatMessages);
    }

    @PostMapping
    public ResponseEntity<ChatRoom> createRoom(@RequestBody ChatRoomDto chatRoomDto, @AuthenticationPrincipal UserDetails userDetails) {
        if (chatRoomRepository.findByName(chatRoomDto.getName()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        ChatRoom room = ChatRoom.builder()
                .name(chatRoomDto.getName())
                .build();

        room.getUsers().add(user);
        user.getChatRooms().add(room);

        chatRoomRepository.save(room);
        return ResponseEntity.ok(room);
    }
}
