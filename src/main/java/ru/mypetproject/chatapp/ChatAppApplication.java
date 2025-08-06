package ru.mypetproject.chatapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.mypetproject.chatapp.model.ChatRoom;
import ru.mypetproject.chatapp.repository.ChatRoomRepository;

@SpringBootApplication
public class ChatAppApplication {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    public static void main(String[] args) {
        SpringApplication.run(ChatAppApplication.class, args);
    }

    @Bean
    public CommandLineRunner createDefaultChatRoom() {
        return args -> {
            if (chatRoomRepository.findByName("Public").isEmpty()) {
                ChatRoom publicRoom = ChatRoom.builder()
                        .name("Public")
                        .build();
                chatRoomRepository.save(publicRoom);
            }
        };
    }

}
