package webchat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import webchat.model.User;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByName(String name);
}
