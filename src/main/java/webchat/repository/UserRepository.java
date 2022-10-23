package webchat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import webchat.model.User;

import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

}
