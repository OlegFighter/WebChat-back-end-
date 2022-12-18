package webchat.repository;

import webchat.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import webchat.model.User;

import java.util.Set;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Set<Chat> findByUser(User user);
}

