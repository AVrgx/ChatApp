package ru.mypetproject.chatapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mypetproject.chatapp.dto.ChatRoomDto;
import ru.mypetproject.chatapp.model.ChatRoom;
import ru.mypetproject.chatapp.model.User;
import ru.mypetproject.chatapp.repository.ChatRoomRepository;
import ru.mypetproject.chatapp.repository.UserRepository;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

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
