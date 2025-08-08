package ru.mypetproject.chatapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.mypetproject.chatapp.dto.ChatRoomDto;
import ru.mypetproject.chatapp.model.ChatRoom;
import ru.mypetproject.chatapp.model.User;
import ru.mypetproject.chatapp.repository.ChatRoomRepository;
import ru.mypetproject.chatapp.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        List<ChatRoom> rooms = chatRoomRepository.findAll();
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
