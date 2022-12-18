package webchat.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    long userId;
    @Column(unique = true)
    String name;
    String password;
//    @ManyToMany(mappedBy = "users")
//    Set<Chat> chats;


    public User(String name){
        this.name = name;
    }
    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
    public User() {}

}
