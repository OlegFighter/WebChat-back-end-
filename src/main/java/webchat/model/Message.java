package webchat.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@EqualsAndHashCode
@ToString
@Getter
@Entity
public class Message {
    @Id
    @GeneratedValue
    long messageId;
    String messageText;
    Date sendingTime;
    long senderId;

    public Message(){}

    public Message(String messageText, Date sendingTime, long senderId) {
        this.messageText = messageText;
        this.sendingTime = sendingTime;
        this.senderId = senderId;
    }
}
