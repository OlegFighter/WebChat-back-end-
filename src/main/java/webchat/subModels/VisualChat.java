package webchat.subModels;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class VisualChat implements Serializable {
    String chatName;
    Long chatId;
}
