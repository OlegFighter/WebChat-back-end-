package webchat.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@EqualsAndHashCode
@ToString
@Getter
@Entity
public class Message implements Comparable<Message>{
    @Id
    @GeneratedValue
    long messageId;
    String messageText;
    LocalDateTime sendingTime;
    long senderId;
    String senderName;

    public Message(){}

    public Message(String messageText, LocalDateTime sendingTime, long senderId, String senderName) {
        this.messageText = messageText;
        this.sendingTime = sendingTime;
        this.senderId = senderId;
        this.senderName = senderName;
    }
    public int compareTo(Message tmp){
        return sendingTime.compareTo(tmp.getSendingTime());
    }
}
