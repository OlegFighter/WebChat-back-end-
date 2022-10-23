package webchat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@Setter
@Getter
@Entity
public class Chat {
    @Id
    @GeneratedValue
    long chatId;
    String chatName;
    long creatorId;
    @OneToMany
    private Set<Message> messagesInTheChat;
    @ManyToMany(cascade =  {CascadeType.ALL})
    private Set<User> users;


    public Chat() {};
    public Chat(String chatName, Set<User> users, long creatorId) {
        this.chatName = chatName;
        this.users = users;
        this.creatorId = creatorId;
    }

    public void addUser(User temp){
        this.users.add(temp);
    }

    public void addMessage(Message temp){
        this.messagesInTheChat.add(temp);
    }
}
