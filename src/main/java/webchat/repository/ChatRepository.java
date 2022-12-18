package webchat.repository;

import org.springframework.data.jpa.repository.Query;
import webchat.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import webchat.model.User;

import java.util.Set;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    default Set<Chat> findByUser(User user){
        return findByUser(user.getUserId());
    }

    @Query(value = "select chat.* from chat inner join chat_users on chat_users.chat_chat_id=chat.chat_id where chat_users.users_user_id=:userId",
            nativeQuery = true
    )
    Set<Chat> findByUser(long userId);
}

