package webchat.AuthenticationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import webchat.notFoundExceptions.UserNotFoundException;
import webchat.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

@Component
public class JPAUserDetailsService implements UserDetailsService{
    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String userName) {
        webchat.model.User user = repository.findByName(userName).orElseThrow(() -> new UsernameNotFoundException(userName));

        List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("user"));

        return new User(user.getName(), user.getPassword(), authorities);
    }
}