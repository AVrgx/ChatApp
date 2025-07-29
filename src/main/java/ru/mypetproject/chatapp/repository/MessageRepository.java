package ru.mypetproject.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mypetproject.chatapp.model.Message;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findByChatRoomIdOrderByTimestampAsc(Long chatRoomId);
}
